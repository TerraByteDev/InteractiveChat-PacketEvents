/*
 * This file is part of InteractiveChat4.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.loohp.interactivechat.listeners.packet.MessagePacketHandler.*;

public class PacketEventsOutMessagePacketHelper {
    protected static final Map<PacketTypeCommon, PacketEventsHandler<?>> PACKET_HANDLERS = new HashMap<>();

    static {
        initializeMessagePacketHandlers();
    }

    private static void initializeMessagePacketHandlers() {
        PACKET_HANDLERS.put(PacketType.Play.Server.DISGUISED_CHAT, new PacketEventsHandler<>(event -> {
            return InteractiveChat.chatListener;
        }, (packet, player) -> {
            ChatComponentType type = ChatComponentType.NativeAdventureComponent;
            Component component = type.convertFrom(packet.getMessage(), player);
            return new PacketAccessorResult(component, type, 0, false);
        }, (packet, component, type, field, sender) -> {
            boolean legacyRGB = InteractiveChat.version.isLegacyRGB();
            String json = legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component);
            boolean longerThanMaxLength = InteractiveChat.sendOriginalIfTooLong && json.length() > InteractiveChat.packetStringMaxLength;
            packet.setMessage((net.kyori.adventure.text.Component) type.convertTo(component, legacyRGB));
            return new PacketWriterResult(longerThanMaxLength, json.length(), sender);
        }, WrapperPlayServerDisguisedChat::new, p -> new WrapperPlayServerDisguisedChat(p.getMessage(), p.getChatFormatting())));

        PACKET_HANDLERS.put(PacketType.Play.Server.CHAT_MESSAGE, new PacketEventsHandler<>(event -> {
            if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19_3)) {
                return InteractiveChat.chatListener;
            }
            WrapperPlayServerChatMessage packet = (WrapperPlayServerChatMessage) event.getLastUsedWrapper();
            if (packet.getMessage().getType().equals(ChatTypes.GAME_INFO)) {
                return InteractiveChat.titleListener;
            } else {
                return InteractiveChat.chatListener;
            }
        }, event -> {
            WrapperPlayServerChatMessage packet = (WrapperPlayServerChatMessage) event.getLastUsedWrapper();
            ChatMessage chatMessage = packet.getMessage();
            if (chatMessage instanceof ChatMessage_v1_16) {
                UUID uuid = ((ChatMessage_v1_16) chatMessage).getSenderUUID();
                if (uuid != null) {
                    return ICPlayerFactory.getICPlayer(uuid);
                }
            }
            return null;
        }, (packet, player) -> {
            ChatComponentType type = ChatComponentType.NativeAdventureComponent;
            ChatMessage chatMessage = packet.getMessage();
            Component component;
            if (chatMessage instanceof ChatMessage_v1_21_5) {
                net.kyori.adventure.text.Component unsignedContent = ((ChatMessage_v1_21_5) chatMessage).getUnsignedChatContent().orElse(null);
                if (unsignedContent == null) {
                    component = type.convertFrom(chatMessage.getChatContent(), player);
                } else {
                    component = type.convertFrom(unsignedContent, player);
                }
            } else if (chatMessage instanceof ChatMessage_v1_19_3) {
                net.kyori.adventure.text.Component unsignedContent = ((ChatMessage_v1_19_3) chatMessage).getUnsignedChatContent().orElse(null);
                if (unsignedContent == null) {
                    component = type.convertFrom(chatMessage.getChatContent(), player);
                } else {
                    component = type.convertFrom(unsignedContent, player);
                }
            } else if (chatMessage instanceof ChatMessage_v1_19_1) {
                net.kyori.adventure.text.Component unsignedContent = ((ChatMessage_v1_19_1) chatMessage).getUnsignedChatContent();
                if (unsignedContent == null) {
                    component = type.convertFrom(chatMessage.getChatContent(), player);
                } else {
                    component = type.convertFrom(unsignedContent, player);
                }
            } else if (chatMessage instanceof ChatMessage_v1_19) {
                net.kyori.adventure.text.Component unsignedContent = ((ChatMessage_v1_19) chatMessage).getUnsignedChatContent();
                if (unsignedContent == null) {
                    component = type.convertFrom(chatMessage.getChatContent(), player);
                } else {
                    component = type.convertFrom(unsignedContent, player);
                }
            } else {
                component = type.convertFrom(chatMessage.getChatContent(), player);
            }
            return new PacketAccessorResult(component, type, 0, false);
        }, (packet, component, type, field, sender) -> {
            boolean legacyRGB = InteractiveChat.version.isLegacyRGB();
            String json = legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component);
            boolean longerThanMaxLength = InteractiveChat.sendOriginalIfTooLong && json.length() > InteractiveChat.packetStringMaxLength;
            ChatMessage chatMessage = packet.getMessage();
            if (chatMessage instanceof ChatMessage_v1_16) {
                if (sender != null) {
                    ((ChatMessage_v1_16) chatMessage).setSenderUUID(sender);
                }
            }
            net.kyori.adventure.text.Component bukkitComponent = (net.kyori.adventure.text.Component) type.convertTo(component, legacyRGB);
            if (chatMessage instanceof ChatMessage_v1_21_5) {
                ((ChatMessage_v1_21_5) chatMessage).setUnsignedChatContent(bukkitComponent);
            } else if (chatMessage instanceof ChatMessage_v1_19_3) {
                ((ChatMessage_v1_19_3) chatMessage).setUnsignedChatContent(bukkitComponent);
            } else if (chatMessage instanceof ChatMessage_v1_19_1) {
                ((ChatMessage_v1_19_1) chatMessage).setUnsignedChatContent(bukkitComponent);
            } else if (chatMessage instanceof ChatMessage_v1_19) {
                ((ChatMessage_v1_19) chatMessage).setUnsignedChatContent(bukkitComponent);
            } else {
                chatMessage.setChatContent(bukkitComponent);
            }
            return new PacketWriterResult(longerThanMaxLength, json.length(), sender);
        }, WrapperPlayServerChatMessage::new, p -> {
            ChatMessage chatMessage = p.getMessage();
            ChatMessage clonedChatMessage;
            if (chatMessage instanceof ChatMessage_v1_21_5) {
                ChatMessage_v1_21_5 c = (ChatMessage_v1_21_5) chatMessage;
                clonedChatMessage = new ChatMessage_v1_21_5(c.getGlobalIndex(), c.getSenderUUID(), c.getIndex(), Arrays.copyOf(c.getSignature(), c.getSignature().length), c.getPlainContent(), c.getTimestamp(), c.getSalt(), c.getLastSeenMessagesPacked(), c.getChatContent(), c.getFilterMask(), c.getChatFormatting());
            } else if (chatMessage instanceof ChatMessage_v1_19_3) {
                ChatMessage_v1_19_3 c = (ChatMessage_v1_19_3) chatMessage;
                clonedChatMessage = new ChatMessage_v1_19_3(c.getSenderUUID(), c.getIndex(), Arrays.copyOf(c.getSignature(), c.getSignature().length), c.getPlainContent(), c.getTimestamp(), c.getSalt(), c.getLastSeenMessagesPacked(), c.getChatContent(), c.getFilterMask(), c.getChatFormatting());
            } else if (chatMessage instanceof ChatMessage_v1_19_1) {
                ChatMessage_v1_19_1 c = (ChatMessage_v1_19_1) chatMessage;
                clonedChatMessage = new ChatMessage_v1_19_1(c.getPlainContent(), c.getChatContent(), c.getUnsignedChatContent(), c.getSenderUUID(), c.getChatFormatting(), Arrays.copyOf(c.getPreviousSignature(), c.getPreviousSignature().length), Arrays.copyOf(c.getSignature(), c.getSignature().length), c.getTimestamp(), c.getSalt(), c.getLastSeenMessages(), c.getFilterMask());
            } else if (chatMessage instanceof ChatMessage_v1_19) {
                ChatMessage_v1_19 c = (ChatMessage_v1_19) chatMessage;
                clonedChatMessage = new ChatMessage_v1_19(c.getChatContent(), c.getUnsignedChatContent(), c.getType(), c.getSenderUUID(), c.getSenderDisplayName(), c.getTeamName(), c.getTimestamp(), c.getSalt(), Arrays.copyOf(c.getSignature(), c.getSignature().length));
            } else if (chatMessage instanceof ChatMessage_v1_16) {
                ChatMessage_v1_16 c = (ChatMessage_v1_16) chatMessage;
                clonedChatMessage = new ChatMessage_v1_16(c.getChatContent(), c.getType(), c.getSenderUUID());
            } else {
                clonedChatMessage = new ChatMessageLegacy(chatMessage.getChatContent(), chatMessage.getType());
            }
            return new WrapperPlayServerChatMessage(clonedChatMessage);
        }));

        PACKET_HANDLERS.put(PacketType.Play.Server.CHAT_PREVIEW_PACKET, new PacketEventsHandler<>(event -> {
            return InteractiveChat.chatListener;
        }, event -> {
            return ICPlayerFactory.getICPlayer((Player) event.getPlayer());
        }, (packet, player) -> {
            ChatComponentType type = ChatComponentType.NativeAdventureComponent;
            Component component = type.convertFrom(packet.getMessage().orElse(net.kyori.adventure.text.Component.empty()), player);
            return new PacketAccessorResult(component, type, 0, true);
        }, (packet, component, type, field, sender) -> {
            if (InteractiveChat.chatPreviewRemoveClickAndHover) {
                component = ComponentStyling.stripEvents(component);
            }
            boolean legacyRGB = InteractiveChat.version.isLegacyRGB();
            String json = legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component);
            boolean longerThanMaxLength = InteractiveChat.sendOriginalIfTooLong && json.length() > InteractiveChat.packetStringMaxLength;
            packet.setMessage((net.kyori.adventure.text.Component) type.convertTo(component, legacyRGB));
            if (sender == null) {
                sender = UUID_NIL;
            }
            return new PacketWriterResult(longerThanMaxLength, json.length(), sender);
        }, WrapperPlayServerChatPreview::new, p -> new WrapperPlayServerChatPreview(p.getQueryId(), p.getMessage().orElse(null))));


        PACKET_HANDLERS.put(PacketType.Play.Server.SYSTEM_CHAT_MESSAGE, new PacketEventsHandler<>(event -> {
            return InteractiveChat.chatListener;
        }, event -> {
            return ICPlayerFactory.getICPlayer((Player) event.getPlayer());
        }, (packet, player) -> {
            ChatComponentType type = ChatComponentType.NativeAdventureComponent;
            Component component = type.convertFrom(packet.getMessage(), player);
            return new PacketAccessorResult(component, type, 0, true);
        }, (packet, component, type, field, sender) -> {
            if (InteractiveChat.chatPreviewRemoveClickAndHover) {
                component = ComponentStyling.stripEvents(component);
            }
            boolean legacyRGB = InteractiveChat.version.isLegacyRGB();
            String json = legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component);
            boolean longerThanMaxLength = InteractiveChat.sendOriginalIfTooLong && json.length() > InteractiveChat.packetStringMaxLength;
            packet.setMessage((net.kyori.adventure.text.Component) type.convertTo(component, legacyRGB));
            if (sender == null) {
                sender = UUID_NIL;
            }
            return new PacketWriterResult(longerThanMaxLength, json.length(), sender);
        }, WrapperPlayServerSystemChatMessage::new, p -> {
            if (p.getType() == null) {
                return new WrapperPlayServerSystemChatMessage(p.isOverlay(), p.getMessage());
            } else {
                return new WrapperPlayServerSystemChatMessage(p.getType(), p.getMessage());
            }
        }));

        PACKET_HANDLERS.put(PacketType.Play.Server.SET_TITLE_TEXT, new PacketEventsHandler<>(event -> {
            return InteractiveChat.titleListener;
        }, (packet, player) -> {
            ChatComponentType type = ChatComponentType.NativeAdventureComponent;
            Component component = type.convertFrom(packet.getTitle(), player);
            return new PacketAccessorResult(component, type, 0, false);
        }, (packet, component, type, field, sender) -> {
            boolean legacyRGB = InteractiveChat.version.isLegacyRGB();
            String json = legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component);
            boolean longerThanMaxLength = InteractiveChat.sendOriginalIfTooLong && json.length() > InteractiveChat.packetStringMaxLength;
            packet.setTitle((net.kyori.adventure.text.Component) type.convertTo(component, legacyRGB));
            if (sender == null) {
                sender = UUID_NIL;
            }
            return new PacketWriterResult(longerThanMaxLength, json.length(), sender);
        }, WrapperPlayServerSetTitleText::new, p -> new WrapperPlayServerSetTitleText(p.getTitle())));

        PACKET_HANDLERS.put(PacketType.Play.Server.SET_TITLE_SUBTITLE, new PacketEventsHandler<>(event -> {
            return InteractiveChat.titleListener;
        }, (packet, player) -> {
            ChatComponentType type = ChatComponentType.NativeAdventureComponent;
            Component component = type.convertFrom(packet.getSubtitle(), player);
            return new PacketAccessorResult(component, type, 0, false);
        }, (packet, component, type, field, sender) -> {
            boolean legacyRGB = InteractiveChat.version.isLegacyRGB();
            String json = legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component);
            boolean longerThanMaxLength = InteractiveChat.sendOriginalIfTooLong && json.length() > InteractiveChat.packetStringMaxLength;
            packet.setSubtitle((net.kyori.adventure.text.Component) type.convertTo(component, legacyRGB));
            if (sender == null) {
                sender = UUID_NIL;
            }
            return new PacketWriterResult(longerThanMaxLength, json.length(), sender);
        }, WrapperPlayServerSetTitleSubtitle::new, p -> new WrapperPlayServerSetTitleSubtitle(p.getSubtitle())));

        PACKET_HANDLERS.put(PacketType.Play.Server.ACTION_BAR, new PacketEventsHandler<>(event -> {
            return InteractiveChat.titleListener;
        }, (packet, player) -> {
            ChatComponentType type = ChatComponentType.NativeAdventureComponent;
            Component component = type.convertFrom(packet.getActionBarText(), player);
            return new PacketAccessorResult(component, type, 0, false);
        }, (packet, component, type, field, sender) -> {
            boolean legacyRGB = InteractiveChat.version.isLegacyRGB();
            String json = legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component);
            boolean longerThanMaxLength = InteractiveChat.sendOriginalIfTooLong && json.length() > InteractiveChat.packetStringMaxLength;
            packet.setActionBarText((net.kyori.adventure.text.Component) type.convertTo(component, legacyRGB));
            if (sender == null) {
                sender = UUID_NIL;
            }
            return new PacketWriterResult(longerThanMaxLength, json.length(), sender);
        }, WrapperPlayServerActionBar::new, p -> new WrapperPlayServerActionBar(p.getActionBarText())));
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