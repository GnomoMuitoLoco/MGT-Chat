package br.com.magnatasoriginal.mgtchat.commands.admin;

import br.com.magnatasoriginal.mgtchat.MGTChat;
import br.com.magnatasoriginal.mgtcore.util.ColorUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.TimeUnit;

/**
 * Comando /mute para silenciar jogadores.
 *
 * Uso:
 * - /mute <player> - Mute permanente
 * - /mute <player> <duration> [s|m|h|d] - Mute temporário
 *
 * WHY: Permite moderadores silenciarem jogadores problemáticos.
 * Requer permissão: mgtchat.admin.moderate ou OP nível 2+
 *
 * @since 1.1.0
 */
public class MuteCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /mute <player> - permanente
        dispatcher.register(Commands.literal("mute")
                .requires(source -> {
                    try {
                        ServerPlayer player = source.getPlayerOrException();
                        return MGTChat.getPermissionService().canModerate(player);
                    } catch (Exception e) {
                        return true; // Console sempre pode
                    }
                })
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> {
                            ServerPlayer admin = ctx.getSource().getPlayerOrException();
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");

                            MGTChat.getMuteStorage().mute(target.getUUID());

                            admin.sendSystemMessage(ColorUtil.translate(
                                "§aVocê mutou §f" + target.getName().getString() + " §apermanentemente."
                            ));
                            target.sendSystemMessage(ColorUtil.translate(
                                "§cVocê foi mutado permanentemente por um moderador."
                            ));

                            MGTChat.getChatLogger().logAdminCommand(admin, "mute", target.getName().getString());

                            return 1;
                        })
                        // /mute <player> <duration> <unit>
                        .then(Commands.argument("duration", IntegerArgumentType.integer(1))
                                .then(Commands.argument("unit", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            builder.suggest("s");
                                            builder.suggest("m");
                                            builder.suggest("h");
                                            builder.suggest("d");
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            ServerPlayer admin = ctx.getSource().getPlayerOrException();
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                            int duration = IntegerArgumentType.getInteger(ctx, "duration");
                                            String unit = StringArgumentType.getString(ctx, "unit");

                                            long durationMillis = parseDuration(duration, unit);

                                            if (durationMillis <= 0) {
                                                admin.sendSystemMessage(ColorUtil.translate(
                                                    "§cUnidade inválida! Use: s (segundos), m (minutos), h (horas), d (dias)"
                                                ));
                                                return 0;
                                            }

                                            MGTChat.getMuteStorage().mute(target.getUUID(), durationMillis);

                                            String timeStr = formatDuration(duration, unit);
                                            admin.sendSystemMessage(ColorUtil.translate(
                                                "§aVocê mutou §f" + target.getName().getString() + " §apor " + timeStr + "."
                                            ));
                                            target.sendSystemMessage(ColorUtil.translate(
                                                "§cVocê foi mutado por " + timeStr + " por um moderador."
                                            ));

                                            MGTChat.getChatLogger().logAdminCommand(
                                                admin,
                                                "mute " + timeStr,
                                                target.getName().getString()
                                            );

                                            return 1;
                                        })
                                )
                        )
                )
        );
    }

    /**
     * Converte duração para milissegundos.
     */
    private static long parseDuration(int value, String unit) {
        return switch (unit.toLowerCase()) {
            case "s" -> TimeUnit.SECONDS.toMillis(value);
            case "m" -> TimeUnit.MINUTES.toMillis(value);
            case "h" -> TimeUnit.HOURS.toMillis(value);
            case "d" -> TimeUnit.DAYS.toMillis(value);
            default -> -1;
        };
    }

    /**
     * Formata duração para exibição.
     */
    private static String formatDuration(int value, String unit) {
        String unitName = switch (unit.toLowerCase()) {
            case "s" -> value == 1 ? "segundo" : "segundos";
            case "m" -> value == 1 ? "minuto" : "minutos";
            case "h" -> value == 1 ? "hora" : "horas";
            case "d" -> value == 1 ? "dia" : "dias";
            default -> unit;
        };
        return value + " " + unitName;
    }
}

