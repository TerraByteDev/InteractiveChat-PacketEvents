# InteractiveChat-PacketEvents (WIP)
An addon plugin for InteractiveChat to support PacketEvents instead of ProtocolLib.\
This is a very simple plugin that implements all the code seen [here](https://github.com/Skullians/InteractiveChatPacketEvents/tree/master/common/src/main/java/com/loohp/interactivechat/listeners/packet).\
Very little modifications to code have been made, apart from making it support PacketEvents.

> [!WARNING]
> This plugin **REQUIRES** v4.3.5.0 of InteractiveChat, or higher.\
> This plugin will NOT boot otherwise.


## Why use another plugin?
PacketEvents shades Adventure, but doesn't relocate it.\
InteractiveChat shades Adventure **and** relocates it.

This creates several issues when it comes to using Adventure, which IC uses heavily.\
It is possible to do this through Reflection within IC itself, however that impacts maintainability and readability.

In the end, we decided to split PacketEvents support into two parts:
1. Remove the inherent dependency on ProtocolLib.
2. Allow external plugins like this to register their own ProtocolProvider.

This allows us to use the now-reflected Adventure API for InteractiveChat, and the normal Adventure which PE / the server will provide.

You can see the corresponding PR here: https://github.com/LOOHP/InteractiveChat/pull/256

## Setup
1. Install the latest version of PacketEvents, and this plugin.
2. Start your server

## Permissions
Requires the `interactivechatpacketevents.checkupdate` permission to check for updates on join.
