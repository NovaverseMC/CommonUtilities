package it.feargames.commonutilities.module.implementation.teleportation;

import it.feargames.commonutilities.annotation.ConfigValue;
import it.feargames.commonutilities.annotation.RegisterListeners;
import it.feargames.commonutilities.module.Module;
import it.feargames.commonutilities.service.PluginService;
import it.feargames.commonutilities.service.ProtocolServiceWrapper;
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
public class Spawn implements Module, Listener {

    private PluginService service;

    @ConfigValue
    private final Boolean enabled = false;
    @ConfigValue
    private final Boolean onJoin = true;
    @ConfigValue
    private final Boolean onRespawn = true;
    @ConfigValue
    private final String destinationWorld = "world";
    @ConfigValue
    private final Double destinationX = 0.0;
    @ConfigValue
    private final Double destinationY = 0.0;
    @ConfigValue
    private final Double destinationZ = 0.0;
    @ConfigValue
    private final Float destinationYaw = 0.0F;
    @ConfigValue
    private final Float destinationPitch = 0.0F;

    @Override
    public void onLoad(String name, PluginService service, ProtocolServiceWrapper protocol) {
        this.service = service;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    private Location getLocation() {
        return new Location(service.getWorld(destinationWorld), destinationX, destinationY,
                destinationZ, destinationYaw, destinationPitch);
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
