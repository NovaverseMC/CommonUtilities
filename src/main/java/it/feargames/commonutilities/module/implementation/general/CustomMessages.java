package it.feargames.commonutilities.module.implementation.general;

import it.feargames.commonutilities.annotation.ConfigValue;
import it.feargames.commonutilities.annotation.RegisterListeners;
import it.feargames.commonutilities.module.Module;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static org.bukkit.ChatColor.translateAlternateColorCodes;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@RegisterListeners
public class CustomMessages implements Module, Listener {

    @ConfigValue
    private final Boolean enabled = false;
    @ConfigValue
    private final Boolean hideJoinMessages = false;
    @ConfigValue
    private final String customJoinMessage = "";
    @ConfigValue
    private final Boolean hideLeaveMessages = false;
    @ConfigValue
    private final String customLeaveMessage = "";
    @ConfigValue
    private final Boolean hideDeathMessages = false;
    @ConfigValue
    private final String customDeathMessage = "";
    @ConfigValue
    private final String customIdleKickMessage = "";

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (hideJoinMessages) {
            event.setJoinMessage(null);
            return;
        }
        if (!customJoinMessage.isEmpty()) {
            event.setJoinMessage(translateAlternateColorCodes('&', customJoinMessage.replace("%", event.getPlayer().getName())));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerQuitEvent event) {
        if (hideLeaveMessages) {
            event.setQuitMessage(null);
            return;
        }
        if (!customLeaveMessage.isEmpty()) {
            event.setQuitMessage(translateAlternateColorCodes('&', customLeaveMessage.replace("%", event.getPlayer().getName())));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (hideDeathMessages) {
            event.setDeathMessage(null);
            return;
        }
        if (!customDeathMessage.isEmpty()) {
            event.setDeathMessage(translateAlternateColorCodes('&', customDeathMessage.replace("%", event.getEntity().getName())));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerKick(PlayerKickEvent event) {
        if (hideLeaveMessages) {
            event.setLeaveMessage("");
        }
        if (!event.getReason().contains("You have been idle for too long!")) {
            return;
        }
        event.setReason(translateAlternateColorCodes('&', customIdleKickMessage.replace("%", event.getPlayer().getName())));
    }

}
