package com.alekiponi.firmaciv.mixin;

import com.alekiponi.alekiships.common.entity.compartment.AbstractCompartmentEntity;
import com.alekiponi.alekiships.common.entity.vehicle.AbstractVehicle;
import com.alekiponi.alekiships.common.entity.vehiclehelper.VehiclePart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Respect the 1.21 vehicle attachment point instead of placing compartments at the part's origin. */
@Mixin(value = VehiclePart.class, remap = false)
public abstract class VehiclePartAttachmentMixin extends Entity {
    protected VehiclePartAttachmentMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Inject(method = "positionRider", at = @At("TAIL"))
    private void firmaciv$positionCompartment(Entity passenger, Entity.MoveFunction move, CallbackInfo ci) {
        if (!(passenger instanceof AbstractCompartmentEntity) || !(getVehicle() instanceof AbstractVehicle)
                || !hasPassenger(passenger)) return;
        var position = getPassengerRidingPosition(passenger).subtract(passenger.getVehicleAttachmentPoint(this));
        move.accept(passenger, position.x, position.y, position.z);
        passenger.setPos(position);
    }
}
