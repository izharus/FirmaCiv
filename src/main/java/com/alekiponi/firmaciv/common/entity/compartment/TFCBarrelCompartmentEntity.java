package com.alekiponi.firmaciv.common.entity.compartment;

import net.minecraft.world.entity.EntityType;
import com.alekiponi.alekiships.common.entity.compartment.*;
import com.alekiponi.alekiships.util.CommonHelper;
import com.alekiponi.firmaciv.common.menu.BarrelCompartmentMenu;
import com.alekiponi.firmaciv.network.ClientBoundBarrelCompartmentUpdatePacket;
import com.alekiponi.firmaciv.network.PacketHandler;
import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.client.particle.TFCParticles;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.BarrelBlockEntity;
import net.dries007.tfc.common.blocks.devices.BarrelBlock;
import net.dries007.tfc.common.capabilities.PartialFluidHandler;
import net.dries007.tfc.common.capabilities.PartialItemHandler;
import net.dries007.tfc.common.capabilities.SidedHandler;
import net.dries007.tfc.common.component.size.IItemSize;
import net.dries007.tfc.common.component.size.ItemSizeManager;
import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.recipes.BarrelRecipe;
import net.dries007.tfc.common.recipes.SealedBarrelRecipe;
import net.dries007.tfc.common.recipes.TFCRecipeTypes;
import net.dries007.tfc.common.recipes.input.BarrelInventory;

import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.CalendarTransaction;
import net.dries007.tfc.util.calendar.Calendars;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A compartment implementation of TFCs {@link BarrelBlockEntity}.
 * Consists of mostly code copied and modified from the TFC implementation on
 * <a href="https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/1e9b5b2202eb7ffee96d637894def32ab804be9a/src/main/java/net/dries007/tfc/common/blockentities/BarrelBlockEntity.java">GitHub</a>
 * <p>
 * We cannot extend {@link ContainerCompartmentEntity} as our menu {@link BarrelCompartmentMenu} needs extra data
 * (the compartment Entity) in order to properly function.
 */
public class TFCBarrelCompartmentEntity extends AbstractCompartmentEntity implements BlockCompartment, CompartmentCloneable, SimpleBlockMenuCompartment, MenuConstructor, Container, IEntityWithComplexSpawn {

    /**
     * Kinda stupid but simple way to set {@link #recipeTick} to 0 so jade works correctly.
     */
    private static final byte RESET_RECIPE_EVENT = (byte) 42;
    private static final EntityDataAccessor<BlockState> DATA_ID_DISPLAY_BLOCK = SynchedEntityData.defineId(
            TFCBarrelCompartmentEntity.class, EntityDataSerializers.BLOCK_STATE);
    private final BarrelCompartmentInventory inventory = new BarrelCompartmentInventory(this);
    private final SidedHandler<IItemHandler> sidedInventory = new SidedHandler<>(this.inventory);
    private final SidedHandler<IFluidHandler> sidedFluidInventory = new SidedHandler<>(this.inventory);
    private final NonNullList<ItemStack> itemStacks = NonNullList.withSize(BarrelBlockEntity.SLOTS, ItemStack.EMPTY);
    @Nullable
    private ResourceLocation recipeName;
    @Nullable
    private SealedBarrelRecipe recipe;
    /**
     * The last tick this barrel was updated in serverTick()
     */
    private long lastUpdateTick = Integer.MIN_VALUE;
    /**
     * The tick this barrel was sealed
     */
    private long sealedTick;
    /**
     * The tick this barrel started working on the current recipe
     */
    private long recipeTick;
    private int soundCooldownTicks = 0;
    /**
     * If the instant recipe needs to be checked again
     */
    private boolean needsInstantRecipeUpdate;

    public TFCBarrelCompartmentEntity(final EntityType<? extends TFCBarrelCompartmentEntity> entityType,
            final Level level) {
        super(entityType, level);
        configureAutomation();
    }

    public TFCBarrelCompartmentEntity(final EntityType<? extends TFCBarrelCompartmentEntity> compartmentType,
            final Level level, final ItemStack itemStack) {
        this(compartmentType, level);

        if (!(itemStack.getItem() instanceof BlockItem blockItem)) return;

        this.setDisplayBlockState(blockItem.getBlock().defaultBlockState());

        this.applyComponentsFromItemStack(itemStack);
    }

