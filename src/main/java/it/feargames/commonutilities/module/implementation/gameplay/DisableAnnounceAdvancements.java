package it.feargames.commonutilities.module.implementation.gameplay;

import it.feargames.commonutilities.annotation.ConfigValue;
import it.feargames.commonutilities.annotation.RegisterListeners;
import it.feargames.commonutilities.module.Module;
import it.feargames.commonutilities.service.PluginService;
import it.feargames.commonutilities.service.ProtocolServiceWrapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@RegisterListeners
public class DisableAnnounceAdvancements implements Module, Listener {

    private PluginService service;

    @ConfigValue
    private Boolean enabled = true;

    @Override
    public void onLoad(String name, PluginService service, ProtocolServiceWrapper protocol) {
        this.service = service;
    }

    @Override
    public void onEnable() {
        for (World world : service.getWorlds()) {
            disableAnnounceAdvancements(world);
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldLoad(WorldLoadEvent event) {
        disableAnnounceAdvancements(event.getWorld());
    }

    private void disableAnnounceAdvancements(World world) {
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
    }

}
