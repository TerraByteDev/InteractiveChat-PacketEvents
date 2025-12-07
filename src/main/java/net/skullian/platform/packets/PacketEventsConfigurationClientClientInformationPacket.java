package net.skullian.platform.packets;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.configuration.client.WrapperConfigClientSettings;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSettings;
import com.loohp.interactivechat.platform.packets.PlatformConfigurationClientClientInformationPacket;

public class PacketEventsConfigurationClientClientInformationPacket extends PlatformConfigurationClientClientInformationPacket<PacketWrapper<?>> {

    public PacketEventsConfigurationClientClientInformationPacket(PacketWrapper<?> handle) {
        super(handle);
    }

    @Override
    public PacketEventsConfigurationClientClientInformationPacket shallowClone() {
        if (handle instanceof WrapperConfigClientSettings) {
            WrapperConfigClientSettings settings = (WrapperConfigClientSettings) handle;
            return new PacketEventsConfigurationClientClientInformationPacket(new WrapperConfigClientSettings(settings.getLocale(), settings.getViewDistance(), settings.getChatVisibility(), settings.isChatColors(),settings.getSkinMask(), settings.getMainHand(), settings.isTextFilteringEnabled(), settings.isServerListingAllowed(), settings.getParticleStatus()));
        } else {
            WrapperPlayClientSettings settings = (WrapperPlayClientSettings) handle;
            return new PacketEventsConfigurationClientClientInformationPacket(new WrapperPlayClientSettings(settings.getLocale(), settings.getViewDistance(), settings.getChatVisibility(), settings.isChatColors(),settings.getSkinMask(), settings.getMainHand(), settings.isTextFilteringEnabled(), settings.isServerListingAllowed(), settings.getParticleStatus()));
        }
    }

    @Override
    public boolean getColorSettings() {
        if (handle instanceof WrapperConfigClientSettings) {
            return ((WrapperConfigClientSettings) handle).isChatColors();
        } else {
            return ((WrapperPlayClientSettings) handle).isChatColors();
        }
    }

}
