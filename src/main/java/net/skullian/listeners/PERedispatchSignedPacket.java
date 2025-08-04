package net.skullian.listeners;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerServerData;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.listeners.packet.RedispatchedSignPacketHandler;
import com.loohp.interactivechat.utils.MCVersion;
import com.loohp.interactivechat.utils.ModernChatSigningUtils;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

import static net.skullian.InteractiveChatPacketEvents.sendDebug;

public class PERedispatchSignedPacket implements PacketListener {

    private static final List<PacketTypeCommon> packetTypes = getPacketTypes();

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType().equals(PacketType.Play.Server.SERVER_DATA)) {
            sendDebug("HANDLING SERVER DATA PACKET: " + event.getPacketType());

            handleServerDataPacket(event);
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (shouldIgnoreEvent(event)) return;

        if (event.getPacketType().equals(PacketType.Play.Client.CHAT_MESSAGE)) {
            sendDebug("HANDLING CHAT MESSAGE PACKET: " + event.getPacketType());

            handleChatPacket(event);
        } else if (event.getPacketType().equals(PacketType.Play.Client.CHAT_COMMAND) || event.getPacketType().equals(PacketType.Play.Client.CHAT_COMMAND_UNSIGNED)) {
            sendDebug("HANDLING CHAT COMMAND PACKET: " + event.getPacketType());

            handleChatCommandPacket(event);
        }

        event.markForReEncode(true);
    }

    private static boolean shouldIgnoreEvent(PacketReceiveEvent event) {
        return event.isCancelled() || !InteractiveChat.protocolPlatform.hasChatSigning() || !packetTypes.contains(event.getPacketType());
    }

    private static List<PacketTypeCommon> getPacketTypes() {
        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_20_5)) {
            return Arrays.asList(
                    PacketType.Play.Client.CHAT_COMMAND,
                    PacketType.Play.Client.CHAT_COMMAND_UNSIGNED,
                    PacketType.Play.Client.CHAT_MESSAGE
            );
        } else {
            return Arrays.asList(
                    PacketType.Play.Client.CHAT_COMMAND,
                    PacketType.Play.Client.CHAT_MESSAGE
            );
        }
    }

    private static void handleChatPacket(PacketReceiveEvent event) {
        if (InteractiveChat.forceUnsignedChatPackets) {
            Player player = event.getPlayer();
            WrapperPlayClientChatMessage packet = new WrapperPlayClientChatMessage(event);

            String message = packet.getMessage();

            sendDebug("PERedispatchSignedPacket HANDLING CHAT MESSAGE: " + message);

            if (message.startsWith("/")) {
                redispatchCommand(event, player, message);
            } else {
                redispatchChatMessage(event, player, message);
            }
        }
    }

    private static void handleChatCommandPacket(PacketReceiveEvent event) {
        Player player = event.getPlayer();

        if (InteractiveChat.forceUnsignedChatCommandPackets && event.getPacketType().equals(PacketType.Play.Client.CHAT_COMMAND)) {
            WrapperPlayClientChatCommand packet = new WrapperPlayClientChatCommand(event);

            String command = "/" + packet.getCommand();

            redispatchCommand(event, player, command);
        }
    }

    private static void redispatchCommand(PacketReceiveEvent event, Player player, String command) {
        event.setCancelled(true);

        RedispatchedSignPacketHandler.redispatchCommand(player, command);
    }

    private static void redispatchChatMessage(PacketReceiveEvent event, Player player, String message) {
        if (!ModernChatSigningUtils.isChatMessageIllegal(message)) {
            event.setCancelled(true);

            RedispatchedSignPacketHandler.redispatchChatMessage(player, message);
        }
    }

    private static void handleServerDataPacket(PacketSendEvent event) {
        if (event.getPacketType().equals(PacketType.Play.Server.SERVER_DATA)) {
            WrapperPlayServerServerData packet = new WrapperPlayServerServerData(event);

            if (InteractiveChat.hideServerUnsignedStatus) packet.setEnforceSecureChat(true);
            event.markForReEncode(true);
        }
    }
}
