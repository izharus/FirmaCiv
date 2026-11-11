/* Adapted from aleki's Ridiculously Simple Roofs 1.0.1 by alekiponi (MIT). */
package com.alekiponi.firmaciv.common.block;

import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SquaredAngleBlock extends StairBlock {
   protected static final VoxelShape TOP_AABB = Block.box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);
   protected static final VoxelShape BOTTOM_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
   protected static final VoxelShape OCTET_NPN = Block.box(0.0, 8.0, 0.0, 8.0, 16.0, 8.0);
   protected static final VoxelShape OCTET_NPP = Block.box(0.0, 8.0, 8.0, 8.0, 16.0, 16.0);
   protected static final VoxelShape OCTET_PPN = Block.box(8.0, 8.0, 0.0, 16.0, 16.0, 8.0);
   protected static final VoxelShape OCTET_PPP = Block.box(8.0, 8.0, 8.0, 16.0, 16.0, 16.0);
   protected static final VoxelShape[] BOTTOM_SHAPES = makeRoofShapes(BOTTOM_AABB, OCTET_NPN, OCTET_PPN, OCTET_NPP, OCTET_PPP);
   private static final int[] SHAPE_BY_STATE = new int[]{12, 5, 3, 10, 14, 13, 7, 11, 13, 7, 11, 14, 8, 4, 1, 2, 4, 1, 2, 8};

   public SquaredAngleBlock(BlockState pBaseState, Properties pProperties) {
      super(pBaseState, pProperties);
   }

   private static VoxelShape[] makeRoofShapes(
      VoxelShape slabShape, VoxelShape northWestCorner, VoxelShape northEastCorner, VoxelShape southWestCorner, VoxelShape southEastCorner
   ) {
      return IntStream.range(0, 16)
         .mapToObj(bitfield -> makeRoofShape(bitfield, slabShape, northWestCorner, northEastCorner, southWestCorner, southEastCorner))
         .toArray(VoxelShape[]::new);
   }

   private static VoxelShape makeRoofShape(
      int bitfield, VoxelShape slabShape, VoxelShape northWestCorner, VoxelShape northEastCorner, VoxelShape southWestCorner, VoxelShape southEastCorner
   ) {
      double move = slabShape == TOP_AABB ? 0.5 : -0.5;
      VoxelShape northWestCornerOpposite = northWestCorner.move(0.0, move, 0.0);
      VoxelShape northEastCornerOpposite = northEastCorner.move(0.0, move, 0.0);
      VoxelShape southWestCornerOpposite = southWestCorner.move(0.0, move, 0.0);
      VoxelShape southEastCornerOpposite = southEastCorner.move(0.0, move, 0.0);
      VoxelShape voxelshape = slabShape;
      if ((bitfield & 1) != 0) {
         VoxelShape var13 = Shapes.or(slabShape, northWestCorner);
         voxelshape = Shapes.join(var13, northWestCornerOpposite, BooleanOp.ONLY_FIRST);
      }

      if ((bitfield & 2) != 0) {
         VoxelShape var14 = Shapes.or(voxelshape, northEastCorner);
         voxelshape = Shapes.join(var14, northEastCornerOpposite, BooleanOp.ONLY_FIRST);
      }

      if ((bitfield & 4) != 0) {
         VoxelShape var15 = Shapes.or(voxelshape, southWestCorner);
         voxelshape = Shapes.join(var15, southWestCornerOpposite, BooleanOp.ONLY_FIRST);
      }

      if ((bitfield & 8) != 0) {
         VoxelShape var16 = Shapes.or(voxelshape, southEastCorner);
         voxelshape = Shapes.join(var16, southEastCornerOpposite, BooleanOp.ONLY_FIRST);
      }

      return voxelshape;
   }

   protected static StairsShape getRoofShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
      Direction direction = (Direction)blockState.getValue(FACING);
      BlockState neighborBlockState = blockGetter.getBlockState(blockPos.relative(direction));
      if (isStairs(neighborBlockState)) {
         Direction neighborFacing = (Direction)neighborBlockState.getValue(FACING);
         if (neighborFacing.getAxis() != ((Direction)blockState.getValue(FACING)).getAxis()
            && canConnect(blockState, blockGetter, blockPos, neighborFacing.getOpposite())) {
            if (neighborFacing == direction.getCounterClockWise()) {
               return StairsShape.OUTER_LEFT;
            }

            return StairsShape.OUTER_RIGHT;
         }
      }

      neighborBlockState = blockGetter.getBlockState(blockPos.relative(direction.getOpposite()));
      if (isStairs(neighborBlockState)) {
         Direction neighborFacing = (Direction)neighborBlockState.getValue(FACING);
         if (neighborFacing.getAxis() != ((Direction)blockState.getValue(FACING)).getAxis()
            && canConnect(blockState, blockGetter, blockPos, neighborFacing)) {
            if (neighborFacing == direction.getCounterClockWise()) {
               return StairsShape.INNER_LEFT;
            }

            return StairsShape.INNER_RIGHT;
         }
      }

      return StairsShape.STRAIGHT;
   }

   private static boolean canConnect(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
      BlockState blockstate = blockGetter.getBlockState(blockPos.relative(direction));
      return !isStairs(blockstate) || blockstate.getValue(FACING) != blockState.getValue(FACING);
   }

   public boolean useShapeForLightOcclusion(BlockState blockState) {
      return true;
   }

   public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext context) {
      return BOTTOM_SHAPES[SHAPE_BY_STATE[this.getShapeIndex(blockState)]];
   }

   private int getShapeIndex(BlockState blockState) {
      return ((StairsShape)blockState.getValue(SHAPE)).ordinal() * 4 + ((Direction)blockState.getValue(FACING)).get2DDataValue();
   }

   public BlockState getStateForPlacement(BlockPlaceContext placeContext) {
      BlockPos blockpos = placeContext.getClickedPos();
      FluidState fluidstate = placeContext.getLevel().getFluidState(blockpos);
      BlockState blockstate = (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, placeContext.getHorizontalDirection()))
         .setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
      return (BlockState)blockstate.setValue(SHAPE, getRoofShape(blockstate, placeContext.getLevel(), blockpos));
   }

   public BlockState updateShape(
      BlockState blockState, Direction direction, BlockState neighborState, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos neighborPos
   ) {
      if ((Boolean)blockState.getValue(WATERLOGGED)) {
         levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
      }

      return direction.getAxis().isHorizontal()
         ? (BlockState)blockState.setValue(SHAPE, getRoofShape(blockState, levelAccessor, blockPos))
         : super.updateShape(blockState, direction, neighborState, levelAccessor, blockPos, neighborPos);
   }

   public BlockState rotate(BlockState blockState, Rotation rotation) {
      return (BlockState)blockState.setValue(FACING, rotation.rotate((Direction)blockState.getValue(FACING)));
   }

   public BlockState mirror(BlockState blockState, Mirror mirror) {
      Direction direction = (Direction)blockState.getValue(FACING);
      StairsShape stairsshape = (StairsShape)blockState.getValue(SHAPE);
      switch (mirror) {
         case LEFT_RIGHT:
            if (direction.getAxis() == Axis.Z) {
               return switch (stairsshape) {
                  case INNER_LEFT -> (BlockState)blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
                  case INNER_RIGHT -> (BlockState)blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
                  case OUTER_LEFT -> (BlockState)blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
                  case OUTER_RIGHT -> (BlockState)blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
                  default -> blockState.rotate(Rotation.CLOCKWISE_180);
               };
            }
            break;
         case FRONT_BACK:
            if (direction.getAxis() == Axis.X) {
               return switch (stairsshape) {
                  case INNER_LEFT -> (BlockState)blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
                  case INNER_RIGHT -> (BlockState)blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
                  case OUTER_LEFT -> (BlockState)blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
                  case OUTER_RIGHT -> (BlockState)blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
                  case STRAIGHT -> blockState.rotate(Rotation.CLOCKWISE_180);
                  default -> throw new IncompatibleClassChangeError();
               };
            }
      }

      return super.mirror(blockState, mirror);
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, SHAPE, WATERLOGGED, HALF});
   }

   public FluidState getFluidState(BlockState blockState) {
      return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
   }

   public boolean isPathfindable(BlockState blockState, PathComputationType computationType) {
      return false;
   }
}
