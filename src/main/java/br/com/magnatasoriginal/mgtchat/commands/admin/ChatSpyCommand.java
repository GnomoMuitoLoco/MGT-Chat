package br.com.magnatasoriginal.mgtchat.commands.admin;

import br.com.magnatasoriginal.mgtchat.MGTChat;
import br.com.magnatasoriginal.mgtcore.util.ColorUtil;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * Comando /chatspy para ativar/desativar modo espião.
 *
 * Uso: /chatspy [on|off]
 *
 * WHY: Permite moderadores verem de qualquer lugar do servidor:
 * - Mensagens locais de todos os jogadores (independente da distância)
 * - Mensagens privadas (tell/msg)
 * - Comandos executados (exceto /login e /register)
 * - Chat global (já visível naturalmente para todos)
 *
 * Requer permissão: mgtchat.admin.spy ou OP nível 2+
 *
 * @since 1.1.0
 * @version 1.1.1 - Melhorado para incluir mensagens locais e comandos
 */
public class ChatSpyCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("chatspy")
                .requires(source -> {
                    try {
                        ServerPlayer player = source.getPlayerOrException();
                        return MGTChat.getPermissionService().canSpy(player);
                    } catch (Exception e) {
                        return true; // Console sempre pode
                    }
                })
                // /chatspy (toggle)
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();

                    boolean isSpying = MGTChat.getChatSpyStorage().isSpying(player);

                    if (isSpying) {
                        MGTChat.getChatSpyStorage().disableSpy(player.getUUID());
                        player.sendSystemMessage(ColorUtil.translate("§cChat Spy desativado."));
                    } else {
                        MGTChat.getChatSpyStorage().enableSpy(player.getUUID());
                        player.sendSystemMessage(ColorUtil.translate("§aChat Spy ativado. Você verá mensagens locais, privadas e comandos."));
                    }

                    MGTChat.getChatLogger().logAdminCommand(
                        player,
                        "chatspy " + (!isSpying ? "on" : "off"),
                        player.getName().getString()
                    );

                    return 1;
                })
                // /chatspy on
                .then(Commands.literal("on")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();

                            MGTChat.getChatSpyStorage().enableSpy(player.getUUID());
                            player.sendSystemMessage(ColorUtil.translate("§aChat Spy ativado. Você verá mensagens locais, privadas e comandos."));

                            MGTChat.getChatLogger().logAdminCommand(player, "chatspy on", player.getName().getString());

                            return 1;
                        })
                )
                // /chatspy off
                .then(Commands.literal("off")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();

                            boolean wasSpying = MGTChat.getChatSpyStorage().disableSpy(player.getUUID());

                            if (!wasSpying) {
                                player.sendSystemMessage(ColorUtil.translate("§eChat Spy já estava desativado."));
                                return 0;
                            }

                            player.sendSystemMessage(ColorUtil.translate("§cChat Spy desativado."));

                            MGTChat.getChatLogger().logAdminCommand(player, "chatspy off", player.getName().getString());

                            return 1;
                        })
                )
        );
    }
}

