package net.skullian.util;

import net.kyori.adventure.text.Component;

public class PacketEventsComponentUtils {

    public static Component deserializePacketEventsComponent(String json) {
        return io.github.retrooper.packetevents.adventure.serializer.gson.GsonComponentSerializer.gson().deserialize(json);
    }

}
