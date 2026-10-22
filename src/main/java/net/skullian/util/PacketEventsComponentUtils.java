package net.skullian.util;

import io.github.retrooper.packetevents.adventure.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.Component;

public class PacketEventsComponentUtils {

    public static Component deserializePacketEventsComponent(String json) {
        return GsonComponentSerializer.gson().deserialize(json);
    }

    public static String serializePacketEventsComponent(Component component) {
        return GsonComponentSerializer.gson().serialize(component);
    }

}
