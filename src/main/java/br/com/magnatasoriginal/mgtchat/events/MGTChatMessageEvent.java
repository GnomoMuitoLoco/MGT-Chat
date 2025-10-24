package br.com.magnatasoriginal.mgtchat.events;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Custom event fired when a chat message is sent through MGT-Chat.
 *
 * WHY: This event is fired for both normal chat and commands (/g, /l),
 * allowing Discord integration mods to listen to all chat messages consistently.
 *
 * NOTE: This solves the bug where /g <message> doesn't relay to Discord
 * while normal global chat does.
 *
 * @since 1.1.0
 */
public class MGTChatMessageEvent extends Event implements ICancellableEvent {

    public enum Channel {
        GLOBAL,
        LOCAL,
        PRIVATE
    }

    private final ServerPlayer sender;
    private final String rawMessage;
    private final Component formattedMessage;
    private final Channel channel;
    private boolean canceled = false;

    public MGTChatMessageEvent(ServerPlayer sender, String rawMessage, Component formattedMessage, Channel channel) {
        this.sender = sender;
        this.rawMessage = rawMessage;
        this.formattedMessage = formattedMessage;
        this.channel = channel;
    }

    public ServerPlayer getSender() {
        return sender;
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public Component getFormattedMessage() {
        return formattedMessage;
    }

    public Channel getChannel() {
        return channel;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}

