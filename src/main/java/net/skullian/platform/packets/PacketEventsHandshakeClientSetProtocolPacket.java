package net.skullian.platform.packets;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.handshaking.client.WrapperHandshakingClientHandshake;
import com.loohp.interactivechat.platform.packets.PlatformHandshakeClientSetProtocolPacket;

public class PacketEventsHandshakeClientSetProtocolPacket extends PlatformHandshakeClientSetProtocolPacket<PacketWrapper<?>> {

    public PacketEventsHandshakeClientSetProtocolPacket(PacketWrapper<?> handle) {
        super(handle);
    }

    @Override
    public PacketEventsHandshakeClientSetProtocolPacket shallowClone() {
        WrapperHandshakingClientHandshake handshake = (WrapperHandshakingClientHandshake) handle;
        return new PacketEventsHandshakeClientSetProtocolPacket(new WrapperHandshakingClientHandshake(handshake.getProtocolVersion(), handshake.getServerAddress(), handshake.getServerPort(), handshake.getIntention()));
    }

    @Override
    public String getServerAddress() {
        return ((WrapperHandshakingClientHandshake) handle).getServerAddress();
    }
}
