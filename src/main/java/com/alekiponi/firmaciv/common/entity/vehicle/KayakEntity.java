package com.alekiponi.firmaciv.common.entity.vehicle;

import com.alekiponi.alekiships.common.entity.vehicle.AbstractAlekiBoatEntity;
import com.alekiponi.alekiships.common.entity.vehiclecapability.IAllowFallDamage;
import com.alekiponi.alekiships.common.entity.compartment.AbstractCompartmentEntity;
import com.alekiponi.firmaciv.Firmaciv;
import com.alekiponi.firmaciv.common.item.FirmacivItems;
import com.alekiponi.firmaciv.util.Helper;
import com.alekiponi.firmaciv.util.KayakMaterial;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;

import static net.neoforged.neoforge.items.ItemHandlerHelper.giveItemToPlayer;

public class KayakEntity extends FirmacivPaddledBoatEntity implements IAllowFallDamage {
    public final int PASSENGER_NUMBER = 1;
    protected final float DAMAGE_THRESHOLD = 10.0f;
    protected final float DAMAGE_RECOVERY = 1.0f;
    public final int[] COMPARTMENTS = {0};
    protected final float PASSENGER_SIZE_LIMIT = 0.6F;
    protected final float HUNGER_FACTOR = 0.02F;

    public KayakEntity(final EntityType<? extends KayakEntity> entityType, final Level level) {
        super(entityType, level, "kayak");
    }

    @Override
    public float getPassengerSizeLimit() {
        return this.PASSENGER_SIZE_LIMIT;
    }

    @Override
    public int[] getCompartmentIndices() {
        return COMPARTMENTS;
    }

    @Override
    public int[][] getCompartmentRotationsArray() {
        return new int[0][];
    }

    @Nullable
    @Override
    public Entity getPilotVehiclePartAsEntity() {
        if (this.isVehicle() && this.getPassengers().size() == this.getMaxPassengers()) {
            return this.getPassengers().get(0);
        }

        return null;
    }

    @Override
    public void tick() {

        if (this.getDamage() > 0.0F) {
            this.setDamage(this.getDamage() - getDamageRecovery());
        }

        Helper.causeExhaustionWhilePaddlingRowing(this, HUNGER_FACTOR);

        super.tick();
    }

    @Override
    protected double windDriftMultiplier() {
        return 0.3;
    }

    @Override
    public boolean hurt(final DamageSource damageSource, final float amount) {
        if (this.isInvulnerableTo(damageSource)) return false;

        if (this.level().isClientSide || this.isRemoved()) return true;

        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        this.setDamage(this.getDamage() + amount * 10.0F);
        this.markHurt();
        this.gameEvent(GameEvent.ENTITY_DAMAGE, damageSource.getEntity());
        final boolean instantKill = damageSource.getEntity() instanceof Player && ((Player) damageSource.getEntity()).getAbilities().instabuild;

        if (instantKill || this.getDamage() > getDamageThreshold()) {
            if (!instantKill && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                if (damageSource.getEntity() instanceof Player player) {
                    giveItemToPlayer(player, this.getDropItem().getDefaultInstance());
                } else {
                    this.spawnAtLocation(this.getDropItem());
                }
            }
            this.discard();
        }

        return true;
    }

    @Override
    public AbstractCompartmentEntity.RidingPose[] getRidingPoses() {
        if (ridingPoses.length == 0 || ridingPoses[0] == null) {
            AbstractCompartmentEntity.RidingPose[] poses = new AbstractCompartmentEntity.RidingPose[this.getMaxPassengers()];
            poses[0] = AbstractCompartmentEntity.RidingPose.ULTRA_COMPACT;
            this.ridingPoses = poses;
        }
        return ridingPoses;
    }

    @Override
    public InteractionResult interact(final Player player, final InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public int getMaxPassengers() {
        return this.PASSENGER_NUMBER;
    }

    @Override
    public int[] getColliderIndices() {
        return new int[0];
    }

    @Override
    public int getCompartmentRotation(int i) {
        return 0;
    }

    protected Vec3 positionRiderByIndex(int index) {
        float localX = 0.0F;
        float localZ = 0.0F;
        float localY = (float) ((this.isRemoved() ? (double) 0.01F : this.getPassengersRidingOffset()));
        return new Vec3(localX, localY, localZ);
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
    protected float getPaddleMultiplier() {
        float paddleMultiplier = 1.0f;
        if (this.getControllingPassenger() instanceof Player player) {
            if (player.isHolding(FirmacivItems.KAYAK_PADDLE.get())) {
                paddleMultiplier = 2.0f;
            }
        }
        return paddleMultiplier;
    }

    @Override
    protected float getMomentumSubtractor() {
        return 0.000f;
    }

    public Item getDropItem() {
        return FirmacivItems.KAYAK.get();
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(FirmacivItems.KAYAK.get());
    }

    public float getStepHeight() {
        return 0.6f;
    }
}