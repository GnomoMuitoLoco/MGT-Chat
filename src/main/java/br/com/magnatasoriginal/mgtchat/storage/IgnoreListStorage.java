package br.com.magnatasoriginal.mgtchat.storage;

import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Armazena a lista de jogadores ignorados de cada player.
 *
 * WHY: Centraliza gerenciamento de ignore list com thread-safety garantido.
 * Prepara para persistência futura (salvar em disco entre restarts).
 *
 * Thread-safe: Usa ConcurrentHashMap e ConcurrentHashMap.newKeySet()
 * para suportar operações concorrentes sem race conditions.
 *
 * @since 1.1.0
 */
public class IgnoreListStorage {

    private final Map<UUID, Set<UUID>> ignoredMap = new ConcurrentHashMap<>();

    /**
     * Adiciona um jogador à lista de ignorados.
     *
     * WHY: Thread-safe usando computeIfAbsent com ConcurrentHashMap.newKeySet().
     *
     * @param player UUID do jogador que está ignorando
     * @param ignored UUID do jogador sendo ignorado
     */
    public void addIgnore(UUID player, UUID ignored) {
        ignoredMap.computeIfAbsent(player, k -> ConcurrentHashMap.newKeySet()).add(ignored);
    }

    /**
     * Remove um jogador da lista de ignorados.
     *
     * @param player UUID do jogador que está removendo o ignore
     * @param ignored UUID do jogador sendo removido da lista
     * @return true se o jogador estava ignorado e foi removido, false caso contrário
     */
    public boolean removeIgnore(UUID player, UUID ignored) {
        Set<UUID> set = ignoredMap.get(player);
        if (set != null) {
            boolean removed = set.remove(ignored);
            // Limpa o set vazio para economizar memória
            if (set.isEmpty()) {
                ignoredMap.remove(player);
            }
            return removed;
        }
        return false;
    }

    /**
     * Verifica se um jogador está ignorando outro.
     *
     * WHY: Método principal usado por MessageBroadcaster para filtrar destinatários.
     *
     * @param listener Jogador que pode estar ignorando
     * @param speaker Jogador sendo verificado
     * @return true se listener está ignorando speaker
     */
    public boolean isIgnoring(ServerPlayer listener, ServerPlayer speaker) {
        Set<UUID> ignored = ignoredMap.get(listener.getUUID());
        return ignored != null && ignored.contains(speaker.getUUID());
    }

    /**
     * Obtém todos os jogadores ignorados por um player.
     *
     * WHY: Útil para comandos administrativos ou UI futura.
     *
     * @param player UUID do jogador
     * @return Set imutável de UUIDs ignorados (vazio se nenhum)
     */
    public Set<UUID> getIgnoredPlayers(UUID player) {
        Set<UUID> ignored = ignoredMap.get(player);
        return ignored != null ? Set.copyOf(ignored) : Set.of();
    }

    /**
     * Limpa todos os ignores de um jogador (quando desconecta).
     *
     * WHY: Prevenir memory leak. Nota: Em produção você pode querer
     * persistir isso em disco ao invés de limpar.
     *
     * @param player UUID do jogador
     */
    public void clearPlayerData(UUID player) {
        ignoredMap.remove(player);
    }

    /**
     * Retorna o número total de entradas de ignore.
     *
     * WHY: Útil para debugging e testes unitários.
     */
    public int getTotalIgnores() {
        return ignoredMap.values().stream()
            .mapToInt(Set::size)
            .sum();
    }

    // TODO: Adicionar métodos save() e load() para persistir em disco
    // TODO: Considerar usar JSON ou NBT para formato de arquivo
}

