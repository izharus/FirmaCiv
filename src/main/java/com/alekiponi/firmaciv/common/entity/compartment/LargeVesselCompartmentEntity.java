package com.alekiponi.firmaciv.common.entity.compartment;

import com.alekiponi.alekiships.common.entity.vehiclehelper.CompartmentType;
import com.alekiponi.alekiships.common.entity.vehiclehelper.compartment.*;
import com.alekiponi.alekiships.util.CommonHelper;
import com.alekiponi.firmaciv.common.menu.LargeVesselCompartmentMenu;
import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.blockentities.LargeVesselBlockEntity;
import net.dries007.tfc.common.blocks.LargeVesselBlock;
import net.dries007.tfc.common.capabilities.PartialItemHandler;
import net.dries007.tfc.common.capabilities.SidedHandler;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.FoodTraits;
import net.dries007.tfc.common.capabilities.size.ItemSizeManager;
import net.dries007.tfc.common.capabilities.size.Size;
import net.dries007.tfc.config.TFCConfig;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Same issue as with {@link TFCBarrelCompartmentEntity} we need our entity in the menu.
 * This really should get changed...
 */
public class LargeVesselCompartmentEntity extends AbstractCompartmentEntity implements BlockCompartment, CompartmentCloneable, SimpleBlockMenuCompartment, MenuConstructor, Container {

    private static final EntityDataAccessor<BlockState> DATA_ID_DISPLAY_BLOCK = SynchedEntityData.defineId(
            LargeVesselCompartmentEntity.class, EntityDataSerializers.BLOCK_STATE);

    private final LargeVesselCompartmentEntity.VesselInventory inventory = new LargeVesselCompartmentEntity.VesselInventory(
            this);
    private final SidedHandler.Builder<IItemHandler> sidedInventory = new SidedHandler.Builder<>(this.inventory);
    private final NonNullList<ItemStack> itemStacks = NonNullList.withSize(LargeVesselBlockEntity.SLOTS,
            ItemStack.EMPTY);

    public LargeVesselCompartmentEntity(final CompartmentType<? extends LargeVesselCompartmentEntity> compartmentType,
            final Level level) {
        super(compartmentType, level);

        if (TFCConfig.SERVER.largeVesselEnableAutomation.get()) {
            this.sidedInventory.on((new PartialItemHandler(this.inventory)).insert(0, 1, 2, 3, 4, 5, 6, 7, 8),
                    (d) -> d != Direction.DOWN);
            this.sidedInventory.on((new PartialItemHandler(this.inventory)).extract(0, 1, 2, 3, 4, 5, 6, 7, 8),
                    Direction.DOWN);
        }
    }

    public LargeVesselCompartmentEntity(final CompartmentType<? extends LargeVesselCompartmentEntity> compartmentType,
            final Level level, final ItemStack itemStack) {
        this(compartmentType, level);

        if (!(itemStack.getItem() instanceof BlockItem blockItem)) throw new IllegalArgumentException(
                "Cannot construct a Vessel compartment from something that isn't a block");

        this.setDisplayBlockState(blockItem.getBlock().defaultBlockState());

        if (itemStack.hasCustomHoverName()) {
            this.setCustomName(itemStack.getHoverName());
        }

        final CompoundTag blockEntityTag = BlockItem.getBlockEntityData(itemStack);
        if (blockEntityTag != null) {
            this.readContents(blockEntityTag);
            this.setDisplayBlockState(this.getDisplayBlockState().setValue(LargeVesselBlock.SEALED, true));
        }
    }

    public static void toggleSeal(final LargeVesselCompartmentEntity vesselCompartment) {
        final boolean previousSealed = vesselCompartment.isSealed();

        if (previousSealed) {
            vesselCompartment.onUnseal();
        } else {
            vesselCompartment.onSeal();
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_DISPLAY_BLOCK, Blocks.AIR.defaultBlockState());
    }

    @Override
    public InteractionResult interact(final Player player, final InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            LargeVesselCompartmentEntity.toggleSeal(this);
            return InteractionResult.SUCCESS;
        }

        if (!this.level().isClientSide && player instanceof final ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, this.getMenuProvider(),
                    friendlyByteBuf -> friendlyByteBuf.writeVarInt(this.getId()));
            return InteractionResult.CONSUME;
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void addAdditionalSaveData(final CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        this.saveContents(compoundTag);
        BlockCompartment.saveBlockstate(this, compoundTag);
    }

