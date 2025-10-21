package br.com.magnatasoriginal.mgtchat.commands;

import br.com.magnatasoriginal.mgtchat.MGTChat;
import br.com.magnatasoriginal.mgtcore.util.ColorUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Comando /tell (e aliases /msg, /w) para enviar mensagens privadas.
 * Comando /r para responder à última mensagem privada.
 *
 * WHY: Refatorado para usar ConversationStorage (thread-safe) e ChatFormatterService.
 * Removido HashMap não thread-safe.
 *
 * @since 1.0.0
 * @version 1.1.0 - Refatorado para usar storage e services
 */
public class TellCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /tell <target> <message>
        dispatcher.register(Commands.literal("tell")
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ServerPlayer sender = ctx.getSource().getPlayerOrException();
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                    String msg = StringArgumentType.getString(ctx, "message");

                                    // WHY: Verifica se o alvo está ignorando o remetente
                                    if (MGTChat.getIgnoreListStorage().isIgnoring(target, sender)) {
                                        sender.sendSystemMessage(ColorUtil.translate("§cEsse jogador está ignorando você."));
                                        return 0;
                                    }

                                    // WHY: Usa ChatFormatterService para formatar mensagens
                                    Component toSender = MGTChat.getChatFormatterService()
                                        .formatPrivateMessage(sender, target, msg, false, true);
                                    Component toTarget = MGTChat.getChatFormatterService()
                                        .formatPrivateMessage(sender, target, msg, false, false);

                                    // WHY: Usa MessageBroadcaster para enviar
                                    MGTChat.getMessageBroadcaster().sendPrivateMessage(sender, target, toSender, toTarget);

                                    // WHY: Registra conversa no ConversationStorage (thread-safe)
                                    MGTChat.getConversationStorage().setConversation(sender.getUUID(), target.getUUID());

                                    // WHY: Log estruturado
                                    MGTChat.getChatLogger().logPrivate(sender, target, msg);

                                    return 1;
                                })
                        )
                )
        );

        // /r <message>
        dispatcher.register(Commands.literal("r")
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerPlayer sender = ctx.getSource().getPlayerOrException();
                            MinecraftServer server = ctx.getSource().getServer();

                            // WHY: Usa ConversationStorage ao invés de Map local
                            ServerPlayer target = MGTChat.getConversationStorage().getLastMessenger(sender, server);

                            if (target == null) {
                                sender.sendSystemMessage(ColorUtil.translate("§cVocê não tem ninguém para responder."));
                                return 0;
                            }

                            String msg = StringArgumentType.getString(ctx, "message");

                            // WHY: Usa ChatFormatterService para formatar replies
                            Component toSender = MGTChat.getChatFormatterService()
                                .formatPrivateMessage(sender, target, msg, true, true);
                            Component toTarget = MGTChat.getChatFormatterService()
                                .formatPrivateMessage(sender, target, msg, true, false);

                            // WHY: Usa MessageBroadcaster
                            MGTChat.getMessageBroadcaster().sendPrivateMessage(sender, target, toSender, toTarget);

                            // WHY: Atualiza conversa (bidirecional)
                            MGTChat.getConversationStorage().setConversation(sender.getUUID(), target.getUUID());

                            // WHY: Log estruturado
                            MGTChat.getChatLogger().logPrivate(sender, target, msg);

                            return 1;
                        })
                )
        );

        // Aliases para /tell
        dispatcher.register(Commands.literal("msg").redirect(dispatcher.getRoot().getChild("tell")));
        dispatcher.register(Commands.literal("w").redirect(dispatcher.getRoot().getChild("tell")));
    }
}

