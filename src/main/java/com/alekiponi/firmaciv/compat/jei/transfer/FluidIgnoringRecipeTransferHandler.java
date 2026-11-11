package com.alekiponi.firmaciv.compat.jei.transfer;

import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Copied from my TFC PR before alc closed it
 * <p>
 * Transfer handler which filters {@link #transferRecipe(AbstractContainerMenu, Object, IRecipeSlotsView, Player, boolean, boolean)}s {@link IRecipeSlotsView} param
 * to exclude any fluid. This filtered {@link IRecipeSlotsView} is then passed to the {@link #wrappedTransferHandler}
 */
public class FluidIgnoringRecipeTransferHandler<C extends AbstractContainerMenu, R> implements IRecipeTransferHandler<C, R> {
    private final IRecipeTransferHandlerHelper transferHelper;
    private final IRecipeTransferHandler<C, R> wrappedTransferHandler;

    /**
     * @param wrappedTransferHandler The {@link IRecipeTransferHandler} to wrap
     */
    public FluidIgnoringRecipeTransferHandler(final IRecipeTransferHandlerHelper transferHelper,
            final IRecipeTransferHandler<C, R> wrappedTransferHandler) {
        this.transferHelper = transferHelper;
        this.wrappedTransferHandler = wrappedTransferHandler;
    }

    @Override
    public Class<? extends C> getContainerClass() {
        return this.wrappedTransferHandler.getContainerClass();
    }

    @Override
    public Optional<MenuType<C>> getMenuType() {
        return this.wrappedTransferHandler.getMenuType();
    }

    @Override
    public RecipeType<R> getRecipeType() {
        return this.wrappedTransferHandler.getRecipeType();
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(final C container, final R recipe, final IRecipeSlotsView recipeSlots,
            final Player player, final boolean maxTransfer, final boolean doTransfer) {
        List<IRecipeSlotView> slotViews = recipeSlots.getSlotViews().stream()
                .filter(iRecipeSlotView -> iRecipeSlotView.getIngredients(NeoForgeTypes.FLUID_STACK).findAny().isEmpty())
                .toList();
        return this.wrappedTransferHandler.transferRecipe(container, recipe,
                this.transferHelper.createRecipeSlotsView(slotViews), player, maxTransfer, doTransfer);
    }
}