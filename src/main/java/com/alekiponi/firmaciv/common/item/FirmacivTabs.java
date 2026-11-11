package com.alekiponi.firmaciv.common.item;

import com.alekiponi.alekiships.common.block.AlekiShipsBlocks;
import com.alekiponi.alekiships.common.item.AlekiShipsItems;
import com.alekiponi.firmaciv.Firmaciv;
import com.alekiponi.firmaciv.common.block.FirmacivBlocks;
import com.alekiponi.firmaciv.util.TFCWood;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class FirmacivTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(
            Registries.CREATIVE_MODE_TAB, Firmaciv.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FIRMACIV_TAB = CREATIVE_MODE_TABS.register("firmaciv_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(FirmacivItems.SEXTANT.get()))
                    .title(Component.translatable("creativetab.firmaciv_tab")).displayItems((pParameters, pOutput) -> {

                        pOutput.accept(AlekiShipsBlocks.OARLOCK.get());
                        pOutput.accept(AlekiShipsBlocks.CLEAT.get());
                        pOutput.accept(AlekiShipsItems.ANCHOR.get());

                        pOutput.accept(FirmacivItems.FIRMACIV_CANNON.get());
                        pOutput.accept(AlekiShipsItems.CANNONBALL.get());

                        pOutput.accept(FirmacivBlocks.BOAT_FRAME_ANGLED.get());
                        pOutput.accept(FirmacivBlocks.BOAT_FRAME_FLAT.get());

                        pOutput.accept(FirmacivItems.SEXTANT.get());
                        pOutput.accept(FirmacivItems.UNFINISHED_SEXTANT.get());

                        pOutput.accept(FirmacivItems.NAV_CLOCK.get());
                        pOutput.accept(FirmacivItems.UNFINISHED_NAV_CLOCK.get());

                        pOutput.accept(FirmacivItems.BAROMETER.get());
                        pOutput.accept(FirmacivItems.UNFINISHED_BAROMETER.get());

                        pOutput.accept(FirmacivItems.FIRMACIV_COMPASS.get());

                        pOutput.accept(FirmacivItems.LARGE_WATERPROOF_HIDE.get());
                        pOutput.accept(FirmacivItems.KAYAK.get());
                        pOutput.accept(FirmacivItems.KAYAK_PADDLE.get());

                        pOutput.accept(FirmacivItems.CANOE_PADDLE.get());

                        pOutput.accept(AlekiShipsItems.OAR.get());
                        pOutput.accept(FirmacivItems.COPPER_BOLT.get());

                        pOutput.accept(FirmacivItems.CANNON_BARREL.get());

                        pOutput.accept(TFCItems.UNREFINED_PAPER.get());
                        pOutput.accept(Items.GUNPOWDER);

                        pOutput.accept(FirmacivItems.ROPE_COIL.get());
                        pOutput.accept(FirmacivItems.SMALL_TRIANGULAR_SAIL.get());
                        pOutput.accept(FirmacivItems.MEDIUM_TRIANGULAR_SAIL.get());
                        pOutput.accept(FirmacivItems.LARGE_TRIANGULAR_SAIL.get());

                        pOutput.accept(FirmacivBlocks.THATCH_ROOFING.get());
                        pOutput.accept(FirmacivBlocks.THATCH_ROOFING_SLAB.get());

                        for (Wood tfcWood : Wood.values()){
                            pOutput.accept(FirmacivBlocks.WOOD_ROOFING.get(tfcWood).get().asItem());
                        }

                    }).build());
}