package com.alekiponi.firmaciv.util;

import com.alekiponi.firmaciv.common.item.AbstractNavItem;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.common.commands.TreeCommand;
import net.dries007.tfc.world.feature.tree.TFCTreeGrower;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraftforge.server.command.EnumArgument;

import java.util.Objects;

public class NavigationCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("navigation")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .then(Commands.argument("type", EnumArgument.enumArgument(AbstractNavItem.NavType.class))
                                .executes(context -> AbstractNavItem.outputCoordinate(BlockPosArgument.getBlockPos(context, "pos").getCenter(), Objects.requireNonNull(context.getSource().getPlayer()), context.getArgument("type", AbstractNavItem.NavType.class)))
                        )
                );
    }

}
