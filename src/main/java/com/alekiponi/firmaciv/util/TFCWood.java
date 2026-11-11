package com.alekiponi.firmaciv.util;

import com.alekiponi.alekiships.common.entity.vehicle.AbstractVehicle;
import com.alekiponi.firmaciv.util.BoatMaterial;
import com.alekiponi.firmaciv.common.block.CanoeComponentBlock;
import com.alekiponi.firmaciv.common.block.FirmacivBlocks;
import com.alekiponi.firmaciv.common.entity.FirmacivEntities;
import com.alekiponi.firmaciv.common.entity.vehicle.CanoeEntity;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public enum TFCWood implements CanoeMaterial, BoatMaterial {
    ACACIA(Wood.ACACIA),
    ASH(Wood.ASH),
    ASPEN(Wood.ASPEN),
    BIRCH(Wood.BIRCH),
    BLACKWOOD(Wood.BLACKWOOD),
    CHESTNUT(Wood.CHESTNUT),
    DOUGLAS_FIR(Wood.DOUGLAS_FIR),
    HICKORY(Wood.HICKORY),
    KAPOK(Wood.KAPOK),
    MANGROVE(Wood.MANGROVE),
    MAPLE(Wood.MAPLE),
    OAK(Wood.OAK),
    PALM(Wood.PALM),
    PINE(Wood.PINE),
    ROSEWOOD(Wood.ROSEWOOD),
    SEQUOIA(Wood.SEQUOIA),
    SPRUCE(Wood.SPRUCE),
    SYCAMORE(Wood.SYCAMORE),
    WHITE_CEDAR(Wood.WHITE_CEDAR),
    WILLOW(Wood.WILLOW);

    public final Wood wood;

    TFCWood(final Wood wood) {
        this.wood = wood;
    }

    public static void registerFrames() {
        for (final TFCWood tfcWood : values()) {
            FirmacivBlocks.BOAT_FRAME_ANGLED.get()
                    .registerFrame(tfcWood.wood.getBlock(Wood.BlockType.PLANKS).get().asItem(),
                            FirmacivBlocks.WOODEN_BOAT_FRAME_ANGLED.get(tfcWood).get());
            FirmacivBlocks.BOAT_FRAME_FLAT.get()
                    .registerFrame(tfcWood.wood.getBlock(Wood.BlockType.PLANKS).get().asItem(),
                            FirmacivBlocks.WOODEN_BOAT_FRAME_FLAT.get(tfcWood).get());
            CanoeComponentBlock.registerCanoeComponent(
                    (RotatedPillarBlock) tfcWood.wood.getBlock(Wood.BlockType.STRIPPED_LOG).get(),
                    FirmacivBlocks.CANOE_COMPONENT_BLOCKS.get(tfcWood).get());
        }
    }

    @Override
    public String getSerializedName() {
        return this.wood.getSerializedName();
    }

    @Override
    public Item getRailing() {
        return TFCItems.LUMBER.get(this.wood).get();
    }

    @Override
    public Item getStrippedLog() {
        return this.wood.getBlock(Wood.BlockType.STRIPPED_LOG).get().asItem();
    }

    @Override
    public boolean withstandsLava() {
        return false;
    }

    @Override
    public BlockState getDeckBlock() {
        return this.wood.getBlock(Wood.BlockType.PLANKS).get().defaultBlockState();
    }

    @Override
    public Optional<EntityType<? extends CanoeEntity>> getCanoeType() {
        return Optional.of(FirmacivEntities.TFC_CANOES.get(this).get());
    }

    @Override
    public Optional<EntityType<? extends net.minecraft.world.entity.Entity>> getEntityType(final BoatType boatType) {
        return switch (boatType) {
            case ROWBOAT -> Optional.of(FirmacivEntities.TFC_ROWBOATS.get(this).get());
            case SLOOP -> Optional.of(FirmacivEntities.TFC_SLOOPS.get(this).get());
            case CONSTRUCTION_SLOOP -> Optional.of(FirmacivEntities.TFC_SLOOPS_UNDER_CONSTRUCTION.get(this).get());
        };
    }
}
