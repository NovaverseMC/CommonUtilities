package it.feargames.commonutilities.module.implementation.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import it.feargames.commonutilities.annotation.ConfigValue;
import it.feargames.commonutilities.annotation.RegisterCommands;
import it.feargames.commonutilities.module.Module;
import it.feargames.commonutilities.service.PluginService;
import it.feargames.commonutilities.service.ProtocolServiceWrapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@RegisterCommands
@CommandPermission("common.raid")
@CommandAlias("raid")
public class Raid extends BaseCommand implements Module {

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

    @Subcommand("start")
    @Description("Start a new raid")
    public void onRaidStart(CommandSender sender) {
        raidCheckedPlayers = new HashSet<>();
        service.broadcast(ChatColor.GREEN + "Server data (functions, advancements, etc...) reloaded!", "common.raid");
    }

    @Subcommand("next")
    @Description("Continue the current raid")
    public void onRaidStart(Player player) {
        if (raidCheckedPlayers == null) {
            player.sendMessage(ChatColor.RED + "You have firstly to start a raid!");
            return;
        }
        for (Player currentPlayer : service.getPlayers()) {
            if (raidCheckedPlayers.contains(currentPlayer.getName())) {
                continue;
            }
            player.teleport(currentPlayer);
            raidCheckedPlayers.add(currentPlayer.getName());
            player.sendMessage(ChatColor.YELLOW + "Teleported to " + currentPlayer.getName());
            return;
        }
        raidCheckedPlayers = null;
        service.broadcast(ChatColor.GREEN + "Raid completed!", "common.raid");
    }

    @HelpCommand
    public void doHelp(CommandSender sender, CommandHelp help) {
        sender.sendMessage("---- CommonUtilities Raid ----");
        help.showHelp();
    }
}
