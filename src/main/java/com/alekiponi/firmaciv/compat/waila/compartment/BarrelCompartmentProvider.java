package com.alekiponi.firmaciv.compat.waila.compartment;

import com.alekiponi.firmaciv.Firmaciv;
import com.alekiponi.firmaciv.common.entity.compartment.TFCBarrelCompartmentEntity;
import net.dries007.tfc.common.recipes.BarrelRecipe;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum BarrelCompartmentProvider implements IEntityComponentProvider {
    INSTANCE;

    public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(Firmaciv.MOD_ID, "barrel");

    @Override
    public void appendTooltip(final ITooltip tooltip, final EntityAccessor entityAccessor,
            final IPluginConfig iPluginConfig) {
        final TFCBarrelCompartmentEntity barrelCompartment = (TFCBarrelCompartmentEntity) entityAccessor.getEntity();

        if (!barrelCompartment.isSealed()) return;

        final long tickLeft = barrelCompartment.getRemainingTicks();

        if (tickLeft <= 0) return;

        final BarrelRecipe recipe = barrelCompartment.getRecipe();

        if (recipe == null) return;

        tooltip.add(barrelCompartment.getRecipeTooltip());
        // this is the translation key used in the barrel class, if that changes we should change it in barrel screen too.
        final ICalendar calendar = Calendars.get(barrelCompartment.level());
        tooltip.add(Component.translatable("tfc.jade.sealed_date",
                calendar.getExactTimeAndDate(barrelCompartment.getSealedTick())));
        tooltip.add(Component.translatable("tfc.jade.time_left", calendar.getTimeDelta(tickLeft)));
    }

    @Override
    public ResourceLocation getUid() {
        return NAME;
    }
}