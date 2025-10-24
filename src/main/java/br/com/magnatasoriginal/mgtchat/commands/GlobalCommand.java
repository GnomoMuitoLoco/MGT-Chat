package br.com.magnatasoriginal.mgtchat.commands;

import br.com.magnatasoriginal.mgtchat.MGTChat;
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
 * Comando /global (e alias /g) para trocar canal ou enviar mensagem global.
 *
 * WHY: Refatorado para usar services e eliminar duplicação de código.
 *
 * @since 1.0.0
 * @version 1.1.0 - Refatorado para usar services
 */
public class GlobalCommand {

    /**
     * Registra comando /global e alias /g.
     *
     * WHY: Usa .redirect() para evitar duplicar lógica entre alias.
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Comando principal /global
        LiteralCommandNode<CommandSourceStack> mainNode = Commands.literal("global")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    ChatChannelManager.setChannel(player, ChatChannelManager.Channel.GLOBAL);
                    player.sendSystemMessage(ColorUtil.translate("§bVocê travou seu chat no canal GLOBAL."));
                    return 1;
                })
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String msg = StringArgumentType.getString(ctx, "message");
                            sendGlobalMessage(player, msg);
                            return 1;
                        })
                )
                .build();

        dispatcher.getRoot().addChild(mainNode);

        // WHY: Alias /g redireciona para /global (sem duplicar código)
        dispatcher.register(Commands.literal("g").redirect(mainNode));
    }

    /**
     * Envia mensagem no chat global usando services.
     *
     * WHY: Delegado aos services para evitar duplicação com ChatEventHandler.
     * NOTE: Fires MGTChatMessageEvent to ensure Discord relay works for /g <message>.
     */
    public static void sendGlobalMessage(ServerPlayer sender, String msg) {
        Component formatted = MGTChat.getChatFormatterService().formatGlobalMessage(sender, msg);

        // WHY: Fire custom event for Discord relay integration (fixes bug where /g doesn't relay)
        br.com.magnatasoriginal.mgtchat.events.MGTChatMessageEvent chatEvent =
            new br.com.magnatasoriginal.mgtchat.events.MGTChatMessageEvent(
                sender, msg, formatted, br.com.magnatasoriginal.mgtchat.events.MGTChatMessageEvent.Channel.GLOBAL
            );
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(chatEvent);

        if (chatEvent.isCanceled()) {
            return; // Another mod canceled the message
        }

        int recipientCount = MGTChat.getMessageBroadcaster().broadcastGlobal(sender, formatted);

        MGTChat.getChatLogger().logGlobal(sender, msg);
    }
}
