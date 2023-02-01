package ht.treechop.common;

import ht.treechop.TreeChop;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import ht.treechop.common.capabilities.ChopSettingsProvider;
import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.config.ConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = TreeChop.MOD_ID, bus = EventBusSubscriber.Bus.FORGE)
public class ForgeCommon {

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        ConfigHandler.updateTags();
    }

    @SubscribeEvent
    public static void onBreakEvent(BlockEvent.BreakEvent event) {
        if (event.isCanceled()
                || !(event.getWorld() instanceof ServerLevel level)
                || !(event.getPlayer() instanceof ServerPlayer agent)) {
            return;
        }

        ItemStack tool = event.getPlayer().getMainHandItem();
        BlockState blockState = event.getState();
        BlockPos pos = event.getPos();
        if (ChopUtil.chop(agent, level, pos, blockState, tool, event)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        final ResourceLocation loc = TreeChop.resource("chop_settings_capability");

        try {
            if (!event.getCapabilities().containsKey(loc)) {
                if (event.getObject() instanceof FakePlayer) {
                    event.addCapability(loc, new ChopSettingsProvider(ConfigHandler.fakePlayerChopSettings.get()));
                } else {
                    event.addCapability(loc, new ChopSettingsProvider());
                }
            }
        } catch (IllegalStateException e) {
            // Ignore, Forge is being a pieca junk
        }
    }

    @SubscribeEvent
    public void registerCaps(RegisterCapabilitiesEvent event) {
        event.register(ChopSettingsCapability.class);
    }

}
