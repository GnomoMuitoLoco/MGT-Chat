package br.com.magnatasoriginal.mgtchat.commands;

import br.com.magnatasoriginal.mgtcore.util.ColorUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class IgnoreCommand {

    // Mapa de ignorados: <jogador, lista de ignorados>
    private static final Map<UUID, Set<UUID>> ignoredMap = new HashMap<>();

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

                            ignoredMap.computeIfAbsent(sender.getUUID(), k -> new HashSet<>()).add(target.getUUID());
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

                            Set<UUID> ignored = ignoredMap.get(sender.getUUID());
                            if (ignored == null || !ignored.remove(target.getUUID())) {
                                sender.sendSystemMessage(ColorUtil.translate("§eVocê já está ouvindo §f" + target.getName().getString() + "§e."));
                                return 0;
                            }

                            sender.sendSystemMessage(ColorUtil.translate("§aVocê voltou a ouvir §f" + target.getName().getString() + "§a."));
                            return 1;
                        })
                )
        );
    }

    // Verifica se um jogador está ignorando outro
    public static boolean isIgnoring(ServerPlayer listener, ServerPlayer speaker) {
        Set<UUID> ignored = ignoredMap.get(listener.getUUID());
        return ignored != null && ignored.contains(speaker.getUUID());
    }
}
