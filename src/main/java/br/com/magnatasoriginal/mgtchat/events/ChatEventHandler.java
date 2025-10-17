package br.com.magnatasoriginal.mgtchat.events;

import br.com.magnatasoriginal.mgtchat.config.ChatConfig;
import br.com.magnatasoriginal.mgtchat.util.ChatChannelManager;
import br.com.magnatasoriginal.mgtcore.util.ColorUtil;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatEventHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<UUID, Long> lastMessageTime = new ConcurrentHashMap<>();
    private final Map<UUID, String> lastMessageContent = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> repeatedCount = new ConcurrentHashMap<>();

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        UUID uuid = player.getUUID();
        String originalMessage = event.getMessage().getString();

        // Anti-spam: delay mínimo
        long now = System.currentTimeMillis();
        long lastTime = lastMessageTime.getOrDefault(uuid, 0L);
        int delay = ChatConfig.COMMON.messageDelay.get();
        if (delay > 0 && (now - lastTime) < delay) {
            event.setCanceled(true);
            player.sendSystemMessage(ColorUtil.translate("§cVocê está enviando mensagens muito rápido!"));
            return;
        }

        // Anti-spam: mensagens repetidas
        String lastMsg = lastMessageContent.getOrDefault(uuid, "");
        if (ChatConfig.COMMON.blockRepeated.get() && originalMessage.equalsIgnoreCase(lastMsg)) {
            int count = repeatedCount.getOrDefault(uuid, 1) + 1;
            repeatedCount.put(uuid, count);
            if (count > ChatConfig.COMMON.maxRepeated.get()) {
                event.setCanceled(true);
                player.sendSystemMessage(ColorUtil.translate("§cMensagem repetida bloqueada."));
                return;
            }
        } else {
            repeatedCount.put(uuid, 1);
        }

        lastMessageTime.put(uuid, now);
        lastMessageContent.put(uuid, originalMessage);

        // Filtro de palavras
        String filteredMessage = applyWordFilter(originalMessage);

        ChatChannelManager.Channel channel = ChatChannelManager.getChannel(player);

        if (channel == ChatChannelManager.Channel.LOCAL) {
            sendLocalMessage(player, filteredMessage);
            event.setCanceled(true);
            return;
        }

        // GLOBAL
        String base = ChatConfig.COMMON.globalPrefix.get() + " " +
                player.getName().getString() + ": " + filteredMessage;
        Component styled = ColorUtil.translate(base);

        event.setCanceled(true); // cancela vanilla
        for (ServerPlayer target : player.server.getPlayerList().getPlayers()) {
            target.sendSystemMessage(styled);
        }

        if (ChatConfig.COMMON.debug.get()) {
            LOGGER.debug("[MGT-Chat] (GLOBAL) {} -> {}", player.getName().getString(), base);
        }
    }

    private void sendLocalMessage(ServerPlayer sender, String message) {
        int range = ChatConfig.COMMON.localRange.get();
        String base = ChatConfig.COMMON.localPrefix.get() + " " +
                sender.getName().getString() + ": " + message;
        Component styled = ColorUtil.translate(base);

        for (ServerPlayer target : sender.server.getPlayerList().getPlayers()) {
            if (target.level() == sender.level() &&
                    target.blockPosition().closerThan(sender.blockPosition(), range)) {
                target.sendSystemMessage(styled);
            }
        }

        if (ChatConfig.COMMON.debug.get()) {
            LOGGER.debug("[MGT-Chat] (LOCAL) {} -> {}", sender.getName().getString(), base);
        }
    }

    private String applyWordFilter(String message) {
        if (!ChatConfig.COMMON.filterEnabled.get()) return message;
        List<String> blocked = ChatConfig.COMMON.blockedWords.get();
        String replacement = ChatConfig.COMMON.replacement.get();
        String result = message;
        for (String bad : blocked) {
            result = result.replaceAll("(?i)" + java.util.regex.Pattern.quote(bad), replacement);
        }
        return result;
    }
}
