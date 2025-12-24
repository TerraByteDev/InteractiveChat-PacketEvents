package net.skullian.platform.packets;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerSystemChatPacket;

public class PacketEventsPlayServerSystemChatPacket extends PlatformPlayServerSystemChatPacket<PacketWrapper<?>> {

    public PacketEventsPlayServerSystemChatPacket(PacketWrapper<?> handle) {
        super(handle);
    }

    @SuppressWarnings("deprecation")
    @Override
    public PacketEventsPlayServerSystemChatPacket shallowClone() {
        if (handle instanceof WrapperPlayServerSystemChatMessage) {
            WrapperPlayServerSystemChatMessage message = (WrapperPlayServerSystemChatMessage) handle;
            if (message.getType() == null) {
                return new PacketEventsPlayServerSystemChatPacket(new WrapperPlayServerSystemChatMessage(message.isOverlay(), message.getMessageJson()));
            } else {
                return new PacketEventsPlayServerSystemChatPacket(new WrapperPlayServerSystemChatMessage(message.getType(), message.getMessageJson()));
            }
        } else {
            WrapperPlayServerChatMessage message = (WrapperPlayServerChatMessage) handle;
            return new PacketEventsPlayServerSystemChatPacket(new WrapperPlayServerChatMessage(message.getMessage()));
        }
    }

}
