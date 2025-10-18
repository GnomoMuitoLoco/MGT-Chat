package br.com.magnatasoriginal.mgtchat.commands;

import br.com.magnatasoriginal.mgtchat.config.ChatConfig;
import br.com.magnatasoriginal.mgtchat.util.ChatChannelManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import br.com.magnatasoriginal.mgtcore.util.ColorUtil;

public class GlobalCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Comando principal /global
        dispatcher.register(Commands.literal("global")
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
        );

        // Alias /g
        dispatcher.register(Commands.literal("g")
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
        );
    }

    private static void sendGlobalMessage(ServerPlayer sender, String msg) {
        String base = ChatConfig.COMMON.globalFormat.get()
                .replace("{prefix}", "")
                .replace("{player}", sender.getName().getString())
                .replace("{message_color}", ChatConfig.COMMON.globalMessageColor.get())
                .replace("{message}", msg);

        Component text = ColorUtil.translate(base);

        for (ServerPlayer target : sender.server.getPlayerList().getPlayers()) {
            target.sendSystemMessage(text);
        }
    }
}
