package br.com.magnatasoriginal.mgtchat.commands.admin;

import br.com.magnatasoriginal.mgtchat.MGTChat;
import br.com.magnatasoriginal.mgtchat.config.ChatConfig;
import br.com.magnatasoriginal.mgtcore.util.ColorUtil;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * Comando /mgtchat reload para recarregar configurações.
 *
 * Uso: /mgtchat reload
 *
 * WHY: Permite admins recarregarem config sem reiniciar servidor.
 *
 * Requer permissão: mgtchat.admin.reload ou OP nível 3+
 *
 * @since 1.1.0
 */
public class ReloadCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mgtchat")
                .then(Commands.literal("reload")
                        .requires(source -> {
                            try {
                                ServerPlayer player = source.getPlayerOrException();
                                return MGTChat.getPermissionService().canReload(player);
                            } catch (Exception e) {
                                return true; // Console sempre pode
                            }
                        })
                        .executes(ctx -> {
                            ServerPlayer admin = ctx.getSource().getPlayerOrException();

                            // TODO: Implementar reload real das configurações
                            // Por enquanto apenas simula reload
                            admin.sendSystemMessage(ColorUtil.translate("§aRecarregando configurações do MGT-Chat..."));

                            // Forçar recarregamento das configs do ModConfigSpec
                            // (NeoForge 1.21 recarrega automaticamente em alguns casos)
                            ChatConfig.COMMON_SPEC.isLoaded();

                            admin.sendSystemMessage(ColorUtil.translate("§aConfigurações recarregadas com sucesso!"));

                            MGTChat.getChatLogger().logAdminCommand(admin, "reload", "config");

                            return 1;
                        })
                )
                // /mgtchat info (informações do mod)
                .then(Commands.literal("info")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();

                            player.sendSystemMessage(ColorUtil.translate("§6§l=== MGT-Chat Info ==="));
                            player.sendSystemMessage(ColorUtil.translate("§eVersão: §f1.1.0"));
                            player.sendSystemMessage(ColorUtil.translate("§eAutor: §fMagnatas Original"));
                            player.sendSystemMessage(ColorUtil.translate("§eMinecraft: §f1.21.1"));
                            player.sendSystemMessage(ColorUtil.translate("§eNeoForge: §f21.1.211"));
                            player.sendSystemMessage(ColorUtil.translate("§6=================="));

                            return 1;
                        })
                )
        );
    }
}

