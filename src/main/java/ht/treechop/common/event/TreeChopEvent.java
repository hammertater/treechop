package ht.treechop.common.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public class TreeChopEvent extends Event {

    private final World world;
    private final PlayerEntity player;

    public TreeChopEvent(World world, PlayerEntity player) {
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
    public static class ChopEvent extends TreeChopEvent {
        public ChopEvent(World world, PlayerEntity player) {
            super(world, player);
        }
    }

    @Cancelable
    public static class FellEvent extends TreeChopEvent {
        public FellEvent(World world, PlayerEntity player) {
            super(world, player);
        }
    }

}
