package ht.treechop.client;

import ht.treechop.client.model.NeoForgeChoppedLogBakedModel;
import net.neoforged.bus.api.IEventBus;

public class NeoForgeClientProxy {
    public static void init(IEventBus modBus) {
        modBus.addListener(NeoForgeChoppedLogBakedModel::overrideBlockStateModels);
    }
}
