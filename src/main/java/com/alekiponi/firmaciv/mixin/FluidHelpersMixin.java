package com.alekiponi.firmaciv.mixin;

import com.alekiponi.firmaciv.util.Helper;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(FluidHelpers.class)
public abstract class FluidHelpersMixin {
    @Inject(method = "getNewFluidWithMixing", at = @At(value = "HEAD"), cancellable = true, remap=false)
    private static void injectCanalSourceCreation(FlowingFluid self, Level level, BlockPos pos, BlockState blockStateIn, int dropOff, CallbackInfoReturnable<FluidState> cir) {
        Optional<FluidState> createdCanalSource = Helper.canalSourceCreation(self, level, pos, blockStateIn);
        createdCanalSource.ifPresent(cir::setReturnValue);
    }

}
