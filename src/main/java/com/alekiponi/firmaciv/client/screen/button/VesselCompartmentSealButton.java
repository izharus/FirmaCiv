package com.alekiponi.firmaciv.client.screen.button;

import com.alekiponi.firmaciv.common.entity.compartment.LargeVesselCompartmentEntity;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.LargeVesselScreen;
import net.dries007.tfc.network.PacketHandler;
import net.dries007.tfc.network.ScreenButtonPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class VesselCompartmentSealButton extends Button {

    private final LargeVesselCompartmentEntity vesselCompartment;

    public VesselCompartmentSealButton(LargeVesselCompartmentEntity vesselCompartment, int guiLeft, int guiTop,
            Component tooltip) {
        super(guiLeft + 123, guiTop + 35, 20, 20, tooltip, (b) -> {
        }, RenderHelpers.NARRATION);
        this.setTooltip(Tooltip.create(tooltip));
        this.vesselCompartment = vesselCompartment;
    }

    public void onPress() {
        PacketDistributor.sendToServer(new ScreenButtonPacket(0, java.util.Optional.empty()));
        this.playDownSound(Minecraft.getInstance().getSoundManager());
    }

    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.blit(LargeVesselScreen.BACKGROUND, this.getX(), this.getY(), 236.0F,
                (float) (this.vesselCompartment.isSealed() ? 0 : 20), 20, 20, 256, 256);
    }
}