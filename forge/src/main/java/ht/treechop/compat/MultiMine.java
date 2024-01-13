package ht.treechop.compat;

import ht.treechop.TreeChop;
import ht.treechop.api.ChopEvent;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.util.TickUtil;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = TreeChop.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class MultiMine {

    static private Long lastChopTick = TickUtil.NEVER;
    static private final Set<BlockPos> lastChops = new HashSet<>();

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        if (ConfigHandler.COMMON.compatForMultiMine.get() && ModList.get().isLoaded("multimine")) {
            MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, MultiMine::onChop);
        }
    }

    public static void onChop(ChopEvent.StartChopEvent event) {
        BlockPos pos = event.getChoppedBlockPos();
        long gameTick = event.getLevel().getGameTime();

        if (lastChopTick == gameTick) {
            if (lastChops.contains(pos)) {
                event.setCanceled(true);
                if (event.getTrigger() instanceof BlockEvent.BreakEvent breakEvent) {
                    breakEvent.setCanceled(true);
                }
            }
        } else {
            lastChops.clear();
            lastChopTick = gameTick;
        }

        lastChops.add(pos);
    }
}
