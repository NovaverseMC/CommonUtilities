package it.feargames.commonutilities.module.implementation.security;

import it.feargames.commonutilities.annotation.ConfigValue;
import it.feargames.commonutilities.annotation.RegisterListeners;
import it.feargames.commonutilities.module.Module;
import it.feargames.commonutilities.service.PluginService;
import it.feargames.commonutilities.service.ProtocolServiceWrapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.messaging.PluginMessageListener;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@RegisterListeners
public class AntiWorldDownloader implements Module, Listener, PluginMessageListener {

    private final static String INCOMING_CHANNEL = "WDL|INIT";
    private final static String OUTGOING_CHANNEL = "WDL|CONTROL";

    private PluginService service;

    @ConfigValue
    private Boolean enabled = false;
    @ConfigValue
    private String punishCommand = "kick %p WDL is not authorized on this server!";

    @Override
    public void onLoad(String name, PluginService service, ProtocolServiceWrapper protocol) {
        this.service = service;
    }

    @Override
    public void onEnable() {
        service.registerIncomingPluginChannel(INCOMING_CHANNEL, this);
        service.registerOutgoingPluginChannel(OUTGOING_CHANNEL);
    }

    @Override
    public void onDisable() {
        service.unregisterIncomingPluginChannel(INCOMING_CHANNEL);
        service.unregisterOutgoingPluginChannel(OUTGOING_CHANNEL);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (player.hasPermission("common.antiworlddownloader.bypass")) {
            return;
        }
        service.dispatchCommand(punishCommand.replace("%", player.getName()));
    }
}
