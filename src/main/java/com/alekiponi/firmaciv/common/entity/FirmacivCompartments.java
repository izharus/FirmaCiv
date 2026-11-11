package com.alekiponi.firmaciv.common.entity;

import com.alekiponi.alekiships.common.AlekiShipsRegistries;
import com.alekiponi.alekiships.common.compartment.*;
import com.alekiponi.firmaciv.common.entity.compartment.*;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.capabilities.Capabilities;

public final class FirmacivCompartments {
    public static final DeferredRegister<CompartmentPlaceableSerializer<?>> SERIALIZERS = DeferredRegister.create(AlekiShipsRegistries.COMPARTMENT_PLACEABLE_SERIALIZER, "firmaciv");
    static {
        var chest = new DirectCompartmentPlaceable<>(DirectCompartmentType.of(FirmacivEntities.TFC_CHEST_COMPARTMENT_ENTITY, TFCChestCompartmentEntity::new));
        var barrel = new DirectCompartmentPlaceable<>(DirectCompartmentType.of(FirmacivEntities.TFC_BARREL_COMPARTMENT_ENTITY, TFCBarrelCompartmentEntity::new));
        var vessel = new DirectCompartmentPlaceable<>(DirectCompartmentType.of(FirmacivEntities.LARGE_VESSEL_COMPARTMENT_ENTITY, LargeVesselCompartmentEntity::new));
        SERIALIZERS.register("chest", chest::getSerializer);
        SERIALIZERS.register("barrel", barrel::getSerializer);
        SERIALIZERS.register("large_vessel", vessel::getSerializer);
    }
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerEntity(Capabilities.ItemHandler.ENTITY_AUTOMATION, FirmacivEntities.TFC_BARREL_COMPARTMENT_ENTITY.get(), TFCBarrelCompartmentEntity::getItemHandler);
        event.registerEntity(Capabilities.FluidHandler.ENTITY, FirmacivEntities.TFC_BARREL_COMPARTMENT_ENTITY.get(), TFCBarrelCompartmentEntity::getFluidHandler);
        event.registerEntity(Capabilities.ItemHandler.ENTITY_AUTOMATION, FirmacivEntities.LARGE_VESSEL_COMPARTMENT_ENTITY.get(), LargeVesselCompartmentEntity::getItemHandler);
        event.registerEntity(Capabilities.ItemHandler.ENTITY_AUTOMATION, FirmacivEntities.TFC_CHEST_COMPARTMENT_ENTITY.get(), (entity, side) -> new net.neoforged.neoforge.items.wrapper.InvWrapper(entity));
    }
}
