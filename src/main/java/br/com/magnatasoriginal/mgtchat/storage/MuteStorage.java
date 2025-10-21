package br.com.magnatasoriginal.mgtchat.storage;

import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Armazena jogadores mutados (silenciados).
 *
 * WHY: Thread-safe storage para sistema de mute com duração.
 * Preparado para persistência futura.
 *
 * @since 1.1.0
 */
public class MuteStorage {

    /**
     * Armazena UUID -> timestamp de quando o mute expira (0 = permanente).
     */
    private final Map<UUID, Long> mutedPlayers = new ConcurrentHashMap<>();

    /**
     * Muta um jogador permanentemente.
     */
    public void mute(UUID player) {
        mutedPlayers.put(player, 0L);
    }

    /**
     * Muta um jogador por duração específica.
     *
     * @param player UUID do jogador
     * @param durationMillis Duração em milissegundos
     */
    public void mute(UUID player, long durationMillis) {
        long expiresAt = System.currentTimeMillis() + durationMillis;
        mutedPlayers.put(player, expiresAt);
    }

    /**
     * Remove mute de um jogador.
     *
     * @return true se estava mutado e foi removido
     */
    public boolean unmute(UUID player) {
        return mutedPlayers.remove(player) != null;
    }

    /**
     * Verifica se um jogador está mutado.
     *
     * WHY: Também remove mutes expirados automaticamente.
     */
    public boolean isMuted(ServerPlayer player) {
        UUID uuid = player.getUUID();
        Long expiresAt = mutedPlayers.get(uuid);

        if (expiresAt == null) {
            return false;
        }

        // Mute permanente
        if (expiresAt == 0L) {
            return true;
        }

        // Verifica se expirou
        if (System.currentTimeMillis() >= expiresAt) {
            mutedPlayers.remove(uuid);
            return false;
        }

        return true;
    }

    /**
     * Obtém tempo restante de mute em milissegundos.
     *
     * @return tempo restante ou -1 se permanente, 0 se não mutado
     */
    public long getRemainingTime(UUID player) {
        Long expiresAt = mutedPlayers.get(player);

        if (expiresAt == null) {
            return 0L;
        }

        if (expiresAt == 0L) {
            return -1L; // Permanente
        }

        long remaining = expiresAt - System.currentTimeMillis();
        return Math.max(0L, remaining);
    }

    /**
     * Obtém todos os jogadores mutados.
     */
    public Set<UUID> getMutedPlayers() {
        return Set.copyOf(mutedPlayers.keySet());
    }

    /**
     * Limpa mutes expirados (chamado periodicamente).
     */
    public void cleanExpired() {
        long now = System.currentTimeMillis();
        mutedPlayers.entrySet().removeIf(entry -> {
            long expiresAt = entry.getValue();
            return expiresAt != 0L && now >= expiresAt;
        });
    }

    // TODO: Adicionar save() e load() para persistir em disco
}

