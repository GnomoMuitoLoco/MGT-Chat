package br.com.magnatasoriginal.mgtchat.commands.admin;

import br.com.magnatasoriginal.mgtchat.MGTChat;
import br.com.magnatasoriginal.mgtcore.util.ColorUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * /desbloquear comando <comando>
 *
 * Reverte o bloqueio de um comando previamente bloqueado neste runtime.
 * Permissão requerida: magnatas.admin.bloquearcomandos
 */
public class UnblockCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("desbloquear")
            .then(Commands.literal("comando")
                .requires(source -> {
                    try {
                        ServerPlayer p = source.getPlayerOrException();
                        return MGTChat.getPermissionService().canBlockCommands(p);
                    } catch (Exception e) {
                        return true; // console
                    }
                })
                .then(Commands.argument("comando", StringArgumentType.word())
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        String cmd = StringArgumentType.getString(ctx, "comando");

                        boolean removed = MGTChat.getBlockedCommandService().removeBlocked(cmd);
                        if (removed) {
                            player.sendSystemMessage(ColorUtil.translate("&aComando desbloqueado: &f/" + cmd));
                            MGTChat.getChatLogger().logAdminCommand(player, "desbloquear comando " + cmd, player.getName().getString());
                            return 1;
                        } else {
                            player.sendSystemMessage(ColorUtil.translate("&eComando não estava bloqueado: &f/" + cmd));
                            return 0;
                        }
                    })
                )
            )
        );
    }
}


