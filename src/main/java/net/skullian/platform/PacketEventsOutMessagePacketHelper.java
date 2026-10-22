package net.skullian.platform;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import com.github.retrooper.packetevents.protocol.chat.ChatTypes;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessageLegacy;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_16;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19_1;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19_3;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_21_5;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerActionBar;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatPreview;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisguisedChat;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetTitleSubtitle;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetTitleText;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.libs.net.kyori.adventure.text.Component;
import com.loohp.interactivechat.listeners.packet.MessagePacketHandler;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import com.loohp.interactivechat.utils.ChatComponentType;
import com.loohp.interactivechat.utils.ComponentStyling;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.MCVersion;
import net.skullian.util.PacketEventsComponentUtils;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.loohp.interactivechat.listeners.packet.MessagePacketHandler.*;

@SuppressWarnings("deprecation")
public class PacketEventsOutMessagePacketHelper {
    protected static final Map<PacketTypeCommon, PacketEventsHandler<?>> PACKET_HANDLERS = new HashMap<>();

    static {
        initializeMessagePacketHandlers();
    }

    private static void initializeMessagePacketHandlers() {
        PACKET_HANDLERS.put(PacketType.Play.Server.DISGUISED_CHAT, new PacketEventsHandler<>(event -> InteractiveChat.chatListener, (packet, player) -> {
            ChatComponentType type = ChatComponentType.NativeAdventureComponent;
            Component component = InteractiveChatComponentSerializer.gson().deserialize(PacketEventsComponentUtils.serializePacketEventsComponent(packet.getMessage()));
            return new PacketAccessorResult(component, type, 0, false);
        }, (packet, component, type, field, sender) -> {
            boolean legacyRGB = InteractiveChat.version.isLegacyRGB();

            String json = legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component);
            boolean longerThanMaxLength = InteractiveChat.sendOriginalIfTooLong && json.length() > InteractiveChat.packetStringMaxLength;

            packet.setMessage(PacketEventsComponentUtils.deserializePacketEventsComponent(json));
            return new PacketWriterResult(longerThanMaxLength, json.length(), sender);
        }, WrapperPlayServerDisguisedChat::new, p -> new WrapperPlayServerDisguisedChat(p.getMessage(), p.getChatFormatting())));

        PACKET_HANDLERS.put(PacketType.Play.Server.CHAT_MESSAGE, new PacketEventsHandler<>(event -> {
            if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19_3)) return InteractiveChat.chatListener;

            WrapperPlayServerChatMessage packet = (WrapperPlayServerChatMessage) event.getLastUsedWrapper();
            return packet.getMessage().getType().equals(ChatTypes.GAME_INFO) ? InteractiveChat.titleListener : InteractiveChat.chatListener;
        }, event -> {
            WrapperPlayServerChatMessage packet = (WrapperPlayServerChatMessage) event.getLastUsedWrapper();
            if (packet.getMessage() instanceof ChatMessage_v1_16 message && message.getSenderUUID() != null) return ICPlayerFactory.getICPlayer(message.getSenderUUID());

            return null;
        }, (packet, player) -> {
            ChatComponentType type = ChatComponentType.NativeAdventureComponent;
            ChatMessage chatMessage = packet.getMessage();
            net.kyori.adventure.text.Component unsignedContent = switch (chatMessage) {
                case ChatMessage_v1_19_3 c -> c.getUnsignedChatContent().orElse(null);
                case ChatMessage_v1_19_1 c -> c.getUnsignedChatContent();
                case ChatMessage_v1_19 c -> c.getUnsignedChatContent();
                default -> null;
            };

            Component component = InteractiveChatComponentSerializer.gson().deserialize(PacketEventsComponentUtils.serializePacketEventsComponent(unsignedContent == null ? chatMessage.getChatContent() : unsignedContent));
            return new PacketAccessorResult(component, type, 0, false);
        }, (packet, component, type, field, sender) -> {
            boolean legacyRGB = InteractiveChat.version.isLegacyRGB();
            String json = legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component);
            boolean longerThanMaxLength = InteractiveChat.sendOriginalIfTooLong && json.length() > InteractiveChat.packetStringMaxLength;

            ChatMessage chatMessage = packet.getMessage();
            if (sender != null && chatMessage instanceof ChatMessage_v1_16 message) message.setSenderUUID(sender);

            net.kyori.adventure.text.Component bukkitComponent = PacketEventsComponentUtils.deserializePacketEventsComponent(json);
            switch (chatMessage) {
                case ChatMessage_v1_19_3 c -> c.setUnsignedChatContent(bukkitComponent);
                case ChatMessage_v1_19_1 c -> c.setUnsignedChatContent(bukkitComponent);
                case ChatMessage_v1_19 c -> c.setUnsignedChatContent(bukkitComponent);
                default -> chatMessage.setChatContent(bukkitComponent);
            }

