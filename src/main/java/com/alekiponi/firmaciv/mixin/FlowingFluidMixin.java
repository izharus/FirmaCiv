package com.alekiponi.firmaciv.mixin;

import com.alekiponi.firmaciv.util.Helper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(FlowingFluid.class)
public abstract class FlowingFluidMixin {

    @Shadow
    protected abstract boolean canPassThroughWall(Direction pDirection, BlockGetter pLevel, BlockPos pPos, BlockState pState, BlockPos pSpreadPos, BlockState pSpreadState);

    @Shadow
    protected abstract boolean isSourceBlockOfThisType(FluidState pState);

    @Shadow
    public abstract FluidState getSource(boolean pFalling);

    @Inject(method = "getNewLiquid", at = @At(value = "HEAD"), cancellable = true)
    public void injectCanalSourceCreation(Level pLevel, BlockPos pPos, BlockState pBlockState, CallbackInfoReturnable<FluidState> cir) {
        Optional<FluidState> createdCanalSource = Helper.canalSourceCreation((FlowingFluid) (Object) this, pLevel, pPos, pBlockState);
        createdCanalSource.ifPresent(cir::setReturnValue);
    }


}
