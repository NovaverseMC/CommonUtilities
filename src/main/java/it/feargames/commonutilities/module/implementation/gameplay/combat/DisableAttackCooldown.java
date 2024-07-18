package it.feargames.commonutilities.module.implementation.gameplay.combat;

import it.feargames.commonutilities.annotation.ConfigValue;
import it.feargames.commonutilities.annotation.RegisterListeners;
import it.feargames.commonutilities.module.Module;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@RegisterListeners
public class DisableAttackCooldown implements Module, Listener {

    @ConfigValue
    private Boolean enabled = false;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(32.0D);
    }

}
