package com.alekiponi.firmaciv.common.entity.vehicle;

import com.alekiponi.alekiships.common.AlekiShipsRegistries;
import com.alekiponi.alekiships.common.entity.vehicle.AbstractAlekiBoatEntity;
import com.alekiponi.alekiships.common.entity.vehicle.RowboatVariant;
import com.alekiponi.alekiships.common.entity.vehicle.RowboatVariants;
import com.alekiponi.alekiships.network.AlekiShipsEntityDataSerializers;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public abstract class FirmacivPaddledBoatEntity extends AbstractAlekiBoatEntity<RowboatVariant> {
    private static final EntityDataAccessor<Holder<RowboatVariant>> VARIANT = SynchedEntityData.defineId(FirmacivPaddledBoatEntity.class, AlekiShipsEntityDataSerializers.ROWBOAT_VARIANT);

    protected FirmacivPaddledBoatEntity(EntityType<? extends FirmacivPaddledBoatEntity> type, Level level, String material) {
        super(type, level);
        this.windModel = com.alekiponi.firmaciv.util.TFCWindModel.INSTANCE;
        setVariant(level.registryAccess().registryOrThrow(AlekiShipsRegistries.ROWBOAT_VARIANT)
            .getHolderOrThrow(ResourceKey.create(AlekiShipsRegistries.ROWBOAT_VARIANT, ResourceLocation.fromNamespaceAndPath("firmaciv", material))));
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, registryAccess().registryOrThrow(AlekiShipsRegistries.ROWBOAT_VARIANT).getHolderOrThrow(RowboatVariants.DEFAULT));
    }
    @Override public Holder<RowboatVariant> getVariant() { return entityData.get(VARIANT); }
    @Override public void setVariant(Holder<RowboatVariant> variant) { entityData.set(VARIANT, variant); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); RowboatVariant.save(tag, getVariant()); }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); RowboatVariant.load(tag, registryAccess(), this::setVariant); }
}
