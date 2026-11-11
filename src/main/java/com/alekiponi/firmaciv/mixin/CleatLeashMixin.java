package com.alekiponi.firmaciv.mixin;

import com.alekiponi.alekiships.common.entity.vehiclehelper.AbstractPassthroughHelper;
import com.alekiponi.alekiships.common.entity.vehiclehelper.CleatEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Restore the lead interaction lost when Ships moved its cleats from Mob to Leashable. */
@Mixin(value = CleatEntity.class, remap = false)
public abstract class CleatLeashMixin extends AbstractPassthroughHelper implements Leashable {
    protected CleatLeashMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (getLeashHolder() == player) {
            if (!level().isClientSide) {
                dropLeash(true, !player.hasInfiniteMaterials());
                playSound(SoundEvents.LEASH_KNOT_BREAK);
                gameEvent(GameEvent.ENTITY_INTERACT, player);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (player.getItemInHand(hand).is(Items.LEAD) && canHaveALeashAttachedToIt()) {
            if (!level().isClientSide) {
                setLeashedTo(player, true);
                if (!player.hasInfiniteMaterials()) player.getItemInHand(hand).shrink(1);
                playSound(SoundEvents.LEASH_KNOT_PLACE);
                gameEvent(GameEvent.ENTITY_INTERACT, player);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.interact(player, hand);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"), cancellable = true)
    private void firmaciv$saveLeash(CompoundTag tag, CallbackInfo ci) {
        writeLeashData(tag, getLeashData());
        ci.cancel();
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"), cancellable = true)
    private void firmaciv$loadLeash(CompoundTag tag, CallbackInfo ci) {
        setLeashData(readLeashData(tag));
        ci.cancel();
    }
}
