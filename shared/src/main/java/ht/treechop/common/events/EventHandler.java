package ht.treechop.common.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class EventHandler {
    protected static EventHandler INSTANCE;

    public static EventHandler get() {
        return INSTANCE;
    }

}
