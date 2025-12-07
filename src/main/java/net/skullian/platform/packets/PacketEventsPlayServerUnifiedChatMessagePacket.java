package net.skullian.platform.packets;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.loohp.interactivechat.libs.net.kyori.adventure.text.Component;
import com.loohp.interactivechat.listeners.packet.MessagePacketHandler;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerUnifiedChatMessagePacket;
import com.loohp.interactivechat.utils.ChatComponentType;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.function.UnaryOperator;

import static com.loohp.interactivechat.listeners.packet.MessagePacketHandler.PacketAccessorResult;

public class PacketEventsPlayServerUnifiedChatMessagePacket extends PlatformPlayServerUnifiedChatMessagePacket<PacketWrapper<?>> {

    private final UnaryOperator<PacketWrapper<?>> cloner;

    public PacketEventsPlayServerUnifiedChatMessagePacket(PacketWrapper<?> handle, MessagePacketHandler<?, PacketWrapper<?>> messagePacketHandler, UnaryOperator<PacketWrapper<?>> cloner) {
        super(handle, messagePacketHandler);
        this.cloner = cloner;
    }

    @Override
    public PacketEventsPlayServerUnifiedChatMessagePacket shallowClone() {
        return new PacketEventsPlayServerUnifiedChatMessagePacket(cloner.apply(handle), messagePacketHandler, cloner);
    }

    @Override
    public PacketAccessorResult read(Player player) {
        return messagePacketHandler.getAccessor().apply(handle, player);
    }

    @Override
    public void write(Component component, ChatComponentType type, int field, UUID sender) {
        messagePacketHandler.getWriter().apply(handle, component, type, field, sender);
    }

}
