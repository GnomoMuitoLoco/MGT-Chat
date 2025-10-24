package br.com.magnatasoriginal.mgtchat.commands.admin;

import br.com.magnatasoriginal.mgtchat.MGTChat;
import br.com.magnatasoriginal.mgtchat.config.ChatConfig;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Comando /mgtchat reload para verificar configurações em runtime.
 *
 * WHY: Permite visualizar formatos de chat, debug e outras configurações atuais.
 * NOTE: NeoForge recarrega configs automaticamente quando o arquivo é modificado,
 * mas este comando permite verificar o estado atual sem precisar olhar logs.
 *
 * @since 1.1.0
 */
public class MGTChatReloadCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mgtchat")
            .then(Commands.literal("reload")
                .requires(source -> {
                    try {
                        ServerPlayer player = source.getPlayerOrException();
                        return MGTChat.getPermissionService().canReload(player);
                    } catch (Exception e) {
                        // Console can always reload
                        return true;
                    }
                })
                .executes(ctx -> {
                    try {
                        // WHY: NeoForge automatically reloads config when file changes
                        // NOTE: Config values are read directly from ChatConfig.COMMON at runtime,
                        // so we just need to inform the user and log current state

                        // Send success message
                        ctx.getSource().sendSuccess(
                            () -> Component.literal("§a[MGT-Chat] Configuração verificada!"),
                            true
                        );

                        // Log the reload
                        MGTChat.LOGGER.info("[MGT-Chat] Config check requested by {}",
                            ctx.getSource().getTextName());

                        // Show current settings
                        boolean debug = ChatConfig.COMMON.debug.get();
                        String globalFormat = ChatConfig.COMMON.globalFormat.get();
                        String localFormat = ChatConfig.COMMON.localFormat.get();

                        ctx.getSource().sendSuccess(
                            () -> Component.literal("§7Debug mode: " + (debug ? "§aON" : "§cOFF")),
                            false
                        );

                        ctx.getSource().sendSuccess(
                            () -> Component.literal("§7Global format: §f" + globalFormat.substring(0, Math.min(40, globalFormat.length())) + "..."),
                            false
                        );

                        ctx.getSource().sendSuccess(
                            () -> Component.literal("§7Local format: §f" + localFormat.substring(0, Math.min(40, localFormat.length())) + "..."),
                            false
                        );

                        ctx.getSource().sendSuccess(
                            () -> Component.literal("§7Dica: Edite o arquivo mgtchat-common.toml e reinicie o servidor para aplicar mudanças."),
                            false
                        );

                        return 1;
                    } catch (Exception e) {
                        ctx.getSource().sendFailure(
                            Component.literal("§c[MGT-Chat] Erro ao verificar configuração: " + e.getMessage())
                        );
                        MGTChat.LOGGER.error("[MGT-Chat] Failed to check config", e);
                        return 0;
                    }
                })
            )
        );
    }
}

