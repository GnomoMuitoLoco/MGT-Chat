package br.com.magnatasoriginal.mgtchat.service;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

/**
 * Serviço de logging estruturado para mensagens de chat.
 *
 * WHY: Centraliza logs de todas as mensagens (global, local, privado, comandos)
 * para facilitar auditoria, debugging e análise de comportamento.
 *
 * Formato padrão: [MGT-Chat][CHANNEL] <Player>: message
 * Todos os logs vão para console e latest.log automaticamente.
 *
 * @since 1.1.0
 */
public class ChatLogger {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Loga uma mensagem do chat global.
     *
     * WHY: Permite rastrear todas as mensagens globais para moderação.
     */
    public void logGlobal(ServerPlayer sender, String message) {
        LOGGER.info("[MGT-Chat][GLOBAL] <{}> {}", sender.getName().getString(), message);
    }

    /**
     * Loga uma mensagem do chat local.
     *
     * WHY: Útil para verificar se mensagens locais estão funcionando corretamente.
     */
    public void logLocal(ServerPlayer sender, String message, int recipientCount) {
        LOGGER.info("[MGT-Chat][LOCAL] <{}> {} (alcançou {} jogadores)",
                sender.getName().getString(), message, recipientCount);
    }

    /**
     * Loga uma mensagem privada.
     *
     * WHY: Auditoria de mensagens privadas para moderação (controverso,
     * mas configurável via config).
     */
    public void logPrivate(ServerPlayer from, ServerPlayer to, String message) {
        LOGGER.info("[MGT-Chat][PRIVATE] {} -> {}: {}",
                from.getName().getString(), to.getName().getString(), message);
    }

    /**
     * Loga execução de comando administrativo.
     *
     * WHY: Rastrear quem usou comandos de moderação (mute, unmute, spy, etc).
     */
    public void logAdminCommand(ServerPlayer admin, String command, String target) {
        LOGGER.info("[MGT-Chat][ADMIN] {} executou: {} em {}",
                admin.getName().getString(), command, target);
    }

    /**
     * Loga bloqueio de spam.
     *
     * WHY: Identificar jogadores que tentam fazer spam repetidamente.
     */
    public void logSpamBlocked(ServerPlayer player, String reason) {
        LOGGER.warn("[MGT-Chat][SPAM] {} bloqueado: {}",
                player.getName().getString(), reason);
    }

    /**
     * Loga aviso de chat local vazio.
     *
     * WHY: Debug de problemas com alcance do chat local.
     */
    public void logLocalEmpty(ServerPlayer sender) {
        LOGGER.debug("[MGT-Chat][LOCAL] {} tentou enviar mensagem mas não há ninguém próximo",
                sender.getName().getString());
    }
}