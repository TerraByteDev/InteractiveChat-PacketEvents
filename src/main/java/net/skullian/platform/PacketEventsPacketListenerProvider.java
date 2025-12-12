package net.skullian.platform;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.configuration.client.WrapperConfigClientSettings;
import com.github.retrooper.packetevents.wrapper.handshaking.client.WrapperHandshakingClientHandshake;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommandUnsigned;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSettings;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerServerData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTabComplete;
import com.github.retrooper.packetevents.wrapper.status.server.WrapperStatusServerResponse;
import com.loohp.interactivechat.platform.PlatformPacketEventListener;
import com.loohp.interactivechat.platform.PlatformPacketListenerPriority;
import com.loohp.interactivechat.platform.PlatformPacketListenerProvider;
import com.loohp.interactivechat.platform.packets.PlatformConfigurationClientClientInformationPacket;
import com.loohp.interactivechat.platform.packets.PlatformHandshakeClientSetProtocolPacket;
import com.loohp.interactivechat.platform.packets.PlatformPlayClientChatCommandPacket;
import com.loohp.interactivechat.platform.packets.PlatformPlayClientChatPacket;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerServerDataPacket;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerTabCompletePacket;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerUnifiedChatMessagePacket;
import com.loohp.interactivechat.platform.packets.PlatformStatusServerServerInfoPacket;
import net.skullian.platform.packets.PacketEventsConfigurationClientClientInformationPacket;
import net.skullian.platform.packets.PacketEventsHandshakeClientSetProtocolPacket;
import net.skullian.platform.packets.PacketEventsPlayClientChatCommandPacket;
import net.skullian.platform.packets.PacketEventsPlayClientChatPacket;
import net.skullian.platform.packets.PacketEventsPlayServerServerDataPacket;
import net.skullian.platform.packets.PacketEventsPlayServerTabCompletePacket;
import net.skullian.platform.packets.PacketEventsPlayServerUnifiedChatMessagePacket;
import net.skullian.platform.packets.PacketEventsStatusServerServerInfoPacket;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class PacketEventsPacketListenerProvider implements PlatformPacketListenerProvider<ProtocolPacketEvent, PacketWrapper<?>> {

    private static PacketListenerPriority c(PlatformPacketListenerPriority priority) {
        return PacketListenerPriority.valueOf(priority.name());
    }

    private final PacketEventsPlatform platform;

    public PacketEventsPacketListenerProvider(PacketEventsPlatform platform) {
        this.platform = platform;
    }

    @Override
    public void listenToHandshakeClientSetProtocol(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<ProtocolPacketEvent, PacketWrapper<?>, PlatformHandshakeClientSetProtocolPacket<PacketWrapper<?>>> listener) {
        platform.getPacketEventsAPI().getEventManager().registerListener(new PacketListener() {
            @Override
            public void onPacketReceive(@NotNull PacketReceiveEvent event) {
                PacketTypeCommon type = event.getPacketType();
                if (type == PacketType.Handshaking.Client.HANDSHAKE) {
                    listener.handle(new PacketEventsPacketEvent<>(event, e -> {
                        WrapperHandshakingClientHandshake wrapper = new WrapperHandshakingClientHandshake((PacketReceiveEvent) e);
                        return new PacketEventsHandshakeClientSetProtocolPacket(wrapper);
                    }));
                }
            }
        }, c(priority));
    }

    @Override
    public void listenToStatusServerServerInfo(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<ProtocolPacketEvent, PacketWrapper<?>, PlatformStatusServerServerInfoPacket<PacketWrapper<?>>> listener) {
        platform.getPacketEventsAPI().getEventManager().registerListener(new PacketListener() {
            @Override
            public void onPacketSend(@NotNull PacketSendEvent event) {
                PacketTypeCommon type = event.getPacketType();
                if (type == PacketType.Status.Server.RESPONSE) {
                    listener.handle(new PacketEventsPacketEvent<>(event, e -> {
                        WrapperStatusServerResponse wrapper = new WrapperStatusServerResponse((PacketSendEvent) e);
                        return new PacketEventsStatusServerServerInfoPacket(wrapper);
                    }));
                }
            }
        }, c(priority));
    }

    @Override
    public void listenToConfigurationClientClientInformation(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<ProtocolPacketEvent, PacketWrapper<?>, PlatformConfigurationClientClientInformationPacket<PacketWrapper<?>>> listener) {
        platform.getPacketEventsAPI().getEventManager().registerListener(new PacketListener() {
            @Override
            public void onPacketReceive(@NotNull PacketReceiveEvent event) {
                PacketTypeCommon type = event.getPacketType();
                if (type == PacketType.Configuration.Client.CLIENT_SETTINGS) {
                    listener.handle(new PacketEventsPacketEvent<>(event, e -> {
                        WrapperConfigClientSettings wrapper = new WrapperConfigClientSettings((PacketReceiveEvent) e);
                        e.setLastUsedWrapper(wrapper);
                        return new PacketEventsConfigurationClientClientInformationPacket(wrapper);
                    }));

                    PacketWrapper<?> wrapper = event.getLastUsedWrapper();
                    if (wrapper != null) {
                        event.setByteBuf(wrapper);
                    }
                } else if (type == PacketType.Play.Client.CLIENT_SETTINGS) {
                    listener.handle(new PacketEventsPacketEvent<>(event, e -> {
                        WrapperPlayClientSettings wrapper = new WrapperPlayClientSettings((PacketReceiveEvent) e);
                        return new PacketEventsConfigurationClientClientInformationPacket(wrapper);
                    }));
                }
            }
        }, c(priority));
    }

    @Override
    public void listenToPlayServerTabComplete(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<ProtocolPacketEvent, PacketWrapper<?>, PlatformPlayServerTabCompletePacket<PacketWrapper<?>>> listener) {
        platform.getPacketEventsAPI().getEventManager().registerListener(new PacketListener() {
            @Override
            public void onPacketSend(@NotNull PacketSendEvent event) {
                PacketTypeCommon type = event.getPacketType();
                if (type == PacketType.Play.Server.TAB_COMPLETE) {
                    listener.handle(new PacketEventsPacketEvent<>(event, e -> {
                        WrapperPlayServerTabComplete wrapper = new WrapperPlayServerTabComplete((PacketSendEvent) e);
                        return new PacketEventsPlayServerTabCompletePacket(wrapper);
                    }));
                }
            }
        }, c(priority));
    }

    @Override
    public void listenToPlayClientChat(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<ProtocolPacketEvent, PacketWrapper<?>, PlatformPlayClientChatPacket<PacketWrapper<?>>> listener) {
        platform.getPacketEventsAPI().getEventManager().registerListener(new PacketListener() {
            @Override
            public void onPacketReceive(@NotNull PacketReceiveEvent event) {
                PacketTypeCommon type = event.getPacketType();
                if (type == PacketType.Play.Client.CHAT_MESSAGE) {
                    listener.handle(new PacketEventsPacketEvent<>(event, e -> {
                        WrapperPlayClientChatMessage wrapper = new WrapperPlayClientChatMessage((PacketReceiveEvent) e);
                        return new PacketEventsPlayClientChatPacket(wrapper);
                    }));
                }
            }
        }, c(priority));
    }

    @Override
    public void listenToPlayChatCommand(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<ProtocolPacketEvent, PacketWrapper<?>, PlatformPlayClientChatCommandPacket<PacketWrapper<?>>> listener) {
        platform.getPacketEventsAPI().getEventManager().registerListener(new PacketListener() {
            @Override
            public void onPacketReceive(@NotNull PacketReceiveEvent event) {
                PacketTypeCommon type = event.getPacketType();
                if (type == PacketType.Play.Client.CHAT_COMMAND) {
                    listener.handle(new PacketEventsPacketEvent<>(event, e -> {
                        WrapperPlayClientChatCommand wrapper = new WrapperPlayClientChatCommand((PacketReceiveEvent) e);
                        e.setLastUsedWrapper(wrapper);
                        return new PacketEventsPlayClientChatCommandPacket(wrapper);
                    }));

                    PacketWrapper<?> wrapper = event.getLastUsedWrapper();
                    if (wrapper != null) {
                        event.setByteBuf(wrapper);
                    }
                } else if (type == PacketType.Play.Client.CHAT_COMMAND_UNSIGNED) {
                    listener.handle(new PacketEventsPacketEvent<>(event, e -> {
                        WrapperPlayClientChatCommandUnsigned wrapper = new WrapperPlayClientChatCommandUnsigned((PacketReceiveEvent) e);
                        return new PacketEventsPlayClientChatCommandPacket(wrapper);
                    }));
                }
            }
        }, c(priority));
    }

    @Override
    public void listenToPlayServerServerData(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<ProtocolPacketEvent, PacketWrapper<?>, PlatformPlayServerServerDataPacket<PacketWrapper<?>>> listener) {
        platform.getPacketEventsAPI().getEventManager().registerListener(new PacketListener() {
            @Override
            public void onPacketSend(@NotNull PacketSendEvent event) {
                PacketTypeCommon type = event.getPacketType();
                if (type == PacketType.Play.Server.SERVER_DATA) {
                    listener.handle(new PacketEventsPacketEvent<>(event, e -> {
                        WrapperPlayServerServerData wrapper = new WrapperPlayServerServerData((PacketSendEvent) e);
                        return new PacketEventsPlayServerServerDataPacket(wrapper);
                    }));
                }
            }
        }, c(priority));
    }

    @Override
    public void listenToPlayServerUnifiedChatMessage(Plugin plugin, PlatformPacketListenerPriority priority, PlatformPacketEventListener<ProtocolPacketEvent, PacketWrapper<?>, PlatformPlayServerUnifiedChatMessagePacket<PacketWrapper<?>>> listener) {
        platform.getPacketEventsAPI().getEventManager().registerListener(new PacketListener() {
            @Override
            public void onPacketSend(@NotNull PacketSendEvent event) {
                PacketTypeCommon type = event.getPacketType();
                PacketEventsOutMessagePacketHelper.PacketEventsHandler<?> handler = PacketEventsOutMessagePacketHelper.PACKET_HANDLERS.get(type);

                if (handler != null) {
                    listener.handle(new PacketEventsPacketEvent<>(event, e -> {
                        PacketWrapper<?> wrapper = handler.getWrapper().apply((PacketSendEvent) e);
                        return new PacketEventsPlayServerUnifiedChatMessagePacket(wrapper, handler.getHandler(), handler.getCloner());
                    }));
                }
            }
        }, c(priority));
    }
}
