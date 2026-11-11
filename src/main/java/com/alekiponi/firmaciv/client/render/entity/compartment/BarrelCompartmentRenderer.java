package com.alekiponi.firmaciv.client.render.entity.compartment;

import com.alekiponi.alekiships.client.render.entity.vehicle.vehiclehelper.BlockCompartmentRenderer;
import com.alekiponi.firmaciv.common.entity.compartment.TFCBarrelCompartmentEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.common.blockentities.BarrelBlockEntity;
import net.dries007.tfc.config.TFCConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;


public class BarrelCompartmentRenderer extends BlockCompartmentRenderer<TFCBarrelCompartmentEntity> {

    public BarrelCompartmentRenderer(final EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderCompartmentContents(final TFCBarrelCompartmentEntity compartmentEntity, final float partialTicks,
            final PoseStack poseStack, final MultiBufferSource bufferSource, final int packedLight) {
        super.renderCompartmentContents(compartmentEntity, partialTicks, poseStack, bufferSource, packedLight);

        if (compartmentEntity.isSealed()) return;

        java.util.Optional.ofNullable(compartmentEntity.getFluidHandler(null))
                .map(fluidHandler -> fluidHandler.getFluidInTank(0)).filter(fluidStack -> !fluidStack.isEmpty())
                .ifPresent(fluidStack -> {
                    final float fillPercent = (float) fluidStack.getAmount() / (float) TFCConfig.SERVER.barrelCapacity.get();
                    final float subtract = fillPercent > 0.03 ? 0 : (0.03F - fillPercent) * 7;
                    RenderHelpers.renderFluidFace(poseStack, fluidStack, bufferSource, 0.1875F + subtract,
                            0.1875F + subtract, 0.8125F - subtract, 0.8125F - subtract,
                            0.140625F + 0.734375F * fillPercent, OverlayTexture.NO_OVERLAY, packedLight);
                });
        java.util.Optional.ofNullable(compartmentEntity.getItemHandler(null))
                .map(itemHandler -> itemHandler.getStackInSlot(BarrelBlockEntity.SLOT_ITEM))
                .filter(itemStack -> !itemStack.isEmpty()).ifPresent(itemStack -> {
                    poseStack.pushPose();
                    poseStack.translate(0.5, 0.15625, 0.5);
                    poseStack.scale(0.5F, 0.5F, 0.5F);
                    poseStack.mulPose(Axis.XP.rotationDegrees(90));
                    Minecraft.getInstance().getItemRenderer()
                            .renderStatic(itemStack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY,
                                    poseStack, bufferSource, compartmentEntity.level(), 0);
                    poseStack.popPose();

                });
    }
}