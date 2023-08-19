package ht.treechop.common.chop;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

import java.util.Collection;

public class ChopTreeResult implements ChopResult {
    private final Level level;
    private final Collection<Chop> chops;

    public ChopTreeResult(Level level, Collection<Chop> chops) {
        this.level = level;
        this.chops = chops;
    }

    @Override
    public void apply(BlockPos targetPos, ServerPlayer agent, ItemStack tool, boolean breakLeaves) {
        GameType gameType = agent.gameMode.getGameModeForPlayer();
        if (level.getBlockState(targetPos).isAir() || agent.blockActionRestricted(level, targetPos, gameType)) {
            return;
        }

        chops.forEach(chop -> chop.apply(level, agent, tool, false));
    }
}
