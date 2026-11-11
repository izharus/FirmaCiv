package com.alekiponi.firmaciv.client.screen;

import com.alekiponi.firmaciv.client.screen.button.VesselCompartmentSealButton;
import com.alekiponi.firmaciv.common.entity.compartment.LargeVesselCompartmentEntity;
import com.alekiponi.firmaciv.common.menu.LargeVesselCompartmentMenu;
import net.dries007.tfc.client.screen.LargeVesselScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;


@OnlyIn(Dist.CLIENT)
public class LargeVesselCompartmentScreen extends AbstractContainerScreen<LargeVesselCompartmentMenu> {

    private static final Component SEAL = Component.translatable("tfc.tooltip.seal_barrel");
    private static final Component UNSEAL = Component.translatable("tfc.tooltip.unseal_barrel");
    private final LargeVesselCompartmentEntity vesselCompartment;

    public LargeVesselCompartmentScreen(final LargeVesselCompartmentMenu compartmentMenu, final Inventory inventory,
            final Component title) {
        super(compartmentMenu, inventory, title);
        this.vesselCompartment = compartmentMenu.getVesselCompartment();
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(
                new VesselCompartmentSealButton(this.vesselCompartment, this.getGuiLeft() + 9, this.getGuiTop(),
                        this.vesselCompartment.isSealed() ? UNSEAL : SEAL));
    }

    @Override
    public void render(final GuiGraphics poseStack, final int mouseX, final int mouseY, final float partialTicks) {
        this.renderBackground(poseStack, mouseX, mouseY, partialTicks);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        if (this.vesselCompartment.isSealed()) {
            this.menu.slots.stream().filter(slot -> slot.index <= 8 && slot.index >= 0)
                    .forEach(slot -> renderSlotHighlight(graphics, slot.x, slot.y, 1));
        }
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float partialTicks, final int mouseX, final int mouseY) {
        graphics.blit(LargeVesselScreen.BACKGROUND, this.leftPos, this.topPos, 0, 0.0F, 0.0F, this.imageWidth,
                this.imageHeight, 256, 256);
    }
}