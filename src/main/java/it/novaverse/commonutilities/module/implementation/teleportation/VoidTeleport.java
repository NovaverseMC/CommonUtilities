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

    @ConfigValue
    private Boolean enabled = false;
    @ConfigValue
    private Long taskPeriod = 5L;
    @ConfigValue
    private Map<String, Double> worlds = ImmutableMap.of("world", 0.0);
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

    private BukkitTask task = null;

    @Override
    public void onLoad(String name, PluginService service) {
        this.service = service;
    }

    @Override
    public void onEnable() {
        task = service.timer(() -> {
            service.getPlayers().forEach(player -> {
                Double minAllowedY = worlds.get(player.getWorld().getName());
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
