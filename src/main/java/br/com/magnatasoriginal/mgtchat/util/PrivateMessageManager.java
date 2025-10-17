package br.com.magnatasoriginal.mgtchat.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PrivateMessageManager {
    private static final Map<UUID, UUID> lastMessengers = new HashMap<>();

    public static void setLastMessenger(ServerPlayer receiver, ServerPlayer sender) {
        lastMessengers.put(receiver.getUUID(), sender.getUUID());
    }

    @Nullable
    public static ServerPlayer getLastMessenger(ServerPlayer player, MinecraftServer server) {
        UUID last = lastMessengers.get(player.getUUID());
        return last != null ? server.getPlayerList().getPlayer(last) : null;
    }
}
