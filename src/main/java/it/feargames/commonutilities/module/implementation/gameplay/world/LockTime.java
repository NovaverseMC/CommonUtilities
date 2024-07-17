package it.feargames.commonutilities.module.implementation.gameplay.world;

import com.google.common.collect.ImmutableMap;
import it.feargames.commonutilities.annotation.ConfigValue;
import it.feargames.commonutilities.annotation.RegisterListeners;
import it.feargames.commonutilities.module.Module;
import it.feargames.commonutilities.service.PluginService;
import it.feargames.commonutilities.service.ProtocolServiceWrapper;
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
    private final Boolean enabled = false;
    @ConfigValue
    private final Map<String, Integer> worlds = ImmutableMap.of("world", 0);

    @Override
    public void onLoad(String name, PluginService service, ProtocolServiceWrapper protocol) {
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
