package br.com.magnatasoriginal.mgtchat.storage;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Armazena histórico de conversas privadas para suporte ao comando /r (reply).
 *
 * WHY: Centraliza gerenciamento de último messenger com thread-safety.
 * Substitui os Maps espalhados em TellCommand e PrivateMessageManager (removido).
 *
 * Thread-safe: Usa ConcurrentHashMap para operações concorrentes.
 *
 * @since 1.1.0
 */
public class ConversationStorage {

    private final Map<UUID, UUID> lastMessengers = new ConcurrentHashMap<>();

    /**
     * Registra que um jogador enviou mensagem privada para outro.
     *
     * WHY: Permite que o destinatário use /r para responder.
     *
     * @param receiver UUID do destinatário
     * @param sender UUID do remetente
     */
    public void setLastMessenger(UUID receiver, UUID sender) {
        lastMessengers.put(receiver, sender);
    }

    /**
     * Registra conversação bidirecional.
     *
     * WHY: Facilita quando ambos jogadores enviam /tell um para o outro,
     * ambos conseguem usar /r.
     *
     * @param player1 Primeiro jogador
     * @param player2 Segundo jogador
     */
    public void setConversation(UUID player1, UUID player2) {
        lastMessengers.put(player1, player2);
        lastMessengers.put(player2, player1);
    }

    /**
     * Obtém o último jogador que enviou mensagem privada.
     *
     * WHY: Usado pelo comando /r para determinar destinatário.
     *
     * @param player Jogador que quer responder
     * @param server Servidor para buscar o ServerPlayer
     * @return ServerPlayer do último messenger, ou null se não existe/offline
     */
    @Nullable
    public ServerPlayer getLastMessenger(ServerPlayer player, MinecraftServer server) {
        UUID lastUuid = lastMessengers.get(player.getUUID());
        if (lastUuid == null) {
            return null;
        }
        return server.getPlayerList().getPlayer(lastUuid);
    }

    /**
     * Limpa dados de conversação de um jogador (quando desconecta).
     *
     * WHY: Prevenir memory leak e evitar tentar responder para jogador offline.
     *
     * @param player UUID do jogador
     */
    public void clearPlayerData(UUID player) {
        lastMessengers.remove(player);
        // Também remove referências a este jogador em outras conversas
        lastMessengers.entrySet().removeIf(entry -> entry.getValue().equals(player));
    }

    /**
     * Verifica se um jogador tem alguém para responder.
     *
     * @param player UUID do jogador
     * @return true se existe histórico de conversa
     */
    public boolean hasConversation(UUID player) {
        return lastMessengers.containsKey(player);
    }
}

