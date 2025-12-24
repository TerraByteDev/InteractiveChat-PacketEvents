package net.skullian.platform.packets;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCustomChatCompletions;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerCustomChatCompletionPacket;

public class PacketEventsPlayServerCustomChatCompletionPacket extends PlatformPlayServerCustomChatCompletionPacket<PacketWrapper<?>> {

    public PacketEventsPlayServerCustomChatCompletionPacket(PacketWrapper<?> handle) {
        super(handle);
    }

    @Override
    public PacketEventsPlayServerCustomChatCompletionPacket shallowClone() {
        WrapperPlayServerCustomChatCompletions completions = (WrapperPlayServerCustomChatCompletions) handle;
        return new PacketEventsPlayServerCustomChatCompletionPacket(new WrapperPlayServerCustomChatCompletions(completions.getAction(), completions.getEntries()));
    }

}
