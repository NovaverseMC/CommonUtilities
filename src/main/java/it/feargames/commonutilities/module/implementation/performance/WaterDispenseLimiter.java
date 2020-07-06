package it.feargames.commonutilities.module.implementation.performance;

import it.feargames.commonutilities.annotation.ConfigValue;
import it.feargames.commonutilities.annotation.RegisterListeners;
import it.feargames.commonutilities.module.Module;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@RegisterListeners
public class WaterDispenseLimiter implements Module, Listener {

    @ConfigValue
    private Boolean enabled = false;
    @ConfigValue
    private int fluidDispenseCooldown = 10000;

    private Map<Location, Long> lastDispenseMap;

    @Override
    public void onEnable() {
        lastDispenseMap = new HashMap<>();
    }

    @EventHandler
    public void onWaterDispenceLimiterCleanup(ChunkUnloadEvent event) {
        lastDispenseMap.keySet().removeIf(location -> location.getChunk().equals(event.getChunk()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onWaterDispenceLimiter(BlockDispenseEvent event) {
        if (event.getBlock().getType() != Material.DISPENSER) {
            return;
        }
        switch (event.getItem().getType()) {
            case BUCKET:
            case WATER_BUCKET:
            case LAVA_BUCKET:
                break;
            default:
                return;
        }
        Long lastDispense = lastDispenseMap.get(event.getBlock().getLocation());
        if (lastDispense == null || System.currentTimeMillis() - lastDispense > fluidDispenseCooldown) {
            lastDispenseMap.put(event.getBlock().getLocation(), System.currentTimeMillis());
            return;
        }
        event.setCancelled(true);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
