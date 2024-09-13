package com.alekiponi.firmaciv.common.item;

import com.alekiponi.firmaciv.events.config.FirmacivConfig;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.text.DecimalFormat;

public abstract class AbstractNavItem extends Item {

    private static final float RANDOM_LAT_LON_SCALE = 0.1f;
    private static final int RANDOM_ALT_SCALE = 4;

    private static double temperatureScale = 20000;
    private static double rainfallScale = 20000;

    public AbstractNavItem(Properties pProperties) {
        super(pProperties);
    }

    public static void updateScales(ServerLevel level){
        if (!FirmacivConfig.SERVER.changeLatitudeLongitudeBasedOnClimateConfig.get()) {
            temperatureScale = 20000;
            rainfallScale = 20000;
            return;
        }
        final ChunkGeneratorExtension extension = (ChunkGeneratorExtension) level.getChunkSource().getGenerator();
        temperatureScale = extension.settings().temperatureScale();
        rainfallScale = extension.settings().rainfallScale();
    }

    public static double[] getNavLocation(Vec3 position) {
        double latitude = (Math.floor(position.get(Direction.Axis.Z) - (double) temperatureScale / 2) / temperatureScale) * 90;
        double longitude = (Math.floor(position.get(Direction.Axis.X)) / ((double) rainfallScale / 2)) * 90;
        double altitude = (position.get(Direction.Axis.Y)) - 64;

        return new double[]{latitude, longitude, altitude};
    }

    public static String[] createNavStrings(Vec3 position) {
        double[] navLocation = getNavLocation(position);

        double latitude = navLocation[NavSelection.LATITUDE.ordinal()] + Math.random() * RANDOM_LAT_LON_SCALE;
        double longitude = navLocation[NavSelection.LONGITUDE.ordinal()] + Math.random() * RANDOM_LAT_LON_SCALE;
        double altitude = navLocation[NavSelection.ALTITUDE.ordinal()] + Math.random() * RANDOM_ALT_SCALE;

        Component latitudeComponent = Component.translatable("latitude");

        Component longitudeComponent = Component.translatable("longitude");

        Component altitudeComponent = Component.translatable("altitude");

        Component degreesComponent = Component.translatable("degrees");

        Component seaLevelComponent = Component.translatable("sea_level");

        Component northComponent = Component.translatable("north");

        Component southComponent = Component.translatable("south");

        Component eastComponent = Component.translatable("east");

        Component westComponent = Component.translatable("west");

        Component above = Component.translatable("above");

        Component below = Component.translatable("below");

        Component meters = Component.translatable("meters");

        DecimalFormat dfAlt = new DecimalFormat("###");
        DecimalFormat df = new DecimalFormat("###.##");

        String latStr = latitudeComponent.getString() + ": " + df.format(Math.abs(latitude))
                + " " + degreesComponent.getString() + " " + (latitude > 0 ? southComponent.getString() : northComponent.getString());

        String lonStr = longitudeComponent.getString() + ": " + df.format(
                Math.abs(longitude)) + " " + degreesComponent.getString() + " " + (longitude > 0 ? eastComponent.getString() : westComponent.getString());

        String altStr = altitudeComponent.getString() + ": " + dfAlt.format(
                Math.abs(altitude)) + " " + meters.getString() + " " + (altitude > 0 ? above.getString() : below.getString()) + " " + seaLevelComponent.getString();

        String latSim = latitudeComponent.getString(3) + ": " + df.format(Math.abs(latitude)) + " " + (latitude > 0 ? southComponent.getString(1) : northComponent.getString(1));
        String lonSim = longitudeComponent.getString(3) + ": " + df.format(Math.abs(longitude)) + " " + (longitude > 0 ? eastComponent.getString(1) : westComponent.getString(1));
        String altSim = altitudeComponent.getString(3) + ": " + dfAlt.format(altitude);

        return new String[]{latStr, lonStr, altStr, latSim, lonSim, altSim};

    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand,
                                                  NavType navType) {

        ItemStack itemstack = pPlayer.getItemInHand(pHand);

        pPlayer.getCooldowns().addCooldown(this, 200);

        if (!pLevel.isClientSide) {
            Vec3 positionClicked = pPlayer.getEyePosition();

            outputCoordinate(positionClicked, pPlayer, navType);
        }

        return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
    }

    public static int outputCoordinate(Vec3 position, Player player, NavType navType) {
        String[] navStrings = createNavStrings(position);

        String locationText = navStrings[NavSelection.LATITUDE.ordinal()];
        String simpleLocationText = navStrings[NavSelection.LAT_SIMPLE.ordinal()];
        Component copyMessage = Component.translatable("copy_latitude");

        switch (navType) {
            case LAT -> {
                locationText = navStrings[NavSelection.LATITUDE.ordinal()];
                simpleLocationText = navStrings[NavSelection.LAT_SIMPLE.ordinal()];
                copyMessage = Component.translatable("copy_latitude");
            }
            case LON -> {
                locationText = navStrings[NavSelection.LONGITUDE.ordinal()];
                simpleLocationText = navStrings[NavSelection.LON_SIMPLE.ordinal()];
                copyMessage = Component.translatable("copy_longitude");
            }
            case ALT -> {
                locationText = navStrings[NavSelection.ALTITUDE.ordinal()];
                simpleLocationText = navStrings[NavSelection.ALT_SIMPLE.ordinal()];
                copyMessage = Component.translatable("copy_altitude");
            }
            case LAT_LON -> {
                locationText =
                        navStrings[AbstractNavItem.NavSelection.LATITUDE.ordinal()] + " | " +
                                navStrings[AbstractNavItem.NavSelection.LONGITUDE.ordinal()];

                simpleLocationText =
                        navStrings[AbstractNavItem.NavSelection.LAT_SIMPLE.ordinal()] + ", " +
                                navStrings[AbstractNavItem.NavSelection.LON_SIMPLE.ordinal()];

                copyMessage = Component.translatable("copy_latlon");
            }
            case LAT_LON_ALT -> {
                locationText =
                        navStrings[AbstractNavItem.NavSelection.LATITUDE.ordinal()] + " | " +
                                navStrings[AbstractNavItem.NavSelection.LONGITUDE.ordinal()] + " | " +
                                navStrings[NavSelection.ALTITUDE.ordinal()];

                simpleLocationText =
                        navStrings[AbstractNavItem.NavSelection.LAT_SIMPLE.ordinal()] + ", " +
                                navStrings[AbstractNavItem.NavSelection.LON_SIMPLE.ordinal()] + ", " +
                                navStrings[AbstractNavItem.NavSelection.ALT_SIMPLE.ordinal()];

                copyMessage = Component.translatable("copy_latlonalt");
            }
        }

        String finalSimpleLocationText = simpleLocationText;
        Component finalCopyMessage = copyMessage;


        Component locationMessage = Component.literal(
                locationText).withStyle(style -> style.withClickEvent(
                        new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, finalSimpleLocationText))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, finalCopyMessage)));


        player.sendSystemMessage(locationMessage);

        return 0;
    }

    public enum NavSelection {
        LATITUDE,
        LONGITUDE,
        ALTITUDE,
        LAT_SIMPLE,
        LON_SIMPLE,
        ALT_SIMPLE
    }

    public enum NavType {
        LAT,
        LON,
        ALT,
        LAT_LON,
        LAT_LON_ALT
    }

}
