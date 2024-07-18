package it.novaverse.commonutilities.module.implementation.teleportation;

import it.novaverse.commonutilities.annotation.ConfigValue;
import it.novaverse.commonutilities.annotation.RegisterListeners;
import it.novaverse.commonutilities.module.Module;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

@RegisterListeners
public class HubPortalCommand implements Module, Listener {
    @ConfigValue
    private Boolean enabled = false;

    @ConfigValue
    private String command = "gamemodes";

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!hasWalkedFullBlock(event.getFrom(), event.getTo())) {
            return;
        }

        Block block = event.getTo().getBlock();
        Material material = block.getType();

        boolean portalBlock = material.name().toLowerCase().contains("portal");
        String patchedCommand = command.replace("%player%", event.getPlayer().getName());
        if (portalBlock) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), patchedCommand);
    }


    public boolean hasWalkedFullBlock(Location from, Location to) {
        return from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ();
    }

}
