package it.novaverse.commonutilities.module.implementation.security;

import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTabComplete;
import com.google.common.collect.Lists;
import it.novaverse.commonutilities.annotation.ConfigValue;
import it.novaverse.commonutilities.annotation.RegisterListeners;
import it.novaverse.commonutilities.module.Module;
import it.novaverse.commonutilities.service.PluginService;
import it.novaverse.commonutilities.service.ProtocolServiceWrapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

import static org.bukkit.ChatColor.translateAlternateColorCodes;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@RegisterListeners
public class CommandSecurity implements Module, Listener {

    private final static String LISTENER_ID = "FixHideTabLegacy";

    @ConfigValue
    private Boolean enabled = true;
    @ConfigValue
    private Boolean preventHiddenSyntax = true;
    @ConfigValue
    private Boolean preventEmptyTab = true;
    @ConfigValue
    private String hiddenSyntaxMessage = "&fUnknown command.";
    @ConfigValue
    private List<String> commandBlacklist = Lists.newArrayList("pl",
            "plugins",
            "ver",
            "version",
            "about",
            "?",
            "me",
            "kill",
            "plugman",
            "efly",
            "esethome",
            "etpa",
            "etpahere",
            "etpyes",
            "etpaccept",
            "etpno",
            "etpdenz",
            "etpdeny",
            "eheal",
            "ekit",
            "eback",
            "erepair",
            "ewarp"
    );
    @ConfigValue
    private String blacklistMessage = "&cYou don't have the permission to perform this command!";

    private ProtocolServiceWrapper protocol;

    @Override
    public void onLoad(String name, PluginService service, ProtocolServiceWrapper protocol) {
        this.protocol = protocol;
    }

    @Override
    public void onEnable() {
        // Tab listener (legacy clients, while using ProtocolSupport/ViaBackwars/ViaRewind)
        protocol.handle(protocol -> {
            protocol.addReceivingListener(LISTENER_ID,
                    PacketListenerPriority.HIGHEST,
                    PacketType.Play.Client.TAB_COMPLETE,
                    event -> {
                        Player player = (Player) event.getPlayer();
                        if (player.hasPermission("common.command.bypass")) return;
                        WrapperPlayClientTabComplete wrapper = new WrapperPlayClientTabComplete(event);
                        String message = wrapper.getText();

                        if (preventEmptyTab && message.isEmpty()) {
                            event.setCancelled(true);
                            return;
                        }

                        String[] components = message.split(" ");
                        String label = components[0];

                        if (preventHiddenSyntax && label.contains(":")) {
                            event.setCancelled(true);
                            return;
                        }

                        for (String currentCommand : commandBlacklist) {
                            if (label.equalsIgnoreCase(currentCommand)) {
                                event.setCancelled(true);
                                return;
                            }
                        }

                    }
            );
        });
    }

    @Override
    public void onDisable() {
        protocol.handle(protocol -> protocol.removePacketListener(LISTENER_ID));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();

        if (player.hasPermission("common.command.bypass")) {
            return;
        }

        String message = event.getMessage();
        String[] components = message.split(" ");
        String label = components[0];

        if (preventHiddenSyntax) {
            if (label.contains(":")) {
                event.setCancelled(true);
                player.sendMessage(translateAlternateColorCodes('&', hiddenSyntaxMessage));
                return;
            }
        }

        for (String currentCommand : commandBlacklist) {
            if (label.equalsIgnoreCase("/" + currentCommand)) {
                player.sendMessage(translateAlternateColorCodes('&', blacklistMessage));
                event.setCancelled(true);
                return;
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
