package net.minecraft.network.protocol.ping;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceLocation;

public class PingPacketTypes {
    public static final PacketType<ClientboundPongResponsePacket> CLIENTBOUND_PONG_RESPONSE = createClientbound("pong_response");
    public static final PacketType<ServerboundPingRequestPacket> SERVERBOUND_PING_REQUEST = createServerbound("ping_request");

    private static <T extends Packet<ClientPongPacketListener>> PacketType<T> createClientbound(String p_320541_) {
        return new PacketType<>(PacketFlow.CLIENTBOUND, new ResourceLocation(p_320541_));
    }

    private static <T extends Packet<ServerPingPacketListener>> PacketType<T> createServerbound(String p_320332_) {
        return new PacketType<>(PacketFlow.SERVERBOUND, new ResourceLocation(p_320332_));
    }
}