    protected void saveContents(final CompoundTag compoundTag) {
        compoundTag.put("inventory", this.inventory.serializeNBT());
    }

    @Override
    protected void readAdditionalSaveData(final CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.readContents(compoundTag);
        BlockCompartment.readBlockstate(this, compoundTag);
    }

    protected void readContents(final CompoundTag compoundTag) {
        this.inventory.deserializeNBT(compoundTag.getCompound("inventory"));
    }

    @Override
    protected ItemStack getDropStack() {
        final ItemStack dropStack = new ItemStack(this.getDisplayBlockState().getBlock());

        // Not sealed so just drop the barrel
        if (!this.isSealed()) return dropStack;

        final CompoundTag compoundTag = new CompoundTag();

        this.saveContents(compoundTag);

        if (!compoundTag.isEmpty()) {
            dropStack.addTagElement(BlockItem.BLOCK_ENTITY_TAG, compoundTag);
        }

        return dropStack;
    }

    @Override
    protected void destroy(final DamageSource damageSource) {
        if (this.isSealed()) {
            super.destroy(damageSource);
            return;
        }

        CommonHelper.dropContents(this.level(), this.getX(), CommonHelper.maxHeightOfCollidableEntities(this),
                this.getZ(), this);
        super.destroy(damageSource);
    }

    @Nullable
    @Override
    public ItemStack getPickResult() {
        return new ItemStack(this.getDisplayBlockState().getBlock());
    }

    @Override
    protected void onBreak() {
        BlockCompartment.playBreakSound(this);
    }

    @Override
    protected void onPlaced() {
        BlockCompartment.playPlaceSound(this);
    }

    @Override
    protected void onHurt(final DamageSource damageSource) {
        BlockCompartment.playHitSound(this);
    }

    @Override
    public BlockState getDisplayBlockState() {
        return this.entityData.get(DATA_ID_DISPLAY_BLOCK);
    }

    @Override
    public void setDisplayBlockState(final BlockState blockState) {
        this.entityData.set(DATA_ID_DISPLAY_BLOCK, blockState);
    }

    public final boolean isSealed() {
        return this.getDisplayBlockState().getValue(LargeVesselBlock.SEALED);
    }

    /**
     * Called when the vessel is un-sealed.
     *
     * @see #toggleSeal(LargeVesselCompartmentEntity)
     */
    public void onUnseal() {
        for (int slotIndex = 0; slotIndex < 9; ++slotIndex) {
            this.setItem(slotIndex, FoodCapability.removeTrait(this.getItem(slotIndex).copy(), FoodTraits.PRESERVED));
        }

        this.setDisplayBlockState(this.getDisplayBlockState().setValue(LargeVesselBlock.SEALED, false));

        this.playSound(TFCSounds.OPEN_VESSEL.get(), SoundSource.BLOCKS, 1.0F + this.random.nextFloat(),
                this.random.nextFloat() + 0.7F + 0.3F);
    }

    /**
     * Called when the vessel is sealed.
     *
     * @see #toggleSeal(LargeVesselCompartmentEntity)
     */
    public void onSeal() {
        for (int slotIndex = 0; slotIndex < 9; ++slotIndex) {
            this.setItem(slotIndex, FoodCapability.applyTrait(this.getItem(slotIndex).copy(), FoodTraits.PRESERVED));
        }

        this.setDisplayBlockState(this.getDisplayBlockState().setValue(LargeVesselBlock.SEALED, true));

        this.playSound(TFCSounds.CLOSE_VESSEL.get(), SoundSource.BLOCKS, 1.0F + this.random.nextFloat(),
                this.random.nextFloat() + 0.7F + 0.3F);
    }

