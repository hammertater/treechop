package ht.treechop.common.compat;

import ht.treechop.TreeChopMod;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = TreeChopMod.MOD_ID)
public class Compat {

    public static final List<Predicate<BlockEvent.BreakEvent>> chopChecks = new ArrayList<>();

    public static void init() {
        NoChopOnRightClick.init();
    }

    public static boolean canChop(BlockEvent.BreakEvent event) {
        return chopChecks.stream().allMatch(check -> check.test(event));
    }

}
