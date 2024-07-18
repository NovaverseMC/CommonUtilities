package it.novaverse.commonutilities.module.implementation.command;

import it.novaverse.commonutilities.annotation.ConfigValue;
import it.novaverse.commonutilities.annotation.RegisterCommands;
import it.novaverse.commonutilities.module.Module;
import it.novaverse.commonutilities.service.PluginService;
import it.novaverse.commonutilities.service.ProtocolServiceWrapper;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;

import java.util.HashSet;
import java.util.Set;

@RegisterCommands
@CommandContainer
public class Raid implements Module {

    private PluginService service;

    @ConfigValue
    private Boolean enabled = true;

    private Set<String> raidCheckedPlayers;

    @Override
    public void onLoad(String name, PluginService service, ProtocolServiceWrapper protocol) {
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
                ChatColor.GREEN + "A new staff raid has started!" + "Use '/raid next' to teleport to the next player!",
                "common.raid"
        );
    }

    @Command("raid next")
    @CommandDescription("Continue the current raid")
    @Permission("common.raid")
    public void onRaidNext(Player player) {
        if (raidCheckedPlayers == null) {
            player.sendMessage(ChatColor.RED + "You have firstly to start a raid!");
            return;
        }

        for (Player currentPlayer : service.getPlayers()) {
            if (raidCheckedPlayers.contains(currentPlayer.getName())) continue;

            player.teleport(currentPlayer);
            raidCheckedPlayers.add(currentPlayer.getName());

            player.sendMessage(ChatColor.YELLOW + "Teleported to " + currentPlayer.getName());
            return;
        }

        raidCheckedPlayers = null;
        service.broadcast(ChatColor.GREEN + "Raid completed!", "common.raid");
    }
}
