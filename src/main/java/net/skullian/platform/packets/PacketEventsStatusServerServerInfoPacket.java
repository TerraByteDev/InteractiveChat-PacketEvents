package net.skullian.platform.packets;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.status.server.WrapperStatusServerResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loohp.interactivechat.platform.packets.PlatformStatusServerServerInfoPacket;

public class PacketEventsStatusServerServerInfoPacket extends PlatformStatusServerServerInfoPacket<PacketWrapper<?>> {

    private static final Gson GSON = new Gson();

    public PacketEventsStatusServerServerInfoPacket(PacketWrapper<?> handle) {
        super(handle);
    }

    @Override
    public PacketEventsStatusServerServerInfoPacket shallowClone() {
        WrapperStatusServerResponse response = (WrapperStatusServerResponse) handle;
        return new PacketEventsStatusServerServerInfoPacket(new WrapperStatusServerResponse(response.getComponentJson()));
    }

    @Override
    public String getMotd() {
        return GSON.toJson(((WrapperStatusServerResponse) handle).getComponent().get("description"));
    }

    @Override
    public void setMotd(String message) {
        WrapperStatusServerResponse response = (WrapperStatusServerResponse) handle;
        JsonObject component = response.getComponent();
        component.add("description", GSON.toJsonTree(message));
        response.setComponent(component);
    }
}
