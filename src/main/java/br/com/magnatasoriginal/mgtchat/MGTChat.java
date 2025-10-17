package br.com.magnatasoriginal.mgtchat;

import br.com.magnatasoriginal.mgtchat.commands.GlobalCommand;
import br.com.magnatasoriginal.mgtchat.commands.LocalCommand;
import br.com.magnatasoriginal.mgtchat.commands.TellCommand;
import br.com.magnatasoriginal.mgtchat.config.ChatConfig;
import br.com.magnatasoriginal.mgtchat.events.ChatEventHandler;
import br.com.magnatasoriginal.mgtchat.events.PrefixHandler;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

/**
 * MGT-Chat
 * Sistema de chat avançado com canais, cores, filtros e integração com MGT-Core.
 */
@Mod(MGTChat.MODID)
public class MGTChat {
    public static final String MODID = "mgtchat";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MGTChat(IEventBus modEventBus) {
        LOGGER.info("[MGT-Chat] Inicializando...");

        // Registrar config no NeoForge 1.21+
        ModLoadingContext.get().getActiveContainer()
                .registerConfig(ModConfig.Type.COMMON, ChatConfig.COMMON_SPEC, MODID + "-common.toml");

        // Registrar listeners do ciclo de vida
        modEventBus.addListener(this::commonSetup);

        // Registrar comandos
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);

        // Registrar eventos globais (chat, servidor, etc.)
        NeoForge.EVENT_BUS.register(new ChatEventHandler());
        NeoForge.EVENT_BUS.register(new PrefixHandler());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("[MGT-Chat] Common setup executado.");

        // Exemplo: ler configs
        String globalPrefix = ChatConfig.COMMON.globalPrefix.get();
        boolean debug = ChatConfig.COMMON.debug.get();

        LOGGER.info("[MGT-Chat] Prefixo global configurado: {}", globalPrefix);
        if (debug) {
            LOGGER.debug("[MGT-Chat] Debug ativado!");
        }
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        LocalCommand.register(event.getDispatcher());
        GlobalCommand.register(event.getDispatcher());
        TellCommand.register(event.getDispatcher());
    }
}
