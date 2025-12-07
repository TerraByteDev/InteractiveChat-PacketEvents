package net.skullian.platform;

import com.github.retrooper.packetevents.protocol.chat.ChatCompletionAction;
import com.github.retrooper.packetevents.protocol.chat.ChatTypes;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessageLegacy;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_16;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCustomChatCompletions;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTabComplete;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.libs.net.kyori.adventure.text.Component;
import com.loohp.interactivechat.objectholders.CustomTabCompletionAction;
import com.loohp.interactivechat.platform.PlatformPacketCreatorProvider;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerCustomChatCompletionPacket;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerSystemChatPacket;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerTabCompletePacket;
import com.loohp.interactivechat.utils.ChatComponentType;
import com.loohp.interactivechat.utils.MCVersion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.kyori.adventure.platform.bukkit.BukkitComponentSerializer;
import net.skullian.platform.packets.PacketEventsPlayServerCustomChatCompletionPacket;
import net.skullian.platform.packets.PacketEventsPlayServerSystemChatPacket;
import net.skullian.platform.packets.PacketEventsPlayServerTabCompletePacket;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PacketEventsPacketCreatorProvider implements PlatformPacketCreatorProvider<PacketWrapper<?>> {

    @Override
    public PlatformPlayServerTabCompletePacket<PacketWrapper<?>> createPlayServerTabCompletePacket(int id, Object suggestions) {
        Suggestions mojangSuggestions = (Suggestions) suggestions;
        WrapperPlayServerTabComplete.CommandRange commandRange = new WrapperPlayServerTabComplete.CommandRange(mojangSuggestions.getRange().getStart(), mojangSuggestions.getRange().getEnd());
        List<WrapperPlayServerTabComplete.CommandMatch> commandMatches = mojangSuggestions.getList().stream()
                .map((m) -> new WrapperPlayServerTabComplete.CommandMatch(m.getText(), m.getTooltip() == null ? null : BukkitComponentSerializer.gson().deserialize(ChatComponentType.IChatBaseComponent.toJsonString(m.getTooltip(), null)))).collect(Collectors.toList());
        return new PacketEventsPlayServerTabCompletePacket(new WrapperPlayServerTabComplete(id, commandRange, commandMatches));
    }

    @Override
    public PlatformPlayServerCustomChatCompletionPacket<PacketWrapper<?>> createPlayServerCustomChatCompletionPacket(CustomTabCompletionAction action, List<String> list) {
        ChatCompletionAction packetEventsAction;
        switch (action) {
            case ADD:
                packetEventsAction = ChatCompletionAction.ADD;
                break;
            case REMOVE:
                packetEventsAction = ChatCompletionAction.REMOVE;
                break;
            default:
                throw new IllegalArgumentException("Unknown action " + action.name());
        }
        return new PacketEventsPlayServerCustomChatCompletionPacket(new WrapperPlayServerCustomChatCompletions(packetEventsAction, list));
    }

    @SuppressWarnings("deprecation")
    @Override
    public PlatformPlayServerSystemChatPacket<PacketWrapper<?>> createPlayServerSystemChatPacket(UUID uuid, Component component) {
        String json = ChatComponentType.AdventureComponent.toJsonString(component, null);
        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19)) {
            return new PacketEventsPlayServerSystemChatPacket(new WrapperPlayServerSystemChatMessage(false, json));
        } else {
            net.kyori.adventure.text.@NotNull Component nativeComponent = BukkitComponentSerializer.gson().deserialize(json);
            ChatMessage message;
            if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16)) {
                message = new ChatMessage_v1_16(nativeComponent, ChatTypes.SYSTEM, uuid);
            } else {
                message = new ChatMessageLegacy(nativeComponent, ChatTypes.SYSTEM);
            }
            return new PacketEventsPlayServerSystemChatPacket(new WrapperPlayServerChatMessage(message));
        }
    }
}
