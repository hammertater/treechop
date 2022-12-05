package ht.treechop.compat;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.events.impl.BreakHandler;
import harmonised.pmmo.storage.ChunkDataHandler;
import harmonised.pmmo.storage.ChunkDataProvider;
import harmonised.pmmo.storage.IChunkData;
import ht.treechop.TreeChop;
import ht.treechop.api.ChopEvent;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.registry.ForgeModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = TreeChop.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ProjectMMO {

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        if (ConfigHandler.COMMON.compatForProjectMMO.get() && ModList.get().isLoaded("pmmo")) {
            MinecraftForge.EVENT_BUS.register(EventHandler.class);
        }
    }

    private static Map<String, Long> getOverrideXp() {
        return Collections.singletonMap("woodcutting", ConfigHandler.COMMON.pmmoOverrideXp.get());
    }

    private static Map<String, Long> getOriginalLogXp(BlockEntity entity) {
        final ResourceLocation defaultXpKey = ForgeRegistries.BLOCKS.getKey(Blocks.OAK_LOG);
        Block log = (entity instanceof ChoppedLogBlock.MyEntity choppedEntity) ? choppedEntity.getOriginalState().getBlock() : Blocks.OAK_LOG;
        ResourceLocation resource = Optional.ofNullable(ForgeRegistries.BLOCKS.getKey(log)).orElse(defaultXpKey);

        double scale = ConfigHandler.COMMON.pmmoScaleXp.get();
        return Core.get(LogicalSide.SERVER).getXpUtils().getObjectExperienceMap(EventType.BLOCK_BREAK, resource)
                .entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Math.round(entry.getValue() * scale))
                );
    }

    private static class EventHandler {
        @SubscribeEvent
        public static void onFinishChop(ChopEvent.FinishChopEvent event) {
            fixPmmoRegistration();

            clearBlockHistory(event.getPlayer(), event.getLevel(), event.getChoppedBlockPos(), event.getChoppedBlockState());

            BreakHandler.handle(new BlockEvent.BreakEvent(
                    event.getLevel(),
                    event.getChoppedBlockPos(),
                    event.getChoppedBlockState(),
                    event.getPlayer()
            ));
        }

        private static void fixPmmoRegistration() {
            if (!xpModifierIsRegistered()) {
                if (ConfigHandler.COMMON.pmmoXpMethod.get() == ProjectMMOChopXp.USE_BLOCK_XP) {
                    APIUtils.registerBlockXpGainTooltipData(TreeChop.resource("chopped_log"), EventType.BLOCK_BREAK, ProjectMMO::getOriginalLogXp);
                } else {
                    Core.get(LogicalSide.SERVER).getXpUtils().setObjectXpGainMap(EventType.BLOCK_BREAK, ForgeModBlocks.CHOPPED_LOG.getId(), getOverrideXp());
                }
            }
        }

        private static boolean xpModifierIsRegistered() {
            return Core.get(LogicalSide.SERVER).getTooltipRegistry().xpGainTooltipExists(ForgeModBlocks.CHOPPED_LOG.getId(), EventType.BLOCK_BREAK);
        }

        private static void clearBlockHistory(Player player, Level level, BlockPos pos, BlockState blockState) {
            try {
                // Copied from harmonised.pmmo.events.impl::calculateXpAward
                LevelChunk chunk = (LevelChunk) level.getChunk(pos);
                IChunkData cap = chunk.getCapability(ChunkDataProvider.CHUNK_CAP).orElseGet(ChunkDataHandler::new);
                cap.delPos(pos);
            } catch (NullPointerException e) {
                TreeChop.LOGGER.warn(String.format("Something went wrong with ProjectMMO compatibility when chopping %s for player %s", blockState, player.toString()));
            }
        }
    }

}