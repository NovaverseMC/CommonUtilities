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

    private PluginService service;
    private ProtocolServiceWrapper protocol;

    @ConfigValue
    private Boolean enabled = true;

    @Override
    public void onLoad(String name, PluginService service, ProtocolServiceWrapper protocol) {
        this.service = service;
        this.protocol = protocol;
    }

    @Override
    public void onEnable() {
        protocol.handle(protocol -> {
            // Protocol docs: http://wiki.vg/Protocol#Teams
            protocol.addSendingListener(LISTENER_ID, ListenerPriority.HIGHEST, PacketType.Play.Server.SCOREBOARD_TEAM, event -> {
                WrapperPlayServerScoreboardTeam wrapper = new WrapperPlayServerScoreboardTeam(event.getPacket());
                if (wrapper.getMode() != WrapperPlayServerScoreboardTeam.Mode.TEAM_CREATED
                        && wrapper.getMode() != WrapperPlayServerScoreboardTeam.Mode.TEAM_UPDATED) {
                    return;
                }
                wrapper.setCollisionRule("never");
            });
        });
        // Can't do that as it breaks scoreboard plugins animations
        //service.getPlayers().forEach(this::sendTeamPacket);
    }

    @Override
    public void onDisable() {
        protocol.handle(protocol -> protocol.removePacketListener(LISTENER_ID));
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // We need to do this at LOWEST priority, so we won't break scoreboard animations
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        sendTeamPacket(event.getPlayer());
    }

    private void sendTeamPacket(final Player player) {
        // Protocol docs: http://wiki.vg/Protocol#Teams
        protocol.handle(protocol -> {
            WrapperPlayServerScoreboardTeam wrapper = new WrapperPlayServerScoreboardTeam();
            wrapper.setName(RandomStringUtils.random(16, true, true));
            wrapper.setMode(WrapperPlayServerScoreboardTeam.Mode.TEAM_CREATED);
            wrapper.setCollisionRule("never");
            wrapper.setPlayers(Collections.singletonList(player.getName()));
            wrapper.sendPacket(player);
        });
    }
}
