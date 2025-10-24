package br.com.magnatasoriginal.mgtchat.events;

import br.com.magnatasoriginal.mgtchat.MGTChat;
import br.com.magnatasoriginal.mgtchat.config.ChatConfig;
import br.com.magnatasoriginal.mgtchat.service.AntiSpamService;
import br.com.magnatasoriginal.mgtchat.service.ChatFormatterService;
import br.com.magnatasoriginal.mgtchat.service.ChatLogger;
import br.com.magnatasoriginal.mgtchat.service.MessageBroadcaster;
import br.com.magnatasoriginal.mgtchat.storage.IgnoreListStorage;
import br.com.magnatasoriginal.mgtchat.util.ChatChannelManager;
import br.com.magnatasoriginal.mgtcore.util.ColorUtil;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Handler principal de eventos de chat.
 *
 * WHY: Refatorado para usar services dedicados (SRP aplicado).
 * Reduzido de 127 linhas para ~70 linhas, removendo duplicação.
 *
 * @since 1.0.0
 * @version 1.1.0 - Refatorado para usar services
 */
public class ChatEventHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final AntiSpamService antiSpamService;
    private final ChatFormatterService formatterService;
    private final MessageBroadcaster broadcaster;
    private final ChatLogger chatLogger;
    private final IgnoreListStorage ignoreListStorage;

    public ChatEventHandler(AntiSpamService antiSpamService,
                           ChatFormatterService formatterService,
                           MessageBroadcaster broadcaster,
                           ChatLogger chatLogger,
                           IgnoreListStorage ignoreListStorage) {
        this.antiSpamService = antiSpamService;
        this.formatterService = formatterService;
        this.broadcaster = broadcaster;
        this.chatLogger = chatLogger;
        this.ignoreListStorage = ignoreListStorage;
    }

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String originalMessage = event.getMessage().getString();

        // WHY: Verificar se jogador está mutado primeiro
        if (MGTChat.getMuteStorage().isMuted(player)) {
            event.setCanceled(true);
            long remainingTime = MGTChat.getMuteStorage().getRemainingTime(player.getUUID());

            if (remainingTime == -1) {
                player.sendSystemMessage(Component.literal("§cVocê está mutado permanentemente."));
            } else {
                long seconds = remainingTime / 1000;
                player.sendSystemMessage(Component.literal("§cVocê está mutado por mais " + seconds + " segundos."));
            }
            return;
        }

        // WHY: Anti-spam check delegado ao service dedicado
        Optional<Component> spamError = antiSpamService.checkSpam(player, originalMessage);
        if (spamError.isPresent()) {
            event.setCanceled(true);
            player.sendSystemMessage(spamError.get());
            chatLogger.logSpamBlocked(player, spamError.get().getString());
            return;
        }

        // WHY: Aplicar filtro de palavras
        String filteredMessage = applyWordFilter(originalMessage);

        // WHY: Verificar canal atual do jogador
        ChatChannelManager.Channel channel = ChatChannelManager.getChannel(player);

        // WHY: Cancelar evento vanilla sempre, vamos mandar nossa própria mensagem
        event.setCanceled(true);

        if (channel == ChatChannelManager.Channel.LOCAL) {
            handleLocalChat(player, filteredMessage);
        } else {
            handleGlobalChat(player, filteredMessage);
        }
    }

    /**
     * Processa mensagem do chat global.
     *
     * WHY: Separado em método privado para clareza.
     * NOTE: Fires MGTChatMessageEvent for Discord integration compatibility.
     */
    private void handleGlobalChat(ServerPlayer sender, String message) {
        Component formatted = formatterService.formatGlobalMessage(sender, message);

        // WHY: Fire custom event for Discord relay integration
        MGTChatMessageEvent chatEvent = new MGTChatMessageEvent(
            sender, message, formatted, MGTChatMessageEvent.Channel.GLOBAL
        );
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(chatEvent);

        if (chatEvent.isCanceled()) {
            return; // Another mod canceled the message
        }

        int recipientCount = broadcaster.broadcastGlobal(sender, formatted);

        // WHY: Log estruturado para auditoria
        chatLogger.logGlobal(sender, message);

        if (ChatConfig.COMMON.debug.get()) {
            LOGGER.debug("[MGT-Chat] Global broadcast: {} recipients", recipientCount);
        }
    }

    /**
     * Processa mensagem do chat local.
     *
     * WHY: Separado em método privado + adiciona aviso se ninguém recebeu.
     * NOTE: Fires MGTChatMessageEvent for Discord integration compatibility.
     */
    private void handleLocalChat(ServerPlayer sender, String message) {
        int range = ChatConfig.COMMON.localRange.get();
        Component formatted = formatterService.formatLocalMessage(sender, message);

        // WHY: Fire custom event for Discord relay integration
        MGTChatMessageEvent chatEvent = new MGTChatMessageEvent(
            sender, message, formatted, MGTChatMessageEvent.Channel.LOCAL
        );
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(chatEvent);

        if (chatEvent.isCanceled()) {
            return; // Another mod canceled the message
        }

        // ALWAYS show the message to the sender so they see what they sent
        sender.sendSystemMessage(formatted);

        // WHY: ignoreSpectators=true para não contar espectadores como destinatários
        int recipientCount = broadcaster.broadcastLocal(sender, formatted, range, true);

        // WHY: Log estruturado
        chatLogger.logLocal(sender, message, recipientCount);

        // WHY: Aviso configurável quando ninguém está por perto
        if (recipientCount == 0) {
            // Mensagem de aviso padronizada (cor e tradução via ColorUtil)
            sender.sendSystemMessage(ColorUtil.translate("&cVocê fala mas ninguém pode te ouvir"));
            chatLogger.logLocalEmpty(sender);
        }

        if (ChatConfig.COMMON.debug.get()) {
            LOGGER.debug("[MGT-Chat] Local broadcast: {} recipients in {}m range", recipientCount, range);
        }
    }

    /**
     * Aplica filtro de palavras proibidas.
     *
     * WHY: Mantido inline pois é simples e específico do handler.
     * TODO: Considerar mover para WordFilterService se crescer complexidade.
     */
    private String applyWordFilter(String message) {
        if (!ChatConfig.COMMON.filterEnabled.get()) return message;

        List<String> blocked = ChatConfig.COMMON.blockedWords.get().stream()
                .map(String::valueOf)
                .toList();

        String replacement = ChatConfig.COMMON.replacement.get();
        String result = message;

        for (String bad : blocked) {
            result = result.replaceAll("(?i)" + java.util.regex.Pattern.quote(bad), replacement);
        }
        return result;
    }
}
