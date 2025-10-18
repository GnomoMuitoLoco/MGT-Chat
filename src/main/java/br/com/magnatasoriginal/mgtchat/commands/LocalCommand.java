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

public class LocalCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Comando principal /local
        dispatcher.register(Commands.literal("local")
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
        );

        // Alias /l
        dispatcher.register(Commands.literal("l")
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
        );
    }

    private static void sendLocalMessage(ServerPlayer sender, String msg) {
        int range = ChatConfig.COMMON.localRange.get();

        String base = ChatConfig.COMMON.localFormat.get()
                .replace("{prefix}", "")
                .replace("{player}", sender.getName().getString())
                .replace("{message_color}", ChatConfig.COMMON.localMessageColor.get())
                .replace("{message}", msg);

        Component text = ColorUtil.translate(base);

        for (ServerPlayer target : sender.server.getPlayerList().getPlayers()) {
            if (target.level() == sender.level() &&
                    target.blockPosition().closerThan(sender.blockPosition(), range)) {
                target.sendSystemMessage(text);
            }
        }
    }
}