    @Override
    public final CompoundTag saveForItemStack() {
        final CompoundTag compoundTag = new CompoundTag();
        this.saveContents(compoundTag);
        if (this.hasCustomName()) {
            compoundTag.putString(ContainerCompartmentEntity.CUSTOM_NAME_KEY,
                    Component.Serializer.toJson(this.getCustomName()));
        }
        return compoundTag;
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider() {
        return new SimpleMenuProvider(this, this.getDisplayName());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int id, final Inventory playerInventory, final Player player) {
        return new LargeVesselCompartmentMenu(id, playerInventory, this);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> capability, final @Nullable Direction facing) {
        if (this.isAlive()) {
            if (capability == ForgeCapabilities.ITEM_HANDLER) {
                return this.sidedInventory.getSidedHandler(facing).cast();
            }
        }

        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.sidedInventory.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        if (TFCConfig.SERVER.largeVesselEnableAutomation.get()) {
            this.sidedInventory.on((new PartialItemHandler(this.inventory)).insert(0, 1, 2, 3, 4, 5, 6, 7, 8),
                    (d) -> d != Direction.DOWN);
            this.sidedInventory.on((new PartialItemHandler(this.inventory)).extract(0, 1, 2, 3, 4, 5, 6, 7, 8),
                    Direction.DOWN);
        }
    }

    @Override
    public final int getContainerSize() {
        return this.itemStacks.size();
    }

    @Override
    public final boolean isEmpty() {
        return this.itemStacks.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public final ItemStack getItem(final int slotIndex) {
        return this.itemStacks.get(slotIndex);
    }

    @Override
    public final ItemStack removeItem(final int slotIndex, final int amount) {
        return ContainerHelper.removeItem(this.itemStacks, slotIndex, amount);
    }

    @Override
    public final ItemStack removeItemNoUpdate(final int slotIndex) {
        return ContainerHelper.takeItem(this.itemStacks, slotIndex);
    }

    @Override
    public final void setItem(final int slotIndex, final ItemStack itemStack) {
        this.itemStacks.set(slotIndex, itemStack);
    }

    @Override
    public void setChanged() {

    }

    @Override
    public boolean stillValid(final Player player) {
        return !this.isRemoved() && this.position().closerThan(player.position(), 8);
    }

    @Override
    public final void clearContent() {
        this.itemStacks.clear();
    }

    public static class VesselInventory extends InvWrapper implements INBTSerializable<CompoundTag> {

        private final LargeVesselCompartmentEntity vesselCompartment;

        public VesselInventory(final LargeVesselCompartmentEntity vesselCompartment) {
            super(vesselCompartment);
            this.vesselCompartment = vesselCompartment;
        }

        @NotNull
        @Override
        public ItemStack insertItem(final int slotIndex, final ItemStack itemStack, final boolean simulate) {
            return this.canModify() ? super.insertItem(slotIndex, itemStack, simulate) : itemStack;
        }

        @NotNull
        @Override
        public ItemStack extractItem(final int slotIndex, final int amount, final boolean simulate) {
            return this.canModify() ? super.extractItem(slotIndex, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public boolean isItemValid(final int slotIndex, final ItemStack itemStack) {
            return this.canModify() && ItemSizeManager.get(itemStack).getSize(itemStack)
                    .isSmallerThan(Size.LARGE) && super.isItemValid(slotIndex, itemStack);
        }

        @Override
        public CompoundTag serializeNBT() {
            final CompoundTag compoundTag = new CompoundTag();
            { // Serialize our container wrapper slots
                final ListTag nbtTagList = new ListTag();
                for (int slotIndex = 0; slotIndex < this.vesselCompartment.getContainerSize(); slotIndex++) {
                    final ItemStack slotStack = this.vesselCompartment.getItem(slotIndex);
                    if (!slotStack.isEmpty()) {
                        final CompoundTag itemTag = new CompoundTag();
                        itemTag.putInt("Slot", slotIndex);
                        slotStack.save(itemTag);
                        nbtTagList.add(itemTag);
                    }
                }
                compoundTag.put("Items", nbtTagList);
                compoundTag.putInt("Size", this.vesselCompartment.getContainerSize());
            }
            return compoundTag;
        }

        @Override
        public void deserializeNBT(final CompoundTag compoundTag) {
            { // Deserialize our container wrapper slots
                final ListTag tagList = compoundTag.getList("Items", Tag.TAG_COMPOUND);
                for (int i = 0; i < tagList.size(); i++) {
                    final CompoundTag itemTags = tagList.getCompound(i);
                    final int slotIndex = itemTags.getInt("Slot");
                    if (slotIndex >= 0 && slotIndex < this.vesselCompartment.getContainerSize()) {
                        this.vesselCompartment.setItem(slotIndex, ItemStack.of(itemTags));
                    }
                }
            }
        }

        private boolean canModify() {
            return !this.vesselCompartment.isSealed();
        }
    }
}