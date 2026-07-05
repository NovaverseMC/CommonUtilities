package it.novaverse.commonutilities.module.implementation.teleportation;

import com.google.common.collect.ImmutableMap;
import it.novaverse.commonutilities.annotation.ConfigValue;
import it.novaverse.commonutilities.module.Module;
import it.novaverse.commonutilities.service.PluginService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class VoidTeleport implements Module {

    private PluginService service;

    @ConfigValue(comment = "Enable or disable this module")
    private Boolean enabled = false;
    @ConfigValue(comment = "Task execution period in ticks (e.g. 5 means check every 5 ticks)")
    private Long taskPeriod = 5L;
    @ConfigValue(comment = "Map of world name to minimum Y coordinate before teleporting")
    private Map<String, Double> worlds = ImmutableMap.of("world", 0.0);
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

    private BukkitTask task = null;

    @Override
    public void onLoad(String name, PluginService service) {
        this.service = service;
    }

    @Override
    public void onEnable() {
        task = service.timer(() -> {
            service.getPlayers().forEach(player -> {
                var minAllowedY = worlds.get(player.getWorld().getName());
                if (minAllowedY == null || player.getLocation().getY() >= minAllowedY) {
                    return;
                }

                player.setFallDistance(0); // Prevent fall damage
                Location location = new Location(service.getWorld(destinationWorld), destinationX, destinationY,
                        destinationZ, destinationYaw, destinationPitch);
                player.teleport(location);
            });
        }, taskPeriod);
    }

    @Override
    public void onDisable() {
        task.cancel();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
