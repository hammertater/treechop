package ht.treechop;

import ht.treechop.forge.ForgeEventHandler;
import ht.treechop.init.ModBlocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("treechop")
public class TreeChopMod
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "treechop";

    public TreeChopMod() {
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandler());
        ModBlocks.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        MinecraftForge.EVENT_BUS.register(this);
    }
}
