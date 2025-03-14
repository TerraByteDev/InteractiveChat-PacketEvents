package net.skullian.platform;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.protocol.chat.ChatTypes;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessageLegacy;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_16;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTabComplete;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.libs.net.kyori.adventure.text.Component;
import com.loohp.interactivechat.objectholders.CustomTabCompletionAction;
import com.loohp.interactivechat.platform.ProtocolPlatform;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.MCVersion;
import com.loohp.interactivechat.utils.NativeAdventureConverter;
import net.md_5.bungee.chat.ComponentSerializer;
import net.skullian.listeners.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.loohp.interactivechat.InteractiveChat.version;

public class PacketEventsPlatform implements ProtocolPlatform {

    private static final Constructor<?> v1_16_chatMessageConstructor;
    private static final Constructor<?> legacyChatMessageConstructor;
    private static final Constructor<?> commandMatchConstructor;

    static {
        v1_16_chatMessageConstructor = Arrays.stream(ChatMessage_v1_16.class.getConstructors())
                .filter(each -> each.getParameterCount() == 3).findFirst().get();
        legacyChatMessageConstructor = Arrays.stream(ChatMessageLegacy.class.getConstructors())
                .filter(each -> each.getParameterCount() == 2).findFirst().get();
        commandMatchConstructor = Arrays.stream(WrapperPlayServerTabComplete.CommandMatch.class.getConstructors())
                .filter(each -> each.getParameterCount() == 2).findFirst().get();
    }

    @Override
    public void initialise() {
        PacketEvents.getAPI().getEventManager().registerListener(new PEOutMessagePacket(), PacketListenerPriority.MONITOR);
        PacketEvents.getAPI().getEventManager().registerListener(new PEClientSettingsPacket(), PacketListenerPriority.NORMAL);

        if (version.isNewerOrEqualTo(MCVersion.V1_19)) {
            PacketEvents.getAPI().getEventManager().registerListener(new PERedispatchSignedPacket(), PacketListenerPriority.HIGHEST);
        }

        if (!version.isLegacy()) {
            PacketEvents.getAPI().getEventManager().registerListener(new PEOutTabCompletePacket(), PacketListenerPriority.HIGH);
        }
    }

    @Override
    public void onBungeecordEnabled() {
        PacketEvents.getAPI().getEventManager().registerListener(new PEServerPingListener(), PacketListenerPriority.NORMAL);
    }

    @Override
    public void sendTabCompletionPacket(Player player, CustomTabCompletionAction action, List<String> list) {
        try {
            List<WrapperPlayServerTabComplete.CommandMatch> suggestions = new ArrayList<>();
            for (String cmd : list) {
                WrapperPlayServerTabComplete.CommandMatch match = (WrapperPlayServerTabComplete.CommandMatch) commandMatchConstructor.newInstance(
                        cmd,
                        null
                );
                suggestions.add(match);
            }

            WrapperPlayServerTabComplete packet = new WrapperPlayServerTabComplete(
                    null,
                    new WrapperPlayServerTabComplete.CommandRange(0, list.size()),
                    suggestions
            );
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendUnprocessedChatMessage(CommandSender sender, UUID uuid, Component component) {
        try {
            if (sender instanceof Player) {

                PacketWrapper<?> packet;
                if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19)) {
                    packet = new WrapperPlayServerSystemChatMessage(false, (net.kyori.adventure.text.Component) component);
                } else {
                    Object nativeComponent = NativeAdventureConverter.componentToNative(component, false);
                    ChatMessage message;
                    if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16)) {
                        message = (ChatMessage) v1_16_chatMessageConstructor.newInstance(
                                nativeComponent,
                                ChatTypes.SYSTEM,
                                uuid
                        );
                    } else {
                        message = (ChatMessage) legacyChatMessageConstructor.newInstance(
                                nativeComponent,
                                ChatTypes.SYSTEM
                        );
                    }

                    packet = new WrapperPlayServerChatMessage(message);
                }

                PacketEvents.getAPI().getPlayerManager().sendPacket(sender, packet);
            } else {
                String json = InteractiveChatComponentSerializer.gson().serialize(component);
                sender.spigot().sendMessage(ComponentSerializer.parse(json));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasChatSigning() {
        return InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19);
    }

    @Override
    public int getProtocolVersion(Player player) {
        return PacketEvents.getAPI().getProtocolManager().getClientVersion(player).getProtocolVersion();
    }
}

