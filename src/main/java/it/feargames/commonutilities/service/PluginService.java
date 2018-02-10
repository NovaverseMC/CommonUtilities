package it.feargames.commonutilities.service;

import it.feargames.commonutilities.CommonUtilities;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class PluginService {

    @NonNull
    private final CommonUtilities plugin;

    public void broadcast(String message, String permission) {
        plugin.getServer().broadcast(message, permission);
    }

    public void registerListener(@NonNull Listener listener) {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    public <T> RegisteredServiceProvider<T> getService(Class<T> service) {
        return plugin.getServer().getServicesManager().getRegistration(service);
    }

    public void unregisterListener(@NonNull Listener listener) {
        HandlerList.unregisterAll(listener);
    }

    public World getWorld(String name) {
        return plugin.getServer().getWorld(name);
    }

    public List<World> getWorlds() {
        return plugin.getServer().getWorlds();
    }

    public Collection<? extends Player> getPlayers() {
        return plugin.getServer().getOnlinePlayers();
    }

    public boolean isPluginEnabled(String name) {
        return plugin.getServer().getPluginManager().isPluginEnabled(name);
    }

    public BukkitTask schedule(@NonNull Runnable runnable) {
        return plugin.getServer().getScheduler().runTask(plugin, runnable);
    }

    public BukkitTask async(@NonNull Runnable runnable) {
        return plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    public BukkitTask delayed(@NonNull Runnable runnable, long delay) {
        return plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delay);
    }

    public BukkitTask asyncDelayed(@NonNull Runnable runnable, long delay) {
        return plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
    }

    public BukkitTask timer(@NonNull Runnable runnable, long delay, long period) {
        return plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, period);
    }

    public BukkitTask timer(@NonNull Runnable runnable, long period) {
        return timer(runnable, 0L, period);
    }

    public BukkitTask asyncTimer(@NonNull Runnable runnable, long delay, long period) {
        return plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period);
    }

    public BukkitTask asyncTimer(@NonNull Runnable runnable, long period) {
        return asyncTimer(runnable, 0L, period);
    }

}
