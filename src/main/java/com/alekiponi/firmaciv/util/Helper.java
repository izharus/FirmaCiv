package com.alekiponi.firmaciv.util;

import com.alekiponi.alekiships.common.entity.vehicle.AbstractAlekiBoatEntity;
import com.alekiponi.alekiships.common.entity.compartment.EmptyCompartmentEntity;
import com.alekiponi.firmaciv.events.config.FirmacivConfig;
import net.dries007.tfc.mixin.accessor.FlowingFluidAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import java.util.Optional;

public class Helper {
    public static Optional<FluidState> canalSourceCreation(FlowingFluid self, Level pLevel, BlockPos pPos, BlockState pBlockState){

        // TODO check to see if there's a large enough volume to spread horizontally?

        if (!FirmacivConfig.SERVER.enableParallelSourceCreationAtSeaLevel.get()){
            return Optional.empty();
        }

        boolean flag = false;
        Direction from = null;

        // canal building should only work at sea level
        if (pLevel.getSeaLevel()-1 == pPos.getY()){
            // if there's adjacent sources on any side, make a source
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos blockpos = pPos.relative(direction);
                BlockState blockstate = pLevel.getBlockState(blockpos);
                FluidState fluidstate = blockstate.getFluidState();
                if (fluidstate.getType().isSame(self) && ((FlowingFluidAccessor)self).invoke$canPassThroughWall(direction, pLevel, pPos, pBlockState, blockpos, blockstate)) {
                    if (fluidstate.isSource() && net.neoforged.neoforge.event.EventHooks.canCreateFluidSource(pLevel, blockpos, blockstate)) {
                        Direction cw = direction.getClockWise();
                        Direction ccw = direction.getCounterClockWise();
                        BlockPos blockpos1 = blockpos.relative(cw);
                        BlockPos blockpos2 = blockpos.relative(ccw);
                        BlockState blockstate1 = pLevel.getBlockState(blockpos1);
                        BlockState blockstate2 = pLevel.getBlockState(blockpos2);
                        FluidState fluidstate1 = blockstate1.getFluidState();
                        FluidState fluidstate2 = blockstate2.getFluidState();
                        if (fluidstate1.getType().isSame((FlowingFluid) (Object) self) && ((FlowingFluidAccessor)self).invoke$canPassThroughWall(cw, pLevel, pPos, pBlockState, blockpos1, blockstate1)) {
                            if (fluidstate1.isSource() && net.neoforged.neoforge.event.EventHooks.canCreateFluidSource(pLevel, blockpos1, blockstate1)) {
                                BlockState blockstate3 = pLevel.getBlockState(pPos.below());
                                FluidState fluidstate3 = blockstate1.getFluidState();
                                if (blockstate3.isSolid() || fluidstate3.getType().isSame((Fluid)(Object)self)) {
                                    //cir.setReturnValue(this.getSource(false));
                                    flag = true;
                                    from = direction;
                                }
                            }
                        } else if (fluidstate2.getType().isSame((FlowingFluid) (Object) self) && ((FlowingFluidAccessor)self).invoke$canPassThroughWall(cw, pLevel, pPos, pBlockState, blockpos2, blockstate2)) {
                            if (fluidstate2.isSource() && net.neoforged.neoforge.event.EventHooks.canCreateFluidSource(pLevel, blockpos2, blockstate2)) {
                                BlockState blockstate3 = pLevel.getBlockState(pPos.below());
                                FluidState fluidstate3 = blockstate1.getFluidState();
                                if (blockstate3.isSolid() || fluidstate3.getType().isSame((Fluid)(Object)self)) {

                                    flag = true;
                                    from = direction;
                                }
                            }
                        }
                    }
                }
            }

            // check to see if the water is enclosed properly
            boolean flag1 = false;
            BlockPos pos1 = pPos;

            if (flag) {
                Direction.Axis across = from.getClockWise().getAxis();
                for (int i = 1; i < 3; i++) {
                    BlockState state = pLevel.getBlockState(pPos.relative(across, i));
                    if (state.isSolid()) {
                        flag1 = true;
                        pos1 = pPos.relative(across, i);
                        break;
                    }
                }
                if (flag1){
                    for (int i = 1; i < 3; i++) {
                        BlockState state = pLevel.getBlockState(pPos.relative(across, -i));
                        if (state.isSolid()) {
                            // the canal is narrow enough to allow for spreading
                            return Optional.of(self.getSource(false));
                        }
                    }
                }

            }
        }
        return Optional.empty();
    }

    public static boolean hasAnyInput(EmptyCompartmentEntity compartment){
        return compartment.getInputDown() || compartment.getInputLeft() || compartment.getInputRight() || compartment.getInputUp();
    }

    public static void causeExhaustionWhilePaddlingRowing(AbstractAlekiBoatEntity thisBoat, float factor){
        // TODO only causes hunger for the pilot; should also cause hunger for other paddling passengers
        if (thisBoat.getControllingPassenger() instanceof Player player
                && thisBoat.getDeltaMovement().multiply(1,0,1).length() > 0){
            if (thisBoat.getPilotCompartment() != null && Helper.hasAnyInput(thisBoat.getPilotCompartment())){
                float k = Math.round(thisBoat.getDeltaMovement().multiply(1,0,1).length() * 100.0F);
                if (k > 0) {
                    player.causeFoodExhaustion(factor * k * 0.01F);
                }
            }
        }
    }

    public static void causeExhaustionWhileSailing(AbstractAlekiBoatEntity thisBoat, float factor){
        if (thisBoat.getControllingPassenger() instanceof Player player){
            if (thisBoat.getPilotCompartment() != null && Helper.hasAnyInput(thisBoat.getPilotCompartment())){
                player.causeFoodExhaustion(factor * 25 * 0.01F);
            }
        }
    }
}
