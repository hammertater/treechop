package ht.treechop.common.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public class ChopEvent extends Event {

    private final World world;
    private final PlayerEntity player;

    public ChopEvent(World world, PlayerEntity player) {
        this.world = world;
        this.player = player;
    }

    public World getWorld() {
        return world;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    @Cancelable
    public static class StartChopEvent extends ChopEvent {
        public StartChopEvent(World world, PlayerEntity player) {
            super(world, player);
        }
    }

    public static class FinishChopEvent extends ChopEvent {
        public FinishChopEvent(World world, PlayerEntity player) {
            super(world, player);
        }
    }

    @Cancelable
    public static class FellEvent extends ChopEvent {
        public FellEvent(World world, PlayerEntity player) {
            super(world, player);
        }
    }

}
