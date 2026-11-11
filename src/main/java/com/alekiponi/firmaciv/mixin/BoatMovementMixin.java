package com.alekiponi.firmaciv.mixin;

import com.alekiponi.alekiships.common.entity.vehicle.AbstractAlekiBoatEntity;
import com.alekiponi.alekiships.common.entity.vehicle.AbstractVehicle;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.world.entity.EntityType;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Remote clients interpolate server updates while the server and local pilot retain their simulation. */
@Mixin(value = AbstractAlekiBoatEntity.class, remap = false)
public abstract class BoatMovementMixin extends AbstractVehicle {
    @Unique private static final EntityDataAccessor<Float> firmaciv$PRECISE_YAW = SynchedEntityData.defineId(AbstractAlekiBoatEntity.class, EntityDataSerializers.FLOAT);
    @Unique private boolean firmaciv$receivedPreciseYaw;

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void firmaciv$definePreciseYaw(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(firmaciv$PRECISE_YAW, 0F);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void firmaciv$publishPreciseYaw(CallbackInfo ci) {
        if (!level().isClientSide) entityData.set(firmaciv$PRECISE_YAW, getYRot());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (key != firmaciv$PRECISE_YAW || !level().isClientSide) return;
        firmaciv$receivedPreciseYaw = true;
        if (isControlledByLocalInstance()) return;
        if (lerpSteps <= 0) {
            lerpX = getX(); lerpY = getY(); lerpZ = getZ(); lerpXRot = getXRot();
        }
        lerpYRot = entityData.get(firmaciv$PRECISE_YAW);
        lerpSteps = 3;
    }

    protected BoatMovementMixin(EntityType<? extends AbstractVehicle> type, Level level) {
        super(type, level);
    }

    // Ships has tickLerp and these fields, but inherits Entity.lerpTo, which teleports immediately.
    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps) {
        this.lerpX = x;
        this.lerpY = y;
        this.lerpZ = z;
        // Vanilla movement packets quantize yaw to 1/256 turn. Do not overwrite
        // the precise target with a coarse angle arriving in the same update.
        this.lerpYRot = firmaciv$receivedPreciseYaw ? entityData.get(firmaciv$PRECISE_YAW) : yaw;
        this.lerpXRot = pitch;
        this.lerpSteps = steps;
        if (steps <= 0) {
            setPos(x, y, z);
            setRot(yaw, pitch);
        }
    }

    @Override public double lerpTargetX() { return lerpSteps > 0 ? lerpX : getX(); }
    @Override public double lerpTargetY() { return lerpSteps > 0 ? lerpY : getY(); }
    @Override public double lerpTargetZ() { return lerpSteps > 0 ? lerpZ : getZ(); }
    @Override public float lerpTargetYRot() { return lerpSteps > 0 ? (float) lerpYRot : getYRot(); }
    @Override public float lerpTargetXRot() { return lerpSteps > 0 ? (float) lerpXRot : getXRot(); }

    @WrapWithCondition(method = "tick", at = {
            @At(value = "INVOKE", target = "Lcom/alekiponi/alekiships/common/entity/vehicle/AbstractAlekiBoatEntity;tickWindInput()V"),
            @At(value = "INVOKE", target = "Lcom/alekiponi/alekiships/common/entity/vehicle/AbstractAlekiBoatEntity;tickControlBoat()V")})
    private boolean firmaciv$controlOwnedBoat(AbstractAlekiBoatEntity<?> boat) {
        return !boat.level().isClientSide || boat.isControlledByLocalInstance();
    }

    @Inject(method = "tickFloatBoat", at = @At("HEAD"), cancellable = true)
    private void firmaciv$simulateOwnedBoat(CallbackInfo ci) {
        if (level().isClientSide && !isControlledByLocalInstance()) {
            setDeltaMovement(Vec3.ZERO);
            ci.cancel();
        }
    }

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE",
            target = "Lcom/alekiponi/alekiships/common/entity/vehicle/AbstractAlekiBoatEntity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"))
    private boolean firmaciv$moveOwnedBoat(AbstractAlekiBoatEntity<?> boat, MoverType type, Vec3 movement) {
        return !boat.level().isClientSide || boat.isControlledByLocalInstance();
    }

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE",
            target = "Lcom/alekiponi/alekiships/common/entity/vehicle/AbstractAlekiBoatEntity;setYRot(F)V"))
    private boolean firmaciv$rotateOwnedBoat(AbstractAlekiBoatEntity<?> boat, float yaw) {
        return !boat.level().isClientSide || boat.isControlledByLocalInstance();
    }
}
