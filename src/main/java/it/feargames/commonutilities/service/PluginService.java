package it.feargames.commonutilities.service;

import it.feargames.commonutilities.CommonUtilities;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.messaging.PluginMessageListener;
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

    public void dispatchCommand(CommandSender sender, String command) {
        plugin.getServer().dispatchCommand(sender, command);
    }

    public void dispatchCommand(String command) {
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
    }

    public <T> RegisteredServiceProvider<T> getService(Class<T> service) {
        return plugin.getServer().getServicesManager().getRegistration(service);
    }

    public void registerIncomingPluginChannel(String channel, PluginMessageListener listener) {
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, channel, listener);
    }

    public void registerOutgoingPluginChannel(String channel) {
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, channel);
    }

    public void unregisterIncomingPluginChannel(String channel) {
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, channel);
    }

    public void unregisterOutgoingPluginChannel(String channel) {
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, channel);
    }

    public void registerListener(@NonNull Listener listener) {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
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

    public void showPlayer(Player player, Player other) {
        player.showPlayer(other);
    }

    public void hidePlayer(Player player, Player other) {
        player.hidePlayer(other);
    }

}
