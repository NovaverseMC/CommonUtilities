package it.feargames.commonutilities.module.implementation.teleportation;

import com.google.common.collect.ImmutableMap;
import it.feargames.commonutilities.annotation.ConfigValue;
import it.feargames.commonutilities.module.Module;
import it.feargames.commonutilities.service.PluginService;
import it.feargames.commonutilities.service.ProtocolServiceWrapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class VoidTeleport implements Module {

    private PluginService service;

    @ConfigValue
    private final Boolean enabled = false;
    @ConfigValue
    private final Long taskPeriod = 5L;
    @ConfigValue
    private final Map<String, Double> worlds = ImmutableMap.of("world", 0.0);
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

    private BukkitTask task = null;

    @Override
    public void onLoad(String name, PluginService service, ProtocolServiceWrapper protocol) {
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
