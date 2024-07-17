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
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@RegisterListeners
public class LockWeather implements Module, Listener {

    private final static int LOCK_DURATION = 20 * 60;

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
        service.getWorlds().forEach(this::lockWeather);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldLoad(WorldLoadEvent event) {
        lockWeather(event.getWorld());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(true);
    }

    private void lockWeather(World world) {
        Integer mode = worlds.get(world.getName());
        if (mode == null) {
            return;
        }

        world.setWeatherDuration(mode > 0 ? LOCK_DURATION : 0);
        world.setThundering(mode > 1);
        world.setThunderDuration(mode > 1 ? LOCK_DURATION : 0);

        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
    }

}
