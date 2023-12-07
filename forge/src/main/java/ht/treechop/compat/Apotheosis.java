package ht.treechop.compat;

import ht.treechop.TreeChop;
import ht.treechop.api.ChopEvent;
import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.settings.ChopSettings;
import ht.treechop.common.util.TickUtil;
import ht.treechop.server.Server;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = TreeChop.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class Apotheosis {


    private static Enchantment chainsaw;

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        if (ConfigHandler.COMMON.compatForApotheosis.get() && ModList.get().isLoaded("apotheosis")) {
            chainsaw = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("apotheosis", "chainsaw"));
            if (chainsaw != null) {
                MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, Apotheosis::onChop);
            }
        }
    }

    public static void onChop(ChopEvent.StartChopEvent event) {
        ItemStack tool = event.getPlayer().getMainHandItem();
        if (tool.getEnchantmentLevel(chainsaw) > 0) {
            if (event.getPlayer() instanceof FakePlayer) {
                event.setCanceled(true);
            } else {
                event.setNumChops(100);
            }
        }
    }
}
