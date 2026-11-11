package com.alekiponi.firmaciv.common.menu;

import com.alekiponi.firmaciv.common.entity.compartment.LargeVesselCompartmentEntity;
import net.dries007.tfc.common.blockentities.BarrelBlockEntity;
import net.dries007.tfc.common.blockentities.LargeVesselBlockEntity;

import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.component.heat.IHeat;
import net.dries007.tfc.common.container.ButtonHandlerContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

public class LargeVesselCompartmentMenu extends AbstractContainerMenu implements ButtonHandlerContainer {

    private final LargeVesselCompartmentEntity vesselCompartment;

    public LargeVesselCompartmentMenu(final int windowId, final Inventory inventory,
            final LargeVesselCompartmentEntity vesselCompartment) {
        super(FirmacivMenus.LARGE_VESSEL_COMPARTMENT_MENU.get(), windowId);
        this.vesselCompartment = vesselCompartment;

        java.util.Optional.ofNullable(this.vesselCompartment.getItemHandler(null)).ifPresent((handler) -> {
            this.addSlot(new SlotItemHandler(handler, 0, 62, 19));
            this.addSlot(new SlotItemHandler(handler, 1, 80, 19));
            this.addSlot(new SlotItemHandler(handler, 2, 98, 19));
            this.addSlot(new SlotItemHandler(handler, 3, 62, 37));
            this.addSlot(new SlotItemHandler(handler, 4, 80, 37));
            this.addSlot(new SlotItemHandler(handler, 5, 98, 37));
            this.addSlot(new SlotItemHandler(handler, 6, 62, 55));
            this.addSlot(new SlotItemHandler(handler, 7, 80, 55));
            this.addSlot(new SlotItemHandler(handler, 8, 98, 55));
        });

        this.addPlayerInventorySlots(inventory);
    }

    static LargeVesselCompartmentMenu fromNetwork(final int windowId, final Inventory inventory,
            final FriendlyByteBuf friendlyByteBuf) {
        final Entity entity = inventory.player.level().getEntity(friendlyByteBuf.readVarInt());
        if (entity instanceof final LargeVesselCompartmentEntity vesselCompartment) {
            return new LargeVesselCompartmentMenu(windowId, inventory, vesselCompartment);
        }
        throw new IllegalStateException(String.format("%s is not a Vessel compartment", entity));
    }

    @Override
    public void clicked(final int slotIndex, final int pButton, final ClickType pClickType, final Player pPlayer) {
        if (slotIndex >= 0 && slotIndex < LargeVesselBlockEntity.SLOTS && this.vesselCompartment.isSealed()) return;
        super.clicked(slotIndex, pButton, pClickType, pPlayer);
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int slotIndex) {
        final Slot slot = slots.get(slotIndex);
        // Only move an item when the index clicked has any contents
        if (slot.hasItem()) {
            final ItemStack slotStack = slot.getItem();
            final ItemStack original = slotStack.copy();


            if (this.vesselCompartment.isSealed()) {
                return ItemStack.EMPTY;
            }

            final @Nullable IHeat heat = HeatCapability.get(slotStack);
            final int containerSlot = java.util.Optional.ofNullable(slotStack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM))
                    .isPresent() && heat != null && heat.getTemperature() == 0 ? BarrelBlockEntity.SLOT_FLUID_CONTAINER_IN : BarrelBlockEntity.SLOT_ITEM;

            if (slotIndex < BarrelBlockEntity.SLOTS) {
                if (!this.moveItemStackTo(slotStack, BarrelBlockEntity.SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotStack, containerSlot, containerSlot + 1, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            slot.onTake(player, slotStack);
            return original;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(final Player player) {
        return this.vesselCompartment.stillValid(player);
    }

    public LargeVesselCompartmentEntity getVesselCompartment() {
        return this.vesselCompartment;
    }

    @Override
    public void onButtonPress(final int buttonId, @Nullable final CompoundTag compoundTag) {
        LargeVesselCompartmentEntity.toggleSeal(this.vesselCompartment);
    }

    /**
     * Adds the player inventory slots to the container.
     */
    private void addPlayerInventorySlots(final Inventory inventory) {
        // Main Inventory. Indexes [9, 36)
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // Hot bar. Indexes [0, 9)
        for (int k = 0; k < 9; k++) {
            this.addSlot(new Slot(inventory, k, 8 + k * 18, 142));
        }
    }
}