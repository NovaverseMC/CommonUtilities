package it.feargames.commonutilities.module.implementation.security;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.server.TemporaryPlayer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import it.feargames.commonutilities.annotation.ConfigValue;
import it.feargames.commonutilities.module.Module;
import it.feargames.commonutilities.service.PluginService;
import it.feargames.commonutilities.service.ProtocolServiceWrapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Log
public class HideUnwantedCommands implements Module {

    private final static String LISTENER_ID = "HideUnwantedCommands";

    private PluginService service;
    private ProtocolServiceWrapper wrapper;

    @ConfigValue
    private Boolean enabled = false;
    @ConfigValue
    private List<String> commandBlacklist = Lists.newArrayList("worldedit", "worldguard");

    @Override
    public void onLoad(String name, PluginService service, ProtocolServiceWrapper wrapper) {
        this.service = service;
        this.wrapper = wrapper;
    }

    @Override
    public void onEnable() {
        wrapper.handle(protocol -> {
            protocol.addSendingListener(LISTENER_ID, ListenerPriority.HIGHEST, PacketType.Play.Server.COMMANDS, event -> {
                if(event.getPlayer() instanceof TemporaryPlayer || event.getPlayer().hasPermission("common.command.bypass")) {
                    return;
                }

                PacketContainer packet = event.getPacket();
                RootCommandNode<?> rootNode = (RootCommandNode<?>) packet.getModifier().read(0);

                Map<String, CommandNode<?>> children = Maps.newLinkedHashMap();
                for (CommandNode<?> node : rootNode.getChildren()) {
                    if (commandBlacklist.contains(node.getName())) {
                        continue;
                    }
                    children.put(node.getName(), node);
                }
                try {
                    FieldUtils.writeField(rootNode, "children", children, true);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    log.log(Level.WARNING, "Unable to handle the Commands packet!", e);
                }
            });
        });
        service.getPlayers().forEach(Player::updateCommands);
    }

    @Override
    public void onDisable() {
        wrapper.handle(protocol -> protocol.removePacketListener(LISTENER_ID));
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
