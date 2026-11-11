package com.alekiponi.firmaciv.common.entity;

import com.alekiponi.alekiships.common.AlekiShipsRegistries;
import com.alekiponi.alekiships.common.entity.CannonEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class FirmacivCannonEntity extends CannonEntity {
    public FirmacivCannonEntity(EntityType<? extends CannonEntity> type, Level level) {
        super(type, level);
        setCannonInput(ResourceKey.create(AlekiShipsRegistries.ENTITY_INPUT, ResourceLocation.fromNamespaceAndPath("firmaciv", "cannon")));
    }
}
