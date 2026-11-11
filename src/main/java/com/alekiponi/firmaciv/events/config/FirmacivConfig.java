package com.alekiponi.firmaciv.events.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public final class FirmacivConfig {
    private static final Pair<ServerConfig, ModConfigSpec> CONFIG = new ModConfigSpec.Builder().configure(ServerConfig::new);
    public static final ServerConfig SERVER = CONFIG.getLeft();

    public static void init(ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, CONFIG.getRight());
    }
}
