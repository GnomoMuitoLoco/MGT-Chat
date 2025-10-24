package br.com.magnatasoriginal.mgtchat.service;

import br.com.magnatasoriginal.mgtchat.config.ChatConfig;
import br.com.magnatasoriginal.mgtcore.util.ColorUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Serviço centralizado de formatação de mensagens de chat.
 *
 * WHY: Evitar duplicação de lógica de formatação entre ChatEventHandler,
 * LocalCommand, GlobalCommand e TellCommand. Facilita manutenção e testes.
 *
 * @since 1.1.0
 */
public class ChatFormatterService {

    /**
     * Formata uma mensagem do chat global.
     *
     * WHY: Centraliza a formatação para garantir consistência entre
     * ChatEventHandler e GlobalCommand.
     *
     * @param sender Jogador enviando a mensagem
     * @param message Conteúdo da mensagem
     * @return Component formatado pronto para envio
     */
    public Component formatGlobalMessage(ServerPlayer sender, String message) {
        String prefix = getPlayerPrefix(sender);
        String suffix = getPlayerSuffix(sender);

        String formatted = ChatConfig.COMMON.globalFormat.get()
            .replace("{prefix}", prefix)
            .replace("{suffix}", suffix)
            .replace("{player}", sender.getName().getString())
            .replace("{message_color}", ChatConfig.COMMON.globalMessageColor.get())
            .replace("{message}", message);

        return ColorUtil.translate(formatted);
    }

    /**
     * Formata uma mensagem do chat local.
     *
     * WHY: Centraliza a formatação para garantir consistência entre
     * ChatEventHandler e LocalCommand.
     *
     * @param sender Jogador enviando a mensagem
     * @param message Conteúdo da mensagem
     * @return Component formatado pronto para envio
     */
    public Component formatLocalMessage(ServerPlayer sender, String message) {
        String prefix = getPlayerPrefix(sender);
        String suffix = getPlayerSuffix(sender);

        String formatted = ChatConfig.COMMON.localFormat.get()
            .replace("{prefix}", prefix)
            .replace("{suffix}", suffix)
            .replace("{player}", sender.getName().getString())
            .replace("{message_color}", ChatConfig.COMMON.localMessageColor.get())
            .replace("{message}", message);

        return ColorUtil.translate(formatted);
    }

    /**
     * Formata uma mensagem privada (tell/reply).
     *
     * WHY: Centraliza formatação de mensagens privadas.
     *
     * @param from Remetente
     * @param to Destinatário
     * @param message Conteúdo
     * @param isReply Se é uma resposta (/r) ou tell novo
     * @param isForSender Se é a mensagem mostrada ao remetente ou ao destinatário
     * @return Component formatado
     */
    public Component formatPrivateMessage(ServerPlayer from, ServerPlayer to, String message,
                                         boolean isReply, boolean isForSender) {
        String format;

        if (isReply) {
            format = isForSender
                ? ChatConfig.COMMON.replyTellFormatTo.get()
                : ChatConfig.COMMON.replyTellFormatFrom.get();
        } else {
            format = isForSender
                ? ChatConfig.COMMON.tellFormatTo.get()
                : ChatConfig.COMMON.tellFormatFrom.get();
        }

        String formatted = format
            .replace("{send_player}", from.getName().getString())
            .replace("{receive_player}", to.getName().getString())
            .replace("{message}", message);

        return ColorUtil.translate(formatted);
    }

    /**
     * Obtém o prefixo do jogador (via LuckPerms ou fallback vazio).
     *
     * WHY: Integração com LuckPerms para resolução de prefixos.
     * Retorna string vazia se LuckPerms não estiver disponível.
     *
     * @param player Jogador
     * @return Prefixo do jogador (pode conter códigos de cor)
     */
    private String getPlayerPrefix(ServerPlayer player) {
        if (br.com.magnatasoriginal.mgtchat.integration.LuckPermsIntegration.isLoaded()) {
            String prefix = br.com.magnatasoriginal.mgtchat.integration.LuckPermsIntegration.getPrefix(player);
            // NOTE: Remove trailing space if prefix exists to prevent double spaces
            return prefix.isEmpty() ? "" : prefix + " ";
        }
        return "";
    }

    /**
     * Obtém o sufixo do jogador (via LuckPerms ou fallback vazio).
     *
     * WHY: Integração com LuckPerms para resolução de sufixos.
     * Retorna string vazia se LuckPerms não estiver disponível.
     *
     * @param player Jogador
     * @return Sufixo do jogador (pode conter códigos de cor)
     */
    private String getPlayerSuffix(ServerPlayer player) {
        if (br.com.magnatasoriginal.mgtchat.integration.LuckPermsIntegration.isLoaded()) {
            String suffix = br.com.magnatasoriginal.mgtchat.integration.LuckPermsIntegration.getSuffix(player);
            // NOTE: Add leading space if suffix exists
            return suffix.isEmpty() ? "" : " " + suffix;
        }
        return "";
    }
}


