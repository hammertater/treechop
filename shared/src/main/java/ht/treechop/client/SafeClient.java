package ht.treechop.client;

import ht.treechop.TreeChop;
import ht.treechop.common.network.ServerUpdateChopsPacket;

/**
 * Expose {@link Client} methods without loading client-only code.
 */
public class SafeClient {

    public static void handleUpdateChopsPacket(ServerUpdateChopsPacket message) {
        if (safe()) {
            Client.handleUpdateChopsPacket(message.getPos(), message.getTag());
        }
    }

    private static boolean safe() {
        return !TreeChop.platform.isDedicatedServer();
    }

}
