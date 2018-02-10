package it.feargames.commonutilities.module.implementation;

import it.feargames.commonutilities.annotation.ConfigValue;
import it.feargames.commonutilities.annotation.RegisterListeners;
import it.feargames.commonutilities.module.Module;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@RegisterListeners
public class DisableItemSwap implements Module, Listener {

    @ConfigValue
    private Boolean enabled = true;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onItemSwap(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);
    }

}
