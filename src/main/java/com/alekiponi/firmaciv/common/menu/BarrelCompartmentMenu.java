package com.alekiponi.firmaciv.common.menu;

import com.alekiponi.firmaciv.common.entity.compartment.TFCBarrelCompartmentEntity;
import net.dries007.tfc.common.blockentities.BarrelBlockEntity;

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

public class BarrelCompartmentMenu extends AbstractContainerMenu implements ButtonHandlerContainer {

    private final TFCBarrelCompartmentEntity barrelCompartment;

    public BarrelCompartmentMenu(final int windowId, final Inventory inventory,
            final TFCBarrelCompartmentEntity barrelCompartment) {
        super(FirmacivMenus.BARREL_COMPARTMENT_MENU.get(), windowId);
        this.barrelCompartment = barrelCompartment;

        java.util.Optional.ofNullable(barrelCompartment.getItemHandler(null)).ifPresent(handler -> {
            this.addSlot(new SlotItemHandler(handler, BarrelBlockEntity.SLOT_FLUID_CONTAINER_IN, 35, 20));
            this.addSlot(new SlotItemHandler(handler, BarrelBlockEntity.SLOT_FLUID_CONTAINER_OUT, 35, 54));
            this.addSlot(new SlotItemHandler(handler, BarrelBlockEntity.SLOT_ITEM, 89, 37));
        });

        this.addPlayerInventorySlots(inventory);
    }

    static BarrelCompartmentMenu fromNetwork(final int windowId, final Inventory inventory,
            final FriendlyByteBuf friendlyByteBuf) {
        final Entity entity = inventory.player.level().getEntity(friendlyByteBuf.readVarInt());
        if (entity instanceof final TFCBarrelCompartmentEntity barrelCompartment) {
            return new BarrelCompartmentMenu(windowId, inventory, barrelCompartment);
        }
        throw new IllegalStateException(String.format("%s is not a Barrel compartment", entity));
    }

    @Override
    public void clicked(final int slotIndex, final int pButton, final ClickType pClickType, final Player pPlayer) {
        if (slotIndex >= 0 && slotIndex < BarrelBlockEntity.SLOTS && this.barrelCompartment.isSealed()) return;
        super.clicked(slotIndex, pButton, pClickType, pPlayer);
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int slotIndex) {
        final Slot slot = slots.get(slotIndex);
        // Only move an item when the index clicked has any contents
        if (slot.hasItem()) {
            final ItemStack slotStack = slot.getItem();
            final ItemStack original = slotStack.copy();


            if (this.barrelCompartment.isSealed()) {
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
        return this.barrelCompartment.stillValid(player);
    }

    public TFCBarrelCompartmentEntity getBarrelCompartment() {
        return this.barrelCompartment;
    }

    /**
     * Adds the player inventory slots to the container.
     */
    private void addPlayerInventorySlots(final Inventory inventory) {
        // Main Inventory. Indexes [9, 36)
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 96 + i * 18));
            }
        }

        // Hot bar. Indexes [0, 9)
        for (int k = 0; k < 9; k++) {
            this.addSlot(new Slot(inventory, k, 8 + k * 18, 154));
        }
    }

    @Override
    public void onButtonPress(final int buttonId, @Nullable final CompoundTag compoundTag) {
        TFCBarrelCompartmentEntity.toggleSeal(this.barrelCompartment);
    }
}