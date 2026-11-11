package com.alekiponi.firmaciv.mixin;

import com.alekiponi.alekiships.common.compartment.CompartmentPlaceable;
import com.alekiponi.alekiships.common.entity.compartment.AbstractCompartmentEntity;
import com.alekiponi.alekiships.common.entity.compartment.EmptyCompartmentEntity;
import com.alekiponi.alekiships.common.entity.vehicle.AbstractVehicle;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Installation changes the passenger tree only on the server; clients receive the new compartment. */
@Mixin(value = EmptyCompartmentEntity.class, remap = false)
public abstract class CompartmentPlacementMixin extends AbstractCompartmentEntity {
    protected CompartmentPlacementMixin(EntityType<? extends AbstractCompartmentEntity> type, Level level) {
        super(type, level);
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void firmaciv$predictPlacement(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!level().isClientSide || player.isSecondaryUseActive()
                || CompartmentPlaceable.fromStack(player.getItemInHand(hand)).isEmpty()) return;
        if (getRootVehicle() instanceof AbstractVehicle boat && !boat.pilotCompartmentAcceptsNonPlayers()
                && boat.getPilotVehiclePartAsEntity() == getVehicle()) {
            cir.setReturnValue(InteractionResult.FAIL);
        } else {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }

    @Redirect(method = "interact", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private void firmaciv$consumeSurvivalItem(ItemStack stack, int count, Player player, InteractionHand hand) {
        if (!player.hasInfiniteMaterials()) stack.shrink(count);
    }
    @Redirect(method = "interact", at = @At(value = "INVOKE",
            target = "Lcom/alekiponi/alekiships/common/entity/compartment/EmptyCompartmentEntity;is(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean firmaciv$isPilotSeat(EmptyCompartmentEntity pilot, Entity candidate) {
        return candidate.getRootVehicle() instanceof AbstractVehicle boat
                && candidate.getVehicle() == boat.getPilotVehiclePartAsEntity();
    }

}
