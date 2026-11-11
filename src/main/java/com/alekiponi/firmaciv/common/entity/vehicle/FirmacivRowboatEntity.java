package com.alekiponi.firmaciv.common.entity.vehicle;

import com.alekiponi.alekiships.common.entity.vehicle.RowboatEntity;
import com.alekiponi.alekiships.common.entity.vehiclecapability.IAllowFallDamage;
import com.alekiponi.firmaciv.util.BoatMaterial;
import com.alekiponi.firmaciv.util.Helper;
import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;

public class FirmacivRowboatEntity extends RowboatEntity implements IAllowFallDamage {

    protected final float HUNGER_FACTOR = 0.03F;

    public FirmacivRowboatEntity(final EntityType<? extends FirmacivRowboatEntity> entityType, final Level level,
            final BoatMaterial boatMaterial) {
        super(entityType, level);
        this.windModel = com.alekiponi.firmaciv.util.TFCWindModel.INSTANCE;
        this.setVariant(level.registryAccess().registryOrThrow(com.alekiponi.alekiships.common.AlekiShipsRegistries.ROWBOAT_VARIANT)
            .getHolderOrThrow(net.minecraft.resources.ResourceKey.create(com.alekiponi.alekiships.common.AlekiShipsRegistries.ROWBOAT_VARIANT,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("firmaciv", boatMaterial.getSerializedName()))));
    }

    @Override
    public void tick(){
        super.tick();
        Helper.causeExhaustionWhilePaddlingRowing(this, HUNGER_FACTOR);
    }

    @Override
    public boolean windShouldAffect(){
        return true;
    }
}