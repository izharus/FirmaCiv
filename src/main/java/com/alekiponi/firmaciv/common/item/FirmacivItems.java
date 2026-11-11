package com.alekiponi.firmaciv.common.item;

import com.alekiponi.firmaciv.Firmaciv;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class FirmacivItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Firmaciv.MOD_ID);

    public static final DeferredHolder<Item, Item> FIRMACIV_CANNON = ITEMS.register("cannon",
            () -> new FirmacivCannonItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> SEXTANT = ITEMS.register("sextant",
            () -> new SextantItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> NAV_CLOCK = ITEMS.register("nav_clock",
            () -> new NavClockItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> BAROMETER = ITEMS.register("barometer",
            () -> new BarometerItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> UNFINISHED_SEXTANT = ITEMS.register("unfinished_sextant",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> UNFINISHED_NAV_CLOCK = ITEMS.register("unfinished_nav_clock",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> UNFINISHED_BAROMETER = ITEMS.register("unfinished_barometer",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> FIRMACIV_COMPASS = ITEMS.register("firmaciv_compass",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> LARGE_WATERPROOF_HIDE = ITEMS.register("large_waterproof_hide",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> KAYAK_PADDLE = ITEMS.register("kayak_paddle",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> CANOE_PADDLE = ITEMS.register("canoe_paddle",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> COPPER_BOLT = ITEMS.register("copper_bolt",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final DeferredHolder<Item, Item> CANNON_BARREL = ITEMS.register("cannon_barrel",
            () -> new Item(new Item.Properties().stacksTo(4)));

    public static final DeferredHolder<Item, Item> ROPE_COIL = ITEMS.register("rope_coil",
            () -> new Item(new Item.Properties().stacksTo(4)));

    public static final DeferredHolder<Item, Item> LARGE_TRIANGULAR_SAIL = ITEMS.register("large_triangular_sail",
            () -> new Item(new Item.Properties().stacksTo(4)));

    public static final DeferredHolder<Item, Item> MEDIUM_TRIANGULAR_SAIL = ITEMS.register("medium_triangular_sail",
            () -> new Item(new Item.Properties().stacksTo(4)));

    public static final DeferredHolder<Item, Item> SMALL_TRIANGULAR_SAIL = ITEMS.register("small_triangular_sail",
            () -> new Item(new Item.Properties().stacksTo(4)));

    public static final DeferredHolder<Item, Item> KAYAK = ITEMS.register("kayak",
            () -> new KayakItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> KAYAK_WITH_PADDLE_ICON_ONLY = ITEMS.register("kayak_with_paddle_icon_only",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> CANOE_ICON_ONLY = ITEMS.register("canoe_icon_only",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> CANOE_WITH_PADDLE_ICON_ONLY = ITEMS.register("canoe_with_paddle_icon_only",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> ROWBOAT_ICON_ONLY = ITEMS.register("rowboat_icon_only",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> SLOOP_ICON_ONLY = ITEMS.register("sloop_icon_only",
            () -> new Item(new Item.Properties().stacksTo(1)));
}
