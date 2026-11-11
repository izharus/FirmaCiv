package com.alekiponi.firmaciv.mixin;

import com.alekiponi.alekiships.common.entity.vehicle.AbstractAlekiBoatEntity;
import com.alekiponi.alekiships.wind.Wind;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Compatibility with Ships 2.0.0-dev: its calm-wind fallback constructs an invalid negative speed. */
@Mixin(value = AbstractAlekiBoatEntity.class, remap = false)
public abstract class BoatWindMixin {
    @Redirect(method = "tickUpdateWind", at = @At(value = "NEW", target = "com/alekiponi/alekiships/wind/Wind"))
    private Wind firmaciv$validCalmWind(float speed, float angle) {
        return Wind.of(speed, angle);
    }
}
