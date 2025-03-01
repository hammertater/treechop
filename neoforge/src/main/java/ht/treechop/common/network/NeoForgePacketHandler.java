package ht.treechop.common.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public abstract class NeoForgePacketHandler {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("treechop");

        registrar.playToServer(
                ClientRequestSettingsPacket.TYPE,
                ClientRequestSettingsPacket.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> payload.handle(context.player(), context::reply)
                )
        );

        registrar.playToClient(
                ServerConfirmSettingsPacket.TYPE,
                ServerConfirmSettingsPacket.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(payload::handle)
        );

        registrar.playToClient(
                ServerPermissionsPacket.TYPE,
                ServerPermissionsPacket.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(payload::handle)
        );

        registrar.playToClient(
                ServerUpdateChopsPacket.TYPE,
                ServerUpdateChopsPacket.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(payload::handle)
        );
    }
}
