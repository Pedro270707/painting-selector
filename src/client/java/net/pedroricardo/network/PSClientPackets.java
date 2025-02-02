package net.pedroricardo.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.pedroricardo.PaintingSelector;
import net.pedroricardo.PaintingSelectorClient;

public class PSClientPackets {
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(PaintingSelectorSyncPacket.PACKET_ID, (payload, context) -> {
            PaintingSelector.LOGGER.info("Joined server with {}", FabricLoader.getInstance().getModContainer(PaintingSelector.MOD_ID).orElseThrow(() -> new RuntimeException(PaintingSelector.MOD_ID + " is not a mod (ID changed?)")).getMetadata().getName());
            PaintingSelectorClient.inPaintingSelectorServer = true;
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> PaintingSelectorClient.inPaintingSelectorServer = false);
    }
}
