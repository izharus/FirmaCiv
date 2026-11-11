package com.alekiponi.firmaciv.compat.waila.compartment;

import com.alekiponi.alekiships.common.entity.compartment.BlockCompartment;
import com.alekiponi.alekiships.compat.waila.compartment.BlockCompartmentProvider;
import com.alekiponi.firmaciv.Firmaciv;
import com.alekiponi.firmaciv.common.entity.compartment.TFCChestCompartmentEntity;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;

/**
 * Like {@link BlockCompartmentProvider} but for our TFC chests as we don't implement {@link BlockCompartment}
 */
public enum TFCChestCompartmentProvider implements IEntityComponentProvider {
    INSTANCE;

    private static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(Firmaciv.MOD_ID, "tfc_chest");

    @Override
    public void appendTooltip(final ITooltip tooltip, final EntityAccessor entityAccessor,
            final IPluginConfig iPluginConfig) {
        tooltip.remove(JadeIds.CORE_OBJECT_NAME);

        final String blockName = I18n.get(((TFCChestCompartmentEntity) entityAccessor.getEntity()).descriptionId());

        // "<BlockName> Compartment"
        final MutableComponent name = Component.translatable("alekiships.jade.compartment_block", blockName);
        final IWailaConfig.IConfigFormatting formatting = IWailaConfig.get().getFormatting();
        tooltip.add(0, snownee.jade.api.theme.IThemeHelper.get().title(name), JadeIds.CORE_OBJECT_NAME);
    }

    @Override
    public ResourceLocation getUid() {
        return NAME;
    }

    @Override
    public int getDefaultPriority() {
        // We want to replace the name only
        return TooltipPosition.HEAD;
    }
}