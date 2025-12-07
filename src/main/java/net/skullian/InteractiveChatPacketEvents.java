package net.skullian;

import com.loohp.interactivechat.InteractiveChat;
import net.skullian.command.CommandHandler;
import net.skullian.platform.PacketEventsPlatform;
import net.skullian.updater.UpdateListener;
import net.skullian.util.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class InteractiveChatPacketEvents extends JavaPlugin {

    public static InteractiveChatPacketEvents instance;
    private static boolean debug = false;

    @Override
    public void onLoad() {
        instance = this;

        InteractiveChat.protocolPlatform = new PacketEventsPlatform();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        debug = InteractiveChatPacketEvents.instance.getConfig().getBoolean("Debug");

        ChatUtils.sendMessage("Initialising ProtocolProvider.");

        getServer().getPluginManager().registerEvents(new UpdateListener(), this);

        //new CommandHandler();
    }

    @Override
    public void onDisable() {
    }

    public static void sendDebug(String message) {
        if (debug) {
            instance.getServer().getConsoleSender().sendMessage(ChatColor.GRAY + "[ICPE|DEBUG] " + message);
        }
    }
}
