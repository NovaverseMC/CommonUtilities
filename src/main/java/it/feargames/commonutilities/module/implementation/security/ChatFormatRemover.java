package it.feargames.commonutilities.module.implementation.security;

import it.feargames.commonutilities.annotation.ConfigValue;
import it.feargames.commonutilities.annotation.RegisterListeners;
import it.feargames.commonutilities.module.Module;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@RegisterListeners
public class ChatFormatRemover implements Module, Listener {

    @ConfigValue
    private final Boolean enabled = false;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.getPlayer().hasPermission("bskyblockfix.chatformat")) {
            return;
        }
        event.setMessage(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', event.getMessage())));
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
