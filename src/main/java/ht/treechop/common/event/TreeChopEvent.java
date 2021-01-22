package ht.treechop.common.event;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public class TreeChopEvent extends Event {

    @Cancelable
    public static class ChopEvent extends TreeChopEvent {
    }

    @Cancelable
    public static class FellEvent extends TreeChopEvent {
    }

}
