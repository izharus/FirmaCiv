/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package com.alekiponi.firmaciv.util;

import java.util.function.Supplier;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraftforge.common.util.Lazy;

import net.dries007.tfc.util.Helpers;

@SuppressWarnings("unused")
public final class FirmacivCommands
{
    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context)
    {
        dispatcher.register(Commands.literal("firmaciv")
                .then(NavigationCommand.create())
        );
    }

    public static <S extends SharedSuggestionProvider> Supplier<SuggestionProvider<S>> register(String id, SuggestionProvider<SharedSuggestionProvider> provider)
    {
        return Lazy.of(() -> SuggestionProviders.register(Helpers.identifier(id), provider));
    }
}