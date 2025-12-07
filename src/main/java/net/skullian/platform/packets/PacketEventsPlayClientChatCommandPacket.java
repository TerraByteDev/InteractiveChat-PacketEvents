package net.skullian.platform.packets;

import com.github.retrooper.packetevents.protocol.chat.SignedCommandArgument;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommandUnsigned;
import com.loohp.interactivechat.platform.packets.PlatformPlayClientChatCommandPacket;

import java.util.List;

public class PacketEventsPlayClientChatCommandPacket extends PlatformPlayClientChatCommandPacket<PacketWrapper<?>> {

    public PacketEventsPlayClientChatCommandPacket(PacketWrapper<?> handle) {
        super(handle);
    }

    @Override
    public PacketEventsPlayClientChatCommandPacket shallowClone() {
        if (handle instanceof WrapperPlayClientChatCommand) {
            WrapperPlayClientChatCommand command = (WrapperPlayClientChatCommand) handle;
            WrapperPlayClientChatCommand clone;
            if (command.getLastSeenMessages() == null) {
                clone = new WrapperPlayClientChatCommand(command.getCommand(), command.getMessageSignData(), command.getSignedArguments(), command.getLegacyLastSeenMessages());
            } else {
                clone = new WrapperPlayClientChatCommand(command.getCommand(), command.getMessageSignData(), command.getSignedArguments(), command.getLastSeenMessages());
            }
            return new PacketEventsPlayClientChatCommandPacket(clone);
        } else {
            WrapperPlayClientChatCommandUnsigned command = (WrapperPlayClientChatCommandUnsigned) handle;
            return new PacketEventsPlayClientChatCommandPacket(new WrapperPlayClientChatCommandUnsigned(command.getCommand()));
        }
    }

    @Override
    public boolean hasArgumentSignatureEntries() {
        if (handle instanceof WrapperPlayClientChatCommand) {
            List<SignedCommandArgument> data = ((WrapperPlayClientChatCommand) handle).getSignedArguments();
            return !data.isEmpty();
        }
        return false;
    }

    @Override
    public String getCommand() {
        if (handle instanceof WrapperPlayClientChatCommand) {
            return ((WrapperPlayClientChatCommand) handle).getCommand();
        } else {
            return ((WrapperPlayClientChatCommandUnsigned) handle).getCommand();
        }
    }

}
