package net.skullian.updater;

import com.loohp.interactivechat.libs.com.loohp.platformscheduler.Scheduler;
import net.skullian.InteractiveChatPacketEvents;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerLoadEvent;

import static net.skullian.updater.Updater.checkUpdate;

public class UpdateListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Scheduler.runTaskLaterAsynchronously(InteractiveChatPacketEvents.instance, () -> {
            Player player = event.getPlayer();
            if (player.hasPermission("interactivechatpacketevents.checkupdate")) {
                checkUpdate(player);
            }
        }, 100);
    }

    @EventHandler
    public void onStart(ServerLoadEvent event) {
        checkUpdate(Bukkit.getConsoleSender());
    }
}
