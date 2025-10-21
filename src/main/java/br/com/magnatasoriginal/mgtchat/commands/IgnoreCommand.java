package br.com.magnatasoriginal.mgtchat.commands;

import br.com.magnatasoriginal.mgtchat.MGTChat;
import br.com.magnatasoriginal.mgtcore.util.ColorUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * Comandos /ignorar e /ouvir para gerenciar lista de jogadores ignorados.
 *
 * WHY: Refatorado para usar IgnoreListStorage (thread-safe).
 * Removido HashMap não thread-safe.
 *
 * @since 1.0.0
 * @version 1.1.0 - Refatorado para usar storage
 */
public class IgnoreCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /ignorar <nick>
        dispatcher.register(Commands.literal("ignorar")
                .then(Commands.argument("target", StringArgumentType.word())
                        .executes(ctx -> {
                            ServerPlayer sender = ctx.getSource().getPlayerOrException();
                            String targetName = StringArgumentType.getString(ctx, "target");
                            ServerPlayer target = sender.server.getPlayerList().getPlayerByName(targetName);

                            if (target == null) {
                                sender.sendSystemMessage(ColorUtil.translate("§cJogador não encontrado."));
                                return 0;
                            }

                            if (sender.getUUID().equals(target.getUUID())) {
                                sender.sendSystemMessage(ColorUtil.translate("§cVocê não pode ignorar a si mesmo."));
                                return 0;
                            }

                            // WHY: Usa IgnoreListStorage thread-safe
                            MGTChat.getIgnoreListStorage().addIgnore(sender.getUUID(), target.getUUID());
                            sender.sendSystemMessage(ColorUtil.translate("§aVocê ignorou §f" + target.getName().getString() + "§a."));
                            return 1;
                        })
                )
        );

        // /ouvir <nick>
        dispatcher.register(Commands.literal("ouvir")
                .then(Commands.argument("target", StringArgumentType.word())
                        .executes(ctx -> {
                            ServerPlayer sender = ctx.getSource().getPlayerOrException();
                            String targetName = StringArgumentType.getString(ctx, "target");
                            ServerPlayer target = sender.server.getPlayerList().getPlayerByName(targetName);

                            if (target == null) {
                                sender.sendSystemMessage(ColorUtil.translate("§cJogador não encontrado."));
                                return 0;
                            }

                            // WHY: Usa IgnoreListStorage thread-safe
                            boolean removed = MGTChat.getIgnoreListStorage().removeIgnore(sender.getUUID(), target.getUUID());

                            if (!removed) {
                                sender.sendSystemMessage(ColorUtil.translate("§eVocê já está ouvindo §f" + target.getName().getString() + "§e."));
                                return 0;
                            }

                            sender.sendSystemMessage(ColorUtil.translate("§aVocê voltou a ouvir §f" + target.getName().getString() + "§a."));
                            return 1;
                        })
                )
        );
    }

    /**
     * Verifica se um jogador está ignorando outro.
     *
     * @deprecated Use {@link br.com.magnatasoriginal.mgtchat.storage.IgnoreListStorage#isIgnoring(ServerPlayer, ServerPlayer)}
     * WHY: Mantido para compatibilidade retroativa.
     */
    @Deprecated
    public static boolean isIgnoring(ServerPlayer listener, ServerPlayer speaker) {
        return MGTChat.getIgnoreListStorage().isIgnoring(listener, speaker);
    }
}

