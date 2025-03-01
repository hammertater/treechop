package ht.treechop.compat;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ObjectType;
import ht.treechop.TreeChop;
import ht.treechop.api.ChopEvent;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.block.ChoppedLogBlock.MyEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@EventBusSubscriber(modid = TreeChop.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ProjectMMO {

    private static boolean broke = false;

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        if (ModList.get().isLoaded("pmmo")) {
            try {
                //This registers custom configuration behavior for the block which is checked before a standard configuration
                APIUtils.registerBlockXpGainTooltipData(TreeChop.resource("chopped_log"), EventType.BLOCK_BREAK, TREE_XP);
                NeoForge.EVENT_BUS.addListener(ProjectMMO::awardXPOnChop);
                Class.forName("harmonised.pmmo.api.APIUtils");
                Class.forName("harmonised.pmmo.api.enums.EventType");
                Class.forName("harmonised.pmmo.api.enums.ObjectType");
            } catch (NoSuchMethodError | ClassNotFoundException e) {
                crank(e);
            }
        }
    }

    private static void crank(Throwable e) {
        if (!broke) {
            TreeChop.LOGGER.error("Something went wrong with Project MMO compatibility!");
            e.printStackTrace();
            broke = true;
        }
    }

    public static void awardXPOnChop(ChopEvent.FinishChopEvent event) {
        try {
            if (!event.getFelled()) {
                Map<String, Long> awardMap = APIUtils.getXpAwardMap(event.getLevel(), event.getChoppedBlockPos(), EventType.BLOCK_BREAK, event.getPlayer());
                awardMap.forEach((skill, amount) -> APIUtils.addXp(skill, event.getPlayer(), amount));
            }
        } catch (NoSuchMethodError e) {
            crank(e);
        }
    }

    /**
     * <p>When invoked, this method confirms the {@link BlockEntity} being supplied is a
     * treechop {@link ChoppedLogBlock} and obtains the original log type. The original
     * type is then used to grab that block's default configuration, which is returned.</p>
     * <p>This behavior ensures that any configuration tied to the "treechop:chopped_log"
     * block is ignored in favor of the output of this function.</p>
     *
     * @return a default configuration setting from the original log block
     */
    private static final Function<BlockEntity, Map<String, Long>> TREE_XP = (blockEntityIn) -> {
        try {
            if (blockEntityIn instanceof MyEntity choppedLog && choppedLog.getLevel() != null) {
                ResourceLocation resource = Optional
                        .of(BuiltInRegistries.BLOCK.getKey(choppedLog.getOriginalState().getBlock()))
                        .orElse(BuiltInRegistries.BLOCK.getKey(Blocks.OAK_LOG));

                return APIUtils.getXpAwardMap(
                        ObjectType.BLOCK,
                        EventType.BLOCK_BREAK,
                        resource,
                        choppedLog.getLevel().isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER,
                        null
                );
            }
        } catch (NoSuchMethodError e) {
            crank(e);
        }
        return new HashMap<>();
    };
}