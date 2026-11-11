package com.alekiponi.firmaciv.common.block;


import com.alekiponi.alekiships.common.block.AngledBoatFrameBlock;
import com.alekiponi.alekiships.common.block.FlatBoatFrameBlock;
import com.alekiponi.alekiships.util.CommonHelper;
import com.alekiponi.firmaciv.Firmaciv;
import com.alekiponi.firmaciv.common.item.FirmacivItems;
import com.alekiponi.firmaciv.util.TFCWood;
import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.EnumMap;
import java.util.function.Supplier;

public final class FirmacivBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK,
            Firmaciv.MOD_ID);

    public static final EnumMap<TFCWood, DeferredHolder<Block, CanoeComponentBlock>> CANOE_COMPONENT_BLOCKS = CommonHelper.mapOfKeys(
            TFCWood.class, tfcWood -> registerBlock("wood/canoe_component_block/" + tfcWood.getSerializedName(),
                    () -> new CanoeComponentBlock(
                            BlockBehaviour.Properties.ofFullCopy(tfcWood.wood.getBlock(Wood.BlockType.STRIPPED_LOG).get())
                                    .mapColor(tfcWood.wood.woodColor()).noOcclusion(), tfcWood)));

    public static final EnumMap<Wood, DeferredHolder<Block, SquaredAngleBlock>> WOOD_ROOFING = CommonHelper.mapOfKeys(
            Wood.class, tfcWood -> registerBlockWithItem("wood/" + tfcWood.getSerializedName() + "_roofing",
                    () -> new SquaredAngleBlock(Blocks.ACACIA_STAIRS.defaultBlockState(),
                            BlockBehaviour.Properties.ofFullCopy(tfcWood.getBlock(Wood.BlockType.STAIRS).get())
                                    .mapColor(tfcWood.woodColor()).noOcclusion())));

    public static final DeferredHolder<Block, Block> THATCH_ROOFING = registerBlockWithItem("thatch_roofing",
            () -> new AngledThatchRoofingBlock(Blocks.ACACIA_STAIRS.defaultBlockState(),
                    BlockBehaviour.Properties.of().strength(0.6F, 0.4F).noOcclusion().isViewBlocking(TFCBlocks::never)
                            .sound(TFCSounds.THATCH)));

    public static final DeferredHolder<Block, Block> THATCH_ROOFING_STAIRS = registerBlock("thatch_roofing_stairs",
            () -> new StairThatchRoofingBlock(Blocks.ACACIA_STAIRS.defaultBlockState(),
                    BlockBehaviour.Properties.of().strength(0.6F, 0.4F).noOcclusion().isViewBlocking(TFCBlocks::never)
                            .sound(TFCSounds.THATCH)));

    public static final DeferredHolder<Block, Block> THATCH_ROOFING_SLAB = registerBlockWithItem("thatch_roofing_slab",
            () -> new FlatThatchRoofingBlock(
                    BlockBehaviour.Properties.of().strength(0.6F, 0.4F).noOcclusion().isViewBlocking(TFCBlocks::never)
                            .sound(TFCSounds.THATCH)));

    public static final DeferredHolder<Block, FirmacivAngledBoatFrameBlock> BOAT_FRAME_ANGLED = registerBlockWithItem(
            "watercraft_frame_angled",
            () -> new FirmacivAngledBoatFrameBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion()));

    public static final EnumMap<TFCWood, DeferredHolder<Block, FirmacivAngledWoodenBoatFrameBlock>> WOODEN_BOAT_FRAME_ANGLED = CommonHelper.mapOfKeys(
            TFCWood.class, tfcWood -> registerBlock("wood/watercraft_frame/angled/" + tfcWood.getSerializedName(),
                    () -> new FirmacivAngledWoodenBoatFrameBlock(tfcWood,
                            BlockBehaviour.Properties.ofFullCopy(BOAT_FRAME_ANGLED.get()))));

    public static final DeferredHolder<Block, FirmacivFlatBoatFrameBlock> BOAT_FRAME_FLAT = registerBlockWithItem(
            "watercraft_frame_flat",
            () -> new FirmacivFlatBoatFrameBlock(BlockBehaviour.Properties.ofFullCopy(BOAT_FRAME_ANGLED.get())));

    public static final EnumMap<TFCWood, DeferredHolder<Block, FirmacivFlatWoodenBoatFrameBlock>> WOODEN_BOAT_FRAME_FLAT = CommonHelper.mapOfKeys(
            TFCWood.class, tfcWood -> registerBlock("wood/watercraft_frame/flat/" + tfcWood.getSerializedName(),
                    () -> new FirmacivFlatWoodenBoatFrameBlock(tfcWood,
                            BlockBehaviour.Properties.ofFullCopy(BOAT_FRAME_FLAT.get()))));

    private static <T extends Block> DeferredHolder<Block, T> registerBlock(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    private static <T extends Block> DeferredHolder<Block, T> registerBlockWithItem(String name, Supplier<T> block) {
        DeferredHolder<Block, T> blockRegistryObject = BLOCKS.register(name, block);
        registerBlockItem(name, blockRegistryObject);
        return blockRegistryObject;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredHolder<Block, T> block) {
        FirmacivItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}