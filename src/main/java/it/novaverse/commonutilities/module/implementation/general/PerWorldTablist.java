package it.novaverse.commonutilities.module.implementation.general;

import com.google.common.collect.ImmutableList;
import it.novaverse.commonutilities.annotation.ConfigValue;
import it.novaverse.commonutilities.annotation.RegisterListeners;
import it.novaverse.commonutilities.module.Module;
import it.novaverse.commonutilities.service.PluginService;
import it.novaverse.commonutilities.service.ProtocolServiceWrapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@RegisterListeners
public class PerWorldTablist implements Module, Listener {

    private PluginService service;

    @ConfigValue
    private Boolean enabled = false;
    @ConfigValue
    private List<String> globalWorlds = ImmutableList.of("Spawn");

    @Override
    public void onLoad(String name, PluginService service, ProtocolServiceWrapper protocol) {
        this.service = service;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(final PlayerJoinEvent event) {
        refresh(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldChange(final PlayerChangedWorldEvent event) {
        refresh(event.getPlayer());
    }

    private boolean isGlobalWorld(final World world) {
        return globalWorlds.contains(world.getName());
    }

    private void refresh(final Player player) {
        final World world = player.getWorld();
        final boolean global = isGlobalWorld(world);
        for (final Player current : service.getPlayers()) {
            if (global) {
                // Show if the player is in a global world
                service.showPlayer(player, current);
                // Hide to players not in global worlds
                if (!isGlobalWorld(current.getWorld())) {
                    service.hidePlayer(current, player);
                }
            } else if (current.getWorld().equals(world)) {
                // Show if same world
                service.showPlayer(player, current);
                service.showPlayer(current, player);
            } else {
                // Hide players in other worlds
                service.hidePlayer(player, current);
                // Don't make the player invisible to players in global worlds
                if (!isGlobalWorld(current.getWorld())) {
                    service.hidePlayer(current, player);
                }
            }
        }
    }

}
