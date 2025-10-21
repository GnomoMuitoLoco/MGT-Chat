package br.com.magnatasoriginal.mgtchat.commands.admin;

import br.com.magnatasoriginal.mgtchat.MGTChat;
import br.com.magnatasoriginal.mgtcore.util.ColorUtil;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Comando /clearchat para limpar o chat.
 *
 * Uso:
 * - /clearchat - Limpa o chat de todos
 * - /clearchat <player> - Limpa o chat de um jogador específico
 *
 * WHY: Útil para limpar spam ou conteúdo indesejado.
 * Requer permissão: mgtchat.admin.moderate ou OP nível 2+
 *
 * @since 1.1.0
 */
public class ClearChatCommand {

    private static final int CLEAR_LINES = 100; // Número de linhas vazias para "limpar" o chat

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("clearchat")
                .requires(source -> {
                    try {
                        ServerPlayer player = source.getPlayerOrException();
                        return MGTChat.getPermissionService().canModerate(player);
                    } catch (Exception e) {
                        return true; // Console sempre pode
                    }
                })
                // /clearchat (todos)
                .executes(ctx -> {
                    ServerPlayer admin = ctx.getSource().getPlayerOrException();

                    // Envia linhas vazias para todos os jogadores
                    for (ServerPlayer player : admin.server.getPlayerList().getPlayers()) {
                        clearChatForPlayer(player);
                    }

                    // Aviso global
                    Component announcement = ColorUtil.translate("§6§l[!] §eChat limpo por um moderador.");
                    for (ServerPlayer player : admin.server.getPlayerList().getPlayers()) {
                        player.sendSystemMessage(announcement);
                    }

                    MGTChat.getChatLogger().logAdminCommand(admin, "clearchat", "all");

                    return 1;
                })
                // /clearchat <player> (específico)
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> {
                            ServerPlayer admin = ctx.getSource().getPlayerOrException();
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");

                            clearChatForPlayer(target);

                            target.sendSystemMessage(ColorUtil.translate(
                                "§6§l[!] §eSeu chat foi limpo por um moderador."
                            ));

                            admin.sendSystemMessage(ColorUtil.translate(
                                "§aChat de §f" + target.getName().getString() + " §alimpo."
                            ));

                            MGTChat.getChatLogger().logAdminCommand(
                                admin,
                                "clearchat",
                                target.getName().getString()
                            );

                            return 1;
                        })
                )
        );
    }

    /**
     * Limpa o chat de um jogador enviando linhas vazias.
     *
     * WHY: Técnica comum usada por plugins de chat para "limpar" visualmente.
     */
    private static void clearChatForPlayer(ServerPlayer player) {
        for (int i = 0; i < CLEAR_LINES; i++) {
            player.sendSystemMessage(Component.literal(""));
        }
    }
}

