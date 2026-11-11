package com.alekiponi.firmaciv.common.entity.vehicle;

import com.alekiponi.alekiships.common.entity.vehicle.SloopUnderConstructionEntity;
import com.alekiponi.firmaciv.util.BoatMaterial;
import com.alekiponi.firmaciv.common.item.FirmacivItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import oshi.util.tuples.Pair;

public class FirmacivSloopUnderConstructionEntity extends SloopUnderConstructionEntity {
    public FirmacivSloopUnderConstructionEntity(
            final EntityType<? extends FirmacivSloopUnderConstructionEntity> entityType, final Level level,
            final BoatMaterial boatMaterial) {
        super(entityType, level);
        this.setVariant(level.registryAccess().registryOrThrow(com.alekiponi.alekiships.common.AlekiShipsRegistries.CONSTRUCTION_SLOOP_VARIANT)
            .getHolderOrThrow(net.minecraft.resources.ResourceKey.create(com.alekiponi.alekiships.common.AlekiShipsRegistries.CONSTRUCTION_SLOOP_VARIANT,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("firmaciv", boatMaterial.getSerializedName()))));
    }

}
