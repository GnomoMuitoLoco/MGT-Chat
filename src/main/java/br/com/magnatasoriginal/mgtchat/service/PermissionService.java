package br.com.magnatasoriginal.mgtchat.service;

import net.minecraft.server.level.ServerPlayer;

/**
 * Serviço de verificação de permissões administrativas.
 *
 * WHY: Suporta múltiplas fontes de permissão (FTB Ranks, OP, ambos)
 * conforme configuração do servidor.
 *
 * @since 1.1.0
 */
public class PermissionService {

    /**
     * Provider de permissões configurável.
     */
    public enum PermissionProvider {
        OP_ONLY,      // Apenas operadores
        FTBRANKS,     // Apenas FTB Ranks
        BOTH          // OP ou FTB Ranks
    }

    // Default para BOTH: usa FTB Ranks se presente OU fallback para OP
    private PermissionProvider provider = PermissionProvider.BOTH;

    /**
     * Define o provider de permissões.
     *
     * WHY: Permite configurar via arquivo TOML.
     */
    public void setProvider(PermissionProvider provider) {
        this.provider = provider;
    }

    /**
     * Verifica se um jogador tem permissão administrativa.
     *
     * WHY: Centraliza lógica de permissões com fallback para OP.
     *
     * @param player Jogador a verificar
     * @param permission Permissão específica (ex: "mgtchat.admin.mute")
     * @return true se tem permissão
     */
    public boolean hasPermission(ServerPlayer player, String permission) {
        switch (provider) {
            case OP_ONLY:
                return hasOpPermission(player);

            case FTBRANKS:
                return hasFtbRanksPermission(player, permission);

            case BOTH:
                return hasOpPermission(player) || hasFtbRanksPermission(player, permission);

            default:
                return false;
        }
    }

    /**
     * Verifica se jogador é OP (nível 2+).
     *
     * WHY: Nível 2 = permissões de comando básicas.
     */
    private boolean hasOpPermission(ServerPlayer player) {
        return player.hasPermissions(2);
    }

    /**
     * Verifica permissão via FTB Ranks.
     *
     * WHY: Integração segura via reflexão quando FTB Ranks está presente.
     */
    private boolean hasFtbRanksPermission(ServerPlayer player, String permission) {
        if (!br.com.magnatasoriginal.mgtchat.integration.FtbRanksIntegration.isLoaded()) return false;
        try {
            return br.com.magnatasoriginal.mgtchat.integration.FtbRanksIntegration.hasPermission(player, permission);
        } catch (Throwable t) {
            // Qualquer falha na integração retorna false e cai no fallback quando provider=BOTH
            return false;
        }
    }

    /**
     * Verifica se jogador pode usar comandos de moderação.
     */
    public boolean canModerate(ServerPlayer player) {
        return hasPermission(player, "mgtchat.admin.moderate");
    }

    /**
     * Verifica se jogador pode recarregar configurações.
     */
    public boolean canReload(ServerPlayer player) {
        return hasPermission(player, "mgtchat.admin.reload");
    }

    /**
     * Verifica se jogador pode usar spy mode.
     */
    public boolean canSpy(ServerPlayer player) {
        return hasPermission(player, "mgtchat.admin.spy");
    }

    /**
     * Verifica se jogador pode bloquear/desbloquear comandos.
     * Permissão exigida: magnatas.admin.bloquearcomandos
     */
    public boolean canBlockCommands(ServerPlayer player) {
        return hasPermission(player, "magnatas.admin.bloquearcomandos");
    }

    /**
     * Verifica se jogador pode usar bypass de comandos bloqueados.
     */
    public boolean canBypassBlockedCommands(ServerPlayer player) {
        return hasPermission(player, "mgtchat.bypass.blockedcommands");
    }
}
