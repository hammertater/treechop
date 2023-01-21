package ht.treechop.compat;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ObjectType;
import ht.treechop.TreeChop;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.block.ChoppedLogBlock.MyEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
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

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        if (ModList.get().isLoaded("pmmo")) {
        	//This registers custom configuration behavior for the block which is checked before a standard configuration
            APIUtils.registerBlockXpGainTooltipData(TreeChop.resource("chopped_log"), EventType.BLOCK_BREAK, TREE_XP);
        }
    }
    
    /**<p>When invoked, this method confirms the {@link BlockEntity} being supplied is a
     * treechop {@link ChoppedLogBlock} and obtains the original log type.  The original
     * type is then used to grab that block's default configuration, which is returned.</p>
     * <p>This behavior ensures that any configuration tied to the "treechop:chopped_log"
     * block is ignored in favor of the output of this function.</p> 
     * 
     * @return a default configuration setting from the original log block
     */
    @SuppressWarnings("resource") //for getLevel call on choppedLog
	private static final Function<BlockEntity, Map<String, Long>> TREE_XP = (blockEntityIn) -> {
    	if (blockEntityIn instanceof ChoppedLogBlock.MyEntity) {
    		ChoppedLogBlock.MyEntity choppedLog = (MyEntity) blockEntityIn;
    		ResourceLocation resource = Optional
    				.ofNullable(ForgeRegistries.BLOCKS.getKey(choppedLog.getOriginalState().getBlock()))
    				.orElse(ForgeRegistries.BLOCKS.getKey(Blocks.OAK_LOG));
    		
    		return APIUtils.getXpAwardMap(ObjectType.BLOCK, EventType.BLOCK_BREAK, resource, choppedLog.getLevel().isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER);
    	}
    	return new HashMap<>();
    };
}