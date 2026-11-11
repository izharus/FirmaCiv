package com.alekiponi.firmaciv.common.entity.vehicle;

import com.alekiponi.alekiships.common.entity.vehicle.SloopUnderConstructionEntity;
import com.alekiponi.firmaciv.util.BoatMaterial;
import com.alekiponi.firmaciv.common.item.FirmacivItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import oshi.util.tuples.Pair;

public class FirmacivSloopUnderConstructionEntity extends SloopUnderConstructionEntity {
    public FirmacivSloopUnderConstructionEntity(
            final EntityType<? extends FirmacivSloopUnderConstructionEntity> entityType, final Level level,
            final BoatMaterial boatMaterial) {
        super(entityType, level);
        this.setVariant(level.registryAccess().registryOrThrow(com.alekiponi.alekiships.common.AlekiShipsRegistries.CONSTRUCTION_SLOOP_VARIANT)
            .getHolderOrThrow(net.minecraft.resources.ResourceKey.create(com.alekiponi.alekiships.common.AlekiShipsRegistries.CONSTRUCTION_SLOOP_VARIANT,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("firmaciv", boatMaterial.getSerializedName()))));
    }

    /** Preserve the remainder of a stack when inserting one construction material (Ships 2.0.0-dev). */
    @Override
    public net.minecraft.world.InteractionResult interactFromConstructionEntity(
            net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        if (isFinished()) return net.minecraft.world.InteractionResult.PASS;
        var state = getConstructionState();
        var stage = state.inputStage();
        var input = getConstructionInput().value();
        var ingredient = input.getIngredient(stage);
        var held = player.getItemInHand(hand);
        if (!ingredient.ingredient().test(held)) return net.minecraft.world.InteractionResult.PASS;
        if (level().isClientSide) return net.minecraft.world.InteractionResult.SUCCESS;
        if (!insert(stage, held.copyWithCount(1)).isEmpty()) return net.minecraft.world.InteractionResult.PASS;
        if (!player.hasInfiniteMaterials()) held.shrink(1);
        int remaining = ingredient.count() - getContentsCount(stage);
        setConstructionState(remaining > 0 ? state.withRemaining(remaining) : state.nextStateOf(input));
        playSound(input.getProgressSound(stage));
        var nextStage = getConstructionState().inputStage();
        if (stage != nextStage) progressConstructionStage(input, stage, nextStage);
        if (isFinished()) finalizeConstruction(player, input);
        return net.minecraft.world.InteractionResult.SUCCESS;
    }

}
