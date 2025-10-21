package br.com.magnatasoriginal.mgtchat.util;

import net.minecraft.server.level.ServerPlayer;

/**
 * Utilitário para detectar jogadores invisíveis (vanish, spectator).
 *
 * WHY: Chat local deve ignorar espectadores e jogadores em vanish
 * ao contar destinatários, para evitar avisar "ninguém por perto"
 * quando há apenas espectadores.
 *
 * @since 1.1.0
 */
public class VanishDetector {

    /**
     * Verifica se um jogador deve ser considerado "invisível" para chat local.
     *
     * WHY: Espectadores não devem contar como destinatários de chat local,
     * e jogadores em modo vanish (se integração futura) também não.
     *
     * @param player Jogador a verificar
     * @return true se o jogador está invisível/espectador
     */
    public static boolean isInvisible(ServerPlayer player) {
        // Espectadores são sempre considerados invisíveis
        if (player.isSpectator()) {
            return true;
        }

        // TODO: Integrar com plugins de vanish (ex: Essentials, CMI)
        // if (VanishIntegration.isVanished(player)) {
        //     return true;
        // }

        return false;
    }

    /**
     * Verifica se um jogador pode receber mensagens de chat local.
     *
     * WHY: Centraliza lógica de filtro para chat local.
     *
     * @param player Jogador a verificar
     * @return true se pode receber mensagens de chat local
     */
    public static boolean canReceiveLocalChat(ServerPlayer player) {
        return !isInvisible(player);
    }
}

