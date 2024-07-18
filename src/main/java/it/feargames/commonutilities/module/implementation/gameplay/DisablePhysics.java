package it.feargames.commonutilities.module.implementation.gameplay;

import it.feargames.commonutilities.annotation.ConfigValue;
import it.feargames.commonutilities.annotation.RegisterListeners;
import it.feargames.commonutilities.module.Module;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@RegisterListeners
public class DisablePhysics implements Module, Listener {

    @ConfigValue
    private Boolean enabled = false;
    @ConfigValue
    private List<String> worlds = new ArrayList<>();
    @ConfigValue
    private Boolean whitelist = true;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (worlds.contains(event.getBlock().getWorld().getName()) != whitelist) {
            return;
        }
        event.setCancelled(true);
    }
}
