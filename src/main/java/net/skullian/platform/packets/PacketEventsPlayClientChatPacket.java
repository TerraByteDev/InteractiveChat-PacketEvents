package net.skullian.platform.packets;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import com.loohp.interactivechat.platform.packets.PlatformPlayClientChatPacket;

public class PacketEventsPlayClientChatPacket extends PlatformPlayClientChatPacket<PacketWrapper<?>> {

    public PacketEventsPlayClientChatPacket(PacketWrapper<?> handle) {
        super(handle);
    }

    @Override
    public PacketEventsPlayClientChatPacket shallowClone() {
        WrapperPlayClientChatMessage message = (WrapperPlayClientChatMessage) handle;
        WrapperPlayClientChatMessage clone;
        if (message.getLastSeenMessages() == null) {
            clone = new WrapperPlayClientChatMessage(message.getMessage(), message.getMessageSignData().orElse(null), message.getLegacyLastSeenMessages());
        } else {
            clone = new WrapperPlayClientChatMessage(message.getMessage(), message.getMessageSignData().orElse(null), message.getLastSeenMessages());
        }
        return new PacketEventsPlayClientChatPacket(clone);
    }

    @Override
    public String getMessage() {
        return ((WrapperPlayClientChatMessage) handle).getMessage();
    }

}
