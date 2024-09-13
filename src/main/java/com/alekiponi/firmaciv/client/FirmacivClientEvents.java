package com.alekiponi.firmaciv.client;

import com.alekiponi.alekiships.AlekiShips;
import com.alekiponi.alekiships.client.resources.PaintedTextureGenerator;
import com.alekiponi.alekiships.common.entity.vehiclehelper.compartment.EmptyCompartmentEntity;
import com.alekiponi.firmaciv.Firmaciv;
import com.alekiponi.firmaciv.client.screen.BarrelCompartmentScreen;
import com.alekiponi.firmaciv.client.screen.LargeVesselCompartmentScreen;
import com.alekiponi.firmaciv.common.item.AbstractNavItem;
import com.alekiponi.firmaciv.common.item.FirmacivItems;
import com.alekiponi.firmaciv.common.menu.FirmacivMenus;
import com.alekiponi.firmaciv.util.TFCWood;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.annotation.Nullable;

public final class FirmacivClientEvents {

    public static void init() {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(FirmacivClientEvents::clientSetup);
        bus.addListener(FirmacivClientEvents::onRegisterReloadListeners);
    }

    private static void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(FirmacivMenus.BARREL_COMPARTMENT_MENU.get(), BarrelCompartmentScreen::new);
            MenuScreens.register(FirmacivMenus.LARGE_VESSEL_COMPARTMENT_MENU.get(), LargeVesselCompartmentScreen::new);

            ItemProperties.register(FirmacivItems.BAROMETER.get(), new ResourceLocation(Firmaciv.MOD_ID, "altitude"),
                    new ClampedItemPropertyFunction() {

                        public float unclampedCall(ItemStack pStack, @Nullable ClientLevel pLevel,
                                @Nullable LivingEntity livingEntity, int p_174668_) {

                            Entity entity = livingEntity != null ? livingEntity : pStack.getEntityRepresentation();
                            if (entity == null) {
                                return 0.0F;
                            } else {
                                if (pLevel == null && entity.level() instanceof ClientLevel) {
                                    pLevel = (ClientLevel) entity.level();
                                }

                                if (pLevel == null) {
                                    return 0.0F;
                                } else {
                                    double height;
                                    if (pLevel.dimensionType().natural()) {
                                        assert livingEntity != null;
                                        height = (entity.getY() + 64) / (pLevel.getHeight());
                                    } else {
                                        height = Math.random();
                                    }

                                    return (float) height;
                                }
                            }
                        }
                    });


            ItemProperties.register(FirmacivItems.FIRMACIV_COMPASS.get(),
                    new ResourceLocation(Firmaciv.MOD_ID, "firmaciv_compass_direction"),
                    new ClampedItemPropertyFunction() {

                        public float unclampedCall(ItemStack pStack, @Nullable ClientLevel pLevel,
                                @Nullable LivingEntity livingEntity, int p_174668_) {


                            float rotation = 0;

                            Entity entity = livingEntity != null ? livingEntity : pStack.getEntityRepresentation();
                            if (entity == null) {
                                return 0.0F;
                            } else {
                                if (pLevel == null && entity.level() instanceof ClientLevel) {
                                    pLevel = (ClientLevel) entity.level();
                                }

                                if (pLevel == null) {
                                    return 0.0F;
                                } else {
                                    double direction;
                                    if (pLevel.dimensionType().natural()) {
                                        assert livingEntity != null;
                                        if (livingEntity instanceof Player && pLevel.isClientSide() && livingEntity.getVehicle() instanceof EmptyCompartmentEntity) {
                                            rotation = Minecraft.getInstance().getCameraEntity().getYRot();
                                        } else {
                                            rotation = entity.getYRot();
                                        }
                                        direction = ((Mth.wrapDegrees(-rotation) + 180) % 360) / 360;
                                    } else {
                                        direction = Math.random();
                                    }

                                    return (float) direction; //(2*3.1415926535f)
                                }
                            }
                        }
                    });


            ItemProperties.register(FirmacivItems.NAV_CLOCK.get(), new ResourceLocation(Firmaciv.MOD_ID, "pm_time"),
                    new ClampedItemPropertyFunction() {
                        private double rotation;
                        private double rota;
                        private long lastUpdateTick;

                        public float unclampedCall(ItemStack stack, @Nullable ClientLevel clientLevel,
                                @Nullable LivingEntity livingEntity, int p_174668_) {
                            net.minecraft.world.entity.Entity entity = livingEntity != null ? livingEntity : stack.getEntityRepresentation();
                            if (entity == null) {
                                return 0.0F;
                            } else {
                                if (clientLevel == null && entity.level() instanceof ClientLevel) {
                                    clientLevel = (ClientLevel) entity.level();
                                }

                                double longitude = Math.abs((AbstractNavItem.getNavLocation(
                                        entity.getEyePosition())[AbstractNavItem.NavSelection.LONGITUDE.ordinal()] % 180) / 180);

                                if (clientLevel == null) {
                                    return 0.0F;
                                } else {
                                    double time;
                                    if (clientLevel.dimensionType().natural()) {
                                        time = clientLevel.getTimeOfDay(1.0F);
                                        time += longitude;
                                        time %= 1.0F;
                                    } else {
                                        time = Math.random();
                                    }

                                    time = this.wobble(clientLevel, time);
                                    return (float) time;
                                }
                            }
                        }


                        private double wobble(Level level, double p_117905_) {
                            if (level.getGameTime() != this.lastUpdateTick) {
                                this.lastUpdateTick = level.getGameTime();
                                double d0 = p_117905_ - this.rotation;
                                d0 = Mth.positiveModulo(d0 + 0.5D, 1.0D) - 0.5D;
                                this.rota += d0 * 0.1D;
                                this.rota *= 0.9D;
                                this.rotation = Mth.positiveModulo(this.rotation + this.rota, 1.0D);
                            }

                            return this.rotation;
                        }
                    });
        });
    }

    private static void onRegisterReloadListeners(final RegisterClientReloadListenersEvent event) {
        {
            final ResourceLocation rowboatPaint = new ResourceLocation(AlekiShips.MOD_ID,
                    "entity/watercraft/rowboat/paint");
            final ResourceLocation sloopPaint = new ResourceLocation(AlekiShips.MOD_ID,
                    "entity/watercraft/sloop/paint");

            for (final TFCWood wood : TFCWood.values()) {
                event.registerReloadListener(new PaintedTextureGenerator(
                        new ResourceLocation(Firmaciv.MOD_ID, "entity/watercraft/rowboat/" + wood.getSerializedName()),
                        rowboatPaint));
                event.registerReloadListener(new PaintedTextureGenerator(
                        new ResourceLocation(Firmaciv.MOD_ID, "entity/watercraft/sloop/" + wood.getSerializedName()),
                        sloopPaint));
            }
        }
    }
}
