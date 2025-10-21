package br.com.magnatasoriginal.mgtchat.storage;

import br.com.magnatasoriginal.mgtchat.util.ChatChannelManager;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Armazena o canal atual de cada jogador (GLOBAL ou LOCAL).
 *
 * WHY: Migrado de util.ChatChannelManager para storage layer,
 * mantendo compatibilidade retroativa através de delegação.
 *
 * Thread-safe: Usa ConcurrentHashMap.
 *
 * @since 1.1.0
 */
public class PlayerChannelStorage {

    private final Map<UUID, ChatChannelManager.Channel> playerChannels = new ConcurrentHashMap<>();

    /**
     * Define o canal do jogador.
     *
     * @param player Jogador
     * @param channel Canal (GLOBAL ou LOCAL)
     */
    public void setChannel(ServerPlayer player, ChatChannelManager.Channel channel) {
        playerChannels.put(player.getUUID(), channel);
    }

    /**
     * Obtém o canal atual do jogador.
     *
     * WHY: Padrão é GLOBAL se nunca foi definido.
     *
     * @param player Jogador
     * @return Canal atual (GLOBAL por padrão)
     */
    public ChatChannelManager.Channel getChannel(ServerPlayer player) {
        return playerChannels.getOrDefault(player.getUUID(), ChatChannelManager.Channel.GLOBAL);
    }

    /**
     * Limpa dados do canal do jogador (quando desconecta).
     *
     * WHY: Prevenir memory leak.
     *
     * @param player UUID do jogador
     */
    public void clearPlayerData(UUID player) {
        playerChannels.remove(player);
    }

    /**
     * Verifica se um jogador está no canal local.
     *
     * @param player Jogador
     * @return true se está em LOCAL
     */
    public boolean isInLocalChannel(ServerPlayer player) {
        return getChannel(player) == ChatChannelManager.Channel.LOCAL;
    }
}


