package it.novaverse.commonutilities.module.implementation.security;

import com.google.common.collect.Lists;
import it.novaverse.commonutilities.annotation.ConfigValue;
import it.novaverse.commonutilities.annotation.RegisterListeners;
import it.novaverse.commonutilities.module.Module;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@RegisterListeners
public class CommandSecurity implements Module, Listener {

    @ConfigValue
    private Boolean enabled = true;
    @ConfigValue
    private Boolean preventHiddenSyntax = true;
    @ConfigValue
    private String hiddenSyntaxMessage = "<white>Unknown command.";
    @ConfigValue
    private List<String> commandBlacklist = Lists.newArrayList("pl",
            "plugins",
            "ver",
            "version",
            "about",
            "?",
            "me",
            "kill",
            "plugman"
    );
    @ConfigValue
    private String blacklistMessage = "<red>You don't have the permission to perform this command!";

    private Set<String> hashBlacklist;

    @Override
    public void onEnable() {
        hashBlacklist = new LinkedHashSet<>(commandBlacklist);
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.updateCommands();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommandSend(PlayerCommandSendEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("common.command.bypass")) {
            return;
        }
        event.getCommands().removeIf(command -> (preventHiddenSyntax && command.contains(":")) || hashBlacklist.contains(command));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("common.command.bypass")) {
            return;
        }

        String message = event.getMessage();
        String[] components = message.split(" ");
        String label = components[0];

        if (preventHiddenSyntax) {
            if (label.contains(":")) {
                event.setCancelled(true);
                player.sendMessage(MiniMessage.miniMessage().deserialize(hiddenSyntaxMessage));
                return;
            }
        }

        for (String currentCommand : commandBlacklist) {
            if (label.equalsIgnoreCase("/" + currentCommand)) {
                player.sendMessage(MiniMessage.miniMessage().deserialize(blacklistMessage));
                event.setCancelled(true);
                return;
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
