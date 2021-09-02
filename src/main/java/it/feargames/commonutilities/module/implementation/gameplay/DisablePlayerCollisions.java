package it.feargames.commonutilities.module.implementation.gameplay;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.InternalStructure;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketContainer;
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

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
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
                PacketContainer packet = event.getPacket();
                int mode = packet.getIntegers().read(0);

                // Created or updated
                if(mode != 0 && mode != 2) {
                    return;
                }

                InternalStructure structure = packet.getOptionalStructures().read(0)
                        .orElseThrow(() -> new RuntimeException("Invalid packet!"));

                structure.getStrings().write(1, "never"); // setCollisionRule
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
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
            packet.getModifier().writeDefaults();

            packet.getIntegers().write(0, 1); // Create team
            packet.getStrings().write(0, RandomStringUtils.random(16, true, true)); // Name

            InternalStructure structure = packet.getOptionalStructures().read(0)
                    .orElseThrow(() -> new RuntimeException("Invalid packet!"));

            structure.getStrings().write(1, "never"); // setCollisionRule
            structure.getSpecificModifier(Collection.class).write(0, Collections.singletonList(player.getName())); // setPlayers

            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }
}
