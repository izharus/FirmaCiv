package com.alekiponi.firmaciv.client.screen.button;

import com.alekiponi.firmaciv.common.entity.compartment.TFCBarrelCompartmentEntity;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.BarrelScreen;
import net.dries007.tfc.network.PacketHandler;
import net.dries007.tfc.network.ScreenButtonPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class BarrelCompartmentSealButton extends Button {

    private final TFCBarrelCompartmentEntity barrelCompartment;

    public BarrelCompartmentSealButton(final TFCBarrelCompartmentEntity barrelCompartment, final int guiLeft,
            final int guiTop, final Component tooltip) {
        super(guiLeft + 123, guiTop + 35, 20, 20, tooltip, (b) -> {
        }, RenderHelpers.NARRATION);
        this.setTooltip(Tooltip.create(tooltip));
        this.barrelCompartment = barrelCompartment;
    }

    @Override
    public void onPress() {
        PacketDistributor.sendToServer(new ScreenButtonPacket(0, java.util.Optional.empty()));
        this.playDownSound(Minecraft.getInstance().getSoundManager());
    }

    @Override
    public void renderWidget(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
        final int v = this.barrelCompartment.isSealed() ? 0 : 20;
        graphics.blit(BarrelScreen.BACKGROUND, this.getX(), this.getY(), 236, (float) v, 20, 20, 256, 256);
    }
}