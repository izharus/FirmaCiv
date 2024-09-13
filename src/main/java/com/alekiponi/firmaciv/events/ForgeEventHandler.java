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
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.MissingMappingsEvent;

@Mod.EventBusSubscriber(modid = Firmaciv.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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

    /**
     * TODO rip this out at some point after people have the new names
     */
    @SubscribeEvent
    public static void onMissingMapping(final MissingMappingsEvent event) {
        // Remap our TFC chest compartment
        for (final var entityTypeMapping : event.getMappings(Registries.ENTITY_TYPE, Firmaciv.MOD_ID)) {
            final String entityName = entityTypeMapping.getKey().getPath();

            if (entityName.equals("compartment_tfcchest"))
                entityTypeMapping.remap(FirmacivEntities.TFC_CHEST_COMPARTMENT_ENTITY.get());
        }

        // Remap our wood frame blocks
        for (final var blockMapping : event.getMappings(Registries.BLOCK, Firmaciv.MOD_ID)) {
            final String blockName = blockMapping.getKey().getPath();

            // Must loop over woods to map to our new name
            for (final TFCWood tfcWood : TFCWood.values()) {
                if (blockName.equals("wood/watercraft_frame_angled/" + tfcWood.getSerializedName()))
                    blockMapping.remap(FirmacivBlocks.WOODEN_BOAT_FRAME_ANGLED.get(tfcWood).get());

                if (blockName.equals("wood/watercraft_frame_flat/" + tfcWood.getSerializedName()))
                    blockMapping.remap(FirmacivBlocks.WOODEN_BOAT_FRAME_FLAT.get(tfcWood).get());
            }
        }


        remapToAlekiShips(event);
    }

    /**
     * Remap stuff we used to provide to AlekiShips
     */
    private static void remapToAlekiShips(final MissingMappingsEvent event) {

        for (final var entityTypeMapping : event.getMappings(Registries.ENTITY_TYPE, Firmaciv.MOD_ID)) {
            final String entityName = entityTypeMapping.getKey().getPath();

            /*
             * This sucks, some dummy added a bunch of compartments that only exist in AlekiShips (totally wasn't me)
             * anyway this section is remapping our compartment names to AlekiShips
             */
            if (entityName.equals("compartment_empty"))
                entityTypeMapping.remap(AlekiShipsEntities.EMPTY_COMPARTMENT_ENTITY.get());

            if (entityName.equals("compartment_block"))
                entityTypeMapping.remap(AlekiShipsEntities.BLOCK_COMPARTMENT_ENTITY.get());

            if (entityName.equals("compartment_barrel"))
                entityTypeMapping.remap(AlekiShipsEntities.BARREL_COMPARTMENT_ENTITY.get());

            if (entityName.equals("compartment_chest"))
                entityTypeMapping.remap(AlekiShipsEntities.CHEST_COMPARTMENT_ENTITY.get());

            if (entityName.equals("compartment_ender_chest"))
                entityTypeMapping.remap(AlekiShipsEntities.ENDER_CHEST_COMPARTMENT_ENTITY.get());

            if (entityName.equals("compartment_shulker_box"))
                entityTypeMapping.remap(AlekiShipsEntities.SHULKER_BOX_COMPARTMENT_ENTITY.get());

            if (entityName.equals("compartment_furnace"))
                entityTypeMapping.remap(AlekiShipsEntities.FURNACE_COMPARTMENT_ENTITY.get());

            if (entityName.equals("compartment_blast_furnace"))
                entityTypeMapping.remap(AlekiShipsEntities.BLAST_FURNACE_COMPARTMENT_ENTITY.get());

            if (entityName.equals("compartment_smoker"))
                entityTypeMapping.remap(AlekiShipsEntities.SMOKER_COMPARTMENT_ENTITY.get());

            if (entityName.equals("compartment_workbench"))
                entityTypeMapping.remap(AlekiShipsEntities.WORKBENCH_COMPARTMENT_ENTITY.get());

            if (entityName.equals("compartment_stonecutter"))
                entityTypeMapping.remap(AlekiShipsEntities.STONECUTTER_COMPARTMENT_ENTITY.get());

            if (entityName.equals("compartment_cartography_table"))
                entityTypeMapping.remap(AlekiShipsEntities.CARTOGRAPHY_TABLE_COMPARTMENT_ENTITY.get());

            if (entityName.equals("compartment_smithing_table"))
                entityTypeMapping.remap(AlekiShipsEntities.SMITHING_TABLE_COMPARTMENT_ENTITY.get());

            if (entityName.equals("compartment_grindstone"))
                entityTypeMapping.remap(AlekiShipsEntities.GRINDSTONE_COMPARTMENT_ENTITY.get());

            if (entityName.equals("compartment_loom"))
                entityTypeMapping.remap(AlekiShipsEntities.LOOM_COMPARTMENT_ENTITY.get());

            if (entityName.equals("compartment_anvil"))
                entityTypeMapping.remap(AlekiShipsEntities.BLOCK_COMPARTMENT_ENTITY.get());

            /*
             * This section is all the helper entities AlekiShips provides for us
             */
            if (entityName.equals("vehicle_part_boat"))
                entityTypeMapping.remap(AlekiShipsEntities.VEHICLE_PART.get());

            if (entityName.equals("vehicle_construction"))
                entityTypeMapping.remap(AlekiShipsEntities.CONSTRUCTION_ENTITY.get());

            if (entityName.equals("vehicle_part_construction"))
                entityTypeMapping.remap(AlekiShipsEntities.VEHICLE_PART.get());

            if (entityName.equals("vehicle_cleat"))
                entityTypeMapping.remap(AlekiShipsEntities.VEHICLE_CLEAT_ENTITY.get());

            if (entityName.equals("vehicle_collider"))
                entityTypeMapping.remap(AlekiShipsEntities.VEHICLE_COLLIDER_ENTITY.get());

            if (entityName.equals("vehicle_switch_sail"))
                entityTypeMapping.remap(AlekiShipsEntities.SAIL_SWITCH_ENTITY.get());

            if (entityName.equals("vehicle_anchor")) entityTypeMapping.remap(AlekiShipsEntities.ANCHOR_ENTITY.get());

            if (entityName.equals("vehicle_switch_windlass"))
                entityTypeMapping.remap(AlekiShipsEntities.WINDLASS_SWITCH_ENTITY.get());

            if (entityName.equals("cannonball")) entityTypeMapping.remap(AlekiShipsEntities.CANNONBALL_ENTITY.get());

            if (entityName.equals("cannon")) entityTypeMapping.remap(AlekiShipsEntities.CANNON_ENTITY.get());

            if (entityName.equals("vehicle_mast")) entityTypeMapping.remap(AlekiShipsEntities.MAST_ENTITY.get());
        }

        // Remap to their blocks
        for (final var blockMapping : event.getMappings(Registries.BLOCK, Firmaciv.MOD_ID)) {
            final String blockName = blockMapping.getKey().getPath();

            if (blockName.equals("oarlock")) blockMapping.remap(AlekiShipsBlocks.OARLOCK.get());

            if (blockName.equals("cleat")) blockMapping.remap(AlekiShipsBlocks.CLEAT.get());
        }

        // Remap to their items
        for (final var itemMapping : event.getMappings(Registries.ITEM, Firmaciv.MOD_ID)) {
            final String itemName = itemMapping.getKey().getPath();

            if (itemName.equals("cannonball")) itemMapping.remap(AlekiShipsItems.CANNONBALL.get());

            if (itemName.equals("cannon")) itemMapping.remap(AlekiShipsItems.CANNON.get());

            if (itemName.equals("oar")) itemMapping.remap(AlekiShipsItems.OAR.get());

            if (itemName.equals("anchor")) itemMapping.remap(AlekiShipsItems.ANCHOR.get());

            if (itemName.equals("oarlock")) itemMapping.remap(AlekiShipsBlocks.OARLOCK.get().asItem());

            if (itemName.equals("cleat")) itemMapping.remap(AlekiShipsBlocks.CLEAT.get().asItem());
        }
    }
}