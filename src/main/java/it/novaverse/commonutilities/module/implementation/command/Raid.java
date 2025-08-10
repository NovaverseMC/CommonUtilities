package it.novaverse.commonutilities.module.implementation.command;

import it.novaverse.commonutilities.annotation.ConfigValue;
import it.novaverse.commonutilities.module.Module;
import it.novaverse.commonutilities.service.PluginService;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;

import java.util.HashSet;
import java.util.Set;

@CommandContainer
public class Raid implements Module, CommonCommand {

    private PluginService service;

    @ConfigValue
    private Boolean enabled = true;

    private Set<String> raidCheckedPlayers;

    @Override
    public void onLoad(String name, PluginService service) {
        this.service = service;
    }

    @Override
    public void onEnable() {
        raidCheckedPlayers = null;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Command("raid start")
    @CommandDescription("Start a new raid")
    @Permission("common.raid")
    public void onRaidStart() {
        raidCheckedPlayers = new HashSet<>();

        service.broadcast(
                "<green>A new staff raid has started! Use '/raid next' to teleport to the next player!",
                "common.raid"
        );
    }

    @Command("raid next")
    @CommandDescription("Continue the current raid")
    @Permission("common.raid")
    public void onRaidNext(Player sender) {
        if (raidCheckedPlayers == null) {
            service.sendMessage(sender, "<red>You have firstly to start a raid!");
            return;
        }

        for (Player currentPlayer : service.getPlayers()) {
            if (raidCheckedPlayers.contains(currentPlayer.getName())) continue;

            sender.teleport(currentPlayer);
            raidCheckedPlayers.add(currentPlayer.getName());

            service.sendMessage(sender, "<yellow>Teleported to " + currentPlayer.getName());
            return;
        }

        raidCheckedPlayers = null;
        service.broadcast("<green>Raid completed!", "common.raid");
    }
}
