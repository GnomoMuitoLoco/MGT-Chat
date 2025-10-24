package br.com.magnatasoriginal.mgtchat.integration;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.minecraft.server.level.ServerPlayer;

/**
 * Integration with LuckPerms for permissions and prefix/suffix resolution.
 *
 * WHY: Replaces FTB Ranks integration with LuckPerms API.
 * Uses safe reflection and null checks to prevent crashes when LuckPerms is not present.
 *
 * @since 1.1.0
 */
public class LuckPermsIntegration {

    private static LuckPerms luckPerms;
    private static boolean initialized = false;
    private static boolean available = false;

    /**
     * Initialize LuckPerms API.
     * Should be called during mod setup.
     *
     * WHY: Lazy initialization to avoid issues if LuckPerms loads after this mod.
     */
    private static void init() {
        if (initialized) return;
        initialized = true;

        try {
            // Check if LuckPerms is loaded as a mod (optional - LuckPerms is usually a plugin)
            // For Fabric/NeoForge, LuckPerms may be present as a library
            luckPerms = LuckPermsProvider.get();
            available = true;
        } catch (IllegalStateException | NoClassDefFoundError e) {
            // LuckPerms not available
            available = false;
        }
    }

    /**
     * Check if LuckPerms is loaded and available.
     *
     * @return true if LuckPerms API is accessible
     */
    public static boolean isLoaded() {
        init();
        return available;
    }

    /**
     * Check if a player has a specific permission node.
     *
     * WHY: Centralizes permission checks with LuckPerms.
     *
     * @param player The player to check
     * @param permission The permission node (e.g., "mgtchat.admin.mute")
     * @return true if player has permission, false otherwise
     */
    public static boolean hasPermission(ServerPlayer player, String permission) {
        if (!isLoaded()) return false;

        try {
            User user = luckPerms.getUserManager().getUser(player.getUUID());
            if (user == null) return false;

            return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Get the player's prefix from LuckPerms.
     *
     * WHY: Resolves %luckperms-prefix% placeholder for chat formatting.
     *
     * @param player The player
     * @return The player's prefix, or empty string if not available
     */
    public static String getPrefix(ServerPlayer player) {
        if (!isLoaded()) return "";

        try {
            User user = luckPerms.getUserManager().getUser(player.getUUID());
            if (user == null) return "";

            CachedMetaData metaData = user.getCachedData().getMetaData();
            String prefix = metaData.getPrefix();
            return prefix != null ? prefix : "";
        } catch (Throwable t) {
            return "";
        }
    }

    /**
     * Get the player's suffix from LuckPerms.
     *
     * WHY: Resolves %luckperms-suffix% placeholder for chat formatting.
     *
     * @param player The player
     * @return The player's suffix, or empty string if not available
     */
    public static String getSuffix(ServerPlayer player) {
        if (!isLoaded()) return "";

        try {
            User user = luckPerms.getUserManager().getUser(player.getUUID());
            if (user == null) return "";

            CachedMetaData metaData = user.getCachedData().getMetaData();
            String suffix = metaData.getSuffix();
            return suffix != null ? suffix : "";
        } catch (Throwable t) {
            return "";
        }
    }
}