            return new PacketWriterResult(longerThanMaxLength, json.length(), sender);
        }, WrapperPlayServerChatMessage::new, p -> {
            ChatMessage chatMessage = p.getMessage();
            ChatMessage clonedChatMessage = switch (chatMessage) {
                case ChatMessage_v1_21_5 c -> new ChatMessage_v1_21_5(c.getGlobalIndex(), c.getSenderUUID(), c.getIndex(), copySignature(c.getSignature()), c.getPlainContent(), c.getTimestamp(), c.getSalt(), c.getLastSeenMessagesPacked(), c.getUnsignedChatContent().orElse(null), c.getFilterMask(), c.getChatFormatting());
                case ChatMessage_v1_19_3 c -> new ChatMessage_v1_19_3(c.getSenderUUID(), c.getIndex(), copySignature(c.getSignature()), c.getPlainContent(), c.getTimestamp(), c.getSalt(), c.getLastSeenMessagesPacked(), c.getUnsignedChatContent().orElse(null), c.getFilterMask(), c.getChatFormatting());
                case ChatMessage_v1_19_1 c -> new ChatMessage_v1_19_1(c.getPlainContent(), c.getChatContent(), c.getUnsignedChatContent(), c.getSenderUUID(), c.getChatFormatting(), copySignature(c.getPreviousSignature()), copySignature(c.getSignature()), c.getTimestamp(), c.getSalt(), c.getLastSeenMessages(), c.getFilterMask());
                case ChatMessage_v1_19 c -> new ChatMessage_v1_19(c.getChatContent(), c.getUnsignedChatContent(), c.getType(), c.getSenderUUID(), c.getSenderDisplayName(), c.getTeamName(), c.getTimestamp(), c.getSalt(), copySignature(c.getSignature()));
                case ChatMessage_v1_16 c -> new ChatMessage_v1_16(c.getChatContent(), c.getType(), c.getSenderUUID());
                default -> new ChatMessageLegacy(chatMessage.getChatContent(), chatMessage.getType());
            };

            return new WrapperPlayServerChatMessage(clonedChatMessage);
        }));

        PACKET_HANDLERS.put(PacketType.Play.Server.CHAT_PREVIEW_PACKET, new PacketEventsHandler<>(event -> InteractiveChat.chatListener, event -> ICPlayerFactory.getICPlayer((Player) event.getPlayer()), (packet, player) -> {
            ChatComponentType type = ChatComponentType.NativeAdventureComponent;
            Component component = InteractiveChatComponentSerializer.gson().deserialize(PacketEventsComponentUtils.serializePacketEventsComponent(packet.getMessage().orElse(net.kyori.adventure.text.Component.empty())));
            return new PacketAccessorResult(component, type, 0, true);
        }, (packet, component, type, field, sender) -> {
            if (InteractiveChat.chatPreviewRemoveClickAndHover) component = ComponentStyling.stripEvents(component);

            boolean legacyRGB = InteractiveChat.version.isLegacyRGB();
            String json = legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component);
            boolean longerThanMaxLength = InteractiveChat.sendOriginalIfTooLong && json.length() > InteractiveChat.packetStringMaxLength;
            packet.setMessage(PacketEventsComponentUtils.deserializePacketEventsComponent(json));

            if (sender == null) sender = UUID_NIL;

            return new PacketWriterResult(longerThanMaxLength, json.length(), sender);
        }, WrapperPlayServerChatPreview::new, p -> new WrapperPlayServerChatPreview(p.getQueryId(), p.getMessage().orElse(null))));

        PACKET_HANDLERS.put(PacketType.Play.Server.SYSTEM_CHAT_MESSAGE, new PacketEventsHandler<>(event -> InteractiveChat.chatListener, (packet, player) -> {
            ChatComponentType type = ChatComponentType.NativeAdventureComponent;
            Component component = InteractiveChatComponentSerializer.gson().deserialize(PacketEventsComponentUtils.serializePacketEventsComponent(packet.getMessage()));
            return new PacketAccessorResult(component, type, 0, false);
        }, (packet, component, type, field, sender) -> {
            if (InteractiveChat.chatPreviewRemoveClickAndHover) component = ComponentStyling.stripEvents(component);

            boolean legacyRGB = InteractiveChat.version.isLegacyRGB();
            String json = legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component);
            boolean longerThanMaxLength = InteractiveChat.sendOriginalIfTooLong && json.length() > InteractiveChat.packetStringMaxLength;
            packet.setMessage(PacketEventsComponentUtils.deserializePacketEventsComponent(json));

            if (sender == null) sender = UUID_NIL;

            return new PacketWriterResult(longerThanMaxLength, json.length(), sender);
        }, WrapperPlayServerSystemChatMessage::new, p -> p.getType() == null ? new WrapperPlayServerSystemChatMessage(p.isOverlay(), p.getMessage()) : new WrapperPlayServerSystemChatMessage(p.getType(), p.getMessage())));

        PACKET_HANDLERS.put(PacketType.Play.Server.SET_TITLE_TEXT, new PacketEventsHandler<>(event -> InteractiveChat.titleListener, (packet, player) -> {
            ChatComponentType type = ChatComponentType.NativeAdventureComponent;
            Component component = InteractiveChatComponentSerializer.gson().deserialize(PacketEventsComponentUtils.serializePacketEventsComponent(packet.getTitle()));
            return new PacketAccessorResult(component, type, 0, false);
        }, (packet, component, type, field, sender) -> {
            boolean legacyRGB = InteractiveChat.version.isLegacyRGB();
            String json = legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component);
            boolean longerThanMaxLength = InteractiveChat.sendOriginalIfTooLong && json.length() > InteractiveChat.packetStringMaxLength;
            packet.setTitle(PacketEventsComponentUtils.deserializePacketEventsComponent(json));

            if (sender == null) sender = UUID_NIL;

            return new PacketWriterResult(longerThanMaxLength, json.length(), sender);
        }, WrapperPlayServerSetTitleText::new, p -> new WrapperPlayServerSetTitleText(p.getTitle())));

        PACKET_HANDLERS.put(PacketType.Play.Server.SET_TITLE_SUBTITLE, new PacketEventsHandler<>(event -> InteractiveChat.titleListener, (packet, player) -> {
            ChatComponentType type = ChatComponentType.NativeAdventureComponent;
            Component component = InteractiveChatComponentSerializer.gson().deserialize(PacketEventsComponentUtils.serializePacketEventsComponent(packet.getSubtitle()));
            return new PacketAccessorResult(component, type, 0, false);
        }, (packet, component, type, field, sender) -> {
            boolean legacyRGB = InteractiveChat.version.isLegacyRGB();
            String json = legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component);
            boolean longerThanMaxLength = InteractiveChat.sendOriginalIfTooLong && json.length() > InteractiveChat.packetStringMaxLength;
            packet.setSubtitle(PacketEventsComponentUtils.deserializePacketEventsComponent(json));

            if (sender == null) sender = UUID_NIL;

            return new PacketWriterResult(longerThanMaxLength, json.length(), sender);
        }, WrapperPlayServerSetTitleSubtitle::new, p -> new WrapperPlayServerSetTitleSubtitle(p.getSubtitle())));

        PACKET_HANDLERS.put(PacketType.Play.Server.ACTION_BAR, new PacketEventsHandler<>(event -> InteractiveChat.titleListener, (packet, player) -> {
            ChatComponentType type = ChatComponentType.NativeAdventureComponent;
            Component component = InteractiveChatComponentSerializer.gson().deserialize(PacketEventsComponentUtils.serializePacketEventsComponent(packet.getActionBarText()));
            return new PacketAccessorResult(component, type, 0, false);
        }, (packet, component, type, field, sender) -> {
            boolean legacyRGB = InteractiveChat.version.isLegacyRGB();
            String json = legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component);
            boolean longerThanMaxLength = InteractiveChat.sendOriginalIfTooLong && json.length() > InteractiveChat.packetStringMaxLength;
            packet.setActionBarText(PacketEventsComponentUtils.deserializePacketEventsComponent(json));

            if (sender == null) sender = UUID_NIL;

            return new PacketWriterResult(longerThanMaxLength, json.length(), sender);
        }, WrapperPlayServerActionBar::new, p -> new WrapperPlayServerActionBar(p.getActionBarText())));
    }

    private static byte[] copySignature(byte[] signature) {
        return signature == null ? null : signature.clone();
    }

    public static class PacketEventsHandler<PacketTyped extends PacketWrapper<?>> {

        private final MessagePacketHandler<ProtocolPacketEvent, PacketWrapper<?>> handler;
        private final Function<PacketSendEvent, PacketTyped> wrapper;
        private final UnaryOperator<PacketWrapper<?>> cloner;

        @SuppressWarnings("unchecked")
        public PacketEventsHandler(PreFilter<ProtocolPacketEvent> preFilter, PacketAccessor<PacketTyped> accessor, PacketWriter<PacketTyped> writer, Function<PacketSendEvent, PacketTyped> wrapper, UnaryOperator<PacketTyped> cloner) {
            this.handler = new MessagePacketHandler<>(preFilter, (PacketAccessor<PacketWrapper<?>>) accessor, (PacketWriter<PacketWrapper<?>>) writer);
            this.wrapper = wrapper;
            this.cloner = p -> cloner.apply((PacketTyped) p);
        }

        @SuppressWarnings("unchecked")
        public PacketEventsHandler(PreFilter<ProtocolPacketEvent> preFilter, DeterminedSenderFinder<ProtocolPacketEvent> determinedSenderFunction, PacketAccessor<PacketTyped> accessor, PacketWriter<PacketTyped> writer, Function<PacketSendEvent, PacketTyped> wrapper, UnaryOperator<PacketTyped> cloner) {
            this.handler = new MessagePacketHandler<>(preFilter, determinedSenderFunction, (PacketAccessor<PacketWrapper<?>>) accessor, (PacketWriter<PacketWrapper<?>>) writer);
            this.wrapper = wrapper;
            this.cloner = p -> cloner.apply((PacketTyped) p);
        }

        public MessagePacketHandler<ProtocolPacketEvent, PacketWrapper<?>> getHandler() {
            return handler;
        }

        public Function<PacketSendEvent, PacketTyped> getWrapper() {
            return wrapper;
        }

        public UnaryOperator<PacketWrapper<?>> getCloner() {
            return cloner;
        }
    }

}
