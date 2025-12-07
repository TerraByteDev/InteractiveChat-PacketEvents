package net.skullian.platform.packets;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerServerData;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerServerDataPacket;
import com.loohp.interactivechat.utils.MCVersion;

public class PacketEventsPlayServerServerDataPacket extends PlatformPlayServerServerDataPacket<PacketWrapper<?>> {

    public PacketEventsPlayServerServerDataPacket(PacketWrapper<?> handle) {
        super(handle);
    }

    @Override
    public PacketEventsPlayServerServerDataPacket shallowClone() {
        WrapperPlayServerServerData data = (WrapperPlayServerServerData) handle;
        return new PacketEventsPlayServerServerDataPacket(new WrapperPlayServerServerData(data.getMOTD(), data.getIcon().orElse(null), data.isPreviewsChat(), data.isEnforceSecureChat()));
    }

    @Override
    public void setServerUnsignedStatus(boolean status) {
        ((WrapperPlayServerServerData) handle).setEnforceSecureChat(status);
    }

}