    public static void toggleSeal(final TFCBarrelCompartmentEntity barrelCompartment) {
        final boolean previousSealed = barrelCompartment.isSealed();

        if (previousSealed) {
            barrelCompartment.onUnseal();
        } else {
            barrelCompartment.onSeal();
        }
    }

    /**
     * Safely sends an update packet if on the server
     *
     * @param barrelCompartment The barrel compartment to update
     */
    private static void trySendUpdatePacket(final TFCBarrelCompartmentEntity barrelCompartment) {
        if (barrelCompartment.level().isClientSide) return;

        PacketDistributor.sendToPlayersTrackingEntity(barrelCompartment,
                new ClientBoundBarrelCompartmentUpdatePacket(barrelCompartment, barrelCompartment.inventory.getFluid(),
                        barrelCompartment.inventory.getStackInSlot(BarrelBlockEntity.SLOT_ITEM)));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ID_DISPLAY_BLOCK, Blocks.AIR.defaultBlockState());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;

        // Must run before checkForCalendarUpdate(), as this sets the current recipe.
        if (this.recipeName != null) {
            this.recipe = this.level().getRecipeManager().byKey(this.recipeName)
                    .map(recipe -> recipe.value() instanceof SealedBarrelRecipe sealedRecipe ? sealedRecipe : null)
                    .orElse(null);
            this.recipeName = null;
        }

        this.checkForCalendarUpdate();

        if (this.level().getGameTime() % 5 == 0) {
            this.updateFluidIOSlots();
        }

        final List<ItemStack> excess = this.inventory.excess;
        if (!excess.isEmpty() && this.inventory.getStackInSlot(BarrelBlockEntity.SLOT_ITEM).isEmpty()) {
            this.inventory.setStackInSlot(BarrelBlockEntity.SLOT_ITEM, excess.remove(0));
        }

        final SealedBarrelRecipe recipe = this.recipe;
        final boolean sealed = this.isSealed();

        if (recipe != null && sealed) {
            final int durationSealed = (int) (Calendars.SERVER.getTicks() - this.recipeTick);
            if (!recipe.isInfinite() && durationSealed > recipe.getDuration()) {
                if (recipe.matches(this.inventory)) {
                    // Recipe completed, so fill outputs
                    recipe.assembleOutputs(this.inventory);
                    this.playSound(recipe.getCompleteSound(), SoundSource.BLOCKS, 1 + this.random.nextFloat(),
                            this.random.nextFloat() + 0.7F + 0.3F);
                }

                // In both cases, update the recipe and sync
                this.updateRecipe();

                // Re-check the recipe. If we have an invalid or infinite recipe, then exit simulation. Otherwise, jump forward to the next recipe completion
                // This handles the case where multiple sequential recipes, such as brining -> pickling -> vinegar preservation would've occurred.
                final SealedBarrelRecipe knownRecipe = this.recipe;
                if (knownRecipe != null) {
                    // We're in a sequential recipe, so apply sealed affects to the new recipe
                    knownRecipe.onSealed(this.inventory);
                }
            }
        }

        if (this.needsInstantRecipeUpdate) {
            this.needsInstantRecipeUpdate = false;
            if (this.inventory.excess.isEmpty()) // Excess must be empty for instant recipes to apply
            {
                final RecipeManager recipeManager = this.level().getRecipeManager();
                Optional.<net.minecraft.world.item.crafting.RecipeHolder<? extends BarrelRecipe>>empty() // For type erasure
                        .or(() -> Optional.ofNullable(BarrelRecipe.get(this.level(), TFCRecipeTypes.BARREL_INSTANT, this.inventory)))
                        .or(() -> Optional.ofNullable(BarrelRecipe.get(this.level(), TFCRecipeTypes.BARREL_INSTANT_FLUID, this.inventory))).ifPresent(instantRecipe -> {
                            instantRecipe.value().assembleOutputs(this.inventory);
                            if (this.soundCooldownTicks == 0) {
                                this.playSound(instantRecipe.value().getCompleteSound(), SoundSource.BLOCKS,
                                        1 + this.random.nextFloat(), this.random.nextFloat() + 0.7F + 0.3F);
                                this.soundCooldownTicks = 5;
                                if (instantRecipe.value().getCompleteSound() == SoundEvents.FIRE_EXTINGUISH && this.level() instanceof ServerLevel server) {
                                    server.sendParticles(TFCParticles.BUBBLE.get(),
                                            this.getX() + this.random.nextFloat() * 0.375 - 0.1875, this.getY() + 15f / 16f,
                                            this.getZ() + this.random.nextFloat() * 0.375 - 0.1875, 6, 0, 0, 0, 1);
                                    server.sendParticles(TFCParticles.STEAM.get(),
                                            this.getX() + this.random.nextFloat() * 0.375 - 0.1875, this.getY() + 15f / 16f,
                                            this.getZ() + this.random.nextFloat() * 0.375 - 0.1875, 6, 0, 0, 0, 1);
                                }
                            }
                        });

            }
        }

