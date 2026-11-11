package com.alekiponi.firmaciv.mixin;

import com.alekiponi.alekiships.common.entity.vehicle.AbstractVehicle;
import net.dries007.tfc.common.TFCTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** TFC's natural waters support boating but are not all in minecraft:water. */
@Mixin(value = AbstractVehicle.class, remap = false)
public abstract class VehicleWaterMixin {
    @Redirect(method = {"getWaterLevelAbove", "checkInWater", "isUnderwater", "checkFallDamage"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean firmaciv$recognizeNaturalWater(FluidState state, TagKey<Fluid> tag) {
        return state.is(tag) || (tag.equals(FluidTags.WATER) && state.is(TFCTags.Fluids.ANY_INFINITE_WATER));
    }
}
