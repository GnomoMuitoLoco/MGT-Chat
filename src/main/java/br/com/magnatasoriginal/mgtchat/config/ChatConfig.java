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
        public final ModConfigSpec.ConfigValue<String> globalPrefix;
        public final ModConfigSpec.ConfigValue<String> localPrefix;
        public final ModConfigSpec.IntValue localRange;
        public final ModConfigSpec.ConfigValue<String> tellColor;
        public final ModConfigSpec.BooleanValue allowHexColors;
        public final ModConfigSpec.BooleanValue allowLegacyColors;

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
            debug = builder.comment("Ativar logs de debug do chat").define("debug", false);
            globalPrefix = builder.comment("Prefixo padrão para mensagens globais").define("globalPrefix", "&6[Global]");
            localPrefix = builder.comment("Prefixo padrão para mensagens locais").define("localPrefix", "&a[Local]");
            localRange = builder.comment("Distância máxima (em blocos) para o chat local").defineInRange("localRange", 100, 10, 10000);
            tellColor = builder.comment("Cor padrão para mensagens privadas (/tell)").define("tellColor", "&d");
            allowHexColors = builder.comment("Permitir uso de cores hexadecimais no chat").define("allowHexColors", true);
            allowLegacyColors = builder.comment("Permitir uso de códigos de cor & e §").define("allowLegacyColors", true);
            builder.pop();

            builder.push("antiSpam");
            messageDelay = builder.comment("Tempo mínimo entre mensagens (ms)").defineInRange("messageDelay", 2000, 0, 60000);
            blockRepeated = builder.comment("Bloquear mensagens repetidas consecutivas").define("blockRepeated", true);
            maxRepeated = builder.comment("Número máximo de mensagens iguais permitidas em sequência").defineInRange("maxRepeated", 2, 1, 10);
            builder.pop();

            builder.push("filter");
            filterEnabled = builder.comment("Ativar filtro de palavras proibidas").define("enabled", true);

            // Usa define(List) para evitar métodos deprecated de lista
            blockedWords = builder.comment("Lista de palavras bloqueadas (case-insensitive)")
                    .defineListAllowEmpty("blockedWords", List.of("palavrão1", "palavrão2", "palavrão3"), o -> o instanceof String);



            replacement = builder.comment("Texto substituto para palavras bloqueadas")
                    .define("replacement", "***");
            builder.pop();

            builder.push("ftbRanks");
            usePrefixes = builder.comment("Ativar integração com FTB Ranks").define("usePrefixes", true);
            format = builder.comment("Formato da mensagem com prefixo. Placeholders: {prefix}, {player}, {message}")
                    .define("format", "{prefix} {player}: {message}");
            builder.pop();
        }
    }
}
