package br.com.magnatasoriginal.mgtchat.commands;

import br.com.magnatasoriginal.mgtchat.MGTChat;
import br.com.magnatasoriginal.mgtchat.config.ChatConfig;
import br.com.magnatasoriginal.mgtchat.util.ChatChannelManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import br.com.magnatasoriginal.mgtcore.util.ColorUtil;

/**
 * Comando /local (e alias /l) para trocar canal ou enviar mensagem local.
 *
 * WHY: Refatorado para usar services e eliminar duplicação de código
 * entre comando principal e alias.
 *
 * @since 1.0.0
 * @version 1.1.0 - Refatorado para usar services
 */
public class LocalCommand {

    /**
     * Registra comando /local e alias /l.
     *
     * WHY: Usa .redirect() para evitar duplicar lógica entre alias.
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Comando principal /local
        LiteralCommandNode<CommandSourceStack> mainNode = Commands.literal("local")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    ChatChannelManager.setChannel(player, ChatChannelManager.Channel.LOCAL);
                    player.sendSystemMessage(ColorUtil.translate("§aVocê travou seu chat no canal LOCAL."));
                    return 1;
                })
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String msg = StringArgumentType.getString(ctx, "message");
                            sendLocalMessage(player, msg);
                            return 1;
                        })
                )
                .build();

        dispatcher.getRoot().addChild(mainNode);

        // WHY: Alias /l redireciona para /local (sem duplicar código)
        dispatcher.register(Commands.literal("l").redirect(mainNode));
    }

    /**
     * Envia mensagem no chat local usando services.
     *
     * WHY: Delegado aos services para evitar duplicação com ChatEventHandler.
     */
    private static void sendLocalMessage(ServerPlayer sender, String msg) {
        int range = ChatConfig.COMMON.localRange.get();
        Component formatted = MGTChat.getChatFormatterService().formatLocalMessage(sender, msg);

        int recipientCount = MGTChat.getMessageBroadcaster().broadcastLocal(sender, formatted, range, true);

        MGTChat.getChatLogger().logLocal(sender, msg, recipientCount);

        // ALWAYS show the message to the sender so they see what they sent
        sender.sendSystemMessage(formatted);

        // WHY: Aviso se ninguém recebeu a mensagem; mostra aviso apenas quando não há destinatários
        if (recipientCount == 0) {
            // Aviso colorido traduzido (usa ColorUtil do MGT-Core)
            sender.sendSystemMessage(ColorUtil.translate("&cVocê fala mas ninguém pode te ouvir"));
        }
    }
}
