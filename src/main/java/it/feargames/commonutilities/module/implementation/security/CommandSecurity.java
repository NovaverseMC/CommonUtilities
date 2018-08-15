package it.feargames.commonutilities.module.implementation.security;

import com.google.common.collect.Lists;
import it.feargames.commonutilities.annotation.ConfigValue;
import it.feargames.commonutilities.annotation.RegisterListeners;
import it.feargames.commonutilities.module.Module;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

import static org.bukkit.ChatColor.translateAlternateColorCodes;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@RegisterListeners
public class CommandSecurity implements Module, Listener {

    @ConfigValue
    private Boolean enabled = true;
    @ConfigValue
    private Boolean preventHiddenSyntax = true;
    @ConfigValue
    private String hiddenSyntaxMessage = "&fUnknown command.";
    @ConfigValue
    private List<String> commandBlacklist = Lists.newArrayList("pl", "plugins", "ver", "version", "about", "?", "me", "kill", "plugman", "efly", "esethome", "etpa", "etpahere", "etpyes", "etpaccept", "etpno", "etpdenz", "etpdeny", "eheal", "ekit", "eback", "erepair", "ewarp");
    @ConfigValue
    private String blacklistMessage = "&cYou don't have the permission to perform this command!";

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();

        if (player.hasPermission("common.command.bypass")) {
            return;
        }

        String message = event.getMessage();
        String[] components = message.split(" ");
        String label = components[0];

        if (preventHiddenSyntax) {
            if (label.contains(":")) {
                event.setCancelled(true);
                player.sendMessage(translateAlternateColorCodes('&', hiddenSyntaxMessage));
                return;
            }
        }

        for (String currentCommand : commandBlacklist) {
            if (label.equalsIgnoreCase("/" + currentCommand)) {
                player.sendMessage(translateAlternateColorCodes('&', blacklistMessage));
                event.setCancelled(true);
                return;
            }
        }
    }

    /* Useless in 1.13
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onTabComplete(TabCompleteEvent event) {
        if (!(event.getSender() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getSender();

        if (player.hasPermission("common.command.bypass")) {
            return;
        }

        String message = event.getBuffer();
        String[] components = message.split(" ");
        String label = components[0];

        if (preventHiddenSyntax) {
            if (label.contains(":")) {
                event.setCancelled(true);
                return;
            }
        }

        for (String currentCommand : commandBlacklist) {
            if (label.equalsIgnoreCase("/" + currentCommand)) {
                event.setCancelled(true);
                return;
            }
        }
    }
    */
}