        if (this.soundCooldownTicks > 0) {
            this.soundCooldownTicks--;
        }

        // Slurp up items
        if (this.level().getGameTime() % 20 == 0 && !sealed) {
            Helpers.gatherAndConsumeItems(this.level(),
                    new AABB(0.25f, 0.0625f, 0.25f, 0.75f, 0.9375f, 0.75f).move(this.position()), this.inventory,
                    BarrelBlockEntity.SLOT_ITEM, BarrelBlockEntity.SLOT_ITEM);
        }


        if (!sealed && this.level().getGameTime() % 4 == 0 && this.level().isRainingAt(this.blockPosition().above())) {
            // Fill with water from rain
            this.inventory.fill(new FluidStack(Fluids.WATER, 1), IFluidHandler.FluidAction.EXECUTE);
        }
    }

    @Override
    public InteractionResult interact(final Player player, final InteractionHand hand) {
        final ItemStack heldStack = player.getItemInHand(hand);
        if (heldStack.isEmpty() && player.isShiftKeyDown()) {
            TFCBarrelCompartmentEntity.toggleSeal(this);
            this.playSound(SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1F, 0.85F);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (!this.isSealed() && FluidHelpers.transferBetweenBlockHandlerAndItem(heldStack, this.inventory, this.level(),
                this.blockPosition(), FluidHelpers.with(player, hand))) {
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (!this.level().isClientSide && player instanceof final ServerPlayer serverPlayer) {
            serverPlayer.openMenu(this.getMenuProvider(),
                    friendlyByteBuf -> friendlyByteBuf.writeVarInt(this.getId()));
            return InteractionResult.CONSUME;
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public ItemStack getDropStack() {
        ItemStack result = new ItemStack(getDisplayBlockState().getBlock());
        if (isSealed()) result.applyComponents(collectComponents());
        return result;
    }

    @Override
    protected void addAdditionalSaveData(final CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);

        BlockCompartment.saveBlockstate(this, compoundTag);

        this.addCommonSaveData(compoundTag);
    }

    @Override
    protected void readAdditionalSaveData(final CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);

        BlockCompartment.readBlockstate(this, compoundTag);

        this.readCommonSaveData(compoundTag);
    }

    private void readCommonSaveData(final CompoundTag compoundTag) {
        this.lastUpdateTick = compoundTag.getLong("lastUpdateTick");
        this.sealedTick = compoundTag.getLong("sealedTick");
        this.recipeTick = compoundTag.getLong("recipeTick");

        this.recipe = null;
        this.recipeName = null;
        if (compoundTag.contains("recipe", Tag.TAG_STRING)) {
            this.recipeName = ResourceLocation.parse(compoundTag.getString("recipe"));
            this.recipe = this.level().getRecipeManager().byKey(this.recipeName)
                    .map(recipe -> recipe.value() instanceof SealedBarrelRecipe barrelRecipe ? barrelRecipe : null)
                    .orElse(null);
        }
        this.inventory.deserializeNBT(compoundTag.getCompound("inventory"));
    }

    private void addCommonSaveData(final CompoundTag compoundTag) {
        compoundTag.putLong("lastUpdateTick", this.lastUpdateTick);
        compoundTag.putLong("sealedTick", this.sealedTick);
        compoundTag.putLong("recipeTick", this.recipeTick);
        if (this.recipe != null) {
            // Recipe saved to sync to client
            compoundTag.putString("recipe", this.level().getRecipeManager().getRecipes().stream().filter(holder -> holder.value() == this.recipe).map(holder -> holder.id().toString()).findFirst().orElse(""));
        } else if (this.recipeName != null) {
            compoundTag.putString("recipeName", this.recipeName.toString());
        }
        compoundTag.put("inventory", this.inventory.serializeNBT());
    }

    public CompoundTag saveForItemStack() {
        final CompoundTag compoundTag = new CompoundTag();
        this.addCommonSaveData(compoundTag);
        if (this.hasCustomName()) {
            compoundTag.putString("CustomName",
                    Component.Serializer.toJson(this.getCustomName(), this.registryAccess()));
        }
        return compoundTag;
    }



    private void updateFluidIOSlots() {
        final ItemStack input = this.inventory.getStackInSlot(BarrelBlockEntity.SLOT_FLUID_CONTAINER_IN);
        if (!input.isEmpty() && this.inventory.getStackInSlot(BarrelBlockEntity.SLOT_FLUID_CONTAINER_OUT).isEmpty()) {
            FluidHelpers.transferBetweenBlockHandlerAndItem(input, this.inventory, this.level(), this.blockPosition(),
                    (newOriginalStack, newContainerStack) -> {
                        if (newContainerStack.isEmpty()) {
                            // No new container was produced, so shove the first stack in the output, and clear the input
                            inventory.setStackInSlot(BarrelBlockEntity.SLOT_FLUID_CONTAINER_IN, ItemStack.EMPTY);
                            inventory.setStackInSlot(BarrelBlockEntity.SLOT_FLUID_CONTAINER_OUT, newOriginalStack);
                        } else {
                            // We produced a new container - this will be the 'filled', so we need to shove *that* in the output
                            inventory.setStackInSlot(BarrelBlockEntity.SLOT_FLUID_CONTAINER_IN, newOriginalStack);
                            inventory.setStackInSlot(BarrelBlockEntity.SLOT_FLUID_CONTAINER_OUT, newContainerStack);
                        }
                    });
        }
    }

    private void checkForCalendarUpdate() {
        if (!this.level().isClientSide) {
            final long thisTick = Calendars.SERVER.getTicks();
            final long lastTick = this.getLastCalendarUpdateTick();
            final long tickDelta = thisTick - lastTick;
            if (lastTick != -2147483648 && tickDelta != 1) {
                this.onCalendarUpdate(tickDelta - 1);
            }

            this.setLastCalendarUpdateTick(thisTick);
        }
    }

    private void onCalendarUpdate(final long ticks) {
        try (final CalendarTransaction calendarTransaction = Calendars.SERVER.transaction()) {
            // Perform the recipe update in the past
            calendarTransaction.add(-ticks);
            this.updateRecipe();
        }

        // Don't simulate
        if (!this.canModify() || this.recipe == null || this.recipe.isInfinite()) return;

        // Otherwise, begin simulation by jumping to the end tick of the current recipe. If that was in the past, we simulate and retry.
        final long currentTick = Calendars.SERVER.getTicks();
        long lastKnownTick = this.recipeTick + this.recipe.getDuration();

        while (lastKnownTick < currentTick) {
            // Need to run the recipe completion, as it occurred in the past
            final long offset = currentTick - lastKnownTick;
            // This event should be in the past
            assert offset >= 0;

            try (final CalendarTransaction calendarTransaction = Calendars.SERVER.transaction()) {
                calendarTransaction.add(-offset);

                final BarrelRecipe recipe = this.recipe;
                if (recipe.matches(this.inventory)) {
                    recipe.assembleOutputs(this.inventory);
                }
                this.updateRecipe();

            }

            // Re-check the recipe. If we have an invalid or infinite recipe, then exit simulation. Otherwise, jump forward to the next recipe completion
            // This handles the case where multiple sequential recipes, such as brining -> pickling -> vinegar preservation would've occurred.
            final SealedBarrelRecipe knownRecipe = this.recipe;

            if (knownRecipe == null) return;

            // We're in a sequential recipe, so apply sealed affects to the new recipe
            knownRecipe.onSealed(this.inventory);

            if (knownRecipe.isInfinite()) return;

            lastKnownTick += this.recipe.getDuration();
        }
    }

    private void updateRecipe() {
        final SealedBarrelRecipe oldRecipe = this.recipe;
        if (this.inventory.excess.isEmpty()) {
            // Will only work on a recipe as long as the 'excess' is empty
            this.recipe = Optional.ofNullable(BarrelRecipe.get(this.level(), TFCRecipeTypes.BARREL_SEALED, this.inventory)).map(net.minecraft.world.item.crafting.RecipeHolder::value).orElse(null);
            if (this.recipe != null && oldRecipe != this.recipe) {
                // The recipe has changed to a new one, so update the recipe ticks
                this.recipeTick = Calendars.get(this.level()).getTicks();
            }
        }
    }

    /**
     * Called when the barrel is sealed.
     *
     * @see #toggleSeal(TFCBarrelCompartmentEntity)
     */
    public void onSeal() {
        if (!this.level().isClientSide) {
            // Drop container items, but allow the main slot to be filled
            for (final int slotIndex : new int[]{BarrelBlockEntity.SLOT_FLUID_CONTAINER_IN, BarrelBlockEntity.SLOT_FLUID_CONTAINER_OUT}) {
                Containers.dropItemStack(this.level(), this.getX(), this.getY(), this.getZ(),
                        this.inventory.getStackInSlot(slotIndex));
            }
        }

        this.sealedTick = Calendars.get(this.level()).getTicks();
        this.updateRecipe();
        if (this.recipe != null) {
            this.recipe.onSealed(this.inventory);
            this.recipeTick = this.sealedTick;
        }

        this.setDisplayBlockState(this.getDisplayBlockState().setValue(BarrelBlock.SEALED, true));

        this.playSound(TFCSounds.CLOSE_BARREL.get(), SoundSource.BLOCKS, 1 + this.random.nextFloat(),
                this.random.nextFloat() + 0.7F + 0.3F);
    }

    /**
     * Called when the barrel is sealed.
     *
     * @see #toggleSeal(TFCBarrelCompartmentEntity)
     */
    public void onUnseal() {
        this.sealedTick = this.recipeTick = 0;
        this.level().broadcastEntityEvent(this, RESET_RECIPE_EVENT);
        if (this.recipe != null) {
            this.recipe.onUnsealed(this.inventory);
        }
        this.updateRecipe();

        this.setDisplayBlockState(this.getDisplayBlockState().setValue(BarrelBlock.SEALED, false));

        this.playSound(TFCSounds.OPEN_BARREL.get(), SoundSource.BLOCKS, 1 + this.random.nextFloat(),
                this.random.nextFloat() + 0.7F + 0.3F);
    }

    @Override
    public void handleEntityEvent(final byte id) {
        if (id == RESET_RECIPE_EVENT) {
            this.recipeTick = 0;
        }
        super.handleEntityEvent(id);
    }

    /**
     * Client-side
     */
    @OnlyIn(Dist.CLIENT)
    public final long getRemainingTicks() {
        if (!this.level().isClientSide) return 0;

        if (this.recipe == null) {
            this.recipe = Optional.ofNullable(BarrelRecipe.get(this.level(), TFCRecipeTypes.BARREL_SEALED, this.inventory)).map(net.minecraft.world.item.crafting.RecipeHolder::value).orElse(null);
        }

        if (this.recipe != null) {
            return this.recipe.getDuration() - (Calendars.get(this.level()).getTicks() - this.recipeTick);
        }

        return 0;
    }

    @Nullable
    @Override
    public ItemStack getPickResult() {
        return new ItemStack(this.getDisplayBlockState().getBlock());
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
    protected void onBreak() {
        if (!isSealed() && level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOENTITYDROPS)) {
            CommonHelper.dropContents(level(), getX(), CommonHelper.maxHeightOfCollidableEntities(this), getZ(), this);
            for (ItemStack stack : inventory.excess) {
                Containers.dropItemStack(level(), getX(), getY(), getZ(), stack);
            }
            inventory.excess.clear();
        }
        BlockCompartment.playBreakSound(this);
    }

    @Nullable
    public final BarrelRecipe getRecipe() {
        return this.recipe;
    }

    public final long getSealedTick() {
        return this.sealedTick;
    }

    @SuppressWarnings("unused")
    public final long getRecipeTick() {
        return this.recipeTick;
    }

    public boolean canModify() {
        return !this.isSealed();
    }

    public final boolean isSealed() {
        return this.getDisplayBlockState().getValue(BarrelBlock.SEALED);
    }

    public final long getLastCalendarUpdateTick() {
        return this.lastUpdateTick;
    }

    public final void setLastCalendarUpdateTick(final long tick) {
        this.lastUpdateTick = tick;
    }

    @Override
    public final BlockState getDisplayBlockState() {
        return this.entityData.get(DATA_ID_DISPLAY_BLOCK);
    }

    @Override
    public final void setDisplayBlockState(final BlockState blockState) {
        this.entityData.set(DATA_ID_DISPLAY_BLOCK, blockState);
    }





    private void configureAutomation() {
        if (TFCConfig.SERVER.barrelEnableAutomation.get()) {
            this.sidedInventory.on(
                            new PartialItemHandler(this.inventory).insert(BarrelBlockEntity.SLOT_FLUID_CONTAINER_IN)
                                    .extract(BarrelBlockEntity.SLOT_FLUID_CONTAINER_OUT), Direction.Plane.HORIZONTAL)
                    .on(new PartialItemHandler(this.inventory).insert(BarrelBlockEntity.SLOT_ITEM), Direction.UP)
                    .on(new PartialItemHandler(this.inventory).extract(BarrelBlockEntity.SLOT_ITEM),
                            Direction.UP.getOpposite());
            this.sidedFluidInventory.on(PartialFluidHandler.insertOnly(this.inventory), Direction.UP)
                    .on(PartialFluidHandler.extractOnly(this.inventory), direction -> direction != Direction.UP);
        }
    }

    @Override
    public final int getContainerSize() {
        return BarrelBlockEntity.SLOTS;
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
        this.needsInstantRecipeUpdate = true;
        this.updateRecipe();
        TFCBarrelCompartmentEntity.trySendUpdatePacket(this);
    }

    @Override
    public boolean stillValid(final Player player) {
        return !this.isRemoved() && this.position().closerThan(player.position(), 8);
    }

    @Override
    public MenuProvider getMenuProvider() {
        return new SimpleMenuProvider(this, this.getDisplayName());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int id, final Inventory playerInventory, final Player player) {
        return new BarrelCompartmentMenu(id, playerInventory, this);
    }

    /**
     * Sets the internal barrel fluid. Used for client synchronization.
     * <p>
     * <strong>Do not use like a setter.</strong> Use the provided capabilities to manipulate the contents.
     * This is only for server -> client syncing purposes which happens automatically
     */
    @OnlyIn(Dist.CLIENT)
    public final void syncContents(final FluidStack fluid, final ItemStack visibleStack) {
        this.inventory.setFluid(fluid);
        this.inventory.setStackInSlot(BarrelBlockEntity.SLOT_ITEM, visibleStack);
    }

    @Override
    public void writeSpawnData(final net.minecraft.network.RegistryFriendlyByteBuf buffer) {
        FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, this.inventory.getFluid());
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, this.inventory.getStackInSlot(BarrelBlockEntity.SLOT_ITEM));
        buffer.writeVarLong(this.recipeTick);
    }

    @Override
    public void readSpawnData(final net.minecraft.network.RegistryFriendlyByteBuf additionalData) {
        this.inventory.setFluid(FluidStack.OPTIONAL_STREAM_CODEC.decode(additionalData));
        this.inventory.setStackInSlot(BarrelBlockEntity.SLOT_ITEM, ItemStack.OPTIONAL_STREAM_CODEC.decode(additionalData));
        this.recipeTick = additionalData.readVarLong();
    }



    @Override
    public final void clearContent() {
        this.itemStacks.clear();
    }

    public final static class BarrelCompartmentInventory extends FluidTank implements BarrelInventory {
        private final TFCBarrelCompartmentEntity barrelCompartment;
        private final List<ItemStack> excess;
        /**
         * If the inventory is pretending to be mutable, despite the barrel being sealed and preventing extractions / insertions
         */
        private boolean mutable;

        public BarrelCompartmentInventory(final TFCBarrelCompartmentEntity barrelCompartment) {
            super(TFCConfig.SERVER.barrelCapacity.get(),
                    fluidStack -> Helpers.isFluid(fluidStack.getFluid(), TFCTags.Fluids.USABLE_IN_BARREL));
            this.barrelCompartment = barrelCompartment;
            this.excess = new ArrayList<>();
        }

        @Override
        public void whileMutable(final Runnable action) {
            try {
                this.mutable = true;
                action.run();
            } finally {
                this.mutable = false;
            }
        }

        @Override
        public void insertItemWithOverflow(final ItemStack itemStack) {
            final ItemStack remainder = this.insertItem(BarrelBlockEntity.SLOT_ITEM, itemStack, false);
            if (!remainder.isEmpty()) {
                this.excess.add(remainder);
            }
        }

        @Override
        protected void onContentsChanged() {
            this.barrelCompartment.setChanged();
        }

        @Override
        public int fill(final FluidStack fluidStack, final FluidAction action) {
            return this.canModify() ? super.fill(fluidStack, action) : 0;
        }

        @Override
        public FluidStack drain(final FluidStack fluidStack, final FluidAction action) {
            return this.canModify() ? super.drain(fluidStack, action) : FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(final int maxDrain, final FluidAction action) {
            return this.canModify() ? super.drain(maxDrain, action) : FluidStack.EMPTY;
        }

        @Override
        public int getSlots() {
            return this.barrelCompartment.getContainerSize();
        }

        @Override
        public ItemStack getStackInSlot(final int slotIndex) {
            return this.barrelCompartment.getItem(slotIndex);
        }

        @Override
        public ItemStack insertItem(final int slotIndex, final ItemStack insertStack, final boolean simulate) {
            if (!this.canModify()) return insertStack;

            if (insertStack.isEmpty()) return ItemStack.EMPTY;

            if (!this.isItemValid(slotIndex, insertStack)) return insertStack;

            final ItemStack existing = this.getStackInSlot(slotIndex);
            int extractLimit = Math.min(this.getSlotLimit(slotIndex), insertStack.getMaxStackSize());
            if (!existing.isEmpty()) {
                if (!ItemStack.isSameItemSameComponents(insertStack, existing)) return insertStack;

                extractLimit -= existing.getCount();
            }

            if (extractLimit <= 0) return insertStack;

            boolean reachedLimit = insertStack.getCount() > extractLimit;
            if (!simulate) {
                if (existing.isEmpty()) {
                    this.setStackInSlot(slotIndex,
                            reachedLimit ? insertStack.copyWithCount(extractLimit) : insertStack);
                } else {
                    existing.grow(reachedLimit ? extractLimit : insertStack.getCount());
                    this.barrelCompartment.setChanged();
                }
            }

            return reachedLimit ? insertStack.copyWithCount(insertStack.getCount() - extractLimit) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack extractItem(final int slotIndex, final int amount, final boolean simulate) {
            if (!this.canModify()) return ItemStack.EMPTY;

            if (amount == 0) {
                return ItemStack.EMPTY;
            }

            final ItemStack existing = this.getStackInSlot(slotIndex);

            if (existing.isEmpty()) {
                return ItemStack.EMPTY;
            }

            final int toExtract = Math.min(amount, existing.getMaxStackSize());
            if (existing.getCount() <= toExtract) {
                if (!simulate) {
                    this.setStackInSlot(slotIndex, ItemStack.EMPTY);
                    return existing;
                } else {
                    return existing.copy();
                }
            }

            if (!simulate) {
                this.setStackInSlot(slotIndex, existing.copyWithCount(existing.getCount() - toExtract));
            }

            return existing.copyWithCount(toExtract);
        }

        @Override
        public int getSlotLimit(final int slotIndex) {
            return this.barrelCompartment.getMaxStackSize();
        }

        @Override
        public boolean isItemValid(final int slotIndex, final ItemStack itemStack) {
            return switch (slotIndex) {
                case BarrelBlockEntity.SLOT_FLUID_CONTAINER_IN ->
                        itemStack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM) != null;
                case BarrelBlockEntity.SLOT_ITEM -> {
                    // We only want to deny heavy/huge (aka things that can hold inventory).
                    // Other than that, barrels don't need a size restriction, and should in general be unrestricted, so we can allow any kind of recipe input (i.e. unfired large vessel)
                    final IItemSize size = ItemSizeManager.get(itemStack);
                    yield size.getSize(itemStack).isSmallerThan(Size.HUGE) || size.getWeight(itemStack)
                            .isSmallerThan(Weight.VERY_HEAVY);
                }
                default -> true;
            };
        }

        @Override
        public void setStackInSlot(final int slotIndex, final ItemStack itemStack) {
            this.barrelCompartment.setItem(slotIndex, itemStack);
            this.barrelCompartment.setChanged();
        }

        public CompoundTag serializeNBT() {
            final CompoundTag compoundTag = new CompoundTag();
            { // Serialize our container wrapper slots
                //noinspection ExtractMethodRecommender
                final ListTag nbtTagList = new ListTag();
                for (int slotIndex = 0; slotIndex < this.barrelCompartment.getContainerSize(); slotIndex++) {
                    final ItemStack slotStack = this.barrelCompartment.getItem(slotIndex);
                    if (!slotStack.isEmpty()) {
                        final CompoundTag itemTag = (CompoundTag) slotStack.save(this.barrelCompartment.registryAccess());
                        itemTag.putInt("Slot", slotIndex);
                        nbtTagList.add(itemTag);
                    }
                }
                final CompoundTag itemStacks = new CompoundTag();
                itemStacks.put("Items", nbtTagList);
                compoundTag.put("inventory", itemStacks);
            }
            compoundTag.put("tank", this.fluid.saveOptional(this.barrelCompartment.registryAccess()));

            if (!this.excess.isEmpty()) {
                final ListTag excessStacks = new ListTag();
                for (final ItemStack itemStack : this.excess) {
                    excessStacks.add(itemStack.save(this.barrelCompartment.registryAccess()));
                }
                compoundTag.put("excess", excessStacks);
            }

            return compoundTag;
        }

        public void deserializeNBT(final CompoundTag compoundTag) {
            { // Deserialize our container wrapper slots
                final ListTag tagList = compoundTag.getCompound("inventory").getList("Items", Tag.TAG_COMPOUND);
                for (int i = 0; i < tagList.size(); i++) {
                    final CompoundTag itemTags = tagList.getCompound(i);
                    final int slotIndex = itemTags.getInt("Slot");
                    if (slotIndex >= 0 && slotIndex < this.barrelCompartment.getContainerSize()) {
                        this.barrelCompartment.setItem(slotIndex, ItemStack.parseOptional(this.barrelCompartment.registryAccess(), itemTags));
                    }
                }
            }

            this.fluid = FluidStack.parseOptional(this.barrelCompartment.registryAccess(), compoundTag.getCompound("tank"));
            this.excess.clear();
            if (compoundTag.contains("excess")) {
                final ListTag excessNbt = compoundTag.getList("excess", Tag.TAG_COMPOUND);
                for (int i = 0; i < excessNbt.size(); i++) {
                    this.excess.add(ItemStack.parseOptional(this.barrelCompartment.registryAccess(), excessNbt.getCompound(i)));
                }
            }
        }

        private boolean canModify() {
            return this.mutable || this.barrelCompartment.canModify();
        }
    }

    public net.neoforged.neoforge.items.IItemHandler getItemHandler(@Nullable Direction side) { return sidedInventory.get(side); }

    public net.neoforged.neoforge.fluids.capability.IFluidHandler getFluidHandler(@Nullable Direction side) { return sidedFluidInventory.get(side); }
    @Override public void applyComponentsFromItemStack(ItemStack stack) {
        setCustomName(stack.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME));
        var data = stack.get(net.dries007.tfc.common.component.TFCComponents.BARREL);
        if (data != null) {
            setDisplayBlockState(getDisplayBlockState().setValue(BarrelBlock.SEALED, true));
            var contents = data.itemContent();
            if (!contents.isEmpty()) setItem(BarrelBlockEntity.SLOT_ITEM, contents.getFirst().copy());
            inventory.excess.clear();
            for (int i = 1; i < contents.size(); i++) inventory.excess.add(contents.get(i).copy());
            inventory.setFluid(data.fluidContent().copy());
            sealedTick = data.sealedTick(); recipeTick = data.recipeTick();
        }
    }
    @Override public net.minecraft.core.component.DataComponentMap collectComponents() {
        var builder = net.minecraft.core.component.DataComponentMap.builder();
        if (hasCustomName()) builder.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, getCustomName());
        var contents = new java.util.ArrayList<ItemStack>();
        contents.add(getItem(BarrelBlockEntity.SLOT_ITEM).copy());
        inventory.excess.forEach(stack -> contents.add(stack.copy()));
        builder.set(net.dries007.tfc.common.component.TFCComponents.BARREL,
            new net.dries007.tfc.common.component.block.BarrelComponent(contents, inventory.getFluid().copy(), sealedTick, recipeTick));
        return builder.build();
    }

    public Component getRecipeTooltip() {
        return level().getRecipeManager().getRecipes().stream().filter(holder -> holder.value() == recipe)
            .findFirst().map(holder -> Component.translatable("tfc.recipe.barrel." + holder.id().getNamespace() + "." + holder.id().getPath().replace('/', '.'))).orElse(Component.empty());
    }
}
