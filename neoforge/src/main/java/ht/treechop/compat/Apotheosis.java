package ht.treechop.compat;

import ht.treechop.TreeChop;
import ht.treechop.api.ChopEvent;
import ht.treechop.common.config.ConfigHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;

@EventBusSubscriber(modid = TreeChop.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class Apotheosis {
    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        if (ConfigHandler.COMMON.compatForApotheosis.get() && ModList.get().isLoaded("apothic_enchanting")) {
            NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, Apotheosis::onChop);
        }
    }

    public static void onChop(ChopEvent.StartChopEvent event) {
        final ResourceKey<Enchantment> chainsaw_key = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("apothic_enchanting", "chainsaw"));
        ItemStack tool = event.getPlayer().getMainHandItem();

        event.getLevel().registryAccess().lookup(Registries.ENCHANTMENT)
                .flatMap(reg -> reg.get(chainsaw_key))
                .ifPresent(chainsaw -> {
                    if (tool.getEnchantmentLevel(chainsaw) > 0) {
                        if (event.getPlayer() instanceof FakePlayer) {
                            event.setCanceled(true);
                        } else {
                            event.setNumChops(100);
                        }
                    }
                });
    }
}