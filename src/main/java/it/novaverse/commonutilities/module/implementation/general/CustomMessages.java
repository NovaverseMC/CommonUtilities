package it.novaverse.commonutilities.module.implementation.general;

import it.novaverse.commonutilities.annotation.ConfigValue;
import it.novaverse.commonutilities.annotation.RegisterListeners;
import it.novaverse.commonutilities.module.Module;
import it.novaverse.commonutilities.service.PluginService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@RegisterListeners
public class CustomMessages implements Module, Listener {

    @ConfigValue
    private Boolean enabled = false;
    @ConfigValue
    private Boolean hideJoinMessages = false;
    @ConfigValue
    private String customJoinMessage = "";
    @ConfigValue
    private Boolean hideLeaveMessages = false;
    @ConfigValue
    private String customLeaveMessage = "";
    @ConfigValue
    private Boolean hideDeathMessages = false;
    @ConfigValue
    private String customDeathMessage = "";
    @ConfigValue
    private String customIdleKickMessage = "";


    private PluginService pluginService;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void onLoad(String name, PluginService service) {
        this.pluginService = service;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (hideJoinMessages) {
            event.joinMessage(null);
            return;
        }

        if (!customJoinMessage.isEmpty()) {
            String replacedMessageString = customJoinMessage.replace("%", event.getPlayer().getName());
            event.joinMessage(pluginService.transformComponent(replacedMessageString));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerQuitEvent event) {
        if (hideLeaveMessages) {
            event.quitMessage(null);
            return;
        }

        if (!customLeaveMessage.isEmpty()) {
            String replacedMessageString = customLeaveMessage.replace("%", event.getPlayer().getName());
            event.quitMessage(pluginService.transformComponent(replacedMessageString));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (hideDeathMessages) {
            event.deathMessage(null);
            return;
        }

        if (!customDeathMessage.isEmpty()) {
            String replacedMessageString = customDeathMessage.replace("%", event.getEntity().getName());
            event.deathMessage(pluginService.transformComponent(replacedMessageString));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerKick(PlayerKickEvent event) {
        if (hideLeaveMessages) event.leaveMessage(Component.empty());
        if (!event.reason().contains(Component.text("You have been idle for too long!"))) return;

        String replacedMessageString = customIdleKickMessage.replace("%", event.getPlayer().getName());
        event.reason(pluginService.transformComponent(replacedMessageString));
    }

}
