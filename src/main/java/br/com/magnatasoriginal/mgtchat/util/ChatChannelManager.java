package br.com.magnatasoriginal.mgtchat.util;

import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatChannelManager {
    public enum Channel { GLOBAL, LOCAL }

    private static final Map<UUID, Channel> playerChannels = new ConcurrentHashMap<>();

    public static void setChannel(ServerPlayer player, Channel channel) {
        playerChannels.put(player.getUUID(), channel);
    }

    public static Channel getChannel(ServerPlayer player) {
        return playerChannels.getOrDefault(player.getUUID(), Channel.GLOBAL);
    }
}
