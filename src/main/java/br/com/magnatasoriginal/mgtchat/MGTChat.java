package br.com.magnatasoriginal.mgtchat;

import br.com.magnatasoriginal.mgtchat.commands.GlobalCommand;
import br.com.magnatasoriginal.mgtchat.commands.IgnoreCommand;
import br.com.magnatasoriginal.mgtchat.commands.LocalCommand;
import br.com.magnatasoriginal.mgtchat.commands.TellCommand;
import br.com.magnatasoriginal.mgtchat.config.ChatConfig;
import br.com.magnatasoriginal.mgtchat.events.ChatEventHandler;
import br.com.magnatasoriginal.mgtchat.service.*;
import br.com.magnatasoriginal.mgtchat.storage.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;

/**
 * MGT-Chat - Sistema de chat avançado com canais, cores, filtros e integração com MGT-Core.
 *
 * WHY: Refatorado para seguir Clean Architecture com services e storage dedicados.
 *
 * @since 1.0.0
 * @version 1.1.0 - Arquitetura refatorada com services
 */
@Mod(MGTChat.MODID)
public class MGTChat {
    public static final String MODID = "mgtchat";
    public static final Logger LOGGER = LogUtils.getLogger();

    // WHY: Services e Storage instanciados no bootstrap e expostos via getters estáticos
    private static AntiSpamService antiSpamService;
    private static ChatFormatterService chatFormatterService;
    private static ChatLogger chatLogger;
    private static IgnoreListStorage ignoreListStorage;
    private static ConversationStorage conversationStorage;
    private static PlayerChannelStorage playerChannelStorage;
    private static MessageBroadcaster messageBroadcaster;
    private static PermissionService permissionService;
    private static MuteStorage muteStorage;
    private static ChatSpyStorage chatSpyStorage;
    private static BlockedCommandService blockedCommandService;

    public MGTChat(IEventBus modEventBus) {
        LOGGER.info("[MGT-Chat] Inicializando v1.1.0...");

        // WHY: Registrar config no NeoForge 1.21+
        ModLoadingContext.get().getActiveContainer()
                .registerConfig(ModConfig.Type.COMMON, ChatConfig.COMMON_SPEC, MODID + "-common.toml");

        // WHY: Instanciar services e storage (bootstrap)
        initializeServices();

        // WHY: Registrar listeners do ciclo de vida
        modEventBus.addListener(this::commonSetup);

        // WHY: Registrar comandos
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);

