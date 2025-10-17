package br.com.magnatasoriginal.mgtchat.commands;

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
                                        sender.sendSystemMessage(Component.literal("§cEsse jogador está ignorando você."));
                                        return 0;
                                    }

                                    // Cor padrão configurável para tell
                                    String color = br.com.magnatasoriginal.mgtchat.config.ChatConfig.COMMON.tellColor.get();

                                    // Envia mensagem privada
                                    Component toTarget = Component.literal(color + "[De " + sender.getName().getString() + "] " + msg);
                                    Component toSender = Component.literal(color + "[Para " + target.getName().getString() + "] " + msg);

                                    target.sendSystemMessage(toTarget);
                                    sender.sendSystemMessage(toSender);

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
                                sender.sendSystemMessage(Component.literal("§cVocê não tem ninguém para responder."));
                                return 0;
                            }

                            ServerPlayer target = server.getPlayerList().getPlayer(last);
                            if (target == null) {
                                sender.sendSystemMessage(Component.literal("§cEsse jogador não está online."));
                                return 0;
                            }

                            String msg = StringArgumentType.getString(ctx, "message");
                            String color = br.com.magnatasoriginal.mgtchat.config.ChatConfig.COMMON.tellColor.get();

                            Component toTarget = Component.literal(color + "[De " + sender.getName().getString() + "] " + msg);
                            Component toSender = Component.literal(color + "[Para " + target.getName().getString() + "] " + msg);

                            target.sendSystemMessage(toTarget);
                            sender.sendSystemMessage(toSender);

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
