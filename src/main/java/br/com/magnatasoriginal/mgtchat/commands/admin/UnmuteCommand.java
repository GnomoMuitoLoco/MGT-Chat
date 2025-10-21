package br.com.magnatasoriginal.mgtchat.commands.admin;

import br.com.magnatasoriginal.mgtchat.MGTChat;
import br.com.magnatasoriginal.mgtcore.util.ColorUtil;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

/**
 * Comando /unmute para remover silenciamento de jogadores.
 *
 * Uso: /unmute <player>
 *
 * WHY: Permite moderadores removerem mutes.
 * Requer permissão: mgtchat.admin.moderate ou OP nível 2+
 *
 * @since 1.1.0
 */
public class UnmuteCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("unmute")
                .requires(source -> {
                    try {
                        ServerPlayer player = source.getPlayerOrException();
                        return MGTChat.getPermissionService().canModerate(player);
                    } catch (Exception e) {
                        return true; // Console sempre pode
                    }
                })
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> {
                            ServerPlayer admin = ctx.getSource().getPlayerOrException();
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");

                            boolean wasMuted = MGTChat.getMuteStorage().unmute(target.getUUID());

                            if (!wasMuted) {
                                admin.sendSystemMessage(ColorUtil.translate(
                                    "§eEsse jogador não estava mutado."
                                ));
                                return 0;
                            }

                            admin.sendSystemMessage(ColorUtil.translate(
                                "§aVocê desmutou §f" + target.getName().getString() + "§a."
                            ));
                            target.sendSystemMessage(ColorUtil.translate(
                                "§aVocê foi desmutado por um moderador."
                            ));

                            MGTChat.getChatLogger().logAdminCommand(admin, "unmute", target.getName().getString());

                            return 1;
                        })
                )
        );
    }
}

