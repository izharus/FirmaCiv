package com.alekiponi.firmaciv.client.screen;

import com.alekiponi.firmaciv.client.screen.button.BarrelCompartmentSealButton;
import com.alekiponi.firmaciv.common.entity.compartment.TFCBarrelCompartmentEntity;
import com.alekiponi.firmaciv.common.menu.BarrelCompartmentMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.BarrelScreen;
import net.dries007.tfc.common.blockentities.BarrelBlockEntity;

import net.dries007.tfc.common.recipes.BarrelRecipe;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.tooltip.Tooltips;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;

@OnlyIn(Dist.CLIENT)
public class BarrelCompartmentScreen extends AbstractContainerScreen<BarrelCompartmentMenu> {

    public static final int TEXT_COLOR = 0x404040;
    private static final Component SEAL = Component.translatable(TerraFirmaCraft.MOD_ID + ".tooltip.seal_barrel");
    private static final Component UNSEAL = Component.translatable(TerraFirmaCraft.MOD_ID + ".tooltip.unseal_barrel");
    private static final int MAX_RECIPE_NAME_LENGTH = 100;
    private final TFCBarrelCompartmentEntity barrelCompartment;

    public BarrelCompartmentScreen(final BarrelCompartmentMenu compartmentMenu, final Inventory inventory,
            final Component title) {
        super(compartmentMenu, inventory, title);
        this.barrelCompartment = compartmentMenu.getBarrelCompartment();
        this.inventoryLabelY += 12;
        this.imageHeight += 12;
    }

    @Override
    public void init() {
        super.init();
        this.addRenderableWidget(new BarrelCompartmentSealButton(this.barrelCompartment, this.leftPos, this.topPos,
                this.barrelCompartment.isSealed() ? UNSEAL : SEAL));
    }

    @Override
    public void render(final GuiGraphics poseStack, final int mouseX, final int mouseY, final float partialTicks) {
        this.renderBackground(poseStack, mouseX, mouseY, partialTicks);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        final int relX = mouseX - this.leftPos;
        final int relY = mouseY - this.topPos;

        if (relX >= 7 && relY >= 19 && relX < 25 && relY < 71) {
            java.util.Optional.ofNullable(this.barrelCompartment.getFluidHandler(null)).ifPresent(fluidHandler -> {
                final FluidStack fluid = fluidHandler.getFluidInTank(0);
                if (!fluid.isEmpty()) {
                    graphics.renderTooltip(font, Tooltips.fluidUnitsOf(fluid), mouseX, mouseY);
                }
            });
        }
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        if (this.barrelCompartment.isSealed()) {
            drawDisabled(graphics, BarrelBlockEntity.SLOT_FLUID_CONTAINER_IN, BarrelBlockEntity.SLOT_ITEM);

            // Draw the text displaying both the seal date, and the recipe name
            final BarrelRecipe recipe = this.barrelCompartment.getRecipe();
            if (recipe != null) {
                final FormattedText resultText = barrelCompartment.getRecipeTooltip();
                if (this.font.width(resultText) > MAX_RECIPE_NAME_LENGTH) {
                    int line = 0;
                    for (final FormattedCharSequence text : this.font.split(resultText, MAX_RECIPE_NAME_LENGTH)) {
                        graphics.drawString(this.font, text,
                                70 + Math.floorDiv(MAX_RECIPE_NAME_LENGTH - this.font.width(text), 2),
                                this.titleLabelY + (line * this.font.lineHeight), TEXT_COLOR, false);
                        line++;
                    }
                } else {
                    graphics.drawString(font, resultText.getString(),
                            70 + Math.floorDiv(MAX_RECIPE_NAME_LENGTH - this.font.width(resultText), 2), 61, TEXT_COLOR,
                            false);
                }
            }
            final String date = Calendars.CLIENT.getExactTimeAndDate(this.barrelCompartment.getSealedTick()).getString();
            graphics.drawString(this.font, date, this.imageWidth / 2 - this.font.width(date) / 2, 74, TEXT_COLOR,
                    false);
        }
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float partialTicks, final int mouseX, final int mouseY) {
        this.drawDefaultBackground(graphics);

        if (net.neoforged.fml.ModList.get().isLoaded("jei")) {
            graphics.blit(BarrelScreen.BACKGROUND, getGuiLeft() + 92, getGuiTop() + 21, 227, 0, 9, 14);
        }

        java.util.Optional.ofNullable(this.barrelCompartment.getFluidHandler(null)).ifPresent(fluidHandler -> {
            final FluidStack fluidStack = fluidHandler.getFluidInTank(0);
            if (!fluidStack.isEmpty()) {
                final TextureAtlasSprite sprite = RenderHelpers.getAndBindFluidSprite(fluidStack);
                final int fillHeight = (int) Math.ceil(
                        (float) 50 * fluidStack.getAmount() / (float) TFCConfig.SERVER.barrelCapacity.get());

                RenderHelpers.fillAreaWithSprite(graphics, sprite, leftPos + 8, topPos + 70 - fillHeight, 16,
                        fillHeight, 16, 16);

                RenderSystem.setShaderColor(1, 1, 1, 1);
                RenderSystem.setShaderTexture(0, BarrelScreen.BACKGROUND);
            }
        });

        graphics.blit(BarrelScreen.BACKGROUND, this.leftPos + 7, this.topPos + 19, 176, 0, 18, 52);
    }

    protected void drawDefaultBackground(final GuiGraphics graphics) {
//        graphics.blit(BarrelScreen.BACKGROUND, this.leftPos, this.topPos, 0, 0, 0, this.imageWidth, this.imageHeight,
//                256, 256);
        graphics.blit(BarrelScreen.BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256,
                256);
    }

    public void drawDisabled(final GuiGraphics graphics, final int start, final int end) {
        java.util.Optional.ofNullable(this.barrelCompartment.getItemHandler(null)).ifPresent(inventory -> {
            // draw disabled texture over the slots
            this.menu.slots.stream().filter(slot -> slot.index <= end && slot.index >= start)
                    .forEach(slot -> renderSlotHighlight(graphics, slot.x, slot.y, 1));
        });
    }
}
