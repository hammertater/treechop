package ht.treechop;

import ht.treechop.common.Common;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.network.PacketHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = TreeChopMod.MOD_ID, name = TreeChopMod.MOD_NAME, version = TreeChopMod.VERSION)
public class TreeChopMod {
    public static final String MOD_ID = "treechop";
    public static final String MOD_NAME = "HT's TreeChop";
    public static final String VERSION = "0.10";

    public static Logger LOGGER; // Pretend this is final

    @SidedProxy(clientSide = "ht.treechop.client.Client", serverSide = "ht.treechop.common.Common")
    public static Common proxy;

    public TreeChopMod() {
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
        event.getModMetadata().version = VERSION;
        ConfigHandler.load(event.getSuggestedConfigurationFile());

        proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        PacketHandler.init();
    }
}
