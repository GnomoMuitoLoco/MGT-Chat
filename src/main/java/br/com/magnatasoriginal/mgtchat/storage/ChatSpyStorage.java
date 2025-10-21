package br.com.magnatasoriginal.mgtchat.storage;

import net.minecraft.server.level.ServerPlayer;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Armazena jogadores em modo "spy" (veem mensagens privadas de outros).
 *
 * WHY: Permite moderadores monitorarem conversas privadas para detectar
 * comportamento inapropriado. Controverso mas comum em servidores.
 *
 * Thread-safe: Usa ConcurrentHashMap.newKeySet().
 *
 * @since 1.1.0
 */
public class ChatSpyStorage {

    private final Set<UUID> spyingPlayers = ConcurrentHashMap.newKeySet();

    /**
     * Ativa spy mode para um jogador.
     */
    public void enableSpy(UUID player) {
        spyingPlayers.add(player);
    }

    /**
     * Desativa spy mode para um jogador.
     *
     * @return true se estava ativo e foi desativado
     */
    public boolean disableSpy(UUID player) {
        return spyingPlayers.remove(player);
    }

    /**
     * Verifica se um jogador está em spy mode.
     */
    public boolean isSpying(ServerPlayer player) {
        return spyingPlayers.contains(player.getUUID());
    }

    /**
     * Obtém todos os jogadores em spy mode.
     *
     * WHY: Usado por MessageBroadcaster para enviar cópia de mensagens privadas.
     */
    public Set<UUID> getSpyingPlayers() {
        return Set.copyOf(spyingPlayers);
    }

    /**
     * Limpa spy mode ao desconectar.
     */
    public void clearPlayerData(UUID player) {
        spyingPlayers.remove(player);
    }
}

