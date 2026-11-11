package com.alekiponi.firmaciv.common.menu;

import com.alekiponi.firmaciv.Firmaciv;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class FirmacivMenus {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU,
            Firmaciv.MOD_ID);


    public static final DeferredHolder<MenuType<?>, MenuType<BarrelCompartmentMenu>> BARREL_COMPARTMENT_MENU = MENU_TYPES.register(
            "barrel_compartment_menu", () -> IMenuTypeExtension.create(BarrelCompartmentMenu::fromNetwork));

    public static final DeferredHolder<MenuType<?>, MenuType<LargeVesselCompartmentMenu>> LARGE_VESSEL_COMPARTMENT_MENU = MENU_TYPES.register(
            "vessel_compartment_menu", () -> IMenuTypeExtension.create(LargeVesselCompartmentMenu::fromNetwork));
}