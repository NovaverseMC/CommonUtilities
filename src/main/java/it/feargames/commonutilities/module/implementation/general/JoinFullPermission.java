package it.feargames.commonutilities.module.implementation.general;

import it.feargames.commonutilities.annotation.ConfigValue;
import it.feargames.commonutilities.annotation.RegisterListeners;
import it.feargames.commonutilities.module.Module;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@RegisterListeners
public class JoinFullPermission implements Module, Listener {

    @ConfigValue
    private Boolean enabled = true;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getResult() == PlayerLoginEvent.Result.KICK_FULL) {
            if (event.getPlayer().hasPermission("common.joinfull")) {
                event.setResult(PlayerLoginEvent.Result.ALLOWED);
            }
        }
    }

}
