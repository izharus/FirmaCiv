package com.alekiponi.firmaciv.compat.jei;

import com.alekiponi.firmaciv.Firmaciv;
import com.alekiponi.firmaciv.client.screen.BarrelCompartmentScreen;
import com.alekiponi.firmaciv.compat.jei.transfer.BarrelTransferInfo;
import com.alekiponi.firmaciv.compat.jei.transfer.FluidIgnoringRecipeTransferHandler;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.ResourceLocation;

import static net.dries007.tfc.compat.jei.JEIIntegration.*;

@JeiPlugin
public class JEIIntegration implements IModPlugin {

    @Override
    public void registerGuiHandlers(final IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(BarrelCompartmentScreen.class, 92, 21, 9, 14, SEALED_BARREL, INSTANT_BARREL,
                INSTANT_FLUID_BARREL);
    }

    @Override
    public void registerRecipeTransferHandlers(final IRecipeTransferRegistration registration) {
        final IRecipeTransferHandlerHelper transferHelper = registration.getTransferHelper();

        // Only sealed barrel recipes, instant barrel recipes are purposefully excluded
        registration.addRecipeTransferHandler(new FluidIgnoringRecipeTransferHandler<>(transferHelper,
                transferHelper.createUnregisteredRecipeTransferHandler(
                        new BarrelTransferInfo<>(transferHelper, SEALED_BARREL))), SEALED_BARREL);
    }

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Firmaciv.MOD_ID, "jei");
    }
}