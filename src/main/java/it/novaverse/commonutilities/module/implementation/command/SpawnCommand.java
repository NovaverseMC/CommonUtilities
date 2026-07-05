package it.novaverse.commonutilities.module.implementation.command;

import it.novaverse.commonutilities.annotation.ConfigValue;
import it.novaverse.commonutilities.module.Module;
import it.novaverse.commonutilities.service.PluginService;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.processing.CommandContainer;

@CommandContainer
public class SpawnCommand implements Module, CommonCommand {

    private PluginService service;

    @ConfigValue(comment = "Enable or disable this module")
    private Boolean enabled = true;
    @ConfigValue(comment = "The destination world name")
    private String destinationWorld = "world";
    @ConfigValue(comment = "Destination X coordinate")
    private Double destinationX = 0.0;
    @ConfigValue(comment = "Destination Y coordinate")
    private Double destinationY = 0.0;
    @ConfigValue(comment = "Destination Z coordinate")
    private Double destinationZ = 0.0;
    @ConfigValue(comment = "Destination Yaw rotation")
    private Float destinationYaw = 0.0F;
    @ConfigValue(comment = "Destination Pitch rotation")
    private Float destinationPitch = 0.0F;

    @Override
    public void onLoad(String name, PluginService service) {
        this.service = service;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    private Location getLocation() {
        return new Location(service.getWorld(destinationWorld), destinationX, destinationY,
                destinationZ, destinationYaw, destinationPitch
        );
    }

    @Command("spawn")
    @CommandDescription("Go to the server spawn.")
    @Permission("common.spawn")
    public void onSendServer(Player sender) {
        sender.teleport(getLocation());
    }
}
