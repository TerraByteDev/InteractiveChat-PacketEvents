package net.skullian;

import com.loohp.interactivechat.InteractiveChat;
import net.skullian.platform.PacketEventsPlatform;
import org.bukkit.plugin.java.JavaPlugin;

public final class InteractiveChatPacketEvents extends JavaPlugin {

    @Override
    public void onLoad() {
        InteractiveChat.protocolPlatform = new PacketEventsPlatform();
    }

    @Override
    public void onEnable() {
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
