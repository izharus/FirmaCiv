package com.alekiponi.firmaciv.events.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.function.Function;

public class ServerConfig {
    public final ForgeConfigSpec.BooleanValue canoeWoodRestriction;

    public final ForgeConfigSpec.BooleanValue shipWoodRestriction;
    public final ForgeConfigSpec.BooleanValue forceReducedDebugInfo;
    public final ForgeConfigSpec.BooleanValue disableVanillaBoatFunctionality;

    public final ForgeConfigSpec.BooleanValue changeLatitudeLongitudeBasedOnClimateConfig;
    public final ForgeConfigSpec.BooleanValue enableParallelSourceCreationAtSeaLevel;

    ServerConfig(ForgeConfigSpec.Builder innerBuilder) {
        Function<String, ForgeConfigSpec.Builder> builder = (name) -> {
            return innerBuilder.translation("firmaciv.config.server." + name);
        };
        innerBuilder.push("general");
        this.canoeWoodRestriction = builder.apply("canoeWoodRestriction")
                .comment(
                        "Force creation of canoes from specified wood types. Turning this to false will make canoes craftable from any wood type.")
                .define("canoeWoodRestriction", true);

        this.shipWoodRestriction = builder.apply("shipWoodRestriction")
                .comment(
                        "Force creation of ships and rowboats from specified wood types. Turning this to false will make ships and rowboats craftable from any wood type.")
                .define("shipWoodRestriction", true);

        this.forceReducedDebugInfo = builder.apply("forceReducedDebugInfo").comment("Force reduceDebugInfo gamerule. Turn this to FALSE if you want coordinates back.")
                .define("forceReducedDebugInfo", true);

        this.disableVanillaBoatFunctionality = builder.apply("disableVanillaBoatFunctionality")
                .comment("Disables vanilla boats and base TFC boats from working.")
                .define("disableVanillaBoatFunctionality", true);

        this.changeLatitudeLongitudeBasedOnClimateConfig = builder.apply("changeLatitudeLongitudeBasedOnClimateConfig")
            .comment("Automatically adjusts latitude and longitude to match the climate scale of the TFC world.")
            .define("changeLatitudeLongitudeBasedOnClimateConfig", true);

        this.enableParallelSourceCreationAtSeaLevel = builder.apply("enableParallelSourceCreationAtSeaLevel")
                .comment("Expands water source creation to make canal building possible at sea level.")
                .define("enableParallelSourceCreationAtSeaLevel", true);

    }
}
