package br.com.magnatasoriginal.mgtchat.integration;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;

public class FtbRanksIntegration {

    public static boolean isLoaded() {
        try {
            return ModList.get().isLoaded("ftbranks");
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean hasPermission(ServerPlayer player, String permission) {
        if (!isLoaded()) return false;
        try {
            // Reflection: dev.ftb.mods.ftbranks.api.FTBRanksAPI.get().getPermissionValue(player, permission).asBoolean()
            Class<?> apiClass = Class.forName("dev.ftb.mods.ftbranks.api.FTBRanksAPI");
            Method getMethod = apiClass.getMethod("get");
            Object api = getMethod.invoke(null);
            Method getPermVal = apiClass.getMethod("getPermissionValue", net.minecraft.world.entity.player.Player.class, String.class);
            Object permVal = getPermVal.invoke(api, player, permission);
            Method asBool = permVal.getClass().getMethod("asBoolean");
            Object res = asBool.invoke(permVal);
            return res instanceof Boolean b && b;
        } catch (Throwable t) {
            return false;
        }
    }
}

