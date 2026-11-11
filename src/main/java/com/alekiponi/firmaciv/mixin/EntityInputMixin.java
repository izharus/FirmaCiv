package com.alekiponi.firmaciv.mixin;

import com.alekiponi.alekiships.common.entity.EntityInput;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Ships 2.0.0-dev loses the held stack and reads beyond the final ingredient on insertion. */
@Mixin(value = EntityInput.class, remap = false)
public abstract class EntityInputMixin {
    @Inject(method = "tryInsert", at = @At("HEAD"), cancellable = true)
    private void firmaciv$insertOne(EntityInput.InputEntity target, ItemStack stack, @Nullable LivingEntity player,
            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        var state = target.getInputState();
        if (!state.entityInputKey().location().getNamespace().equals("firmaciv")) return;
        var input = (EntityInput) (Object) this;
        int stage = state.inputStage();
        if (input.isFinished(target) || !input.getIngredient(stage).ingredient().test(stack)) {
            cir.setReturnValue(InteractionResultHolder.pass(stack));
            return;
        }
        var rejected = target.insert(stage, stack.copyWithCount(1));
        if (!rejected.isEmpty()) {
            cir.setReturnValue(InteractionResultHolder.pass(stack));
            return;
        }
        int remaining = input.getIngredient(stage).count() - target.getContentsCount(stage);
        if (remaining <= 0) {
            stage++;
            remaining = stage < input.getTotalInputs() ? input.getIngredient(stage).count() : 0;
        }
        target.setInputState(new EntityInput.EntityInputState(state.entityInputKey(), stage, remaining));
        var remainder = stack.copy();
        if (player == null || !player.hasInfiniteMaterials()) remainder.shrink(1);
        cir.setReturnValue(InteractionResultHolder.success(remainder));
    }
}
