package br.com.magnatasoriginal.mgtchat.util;

import br.com.magnatasoriginal.mgtchat.storage.PlayerChannelStorage;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @deprecated Use {@link PlayerChannelStorage} instead.
 * Esta classe permanece apenas para compatibilidade retroativa.
 *
 * WHY: Migrado para storage layer seguindo Clean Architecture.
 * Mantém API pública para não quebrar código externo.
 *
 * @since 1.0.0
 */
@Deprecated
public class ChatChannelManager {
    public enum Channel { GLOBAL, LOCAL }

    private static final Map<UUID, Channel> playerChannels = new ConcurrentHashMap<>();

    // TODO: Injetar PlayerChannelStorage aqui quando MGTChat for refatorado
    // Por enquanto mantém implementação inline

    public static void setChannel(ServerPlayer player, Channel channel) {
        playerChannels.put(player.getUUID(), channel);
    }

    public static Channel getChannel(ServerPlayer player) {
        return playerChannels.getOrDefault(player.getUUID(), Channel.GLOBAL);
    }
}
