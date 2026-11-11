package com.alekiponi.firmaciv.util;

import com.alekiponi.firmaciv.events.config.ServerConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class FirmacivTags {

    public static class Blocks {

        public static final TagKey<Block> CAN_MAKE_CANOE = create("can_make_canoe");

        public static final TagKey<Block> CANOE_COMPONENT_BLOCKS = create("canoe_component_blocks");
        public static final TagKey<Block> CAN_MAKE_CANOE_UNRESTRICTED = create("can_make_canoe_unrestricted");

        private static TagKey<Block> create(String id) {
            return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("firmaciv", id));
        }
    }

    public static class Items {
        public static final TagKey<Item> SAWS = net.dries007.tfc.common.TFCTags.Items.TOOLS_SAW;
        public static final TagKey<Item> AXES = net.minecraft.tags.ItemTags.AXES;

        public static final TagKey<Item> CHESTS = create("chests");

        /**
         * All the items considered hard woods that {@link ServerConfig#shipWoodRestriction} cares about
         */
        public static final TagKey<Item> HARD_WOOD = create("hard_wood");
        /**
         * TFC's large vessels tag
         */
        public static final TagKey<Item> FIRED_LARGE_VESSELS = getFromTFC("fired_large_vessels");
        /**
         * TFC's barrels tag
         */
        public static final TagKey<Item> BARRELS = getFromTFC("barrels");

        public static TagKey<Item> getFromTFC(String id) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("tfc", id));
        }

        public static TagKey<Item> create(String id) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("firmaciv", id));
        }
    }
}