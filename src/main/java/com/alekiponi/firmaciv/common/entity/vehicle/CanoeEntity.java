package com.alekiponi.firmaciv.common.entity.vehicle;

import com.alekiponi.alekiships.common.entity.vehicle.AbstractAlekiBoatEntity;
import com.alekiponi.alekiships.common.entity.vehiclecapability.IAllowFallDamage;
import com.alekiponi.alekiships.common.entity.vehiclecapability.IHaveCleats;
import com.alekiponi.alekiships.common.entity.vehiclecapability.IHaveColliders;
import com.alekiponi.alekiships.common.entity.compartment.AbstractCompartmentEntity;
import com.alekiponi.firmaciv.util.BoatMaterial;
import com.alekiponi.firmaciv.common.item.FirmacivItems;
import com.alekiponi.firmaciv.util.Helper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class CanoeEntity extends FirmacivPaddledBoatEntity implements IHaveCleats, IHaveColliders, IAllowFallDamage {
    private static final EntityDataAccessor<Integer> DATA_ID_LENGTH = SynchedEntityData.defineId(CanoeEntity.class,
            EntityDataSerializers.INT);
    public final int PASSENGER_NUMBER = 5;
    public final int[] CLEATS = {2};
    public final int[] COMPARTMENTS = {0,1};
    protected final float DAMAGE_THRESHOLD = 80.0f;
    protected final float DAMAGE_RECOVERY = 2.0f;
    protected final float PASSENGER_SIZE_LIMIT = 0.9F;

    protected final float HUNGER_FACTOR = 0.02F;

    public final int[] COLLIDERS = {3, 4};

    public CanoeEntity(final EntityType<? extends CanoeEntity> entityType, final Level level,
                       final BoatMaterial boatMaterial) {
        super(entityType, level, boatMaterial.getSerializedName());


    }

    @Override
    public void tick(){
        super.tick();
        Helper.causeExhaustionWhilePaddlingRowing(this, HUNGER_FACTOR);
    }

    @Override
    public InteractionResult interact(final Player player, final InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(FirmacivItems.CANOE_PADDLE.get()) && player.isCreative() && player.isSecondaryUseActive()) {
            if(this.getLength() == 5){
                this.setLength(3);
            } else {
                this.setLength(this.getLength() +1);
            }
            return InteractionResult.SUCCESS;
        }
        return super.interact(player, hand);
    }

    @Override
    public AbstractCompartmentEntity.RidingPose[] getRidingPoses() {
        if (ridingPoses.length == 0 || ridingPoses[0] == null)
        {
            AbstractCompartmentEntity.RidingPose[] poses = new AbstractCompartmentEntity.RidingPose[this.getMaxPassengers()];
            for (int i = 0; i < this.getMaxPassengers(); i++)
            {
                poses[i] = AbstractCompartmentEntity.RidingPose.COMPACT;
            }
            this.ridingPoses = poses;
        }
        return ridingPoses;
    }

    @Override
    public float getPassengerSizeLimit() {
        return PASSENGER_SIZE_LIMIT;
    }

    @Override
    public int[][] getCompartmentRotationsArray() {
        return new int[0][];
    }

    @Override
    public int[] getCompartmentIndices() {
        return COMPARTMENTS;
    }

    @Override
    public int[] getColliderIndices() {
        return COLLIDERS;
    }

    @Override
    public int getCompartmentRotation(int i) {
        return 0;
    }

    @Override
    protected float getPaddleMultiplier() {
        float paddleMultiplier = 1.0f;
        if(this.getControllingPassenger() instanceof Player player){
            if (player.isHolding(FirmacivItems.CANOE_PADDLE.get())) {
                paddleMultiplier = 1.6f;
            }
        }

        int i = 0;
        for (Entity entity : this.getIndirectPassengers()) {
            if (entity instanceof Player player2) {
                if (player2.isHolding(FirmacivItems.CANOE_PADDLE.get())) {
                    i++;
                }
            }
        }
        if (i == 2) {
            paddleMultiplier = 2.0f;
        }
        return paddleMultiplier;
    }

    @Override
    protected float getMomentumSubtractor(){
        return 0.010f;
    }

    @Nullable
    @Override
    public Entity getPilotVehiclePartAsEntity() {
        if (this.isVehicle() && this.getPassengers().size() == this.getMaxPassengers()) {
            return this.getPassengers().get(1);
        }

        return null;
    }

    protected Vec3 positionRiderByIndex(int index){
        float localX = 0.0F;
        float localZ = 0.0F;
        float localY = (float) ((this.isRemoved() ? (double) 0.01F : this.getPassengersRidingOffset()));
        switch (index) {
            case 0 -> {
                //forward seat
                localZ = 0.0f;
                localY += 0.0625f;
                if(this.getLength() == 3){
                    localX = 0.3f;
                } else if(this.getLength() == 4){
                    localX = 0.4f;
                } else if(this.getLength() == 5){
                    localX = 1.0f;
                }
            }
            case 1 -> {
                //rear seat
                localZ = 0.0f;
                localY += 0.0625f;
                if(this.getLength() == 3){
                    localX = -0.7f;
                } else if(this.getLength() == 4){
                    localX = -0.7f - 0.1f;
                } else if(this.getLength() == 5){
                    localX = -0.7f - 0.6f;
                }
            }
            //cleat
            case 2 -> {
                localZ = 0.0f;
                localY += 0.45f;
                if(this.getLength() == 3){
                    localX = 1.0f;
                } else if(this.getLength() == 4){
                    localX = 1.5f;
                } else if(this.getLength() == 5){
                    localX = 2.0f;
                }
            }
            case 3 -> {
                localZ = 0.0f;
                localY += 0.0;
                if(this.getLength() == 3){
                    localX = 0.7f;
                } else if(this.getLength() == 4){
                    localX = 1.3f;
                } else if(this.getLength() == 5){
                    localX = 1.7f;
                }
            }
            case 4 -> {
                localZ = 0.0f;
                localY += 0.0;
                if(this.getLength() == 3){
                    localX = -0.7f;
                } else if(this.getLength() == 4){
                    localX = -1.3f;
                } else if(this.getLength() == 5){
                    localX = -1.7f;
                }
            }
        }
        return new Vec3(localX, localY, localZ);
    }

    @Override
    public net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> getLootTable() {
        // Canoes have no hull salvage; do not inherit rowboat planks and metal oarlocks.
        return net.minecraft.world.level.storage.loot.BuiltInLootTables.EMPTY;
    }

    public Item getDropItem() {
        return FirmacivItems.CANOE_ICON_ONLY.get();
    }

    public void setLength(int length){
        this.entityData.set(DATA_ID_LENGTH, Mth.clamp(length, 3, 5));
    }

    public int getLength(){
        return this.entityData.get(DATA_ID_LENGTH);
    }

    @Override
    public int getMaxPassengers() {
        return PASSENGER_NUMBER;
    }

    @Override
    public float getDamageThreshold() {
        return this.DAMAGE_THRESHOLD;
    }

    @Override
    public float getDamageRecovery() {
        return this.DAMAGE_RECOVERY;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(this.getDropItem());
    }

    @Override
    public float[] getDefaultColliderDimensions() {
        return new float[]{1.125f, 0.625f};
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ID_LENGTH, 3);

    }

    @Override
    protected double windDriftMultiplier() {
        return 0.5;
    }

    @Override
    protected void readAdditionalSaveData(final CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.setLength(compoundTag.getInt("length"));
    }

    @Override
    protected void addAdditionalSaveData(final CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("length", this.getLength());
    }

    public float getStepHeight(){
        return 0.0f;
    }

    @Override
    public int[] getCleatIndices() {
        return CLEATS;
    }

    @Override
    public float getCleatMovementMultiplier() {
        return 0;
    }
}

