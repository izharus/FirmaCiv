package com.alekiponi.firmaciv;



import com.alekiponi.firmaciv.client.FirmacivClientEvents;
import com.alekiponi.firmaciv.common.block.FirmacivBlocks;
import com.alekiponi.firmaciv.common.blockentity.FirmacivBlockEntities;
import com.alekiponi.firmaciv.common.entity.FirmacivEntities;
import com.alekiponi.firmaciv.common.item.FirmacivItems;
import com.alekiponi.firmaciv.common.item.FirmacivTabs;
import com.alekiponi.firmaciv.common.menu.FirmacivMenus;
import com.alekiponi.firmaciv.events.config.FirmacivConfig;
import com.alekiponi.firmaciv.network.PacketHandler;
import com.alekiponi.firmaciv.util.FirmacivInteractionManager;
import com.alekiponi.firmaciv.util.TFCWood;
import com.mojang.logging.LogUtils;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(Firmaciv.MOD_ID)
public final class Firmaciv {
    public static final String MOD_ID = "firmaciv";

    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public Firmaciv(IEventBus eventBus, ModContainer container) {

        FirmacivTabs.CREATIVE_MODE_TABS.register((eventBus));
        com.alekiponi.firmaciv.util.TFCWindModel.SERIALIZERS.register(eventBus);
        FirmacivItems.ITEMS.register(eventBus);
        FirmacivBlocks.BLOCKS.register(eventBus);
        FirmacivBlockEntities.BLOCK_ENTITIES.register(eventBus);
        FirmacivEntities.ENTITY_TYPES.register(eventBus);
        com.alekiponi.firmaciv.common.entity.FirmacivCompartments.SERIALIZERS.register(eventBus);
        eventBus.addListener(com.alekiponi.firmaciv.common.entity.FirmacivCompartments::registerCapabilities);
        FirmacivMenus.MENU_TYPES.register(eventBus);

        eventBus.addListener(this::setup);
        FirmacivConfig.init(container);
        eventBus.addListener(PacketHandler::init);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            FirmacivClientEvents.init(eventBus);
        }
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            FirmacivInteractionManager.init();
            TFCWood.registerFrames();


        });
    }
}