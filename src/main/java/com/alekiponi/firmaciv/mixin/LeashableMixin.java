package com.alekiponi.firmaciv.mixin;

import com.alekiponi.alekiships.common.entity.vehiclehelper.CleatEntity;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** A cleat is a fixed part of a vehicle, including when a saved or fence leash is restored. */
@Mixin(value = Leashable.class, remap = false)
public interface LeashableMixin {
    @WrapWithCondition(method = "setLeashedTo(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Z)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;stopRiding()V"))
    private static boolean firmaciv$keepMooringMounted(Entity entity) {
        return !(entity instanceof CleatEntity);
    }
}
