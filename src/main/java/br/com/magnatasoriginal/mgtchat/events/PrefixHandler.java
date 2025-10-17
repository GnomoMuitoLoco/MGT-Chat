package br.com.magnatasoriginal.mgtchat.events;

import br.com.magnatasoriginal.mgtchat.config.ChatConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import org.slf4j.Logger;

/**
 * Responsável por aplicar prefixos (ex.: integração futura com FTB Ranks).
 */
public class PrefixHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        if (!ChatConfig.COMMON.usePrefixes.get()) {
            return; // Integração desativada
        }

        ServerPlayer player = event.getPlayer();
        String playerName = player.getName().getString();
        String message = event.getMessage().getString();

        // Aqui você buscaria o prefixo real do FTB Ranks.
        String prefix = getPlayerPrefix(player);

        // Monta a mensagem formatada com base no formato definido em config
        String format = ChatConfig.COMMON.format.get();
        String formatted = format
                .replace("{prefix}", prefix)
                .replace("{player}", playerName)
                .replace("{message}", message);

        // Substitui a mensagem original
        event.setMessage(Component.literal(formatted));

        if (ChatConfig.COMMON.debug.get()) {
            LOGGER.debug("[MGT-Chat] Mensagem formatada com prefixo: {}", formatted);
        }
    }

    /**
     * Recupera o prefixo do jogador.
     * Integração real com FTB Ranks deve ser implementada aqui.
     */
    private String getPlayerPrefix(ServerPlayer player) {
        // TODO: Integrar com FTB Ranks API
        return "[Jogador]"; // Placeholder
    }
}
