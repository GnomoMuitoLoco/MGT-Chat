package br.com.magnatasoriginal.mgtchat.service;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serviço para gerenciar comandos bloqueados em tempo de execução.
 * Armazena nodes removidos para permitir desbloqueio posterior.
 */
public class BlockedCommandService {
    private final Set<String> blocked = ConcurrentHashMap.newKeySet();
    private final Map<String, CommandNode<CommandSourceStack>> removedNodes = new ConcurrentHashMap<>();
    private CommandDispatcher<CommandSourceStack> dispatcher;

    public void setDispatcher(CommandDispatcher<CommandSourceStack> dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void loadFromConfig(java.util.List<? extends String> fromConfig) {
        blocked.clear();
        if (fromConfig == null) return;
        for (Object o : fromConfig) {
            if (o instanceof String s) blocked.add(normalize(s));
        }
    }

    public void applyConfigToDispatcher() {
        if (dispatcher == null) return;
        for (String cmd : Set.copyOf(blocked)) {
            removeNode(cmd);
        }
    }

    public void addBlocked(String cmd) {
        String n = normalize(cmd);
        blocked.add(n);
        removeNode(n);
    }

    public boolean removeBlocked(String cmd) {
        String n = normalize(cmd);
        boolean removed = blocked.remove(n);
        if (removed) {
            restoreNode(n);
        }
        return removed;
    }

    public boolean isBlocked(String cmd) {
        return blocked.contains(normalize(cmd));
    }

    public Set<String> getBlockedCommands() {
        return Set.copyOf(blocked);
    }

    private void removeNode(String cmd) {
        if (dispatcher == null) return;
        CommandNode<CommandSourceStack> node = dispatcher.getRoot().getChild(cmd);
        if (node != null) {
            removedNodes.put(cmd, node);
            dispatcher.getRoot().getChildren().remove(node);
        }
    }

    private void restoreNode(String cmd) {
        if (dispatcher == null) return;
        CommandNode<CommandSourceStack> node = removedNodes.remove(cmd);
        if (node != null) {
            dispatcher.getRoot().addChild(node);
        }
    }

    private String normalize(String s) {
        if (s == null) return "";
        String n = s.trim();
        if (n.startsWith("/")) n = n.substring(1);
        return n.toLowerCase();
    }
}

