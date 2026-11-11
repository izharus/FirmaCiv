package com.alekiponi.firmaciv.common.blockentity;

import com.alekiponi.firmaciv.Firmaciv;
import net.dries007.tfc.util.registry.RegistrationHelpers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.alekiponi.firmaciv.common.block.FirmacivBlocks.CANOE_COMPONENT_BLOCKS;

public final class FirmacivBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            Registries.BLOCK_ENTITY_TYPE, Firmaciv.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CanoeComponentBlockEntity>> CANOE_COMPONENT_BLOCK_ENTITY = register(
            "canoe_component_block_entity", CanoeComponentBlockEntity::new, CANOE_COMPONENT_BLOCKS.values().stream());

    private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(String name,
            BlockEntityType.BlockEntitySupplier<T> factory, Supplier<? extends Block> block) {
        return RegistrationHelpers.register(BLOCK_ENTITIES, name, factory, block);
    }

    private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(String name,
            BlockEntityType.BlockEntitySupplier<T> factory, Stream<? extends Supplier<? extends Block>> blocks) {
        return RegistrationHelpers.register(BLOCK_ENTITIES, name, factory, blocks);
    }
}