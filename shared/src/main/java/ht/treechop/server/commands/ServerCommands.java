package ht.treechop.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import ht.treechop.TreeChopException;
import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.chop.FellTreeResult;
import ht.treechop.common.util.LevelUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public class ServerCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("treechop")
                .requires(source -> source.hasPermission(2));

        builder.then(Commands.literal("chop")
                .then(Commands.argument("chopPos", BlockPosArgument.blockPos())
                        .then(Commands.argument("chopCount", IntegerArgumentType.integer(0))
                                .executes(ServerCommands::chop))));

        dispatcher.register(builder);
    }

    private static int chop(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        BlockPos pos = context.getArgument("chopPos", Coordinates.class).getBlockPos(source);
        int numChops = context.getArgument("chopCount", Integer.class);

        try {
            boolean felled = !ChopUtil.chop(
                    source.getPlayer(),
                    source.getLevel(),
                    pos,
                    source.getLevel().getBlockState(pos),
                    ItemStack.EMPTY,
                    context,
                    numChops,
                    true,
                    false
            );

            if (felled) {
                LevelUtil.harvestBlock(source.getPlayer(), source.getLevel(), pos, ItemStack.EMPTY);
            }
        } catch (TreeChopException e) {
            // ignore
        }

        return 1;
    }
}
