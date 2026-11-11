package com.alekiponi.firmaciv.events;

import com.alekiponi.alekiships.common.block.AlekiShipsBlocks;
import com.alekiponi.alekiships.common.entity.AlekiShipsEntities;
import com.alekiponi.alekiships.common.item.AlekiShipsItems;
import com.alekiponi.firmaciv.Firmaciv;
import com.alekiponi.firmaciv.common.block.FirmacivBlocks;
import com.alekiponi.firmaciv.common.entity.FirmacivEntities;
import com.alekiponi.firmaciv.common.item.AbstractNavItem;
import com.alekiponi.firmaciv.events.config.FirmacivConfig;
import com.alekiponi.firmaciv.util.FirmacivCommands;
import com.alekiponi.firmaciv.util.TFCWood;
import net.dries007.tfc.common.commands.TFCCommands;
import net.minecraft.client.gui.font.providers.UnihexProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.DimensionTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;


@EventBusSubscriber(modid = Firmaciv.MOD_ID)
public final class ForgeEventHandler {

    /**
     * Set {@link GameRules#RULE_REDUCEDDEBUGINFO} to true
     */
    @SubscribeEvent
    public static void onWorldLoad(final LevelEvent.Load event) {
        if (!(event.getLevel() instanceof final ServerLevel level)) return;

        level.getGameRules().getRule(GameRules.RULE_REDUCEDDEBUGINFO).set(FirmacivConfig.SERVER.forceReducedDebugInfo.get(), level.getServer());
        if(level.dimensionType().natural()){
            // will probably break with unexpected dimension things going on, like multiple natural dimensions, since it sets a static constant
            AbstractNavItem.updateScales(level);
        }
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event)
    {
        FirmacivCommands.registerCommands(event.getDispatcher(), event.getBuildContext());
    }

    /**
     * Disable vanilla boats
     */
    @SubscribeEvent
    public static void onEntityInteract(final PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Boat)) return;
        if (!FirmacivConfig.SERVER.disableVanillaBoatFunctionality.get()) return;

        event.setCancellationResult(InteractionResult.FAIL);
        event.setCanceled(true);
    }

}
