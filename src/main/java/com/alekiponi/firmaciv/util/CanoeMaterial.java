package com.alekiponi.firmaciv.util;

import com.alekiponi.firmaciv.util.BoatMaterial;
import com.alekiponi.firmaciv.common.entity.vehicle.CanoeEntity;
import net.minecraft.world.entity.EntityType;

import java.util.Optional;

public interface CanoeMaterial {

    /**
     * A simple getter for canoe entities associated with a material
     *
     * @return An optional canoe entity type
     */
    Optional<EntityType<? extends CanoeEntity>> getCanoeType();
}