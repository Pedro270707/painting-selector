package net.pedroricardo.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.pedroricardo.PaintingSelector;

public record PaintingSelectorSyncPacket() implements CustomPayload {
    public static final CustomPayload.Id<PaintingSelectorSyncPacket> PACKET_ID = new CustomPayload.Id<>(new Identifier(PaintingSelector.MOD_ID, "sync_client"));
    public static final PacketCodec<PacketByteBuf, PaintingSelectorSyncPacket> CODEC = PacketCodec.unit(new PaintingSelectorSyncPacket());

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}