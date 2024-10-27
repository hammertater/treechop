//package ht.treechop.compat;
//
//import ht.treechop.TreeChop;
//import ht.treechop.api.ChopEvent;
//import ht.treechop.common.config.ConfigHandler;
//import net.minecraft.core.*;
//import net.minecraft.core.registries.Registries;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.enchantment.Enchantment;
//import net.minecraftforge.common.MinecraftForge;
//import net.minecraftforge.common.util.FakePlayer;
//import net.minecraftforge.eventbus.api.EventPriority;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.ModList;
//import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
//import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
//
//import java.util.Optional;
//
//@EventBusSubscriber(modid = TreeChop.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
//public class Apotheosis {
//
//    @SubscribeEvent
//    public static void commonSetup(FMLCommonSetupEvent event) {
//        if (ConfigHandler.COMMON.compatForApotheosis.get() && ModList.get().isLoaded("apotheosis")) {
//            MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, Apotheosis::onChop);
//        }
//    }
//
//    public static void onChop(ChopEvent.StartChopEvent event) {
//        ResourceLocation key = ResourceLocation.fromNamespaceAndPath("apotheosis", "chainsaw");
//
//        Optional<Registry<Enchantment>> registry = event.getLevel().registryAccess().registry(Registries.ENCHANTMENT);
//        registry.flatMap(enchantments -> enchantments.getHolder(key)).flatMap(chainsaw -> {
//            ItemStack tool = event.getPlayer().getMainHandItem();
//
//            if (tool.getEnchantments().getLevel(chainsaw) > 0) {
//                if (event.getPlayer() instanceof FakePlayer) {
//                    event.setCanceled(true);
//                } else {
//                    event.setNumChops(100);
//                }
//            }
//        })
//    });
//    }
//}
