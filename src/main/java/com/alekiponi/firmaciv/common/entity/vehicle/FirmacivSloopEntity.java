package com.alekiponi.firmaciv.common.entity.vehicle;

import com.alekiponi.alekiships.common.entity.vehicle.SloopEntity;
import com.alekiponi.alekiships.common.entity.vehiclecapability.IAllowFallDamage;
import com.alekiponi.firmaciv.util.BoatMaterial;
import com.alekiponi.firmaciv.util.Helper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class FirmacivSloopEntity extends SloopEntity implements IAllowFallDamage {

    protected final float HUNGER_FACTOR = 0.04F;
    public FirmacivSloopEntity(final EntityType<? extends FirmacivSloopEntity> entityType, final Level level,
            final BoatMaterial boatMaterial) {
        super(entityType, level);
        this.windModel = com.alekiponi.firmaciv.util.TFCWindModel.INSTANCE;
        this.setVariant(level.registryAccess().registryOrThrow(com.alekiponi.alekiships.common.AlekiShipsRegistries.SLOOP_VARIANT)
            .getHolderOrThrow(net.minecraft.resources.ResourceKey.create(com.alekiponi.alekiships.common.AlekiShipsRegistries.SLOOP_VARIANT,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("firmaciv", boatMaterial.getSerializedName()))));
    }

    @Override
    public void tick(){
        super.tick();
        Helper.causeExhaustionWhilePaddlingRowing(this, HUNGER_FACTOR);
    }
}