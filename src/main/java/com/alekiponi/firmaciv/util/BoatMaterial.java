package com.alekiponi.firmaciv.util;

import com.alekiponi.alekiships.common.entity.vehicle.AbstractVehicle;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import java.util.Optional;

public interface BoatMaterial extends StringRepresentable {
    enum BoatType { ROWBOAT, SLOOP, CONSTRUCTION_SLOOP }
    Item getRailing();
    Item getStrippedLog();
    boolean withstandsLava();
    BlockState getDeckBlock();
    default Item getDeckItem() { return getDeckBlock().getBlock().asItem(); }
    Optional<EntityType<? extends net.minecraft.world.entity.Entity>> getEntityType(BoatType type);
}
