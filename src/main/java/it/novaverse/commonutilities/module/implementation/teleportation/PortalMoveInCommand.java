package it.novaverse.commonutilities.module.implementation.teleportation;

import com.google.common.collect.Maps;
import it.novaverse.commonutilities.annotation.ConfigValue;
import it.novaverse.commonutilities.annotation.RegisterListeners;
import it.novaverse.commonutilities.hook.PlaceholderHook;
import it.novaverse.commonutilities.module.Module;
import it.novaverse.commonutilities.service.PluginService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Map;

@RegisterListeners
public class PortalMoveInCommand implements Module, Listener {
    @ConfigValue(comment = "Enable or disable this module")
    private Boolean enabled = false;

    @ConfigValue(comment = "Map of portal block types to commands executed. Key is portal type (e.g. NETHER_PORTAL), value is command.", type = ConfigValue.ValueType.STRING_MAP)
    private Map<String, String> portalCommand = Maps.newHashMap();

    @ConfigValue(comment = "If true, the player is pushed out of the portal in the direction opposite to the one they came from.")
    private Boolean knockbackOnEnter = false;

    @ConfigValue(comment = "Horizontal strength of the knockback out of the portal.")
    private Double knockbackStrength = 0.8;

    @ConfigValue(comment = "Vertical strength of the knockback out of the portal.")
    private Double knockbackVerticalStrength = 0.3;

    @ConfigValue(comment = "Delay in ticks before the command is executed. Use 0 to run it immediately.")
    private Integer commandDelayTicks = 10;

    private PluginService service;

    @Override
    public void onLoad(String name, PluginService service) {
        this.service = service;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!hasWalkedFullBlock(event.getFrom(), event.getTo())) {
            return;
        }

        var block = event.getTo().getBlock();
        var material = block.getType();
        boolean portalBlock = material.name().toLowerCase().contains("portal");

        if (!portalBlock) {
            return;
        }

        var command = portalCommand.get(material.name().toLowerCase());
        if (command == null) {
            return;
        }

        var player = event.getPlayer();

        if (knockbackOnEnter) {
            knockBack(event.getFrom(), event.getTo(), player);
        }

        var patchedCommand = command.replace("%player%", player.getName());
        if (commandDelayTicks <= 0) {
            dispatch(player, patchedCommand);
        } else {
            service.delayed(() -> {
                if (!player.isOnline()) {
                    return;
                }
                dispatch(player, patchedCommand);
            }, commandDelayTicks);
        }
    }

    private void dispatch(Player player, String command) {
        // Resolve the placeholders against the player entering the portal, if PlaceholderAPI is available
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            command = PlaceholderHook.setPlaceholders(player, command);
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    private void knockBack(Location from, Location to, Player player) {
        // Direction opposite to the one the player entered the portal with
        var direction = from.toVector().subtract(to.toVector());
        direction.setY(0);
        if (direction.lengthSquared() == 0) {
            // Fall back to the opposite of the facing direction
            direction = player.getLocation().getDirection().setY(0).multiply(-1);
            if (direction.lengthSquared() == 0) {
                return;
            }
        }
        var velocity = direction.normalize().multiply(knockbackStrength);
        velocity.setY(knockbackVerticalStrength);
        player.setVelocity(velocity);
    }

    public boolean hasWalkedFullBlock(Location from, Location to) {
        return from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ();
    }

}
