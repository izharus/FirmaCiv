package com.alekiponi.firmaciv.common.entity;

import com.alekiponi.alekiships.common.entity.CannonEntity;
import com.alekiponi.alekiships.common.entity.vehicle.RowboatEntity;
import com.alekiponi.alekiships.common.entity.vehicle.SloopEntity;
import com.alekiponi.alekiships.common.entity.vehicle.SloopUnderConstructionEntity;
import net.minecraft.world.entity.EntityType;
import com.alekiponi.alekiships.common.entity.compartment.AbstractCompartmentEntity;
import com.alekiponi.alekiships.util.CommonHelper;
import com.alekiponi.firmaciv.common.entity.compartment.LargeVesselCompartmentEntity;
import com.alekiponi.firmaciv.common.entity.compartment.TFCBarrelCompartmentEntity;
import com.alekiponi.firmaciv.common.entity.compartment.TFCChestCompartmentEntity;
import com.alekiponi.firmaciv.common.entity.vehicle.*;
import com.alekiponi.firmaciv.util.FirmacivTags;
import com.alekiponi.firmaciv.util.TFCWood;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.EnumMap;
import java.util.Locale;

import static com.alekiponi.firmaciv.Firmaciv.MOD_ID;

public final class FirmacivEntities {

    private static final int LARGE_VEHICLE_TRACKING = 20;
    private static final int VEHICLE_HELPER_TRACKING = LARGE_VEHICLE_TRACKING + 1;
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(
            Registries.ENTITY_TYPE, MOD_ID);

    public static final EnumMap<TFCWood, DeferredHolder<EntityType<?>, EntityType<FirmacivRowboatEntity>>> TFC_ROWBOATS = CommonHelper.mapOfKeys(
            TFCWood.class, tfcWood -> registerRowboat(tfcWood,
                    EntityType.Builder.of((entityType, level) -> new FirmacivRowboatEntity(entityType, level, tfcWood),
                            MobCategory.MISC)));

    public static final EnumMap<TFCWood, DeferredHolder<EntityType<?>, EntityType<FirmacivSloopEntity>>> TFC_SLOOPS = CommonHelper.mapOfKeys(
            TFCWood.class, tfcWood -> registerSloop(tfcWood,
                    EntityType.Builder.of((entityType, level) -> new FirmacivSloopEntity(entityType, level, tfcWood),
                            MobCategory.MISC)));

    public static final EnumMap<TFCWood, DeferredHolder<EntityType<?>, EntityType<FirmacivSloopUnderConstructionEntity>>> TFC_SLOOPS_UNDER_CONSTRUCTION = CommonHelper.mapOfKeys(
            TFCWood.class, tfcWood -> registerSloopConstruction(tfcWood,
                    EntityType.Builder.of(
                            (entityType, level) -> new FirmacivSloopUnderConstructionEntity(entityType, level, tfcWood),
                            MobCategory.MISC)));

    public static final EnumMap<TFCWood, DeferredHolder<EntityType<?>, EntityType<CanoeEntity>>> TFC_CANOES = CommonHelper.mapOfKeys(
            TFCWood.class, tfcWood -> registerCanoe(tfcWood,
                    EntityType.Builder.of(
                            (entityType, level) -> new CanoeEntity(entityType, level, tfcWood),
                            MobCategory.MISC)));

    public static final DeferredHolder<EntityType<?>, EntityType<KayakEntity>> KAYAK_ENTITY = register("kayak",
            EntityType.Builder.of(KayakEntity::new, MobCategory.MISC).sized(0.79F, 0.625F));

    public static final DeferredHolder<EntityType<?>, EntityType<FirmacivCannonEntity>> FIRMACIV_CANNON_ENTITY = register("cannon",
            EntityType.Builder.of(FirmacivCannonEntity::new, MobCategory.MISC).sized(0.8F, 0.8F).fireImmune());

    public static final DeferredHolder<EntityType<?>, EntityType<TFCChestCompartmentEntity>> TFC_CHEST_COMPARTMENT_ENTITY = register("compartment_tfc_chest", EntityType.Builder.<TFCChestCompartmentEntity>of(TFCChestCompartmentEntity::new, MobCategory.MISC).sized(0.6F, 0.7F).ridingOffset(0.125F).fireImmune());
    public static final DeferredHolder<EntityType<?>, EntityType<TFCBarrelCompartmentEntity>> TFC_BARREL_COMPARTMENT_ENTITY = register("compartment_tfc_barrel", EntityType.Builder.<TFCBarrelCompartmentEntity>of(TFCBarrelCompartmentEntity::new, MobCategory.MISC).sized(0.6F, 0.7F).ridingOffset(0.125F).fireImmune());
    public static final DeferredHolder<EntityType<?>, EntityType<LargeVesselCompartmentEntity>> LARGE_VESSEL_COMPARTMENT_ENTITY = register("compartment_large_vessel", EntityType.Builder.<LargeVesselCompartmentEntity>of(LargeVesselCompartmentEntity::new, MobCategory.MISC).sized(0.6F, 0.7F).ridingOffset(0.125F).fireImmune());

    private static <E extends RowboatEntity> DeferredHolder<EntityType<?>, EntityType<E>> registerRowboat(final TFCWood tfcWood,
                                                                                           final EntityType.Builder<E> builder) {
        return register("rowboat/" + tfcWood.getSerializedName(), builder.sized(1.875F, 0.625F));
    }

    private static <E extends CanoeEntity> DeferredHolder<EntityType<?>, EntityType<E>> registerCanoe(final TFCWood tfcWood,
                                                                                       final EntityType.Builder<E> builder) {
        return register("dugout_canoe/" + tfcWood.getSerializedName(), builder.sized(1.125F, 0.625F));
    }

    private static <E extends SloopEntity> DeferredHolder<EntityType<?>, EntityType<E>> registerSloop(final TFCWood tfcWood,
                                                                                       final EntityType.Builder<E> builder) {
        return register("sloop/" + tfcWood.getSerializedName(),
                builder.sized(3F, 0.75F).clientTrackingRange(LARGE_VEHICLE_TRACKING).fireImmune());
    }

    private static <E extends SloopUnderConstructionEntity> DeferredHolder<EntityType<?>, EntityType<E>> registerSloopConstruction(
            final TFCWood bopWood, final EntityType.Builder<E> builder) {
        return register("sloop_construction/" + bopWood.getSerializedName(),
                builder.sized(4F, 0.75F).clientTrackingRange(LARGE_VEHICLE_TRACKING).fireImmune().noSummon());
    }

    private static <E extends Entity> DeferredHolder<EntityType<?>, EntityType<E>> register(final String name,
                                                                             final EntityType.Builder<E> builder) {
        return register(name, builder, true);
    }

    @SuppressWarnings("SameParameterValue")
    private static <E extends Entity> DeferredHolder<EntityType<?>, EntityType<E>> register(final String name,
                                                                             final EntityType.Builder<E> builder, final boolean serialize) {
        final String id = name.toLowerCase(Locale.ROOT);
        return ENTITY_TYPES.register(id, () -> {
            if (!serialize) builder.noSave();
            return builder.build(MOD_ID + ":" + id);
        });
    }
}