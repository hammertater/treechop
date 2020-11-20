package ht.treechop.init;

import ht.treechop.TreeChopMod;
import ht.treechop.block.ChoppedLog;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TreeChopMod.MOD_ID);

    // Blocks
    public static final RegistryObject<Block> CHOPPED_LOG = BLOCKS.register("chopped_log",
            () -> new ChoppedLog(
                    AbstractBlock.Properties.create(
                            Material.WOOD,
                            MaterialColor.WOOD)
                            .hardnessAndResistance(2.0F)
                            .sound(SoundType.WOOD))
    );
//    public static final RegistryObject<Block> STRESSED_CHOPPED_LOG = BLOCKS.register("stressed_chopped_log", StressedChoppedLog::new);

}
