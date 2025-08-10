package it.novaverse.commonutilities.module.implementation.gameplay.world;

import com.google.common.collect.ImmutableMap;
import it.novaverse.commonutilities.annotation.ConfigValue;
import it.novaverse.commonutilities.annotation.RegisterListeners;
import it.novaverse.commonutilities.module.Module;
import it.novaverse.commonutilities.service.PluginService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@RegisterListeners
public class LockTime implements Module, Listener {

    private PluginService service;

    @ConfigValue
    private Boolean enabled = false;
    @ConfigValue
    private Map<String, Integer> worlds = ImmutableMap.of("world", 0);

    @Override
    public void onLoad(String name, PluginService service) {
        this.service = service;
    }

    @Override
    public void onEnable() {
        service.getWorlds().forEach(this::lockTime);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldLoad(WorldLoadEvent event) {
        lockTime(event.getWorld());
    }

    private void lockTime(World world) {
        Integer ticks = worlds.get(world.getName());
        if (ticks == null) {
            return;
        }
        world.setTime(ticks);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
    }

}
