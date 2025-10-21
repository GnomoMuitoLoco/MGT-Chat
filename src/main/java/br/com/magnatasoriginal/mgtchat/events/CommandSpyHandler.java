package br.com.magnatasoriginal.mgtchat.events;

import br.com.magnatasoriginal.mgtchat.MGTChat;
import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.CommandEvent;

/**
 * Handler para espionar comandos executados por jogadores.
 *
 * WHY: Permite administradores verem todos os comandos executados via ChatSpy.
 * Também loga todos os comandos no console do servidor para auditoria.
 *
 * @since 1.1.1
 */
public class CommandSpyHandler {

    @SubscribeEvent
    public void onCommand(CommandEvent event) {
        try {
            ParseResults<CommandSourceStack> parseResults = event.getParseResults();
            CommandSourceStack source = parseResults.getContext().getSource();

            // Verificar se é um jogador (não console)
            if (!source.isPlayer()) {
                return;
            }

            ServerPlayer player = source.getPlayerOrException();
            String command = parseResults.getReader().getString();

            if (command == null || command.isEmpty()) {
                return;
            }

            String lowerCommand = command.trim().toLowerCase();

            // Excluir comandos sensíveis (login/register)
            if (lowerCommand.startsWith("login ") || lowerCommand.equals("login") ||
                lowerCommand.startsWith("register ") || lowerCommand.equals("register")) {
                return;
            }

            // Log no console do servidor para auditoria
            MGTChat.LOGGER.info("[MGT-Chat][COMMAND] {} executou: /{}",
                player.getName().getString(), command);

            // Enviar para jogadores com ChatSpy ativo
            var allPlayers = player.server.getPlayerList().getPlayers();
            var pos = player.blockPosition();
            String worldName = player.level().dimension().location().getPath();
            String coords = String.format("§7[§e%s §7%d,%d,%d§7]",
                worldName, pos.getX(), pos.getY(), pos.getZ());

            Component spyMsg = Component.literal("§7[SPY-CMD] ")
                .append(coords)
                .append(" §f")
                .append(player.getName())
                .append(Component.literal("§7: §6/" + command));

            for (ServerPlayer spy : allPlayers) {
                // Não enviar para o próprio executor
                if (spy.equals(player)) continue;

                // Verificar se está com ChatSpy ativo
                if (MGTChat.getChatSpyStorage().isSpying(spy)) {
                    spy.sendSystemMessage(spyMsg);
                }
            }

        } catch (Throwable ignored) {
            // Ignorar erros silenciosamente para não quebrar comandos
        }
    }
}

