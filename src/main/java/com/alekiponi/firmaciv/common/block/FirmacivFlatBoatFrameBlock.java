package com.alekiponi.firmaciv.common.block;

import com.alekiponi.alekiships.common.block.FlatBoatFrameBlock;
import com.alekiponi.firmaciv.events.config.FirmacivConfig;
import com.alekiponi.firmaciv.util.FirmacivTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import java.util.*;

public class FirmacivFlatBoatFrameBlock extends FlatBoatFrameBlock {
    private final Map<Item, Block> frames = new IdentityHashMap<>();
    public FirmacivFlatBoatFrameBlock(Properties properties) { super(properties); }
    public void registerFrame(Item item, Block frame) { frames.put(item, frame); }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (FirmacivConfig.SERVER.shipWoodRestriction.get() && !stack.is(FirmacivTags.Items.HARD_WOOD)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        Block frame = frames.get(stack.getItem());
        if (frame == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!level.isClientSide) {
            level.setBlockAndUpdate(pos, frame.withPropertiesOf(state));
            if (!player.hasInfiniteMaterials()) stack.shrink(1);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
