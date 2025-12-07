package net.skullian.platform;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.loohp.interactivechat.platform.ProtocolPlatform;
import com.loohp.interactivechat.platform.packets.PlatformPacket;
import net.skullian.InteractiveChatPacketEvents;
import net.skullian.player.PacketEventsDummyPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class PacketEventsPlatform implements ProtocolPlatform<ProtocolPacketEvent, PacketWrapper<?>> {

    private final PacketEventsPacketListenerProvider listenerProvider;
    private final PacketEventsPacketCreatorProvider creatorProvider;

    public PacketEventsPlatform() {
        this.listenerProvider = new PacketEventsPacketListenerProvider();
        this.creatorProvider = new PacketEventsPacketCreatorProvider();
    }

    @Override
    public boolean hasChatSigning() {
        return PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_19_1);
    }

    @Override
    public int getProtocolVersion(Player player) {
        return PacketEvents.getAPI().getProtocolManager().getClientVersion(player).getProtocolVersion();
    }

    @Override
    public Player newTemporaryPlayer(String name, UUID uuid) {
        return PacketEventsDummyPlayer.newInstance(name, uuid);
    }

    @Override
    public void sendServerPacket(Player player, PlatformPacket<?> platformPacket, boolean filtered) {
        PacketWrapper<?> packet = (PacketWrapper<?>) platformPacket.shallowClone().getHandle();
        System.out.println(packet);
        if (filtered) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
        } else {
            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet);
        }
    }

    @Override
    public PacketEventsPacketListenerProvider getPlatformPacketListenerProvider() {
        return listenerProvider;
    }

    @Override
    public PacketEventsPacketCreatorProvider getPlatformPacketCreatorProvider() {
        return creatorProvider;
    }

    @Override
    public Plugin getRegisteredPlugin() {
        return InteractiveChatPacketEvents.instance;
    }

    @Override
    public Plugin getProtocolPlatformPlugin() {
        return (Plugin) PacketEvents.getAPI().getPlugin();
    }
}

