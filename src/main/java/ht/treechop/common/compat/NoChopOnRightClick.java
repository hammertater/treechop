package ht.treechop.common.compat;

import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashSet;
import java.util.Set;

public class NoChopOnRightClick {

    private static final Set<Entity> playersCurrentlyClicking = new HashSet<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockStartClick(PlayerInteractEvent.RightClickBlock event) {
        playersCurrentlyClicking.add(event.getPlayer());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockFinishClick(PlayerInteractEvent.RightClickBlock event) {
        playersCurrentlyClicking.remove(event.getPlayer());
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(NoChopOnRightClick.class);
        Compat.chopChecks.add(event -> !isPlayerRightClicking(event));
    }

    private static boolean isPlayerRightClicking(BlockEvent.BreakEvent event) {
        return playersCurrentlyClicking.contains(event.getPlayer());
    }
}
