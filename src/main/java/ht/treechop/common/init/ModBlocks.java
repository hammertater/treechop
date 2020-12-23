package ht.treechop.common.init;

import ht.treechop.TreeChopMod;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.properties.ChoppedLogShape;
import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.stream.Collectors;

//@GameRegistry.ObjectHolder(TreeChopMod.MOD_ID)
@Mod.EventBusSubscriber(modid = TreeChopMod.MOD_ID)
public class ModBlocks {

    public static final EnumMap<ChoppedLogShape, ChoppedLogBlock> CHOPPED_LOGS = new EnumMap<ChoppedLogShape, ChoppedLogBlock>(
            Arrays.stream(ChoppedLogShape.values())
                    .collect(Collectors.toMap(shape->shape, ChoppedLogBlock::new))
    );

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> reg = event.getRegistry();
        for (ChoppedLogBlock choppedLogBlock : CHOPPED_LOGS.values()) {
            reg.register(choppedLogBlock);
        }
    }

}