        // WHY: Registrar limpeza de dados ao desligar servidor
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);

        // WHY: Registrar eventos globais (chat, servidor, etc.)
        ChatEventHandler chatEventHandler = new ChatEventHandler(
            antiSpamService,
            chatFormatterService,
            messageBroadcaster,
            chatLogger,
            ignoreListStorage
        );
        NeoForge.EVENT_BUS.register(chatEventHandler);

        // WHY: Registrar handler para espionar comandos executados
        NeoForge.EVENT_BUS.register(new br.com.magnatasoriginal.mgtchat.events.CommandSpyHandler());

        // TODO: Desabilitar PrefixHandler por enquanto (conflita com ChatEventHandler)
        // NeoForge.EVENT_BUS.register(new PrefixHandler());
    }

    /**
     * Inicializa todos os services e storage.
     *
     * WHY: Centralizando bootstrap para facilitar testes e modificações futuras.
     */
    private void initializeServices() {
        LOGGER.info("[MGT-Chat] Inicializando services...");

        antiSpamService = new AntiSpamService();
        chatFormatterService = new ChatFormatterService();
        chatLogger = new ChatLogger();
        ignoreListStorage = new IgnoreListStorage();
        conversationStorage = new ConversationStorage();
        playerChannelStorage = new PlayerChannelStorage();
        messageBroadcaster = new MessageBroadcaster(ignoreListStorage);
        permissionService = new PermissionService();
        muteStorage = new MuteStorage();
        chatSpyStorage = new ChatSpyStorage();
        blockedCommandService = new br.com.magnatasoriginal.mgtchat.service.BlockedCommandService();

        // WHY: Config não está disponível ainda no construtor, será carregado em onRegisterCommands

        LOGGER.info("[MGT-Chat] Services inicializados com sucesso!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("[MGT-Chat] Common setup executado.");

        String globalFormat = ChatConfig.COMMON.globalFormat.get();
        String globalMessageColor = ChatConfig.COMMON.globalMessageColor.get();
        boolean debug = ChatConfig.COMMON.debug.get();

        LOGGER.info("[MGT-Chat] Formato global configurado: {}", globalFormat);
        LOGGER.info("[MGT-Chat] Cor padrão das mensagens globais: {}", globalMessageColor);

        if (debug) {
            LOGGER.debug("[MGT-Chat] Debug ativado!");
        }
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // WHY: Remover comandos vanilla que conflitam (tell/msg/w/r)
        dispatcher.getRoot().getChildren().removeIf(node -> {
            String name = node.getName();
            return "tell".equals(name) || "msg".equals(name) || "w".equals(name) || "r".equals(name);
        });

        // Attach dispatcher to blocked command service and apply config removals
        blockedCommandService.setDispatcher(dispatcher);
        if (ChatConfig.COMMON.blockedCommandsEnabled.get()) {
            blockedCommandService.applyConfigToDispatcher();
        }

        // WHY: Registrar nossos comandos
        LocalCommand.register(dispatcher);
        GlobalCommand.register(dispatcher);
        TellCommand.register(dispatcher);
        IgnoreCommand.register(dispatcher);

        // WHY: Registrar comandos administrativos
        br.com.magnatasoriginal.mgtchat.commands.admin.MuteCommand.register(dispatcher);
        br.com.magnatasoriginal.mgtchat.commands.admin.UnmuteCommand.register(dispatcher);
        br.com.magnatasoriginal.mgtchat.commands.admin.ClearChatCommand.register(dispatcher);
        br.com.magnatasoriginal.mgtchat.commands.admin.ChatSpyCommand.register(dispatcher);
        br.com.magnatasoriginal.mgtchat.commands.admin.ReloadCommand.register(dispatcher);
        // New admin commands for block/unblock
        br.com.magnatasoriginal.mgtchat.commands.admin.BlockCommand.register(dispatcher);
        br.com.magnatasoriginal.mgtchat.commands.admin.UnblockCommand.register(dispatcher);

        // WHY: Command spy agora usa CommandSpyHandler (evento CommandEvent) ao invés de dispatcher.setConsumer()
        // Motivo: setConsumer() pode ser sobrescrito por outros mods, evento é mais confiável

        LOGGER.info("[MGT-Chat] Comandos registrados: local, global, tell, ignorar, mute, unmute, clearchat, chatspy, bloquear, desbloquear");
    }

    /**
     * Limpa dados de todos os jogadores ao desligar servidor.
     *
     * WHY: Prevenir memory leak e garantir estado limpo no próximo boot.
     */
    private void onServerStopping(ServerStoppingEvent event) {
        LOGGER.info("[MGT-Chat] Servidor desligando, limpando dados...");
        // TODO: Implementar limpeza quando adicionar evento de player disconnect
    }

    // ============================================================
    // WHY: Getters estáticos para acesso aos services
    // Facilita uso em comandos sem precisar passar dependências manualmente.
    // ============================================================

    public static AntiSpamService getAntiSpamService() {
        return antiSpamService;
    }

    public static ChatFormatterService getChatFormatterService() {
        return chatFormatterService;
    }

    public static ChatLogger getChatLogger() {
        return chatLogger;
    }

    public static IgnoreListStorage getIgnoreListStorage() {
        return ignoreListStorage;
    }

    public static ConversationStorage getConversationStorage() {
        return conversationStorage;
    }

    public static PlayerChannelStorage getPlayerChannelStorage() {
        return playerChannelStorage;
    }

    public static MessageBroadcaster getMessageBroadcaster() {
        return messageBroadcaster;
    }

    public static PermissionService getPermissionService() {
        return permissionService;
    }

    public static MuteStorage getMuteStorage() {
        return muteStorage;
    }

    public static ChatSpyStorage getChatSpyStorage() {
        return chatSpyStorage;
    }

    public static BlockedCommandService getBlockedCommandService() {
        return blockedCommandService;
    }
}
