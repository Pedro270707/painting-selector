package net.pedroricardo.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.pedroricardo.PaintingSelector;

public record PaintingChangePacket(int slot, Identifier paintingId) implements CustomPayload {
    public static final CustomPayload.Id<PaintingChangePacket> PACKET_ID = new CustomPayload.Id<>(new Identifier(PaintingSelector.MOD_ID, "change_painting"));
    public static final PacketCodec<ByteBuf, PaintingChangePacket> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, PaintingChangePacket::slot, Identifier.PACKET_CODEC, PaintingChangePacket::paintingId, PaintingChangePacket::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
