package br.com.magnatasoriginal.mgtchat.commands.admin;

import br.com.magnatasoriginal.mgtchat.MGTChat;
import br.com.magnatasoriginal.mgtcore.util.ColorUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * /bloquear comando <comando>
 *
 * Bloqueia um comando (remove do dispatcher) até ser desbloqueado ou o servidor reiniciar.
 * Permissão requerida: magnatas.admin.bloquearcomandos (via FTB Ranks se disponível, senão OP).
 */
public class BlockCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bloquear")
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

                                    MGTChat.getBlockedCommandService().addBlocked(cmd);
                                    player.sendSystemMessage(ColorUtil.translate("&aComando bloqueado: &f/" + cmd));
                                    MGTChat.getChatLogger().logAdminCommand(player, "bloquear comando " + cmd, player.getName().getString());
                                    return 1;
                                })
                        )
                )
        );
    }
}