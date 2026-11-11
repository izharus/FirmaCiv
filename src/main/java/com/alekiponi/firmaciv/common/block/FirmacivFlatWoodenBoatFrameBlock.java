package com.alekiponi.firmaciv.common.block;

import com.alekiponi.alekiships.common.block.*;
import com.alekiponi.alekiships.util.CommonHelper;
import com.alekiponi.firmaciv.util.BoatMaterial;
import com.alekiponi.firmaciv.Firmaciv;
import com.alekiponi.firmaciv.common.item.FirmacivItems;
import net.dries007.tfc.common.TFCTags;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class FirmacivFlatWoodenBoatFrameBlock extends FlatBoatFrameBlock {
    public static final IntegerProperty FRAME_PROCESSED = FirmacivBlockStateProperties.FRAME_PROCESSED_7;
    public static final int FULLY_PLANKED = 3;
    private static final int FULLY_PROCESSED = 7;
    public final BoatMaterial boatMaterial;

    public FirmacivFlatWoodenBoatFrameBlock(final BoatMaterial boatMaterial, final Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FRAME_PROCESSED, 0));
        this.boatMaterial = boatMaterial;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FRAME_PROCESSED);
    }

    private InteractionResult interactFrame(final BlockState blockState, final Level level, final BlockPos blockPos,
                                 final Player player, final InteractionHand hand, final BlockHitResult hitResult) {

        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        final ItemStack heldStack = player.getItemInHand(hand);

        final int processState = blockState.getValue(FRAME_PROCESSED);

        // Try extract
        if (heldStack.isEmpty() && !level.isClientSide) {
            // Extract an item
            if (processState <= FULLY_PLANKED) {
                CommonHelper.giveItemToPlayer(player, new ItemStack(this.boatMaterial.getDeckItem()));
            } else {
                CommonHelper.giveItemToPlayer(player, new ItemStack(FirmacivItems.COPPER_BOLT.get()));
            }

            // Set ourselves back to our base
            if (processState == 0) {
                level.setBlockAndUpdate(blockPos, FirmacivBlocks.BOAT_FRAME_FLAT.get().defaultBlockState());
                return InteractionResult.SUCCESS;
            }

            level.setBlockAndUpdate(blockPos, blockState.setValue(FRAME_PROCESSED, processState - 1));

            return InteractionResult.SUCCESS;
        }

        // Should we do plank stuff
        if (heldStack.is(this.boatMaterial.getDeckItem())) {
            // Must be [0,3)
            if (processState < FULLY_PLANKED) {
                if (!player.getAbilities().instabuild) {
                    heldStack.shrink(1);
                }
                level.setBlockAndUpdate(blockPos, blockState.cycle(FRAME_PROCESSED));
                level.playSound(null, blockPos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.5F,
                        level.getRandom().nextFloat() * 0.1F + 0.9F);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.CONSUME;
        }

        // Should we do bolt stuff
        if (heldStack.is(FirmacivItems.COPPER_BOLT.get()) && player.getOffhandItem().is(TFCTags.Items.TOOLS_HAMMER)) {
            // Must be [3,7)
            if (FULLY_PLANKED <= processState && processState < FULLY_PROCESSED) {
                if (!player.getAbilities().instabuild) {
                    heldStack.shrink(1);
                }
                level.setBlockAndUpdate(blockPos, blockState.cycle(FRAME_PROCESSED));
                level.playSound(null, blockPos, SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 1.5F,
                        level.getRandom().nextFloat() * 0.1F + 0.9F);
                if (processState + 1 == FULLY_PROCESSED) {
                    com.alekiponi.alekiships.common.recipe.EntityMultiblockRecipe.tryAssembleMultiblock(level, blockPos, player, true);
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
            return InteractionResult.FAIL;
        }

        return InteractionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public ItemStack getCloneItemStack(final net.minecraft.world.level.LevelReader blockGetter, final BlockPos blockPos,
                                       final BlockState blockState) {
        // We don't exist as an item so pass it the base version instead
        return FirmacivBlocks.BOAT_FRAME_FLAT.get().getCloneItemStack(blockGetter, blockPos, blockState);
    }

    public IntegerProperty getProcessingProperty() {
        return FRAME_PROCESSED;
    }

    public int getProcessingLimit() {
        return FULLY_PROCESSED;
    }

    public BoatMaterial getBoatMaterial() {
        return boatMaterial;
    }

    @Override protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return net.minecraft.world.ItemInteractionResult.SUCCESS;
        return net.minecraft.world.ItemInteractionResult.valueOf(interactFrame(state, level, pos, player, hand, hit).name().replace("PASS", "PASS_TO_DEFAULT_BLOCK_INTERACTION"));
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return level.isClientSide ? InteractionResult.SUCCESS : interactFrame(state, level, pos, player, InteractionHand.MAIN_HAND, hit);
    }
}
