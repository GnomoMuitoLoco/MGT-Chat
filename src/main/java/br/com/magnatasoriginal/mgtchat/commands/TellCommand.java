package br.com.magnatasoriginal.mgtchat.commands;

import br.com.magnatasoriginal.mgtchat.config.ChatConfig;
import br.com.magnatasoriginal.mgtcore.placeholders.PlaceholderService;
import br.com.magnatasoriginal.mgtcore.util.ColorUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TellCommand {

    // Mapa para armazenar último remetente de cada jogador
    private static final Map<UUID, UUID> lastMessengers = new HashMap<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /tell <target> <message>
        dispatcher.register(Commands.literal("tell")
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ServerPlayer sender = ctx.getSource().getPlayerOrException();
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                    String msg = StringArgumentType.getString(ctx, "message");

                                    // Verifica se o alvo está ignorando o remetente
                                    if (IgnoreCommand.isIgnoring(target, sender)) {
                                        sender.sendSystemMessage(ColorUtil.translate("§cEsse jogador está ignorando você."));
                                        return 0;
                                    }

                                    // Mensagem para o remetente
                                    String toSenderRaw = PlaceholderService.resolveContext(
                                            ChatConfig.COMMON.tellFormatTo.get(),
                                            sender, target, msg
                                    );
                                    Component toSender = ColorUtil.translate(toSenderRaw);

                                    // Mensagem para o destinatário
                                    String toTargetRaw = PlaceholderService.resolveContext(
                                            ChatConfig.COMMON.tellFormatFrom.get(),
                                            sender, target, msg
                                    );
                                    Component toTarget = ColorUtil.translate(toTargetRaw);

                                    sender.sendSystemMessage(toSender);
                                    target.sendSystemMessage(toTarget);

                                    // Registra último remetente para o /r
                                    lastMessengers.put(target.getUUID(), sender.getUUID());

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

                            UUID last = lastMessengers.get(sender.getUUID());
                            if (last == null) {
                                sender.sendSystemMessage(ColorUtil.translate("§cVocê não tem ninguém para responder."));
                                return 0;
                            }

                            ServerPlayer target = server.getPlayerList().getPlayer(last);
                            if (target == null) {
                                sender.sendSystemMessage(ColorUtil.translate("§cEsse jogador não está online."));
                                return 0;
                            }

                            String msg = StringArgumentType.getString(ctx, "message");

                            // Mensagem para o remetente (reply)
                            String toSenderRaw = PlaceholderService.resolveContext(
                                    ChatConfig.COMMON.replyTellFormatTo.get(),
                                    sender, target, msg
                            );
                            Component toSender = ColorUtil.translate(toSenderRaw);

                            // Mensagem para o destinatário (reply)
                            String toTargetRaw = PlaceholderService.resolveContext(
                                    ChatConfig.COMMON.replyTellFormatFrom.get(),
                                    sender, target, msg
                            );
                            Component toTarget = ColorUtil.translate(toTargetRaw);

                            sender.sendSystemMessage(toSender);
                            target.sendSystemMessage(toTarget);

                            // Atualiza último remetente
                            lastMessengers.put(target.getUUID(), sender.getUUID());

                            return 1;
                        })
                )
        );

        // Aliases para /tell
        dispatcher.register(Commands.literal("msg").redirect(dispatcher.getRoot().getChild("tell")));
        dispatcher.register(Commands.literal("w").redirect(dispatcher.getRoot().getChild("tell")));
    }
}
