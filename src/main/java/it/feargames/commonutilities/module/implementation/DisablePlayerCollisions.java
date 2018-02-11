package it.feargames.commonutilities.module.implementation;

import com.comphenix.packetwrapper.WrapperPlayServerScoreboardTeam;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import it.feargames.commonutilities.annotation.ConfigValue;
import it.feargames.commonutilities.annotation.RegisterListeners;
import it.feargames.commonutilities.module.Module;
import it.feargames.commonutilities.service.PluginService;
import it.feargames.commonutilities.service.ProtocolServiceWrapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.Collections;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@RegisterListeners
public class DisablePlayerCollisions implements Module, Listener {

    private final static String LISTENER_ID = "DisablePlayerCollisions";

    private ProtocolServiceWrapper wrapper;

    @ConfigValue
    private Boolean enabled = true;

    @Override
    public void onLoad(String name, PluginService service, ProtocolServiceWrapper wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public void onEnable() {
        wrapper.getProtocolService().ifPresent(protocol -> {
            protocol.addSendingListener(LISTENER_ID, ListenerPriority.HIGHEST, PacketType.Play.Server.SCOREBOARD_TEAM, event -> {
                WrapperPlayServerScoreboardTeam wrapper = new WrapperPlayServerScoreboardTeam(event.getPacket());
                wrapper.setCollisionRule("never");
                event.setPacket(wrapper.getHandle());
            });
        });
    }

    @Override
    public void onDisable() {

    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onItemSwap(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        WrapperPlayServerScoreboardTeam wrapper = new WrapperPlayServerScoreboardTeam();
        wrapper.setMode(WrapperPlayServerScoreboardTeam.Mode.TEAM_CREATED);
        wrapper.setName(UUID.randomUUID().toString().substring(0, 15));
        wrapper.setPlayers(Collections.singletonList(player.getName()));
        wrapper.setCollisionRule("never");
        wrapper.sendPacket(player);
    }

}
