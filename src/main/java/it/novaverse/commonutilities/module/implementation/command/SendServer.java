package it.novaverse.commonutilities.module.implementation.command;

import it.novaverse.commonutilities.annotation.ConfigValue;
import it.novaverse.commonutilities.module.Module;
import it.novaverse.commonutilities.service.PluginService;
import it.novaverse.commonutilities.service.ProtocolServiceWrapper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;

@CommandContainer
public class SendServer implements Module, CommonCommand {

    private PluginService service;

    @ConfigValue
    private Boolean enabled = true;

    @Override
    public void onLoad(String name, PluginService service, ProtocolServiceWrapper protocol) {
        this.service = service;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Command("sendserver <player> <server>")
    @CommandDescription("Sends a player to a server")
    @Permission("common.sendserver")
    public void onSendServer(CommandSender sender, @Argument("player") Player player, @Argument("server") String server) {
        service.sendMessage(sender, "<green>Sending " + player.getName() + " to server " + server);
        service.connectToServer(player, server);
    }
}
