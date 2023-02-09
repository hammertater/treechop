package ht.treechop.compat;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ObjectType;
import ht.treechop.TreeChop;
import ht.treechop.api.ChopEvent;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.block.ChoppedLogBlock.MyEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

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
                MinecraftForge.EVENT_BUS.addListener(ProjectMMO::awardXPOnChop);
            } catch (NoSuchMethodError e) {
                crank(e);
            }
        }
    }

    private static void crank(NoSuchMethodError e) {
        if (!broke) {
            TreeChop.LOGGER.error("Something went wrong with Project MMO compatibility!");
            e.printStackTrace();
            broke = true;
        }
    }

    public static void awardXPOnChop(ChopEvent.FinishChopEvent event) {
        try {
            Map<String, Long> awardMap = APIUtils.getXpAwardMap(event.getLevel(), event.getChoppedBlockPos(), EventType.BLOCK_BREAK, event.getPlayer());
            awardMap.forEach((skill, amount) -> APIUtils.addXp(skill, event.getPlayer(), amount));
        } catch (NoSuchMethodError e) {
            crank(e);
        }
    }

    /**<p>When invoked, this method confirms the {@link BlockEntity} being supplied is a
     * treechop {@link ChoppedLogBlock} and obtains the original log type. The original
     * type is then used to grab that block's default configuration, which is returned.</p>
     * <p>This behavior ensures that any configuration tied to the "treechop:chopped_log"
     * block is ignored in favor of the output of this function.</p>
     *
     * @return a default configuration setting from the original log block
     */
    @SuppressWarnings("DataFlowIssue")
    private static final Function<BlockEntity, Map<String, Long>> TREE_XP = (blockEntityIn) -> {
        try {
            if (blockEntityIn instanceof MyEntity choppedLog && choppedLog.getLevel() != null) {
                ResourceLocation resource = Optional
                        .ofNullable(ForgeRegistries.BLOCKS.getKey(choppedLog.getOriginalState().getBlock()))
                        .orElse(ForgeRegistries.BLOCKS.getKey(Blocks.OAK_LOG));

                return APIUtils.getXpAwardMap(ObjectType.BLOCK, EventType.BLOCK_BREAK, resource, choppedLog.getLevel().isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER, null);
            }
        } catch (NoSuchMethodError e) {
            crank(e);
        }
        return new HashMap<>();
    };
}