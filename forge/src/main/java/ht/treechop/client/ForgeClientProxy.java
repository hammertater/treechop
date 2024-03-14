package ht.treechop.client;

import ht.treechop.client.model.ForgeChoppedLogBakedModel;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ForgeClientProxy {
    public static void init() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(ForgeChoppedLogBakedModel::overrideBlockStateModels);
    }
}
