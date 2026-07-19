package net.skullian.util;

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

// to be brutally honest, this is just bloat, but I can't actually be bothered to remove it
public class ChatUtils {

    public static final String PREFIX = "<gray>[<reset><b><gradient:#0836FB:#00B6FF>InteractiveChat-PacketEvents</gradient></b><gray>]<reset>";

    public static void sendMessage(Object message, CommandSender... senders) {
        if (senders.length == 0) {
            senders = new ConsoleCommandSender[] {Bukkit.getConsoleSender()};
        }
        Component component = message instanceof Component ? MiniMessage.miniMessage().deserialize(PREFIX).append(Component.text(" ")).append((Component) message) : MiniMessage.miniMessage().deserialize(PREFIX + " " + message);
        BaseComponent spigotComponent = ComponentSerializer.deserialize(GsonComponentSerializer.gson().serialize(component));
        for (CommandSender sender : senders) {
            sender.spigot().sendMessage(spigotComponent);
        }
    }
}
