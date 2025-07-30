package net.skullian.platform;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.libs.com.loohp.platformscheduler.ScheduledTask;
import com.loohp.interactivechat.libs.com.loohp.platformscheduler.Scheduler;
import com.loohp.interactivechat.objectholders.AsyncChatSendingExecutor;
import com.loohp.interactivechat.objectholders.OutboundPacket;
import org.bukkit.Bukkit;

import java.util.function.LongSupplier;

public class PacketEventsAsyncChatSendingExecutor extends AsyncChatSendingExecutor {
    public PacketEventsAsyncChatSendingExecutor(LongSupplier executionWaitTime, long killThreadAfter) {
        super(executionWaitTime, killThreadAfter);
    }

    @Override
    public ScheduledTask packetSender() {
        return Scheduler.runTaskTimer(InteractiveChat.plugin, () -> {
            while (!sendingQueue.isEmpty()) {
                OutboundPacket out = sendingQueue.poll();
                try {
                    if (out.getReciever().isOnline() && out.getPacket() != null) {
                        PacketWrapper<?> wrapper = (PacketWrapper<?>) out.getPacket();
                        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(out.getReciever(), wrapper);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1);
    }
}
