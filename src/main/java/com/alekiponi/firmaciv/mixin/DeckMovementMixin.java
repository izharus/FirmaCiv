package com.alekiponi.firmaciv.mixin;

import com.alekiponi.alekiships.common.entity.vehicle.AbstractAlekiBoatEntity;
import com.alekiponi.alekiships.common.entity.vehicle.AbstractVehicle;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Carry a standing player with the deck's actual transform, including remote interpolation and rotation. */
@Mixin(value = AbstractAlekiBoatEntity.class, remap = false)
public abstract class DeckMovementMixin extends AbstractVehicle {
    @Unique private Vec3 firmaciv$previousPosition;
    @Unique private float firmaciv$previousYaw;
    @Unique private LocalPlayer firmaciv$standingPlayer;

    protected DeckMovementMixin(EntityType<? extends AbstractVehicle> type, Level level) {
        super(type, level);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void firmaciv$captureDeck(CallbackInfo ci) {
        firmaciv$standingPlayer = null;
        if (!level().isClientSide) return;
        var player = Minecraft.getInstance().player;
        if (player == null || player.isPassenger() || player.getAbilities().flying || player.input.jumping
                || player.getDeltaMovement().y > 0.05) return;
        var feet = player.getBoundingBox();
        boolean supported = firmaciv$supportsFeet(getBoundingBox(), feet);
        if (!supported) for (var collider : getColliders()) {
            if (firmaciv$supportsFeet(collider.getBoundingBox(), feet)) { supported = true; break; }
        }
        if (!supported) for (var compartment : getCompartments()) {
            if (compartment.canBeCollidedWith() && firmaciv$supportsFeet(compartment.getBoundingBox(), feet)) { supported = true; break; }
        }
        if (supported) {
            firmaciv$standingPlayer = player;
            firmaciv$previousPosition = position();
            firmaciv$previousYaw = getYRot();
        }
    }

    @Unique
    private static boolean firmaciv$supportsFeet(AABB deck, AABB player) {
        return Math.abs(deck.maxY - player.minY) <= 0.05
                && player.maxX > deck.minX && player.minX < deck.maxX
                && player.maxZ > deck.minZ && player.minZ < deck.maxZ;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void firmaciv$carryWithDeck(CallbackInfo ci) {
        var player = firmaciv$standingPlayer;
        if (player == null || player.isPassenger()) return;
        float yawChange = Mth.wrapDegrees(getYRot() - firmaciv$previousYaw);
        var offset = player.position().subtract(firmaciv$previousPosition);
        // Vec3.yRot uses Minecraft's approximate float sine table. Repeating
        // tiny turns with it accumulates drift for a stationary deck passenger.
        double angle = Math.toRadians(-yawChange);
        double sin = Math.sin(angle), cos = Math.cos(angle);
        var rotated = new Vec3(offset.x * cos + offset.z * sin, offset.y, offset.z * cos - offset.x * sin);
        player.setPos(position().add(rotated));
        firmaciv$standingPlayer = null;
    }

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE",
            target = "Lcom/alekiponi/alekiships/common/entity/vehicle/AbstractAlekiBoatEntity;tickTakeEntitiesForARide()V"))
    private boolean firmaciv$useServerEntityTransport(AbstractAlekiBoatEntity<?> boat) {
        return !boat.level().isClientSide;
    }
}
