package com.alekiponi.firmaciv.common.entity.vehicle;

import com.alekiponi.alekiships.common.entity.vehicle.SloopEntity;
import com.alekiponi.alekiships.common.entity.vehiclecapability.IAllowFallDamage;
import com.alekiponi.alekiships.util.BoatMaterial;
import com.alekiponi.firmaciv.util.Helper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class FirmacivSloopEntity extends SloopEntity implements IAllowFallDamage {

    protected final float HUNGER_FACTOR = 0.04F;
    public FirmacivSloopEntity(final EntityType<? extends FirmacivSloopEntity> entityType, final Level level,
            final BoatMaterial boatMaterial) {
        super(entityType, level, boatMaterial);
    }

    @Override
    public void tick(){
        super.tick();
        Helper.causeExhaustionWhilePaddlingRowing(this, HUNGER_FACTOR);
    }
}