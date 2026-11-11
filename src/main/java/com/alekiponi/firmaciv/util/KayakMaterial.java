package com.alekiponi.firmaciv.util;

import com.alekiponi.alekiships.common.entity.vehicle.AbstractVehicle;
import com.alekiponi.firmaciv.util.BoatMaterial;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public enum KayakMaterial implements BoatMaterial {
    KAYAK;

    @Override
    public Item getRailing() {
        return null;
    }

    @Override
    public Item getStrippedLog() {
        return null;
    }

    @Override
    public boolean withstandsLava() {
        return false;
    }

    @Override
    public BlockState getDeckBlock() {
        return null;
    }

    @Override
    public Optional<EntityType<? extends net.minecraft.world.entity.Entity>> getEntityType(BoatType boatType) {
        return Optional.empty();
    }

    @Override
    public String getSerializedName() {
        return null;
    }
}
