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

@RegisterListeners
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class TeleportOnJoin implements Module, Listener {

    private PluginService service;

    @ConfigValue
    private Boolean enabled = false;
    @ConfigValue
    private String destinationWorld = "world";
    @ConfigValue
    private Double destinationX = 0.0;
    @ConfigValue
    private Double destinationY = 0.0;
    @ConfigValue
    private Double destinationZ = 0.0;
    @ConfigValue
    private Float destinationYaw = 0.0F;
    @ConfigValue
    private Float destinationPitch = 0.0F;

    @Override
    public void onLoad(String name, PluginService service, ProtocolServiceWrapper protocol) {
        this.service = service;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Location location = new Location(service.getWorld(destinationWorld), destinationX, destinationY,
                destinationZ, destinationYaw, destinationPitch);
        service.schedule(() -> event.getPlayer().teleport(location));
    }

}
