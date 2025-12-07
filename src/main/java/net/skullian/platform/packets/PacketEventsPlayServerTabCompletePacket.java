package net.skullian.platform.packets;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTabComplete;
import com.loohp.interactivechat.objectholders.CommandSuggestion;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerTabCompletePacket;
import com.loohp.interactivechat.utils.ChatComponentType;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.kyori.adventure.text.Component;

import java.util.stream.Collectors;

public class PacketEventsPlayServerTabCompletePacket extends PlatformPlayServerTabCompletePacket<PacketWrapper<?>> {

    public PacketEventsPlayServerTabCompletePacket(PacketWrapper<?> handle) {
        super(handle);
    }

    @Override
    public PacketEventsPlayServerTabCompletePacket shallowClone() {
        WrapperPlayServerTabComplete tabComplete = (WrapperPlayServerTabComplete) handle;
        return new PacketEventsPlayServerTabCompletePacket(new WrapperPlayServerTabComplete(tabComplete.getTransactionId().orElse(null), tabComplete.getCommandRange().orElseThrow(IllegalStateException::new), tabComplete.getCommandMatches()));
    }

    @Override
    public CommandSuggestion<?> getCommandSuggestions() {
        WrapperPlayServerTabComplete tabComplete = (WrapperPlayServerTabComplete) handle;
        StringRange range = tabComplete.getCommandRange().map(r -> StringRange.between(r.getBegin(), r.getEnd())).orElseThrow(IllegalStateException::new);
        Suggestions suggestion = new Suggestions(range, tabComplete.getCommandMatches().stream()
                .map((m) -> new Suggestion(range, m.getText(), m.getTooltip().map(PacketEventsPlayServerTabCompletePacket::c).orElse(null))).collect(Collectors.toList()));
        return CommandSuggestion.of(tabComplete.getTransactionId().orElse(-1), suggestion);
    }

    private static Message c(Component component) {
        com.loohp.interactivechat.libs.net.kyori.adventure.text.Component icComponent = ChatComponentType.NativeAdventureComponent.convertFrom(component, null);
        return (Message) ChatComponentType.IChatBaseComponent.convertTo(icComponent, false);
    }

    @Override
    public void setPacket(PlatformPlayServerTabCompletePacket<?> packet) {
        WrapperPlayServerTabComplete other = (WrapperPlayServerTabComplete) packet.getHandle();
        WrapperPlayServerTabComplete tabComplete = (WrapperPlayServerTabComplete) handle;
        tabComplete.setTransactionId(other.getTransactionId().orElse(null));
        tabComplete.setCommandRange(other.getCommandRange().orElse(null));
        tabComplete.setCommandMatches(other.getCommandMatches());
    }

}
