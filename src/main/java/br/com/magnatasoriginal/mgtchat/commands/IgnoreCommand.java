package br.com.magnatasoriginal.mgtchat.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class IgnoreCommand {
    // Mapa de jogadores ignorados: quem ignora → conjunto de ignorados
    private static final Map<UUID, Set<UUID>> ignoredPlayers = new ConcurrentHashMap<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ignore")
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");

                            if (player.getUUID().equals(target.getUUID())) {
                                player.sendSystemMessage(Component.literal("§cVocê não pode ignorar a si mesmo."));
                                return 0;
                            }

                            ignoredPlayers.computeIfAbsent(player.getUUID(), k -> new HashSet<>()).add(target.getUUID());
                            player.sendSystemMessage(Component.literal("§eVocê agora está ignorando " + target.getName().getString() + "."));
                            return 1;
                        })
                )
        );

        dispatcher.register(Commands.literal("unignore")
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");

                            Set<UUID> ignored = ignoredPlayers.getOrDefault(player.getUUID(), new HashSet<>());
                            if (ignored.remove(target.getUUID())) {
                                player.sendSystemMessage(Component.literal("§aVocê deixou de ignorar " + target.getName().getString() + "."));
                            } else {
                                player.sendSystemMessage(Component.literal("§cVocê não estava ignorando " + target.getName().getString() + "."));
                            }
                            return 1;
                        })
                )
        );
    }

    public static boolean isIgnoring(ServerPlayer player, ServerPlayer sender) {
        return ignoredPlayers.getOrDefault(player.getUUID(), Collections.emptySet())
                .contains(sender.getUUID());
    }
}
