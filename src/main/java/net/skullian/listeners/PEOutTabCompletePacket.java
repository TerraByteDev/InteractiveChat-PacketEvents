package net.skullian.listeners;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTabComplete;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.libs.net.kyori.adventure.text.Component;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.utils.NativeAdventureConverter;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.loohp.interactivechat.listeners.packet.OutTabCompletePacketHandler.createComponent;
import static com.loohp.interactivechat.listeners.packet.OutTabCompletePacketHandler.findICPlayer;

public class PEOutTabCompletePacket implements PacketListener {

    private static Method commandMatchSetTooltipMethod;

    static {
        try {
            commandMatchSetTooltipMethod = Arrays.stream(WrapperPlayServerTabComplete.CommandMatch.class.getMethods())
                    .filter(each -> each.getName().equals("setTooltip") && each.getParameterCount() == 1).findFirst().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (shouldProcessPacket(event)) {
            processPacket(event);
        }
    }

    private static boolean shouldProcessPacket(PacketSendEvent event) {
        return !event.isCancelled() && event.getPacketType().equals(PacketType.Play.Server.TAB_COMPLETE);
    }

    private static void processPacket(PacketSendEvent event) {
        WrapperPlayServerTabComplete packet = new WrapperPlayServerTabComplete(event);
        Player tabCompleter = event.getPlayer();

        List<WrapperPlayServerTabComplete.CommandMatch> matches = packet.getCommandMatches();

        List<WrapperPlayServerTabComplete.CommandMatch> newMatches = new ArrayList<>();
        for (WrapperPlayServerTabComplete.CommandMatch match : matches) {
            newMatches.add(processMatch(match, tabCompleter));
        }

        packet.setCommandMatches(newMatches);
        event.markForReEncode(true);
    }

    private static WrapperPlayServerTabComplete.CommandMatch processMatch(WrapperPlayServerTabComplete.CommandMatch match, Player tabCompleter) {
        String text = match.getText();
        int pos = text.indexOf("\0");
        if (pos < 0) {
            return processTextMatch(match, tabCompleter);
        } else {
            return processTooltipMatch(match, pos, text);
        }
    }

    private static WrapperPlayServerTabComplete.CommandMatch processTextMatch(WrapperPlayServerTabComplete.CommandMatch match, Player tabCompleter) {
        if (InteractiveChat.useTooltipOnTab) {
            try {
                ICPlayer icPlayer = findICPlayer(match.getText());
                if (icPlayer != null) {
                    Component component = createComponent(icPlayer, tabCompleter);
                    Object nativeComponent = NativeAdventureConverter.componentToNative(component, false);

                    commandMatchSetTooltipMethod.invoke(
                            match,
                            nativeComponent
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return match;
    }

    private static WrapperPlayServerTabComplete.CommandMatch processTooltipMatch(WrapperPlayServerTabComplete.CommandMatch match, int pos, String text) {
        try {
            String tooltip = text.substring(pos + 1);
            text = text.substring(0, pos);

            match.setText(text);

            Object nativeComponent = NativeAdventureConverter.componentToNative(Component.text(tooltip), false);
            commandMatchSetTooltipMethod.invoke(
                    match,
                    nativeComponent
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return match;
    }
}
