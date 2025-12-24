package net.skullian.platform;

import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.loohp.interactivechat.platform.PlatformPacketEvent;
import com.loohp.interactivechat.platform.packets.PlatformPacket;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Function;

public class PacketEventsPacketEvent<PlatformPacketTyped extends PlatformPacket<PacketWrapper<?>>> implements PlatformPacketEvent<ProtocolPacketEvent, PacketWrapper<?>, PlatformPacketTyped> {

    private final ProtocolPacketEvent handle;
    private final PlatformPacketTyped packetTyped;

    public PacketEventsPacketEvent(ProtocolPacketEvent handle, Function<ProtocolPacketEvent, PlatformPacketTyped> converter) {
        this.handle = handle;
        this.packetTyped = converter.apply(handle);
    }

    @Override
    public ProtocolPacketEvent getHandle() {
        return handle;
    }

    @Override
    public PlatformPacketTyped getPacket() {
        return packetTyped;
    }

    @Override
    public Player getPlayer() {
        return handle.getPlayer();
    }

    @Override
    public boolean isPlayerTemporary() {
        ConnectionState state = handle.getConnectionState();
        return state == ConnectionState.HANDSHAKING || state == ConnectionState.STATUS;
    }

    @Override
    public UUID getPlayerUniqueId() {
        return handle.getUser().getUUID();
    }

    @Override
    public Object getIdentityObject() {
        return handle.getAddress();
    }

    @Override
    public boolean isCancelled() {
        return handle.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancelled) {
        handle.setCancelled(cancelled);
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        //do nothing
    }

    @Override
    public boolean isFiltered() {
        return true;
    }

}
