package br.com.magnatasoriginal.mgtchat.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class ChatConfig {

    public static final ModConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        COMMON = new Common(builder);
        COMMON_SPEC = builder.build();
    }

    public static class Common {
        // General
        public final ModConfigSpec.BooleanValue debug;
        public final ModConfigSpec.ConfigValue<String> globalFormat;
        public final ModConfigSpec.ConfigValue<String> localFormat;
        public final ModConfigSpec.ConfigValue<String> globalMessageColor;
        public final ModConfigSpec.ConfigValue<String> localMessageColor;
        public final ModConfigSpec.IntValue localRange;
        public final ModConfigSpec.BooleanValue allowHexColors;
        public final ModConfigSpec.BooleanValue allowLegacyColors;

        // Private (Tell)
        public final ModConfigSpec.ConfigValue<String> tellFormatTo;
        public final ModConfigSpec.ConfigValue<String> tellFormatFrom;
        public final ModConfigSpec.ConfigValue<String> replyTellFormatTo;
        public final ModConfigSpec.ConfigValue<String> replyTellFormatFrom;

        // AntiSpam
        public final ModConfigSpec.IntValue messageDelay;
        public final ModConfigSpec.BooleanValue blockRepeated;
        public final ModConfigSpec.IntValue maxRepeated;

        // Filter
        public final ModConfigSpec.BooleanValue filterEnabled;
        public final ModConfigSpec.ConfigValue<List<? extends String>> blockedWords;
        public final ModConfigSpec.ConfigValue<String> replacement;

        // FTB Ranks
        public final ModConfigSpec.BooleanValue usePrefixes;
        public final ModConfigSpec.ConfigValue<String> format;

        Common(ModConfigSpec.Builder builder) {
            builder.push("general");

            debug = builder.comment("Ativar logs de debug do chat")
                    .define("debug", false);

            // Formatos por canal com cor apenas na mensagem
            globalFormat = builder.comment("Formato do chat global. Placeholders: {prefix}, {player}, {message_color}, {message}")
                    .define("globalFormat", "&8[&3g&8] &r{prefix} &r{player}&f: {message_color}{message}");

            localFormat = builder.comment("Formato do chat local. Placeholders: {prefix}, {player}, {message_color}, {message}")
                    .define("localFormat", "&8[&el&8] &r{prefix} &r{player}&f: {message_color}{message}");

            globalMessageColor = builder.comment("Cor aplicada às mensagens globais")
                    .define("globalMessageColor", "&3");

            localMessageColor = builder.comment("Cor aplicada às mensagens locais")
                    .define("localMessageColor", "&e");

            localRange = builder.comment("Distância máxima (em blocos) para o chat local")
                    .defineInRange("localRange", 100, 10, 10000);

            allowHexColors = builder.comment("Permitir uso de cores hexadecimais no chat")
                    .define("allowHexColors", true);

            allowLegacyColors = builder.comment("Permitir uso de códigos de cor & e §")
                    .define("allowLegacyColors", true);

            builder.pop();

            builder.push("private");

            tellFormatTo = builder.comment("Formato da mensagem privada enviada. Placeholders: {send_player}, {receive_player}, {message}")
                    .define("tellFormatTo", "&8[&cr&8] &7Sussurou para &r{receive_player}: &c{message}");

            tellFormatFrom = builder.comment("Formato da mensagem privada recebida. Placeholders: {send_player}, {receive_player}, {message}")
                    .define("tellFormatFrom", "&8[&cr&8] &r{send_player} sussurrou: &c{message}");

            replyTellFormatTo = builder.comment("Formato da mensagem enviada via /r. Placeholders: {send_player}, {receive_player}, {message}")
                    .define("replyTellFormatTo", "&8[&cr&8] &7Sussurou para &r{receive_player}: &c{message}");

            replyTellFormatFrom = builder.comment("Formato da mensagem recebida via /r. Placeholders: {send_player}, {receive_player}, {message}")
                    .define("replyTellFormatFrom", "&8[&cr&8] &r{send_player} sussurrou: &c{message}");

            builder.pop();

            builder.push("antiSpam");

            messageDelay = builder.comment("Tempo mínimo entre mensagens (ms)")
                    .defineInRange("messageDelay", 2000, 0, 60000);

            blockRepeated = builder.comment("Bloquear mensagens repetidas consecutivas")
                    .define("blockRepeated", true);

            maxRepeated = builder.comment("Número máximo de mensagens iguais permitidas em sequência")
                    .defineInRange("maxRepeated", 2, 1, 10);

            builder.pop();

            builder.push("filter");

            filterEnabled = builder.comment("Ativar filtro de palavras proibidas")
                    .define("enabled", true);

            blockedWords = builder.comment("Lista de palavras bloqueadas (case-insensitive)")
                    .defineList("blockedWords",
                            List.of("palavrão1", "palavrão2", "palavrão3"),
                            o -> o instanceof String);

            replacement = builder.comment("Texto substituto para palavras bloqueadas")
                    .define("replacement", "***");

            builder.pop();

            builder.push("ftbRanks");

            usePrefixes = builder.comment("Ativar integração com FTB Ranks")
                    .define("usePrefixes", true);

            format = builder.comment("Formato da mensagem com prefixo. Placeholders: {prefix}, {player}, {message}")
                    .define("format", "{prefix} {player}: {message}");

            builder.pop();
        }
    }
}
