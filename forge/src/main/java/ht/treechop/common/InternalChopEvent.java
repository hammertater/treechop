package ht.treechop.common;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public class InternalChopEvent extends Event {

    private final Level level;
    private final Player player;
    private final BlockPos choppedBlockPos;

    public InternalChopEvent(Level level, Player player, BlockPos choppedBlockPos) {
        this.level = level;
        this.player = player;
        this.choppedBlockPos = choppedBlockPos;
    }

    public Level getLevel() {
        return level;
    }

    public Player getPlayer() {
        return player;
    }

    public BlockPos getChoppedBlockPos() {
        return choppedBlockPos;
    }

    @Cancelable
    public static class PreChopEvent extends InternalChopEvent {
        public PreChopEvent(Level level, ServerPlayer player, BlockPos blockPos) {
            super(level, player, blockPos);
        }
    }
}
