package br.com.magnatasoriginal.mgtchat.service;

import br.com.magnatasoriginal.mgtchat.config.ChatConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serviço de detecção de spam (rate limiting e mensagens repetidas).
 *
 * WHY: Separar a lógica de anti-spam do ChatEventHandler para melhor
 * testabilidade e aplicação do Single Responsibility Principle.
 *
 * Thread-safe: Usa ConcurrentHashMap para suportar ambiente multi-threaded.
 *
 * @since 1.1.0
 */
public class AntiSpamService {

    private final Map<UUID, Long> lastMessageTime = new ConcurrentHashMap<>();
    private final Map<UUID, String> lastMessageContent = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> repeatedCount = new ConcurrentHashMap<>();

    /**
     * Verifica se o jogador pode enviar uma mensagem ou está em spam.
     *
     * WHY: Centraliza todas as validações de anti-spam em um único lugar,
     * permitindo que diferentes canais (global, local, privado) usem a mesma lógica.
     *
     * @param player Jogador enviando a mensagem
     * @param message Conteúdo da mensagem
     * @return Optional.empty() se permitido, ou Optional.of(reason) se bloqueado
     */
    public Optional<Component> checkSpam(ServerPlayer player, String message) {
        UUID uuid = player.getUUID();

        // Rate limiting check
        Optional<Component> rateLimitError = checkRateLimit(uuid);
        if (rateLimitError.isPresent()) {
            return rateLimitError;
        }

        // Repeated messages check
        Optional<Component> repeatedError = checkRepeatedMessage(uuid, message);
        if (repeatedError.isPresent()) {
            return repeatedError;
        }

        // Update trackers
        updateTrackers(uuid, message);

        return Optional.empty();
    }

    /**
     * Verifica se o jogador está enviando mensagens muito rápido.
     *
     * WHY: Prevenir flood/spam no chat.
     */
    private Optional<Component> checkRateLimit(UUID uuid) {
        int delay = ChatConfig.COMMON.messageDelay.get();
        if (delay <= 0) return Optional.empty();

        long now = System.currentTimeMillis();
        long lastTime = lastMessageTime.getOrDefault(uuid, 0L);

        if ((now - lastTime) < delay) {
            return Optional.of(Component.literal("§cVocê está enviando mensagens muito rápido!"));
        }

        return Optional.empty();
    }

    /**
     * Verifica se o jogador está repetindo a mesma mensagem.
     *
     * WHY: Prevenir spam repetitivo.
     */
    private Optional<Component> checkRepeatedMessage(UUID uuid, String message) {
        if (!ChatConfig.COMMON.blockRepeated.get()) {
            return Optional.empty();
        }

        String lastMsg = lastMessageContent.getOrDefault(uuid, "");

        if (message.equalsIgnoreCase(lastMsg)) {
            int count = repeatedCount.getOrDefault(uuid, 1) + 1;
            repeatedCount.put(uuid, count);

            if (count > ChatConfig.COMMON.maxRepeated.get()) {
                return Optional.of(Component.literal("§cMensagem repetida bloqueada."));
            }
        } else {
            repeatedCount.put(uuid, 1);
        }

        return Optional.empty();
    }

    /**
     * Atualiza os trackers de tempo e conteúdo da última mensagem.
     */
    private void updateTrackers(UUID uuid, String message) {
        lastMessageTime.put(uuid, System.currentTimeMillis());
        lastMessageContent.put(uuid, message);
    }

    /**
     * Limpa os dados de um jogador específico (útil quando jogador desconecta).
     *
     * WHY: Prevenir memory leak de jogadores que saíram do servidor.
     */
    public void clearPlayerData(UUID uuid) {
        lastMessageTime.remove(uuid);
        lastMessageContent.remove(uuid);
        repeatedCount.remove(uuid);
    }
}