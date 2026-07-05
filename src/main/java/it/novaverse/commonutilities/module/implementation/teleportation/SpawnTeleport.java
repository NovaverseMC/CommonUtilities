package it.novaverse.commonutilities.module.implementation.teleportation;

import it.novaverse.commonutilities.annotation.ConfigValue;
import it.novaverse.commonutilities.annotation.RegisterListeners;
import it.novaverse.commonutilities.module.Module;
import it.novaverse.commonutilities.service.PluginService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

@RegisterListeners
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class SpawnTeleport implements Module, Listener {

    private PluginService service;

    @ConfigValue(comment = "Enable or disable this module")
    private Boolean enabled = false;
    @ConfigValue(comment = "If true, teleport players to spawn when they join")
    private Boolean onJoin = true;
    @ConfigValue(comment = "If true, teleport players to spawn when they respawn")
    private Boolean onRespawn = true;
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!onJoin) {
            return;
        }
        event.getPlayer().teleport(getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        if (!onRespawn) {
            return;
        }
        event.setRespawnLocation(getLocation());
    }

}
