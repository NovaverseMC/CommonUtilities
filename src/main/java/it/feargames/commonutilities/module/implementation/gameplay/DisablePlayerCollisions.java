package it.feargames.commonutilities.module.implementation.gameplay;

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
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Collections;

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
            // Protocol docs: http://wiki.vg/Protocol#Teams
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
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        // Protocol docs: http://wiki.vg/Protocol#Teams
        wrapper.getProtocolService().ifPresent(protocol -> {
            WrapperPlayServerScoreboardTeam wrapper = new WrapperPlayServerScoreboardTeam();
            wrapper.setName(RandomStringUtils.random(16, true, true));
            wrapper.setMode(WrapperPlayServerScoreboardTeam.Mode.TEAM_CREATED);
            wrapper.setCollisionRule("never");
            wrapper.setPlayers(Collections.singletonList(player.getName()));
        });
    }
}
