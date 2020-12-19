package ht.treechop.common.init;

import ht.treechop.TreeChopMod;
import ht.treechop.common.block.ChoppedLogBlock;
import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@GameRegistry.ObjectHolder(TreeChopMod.MOD_ID)
@Mod.EventBusSubscriber(modid = TreeChopMod.MOD_ID)
public class ModBlocks {
    public static final ChoppedLogBlock CHOPPED_LOG = new ChoppedLogBlock();

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> reg = event.getRegistry();
        reg.register(CHOPPED_LOG);
    }

}
