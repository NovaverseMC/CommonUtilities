package it.novaverse.commonutilities.module.implementation.teleportation;

import it.novaverse.commonutilities.annotation.ConfigValue;
import it.novaverse.commonutilities.annotation.RegisterListeners;
import it.novaverse.commonutilities.module.Module;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@RegisterListeners
public class WorldProtection implements Module, Listener {
    private static final int MAX_FOOD_LEVEL = 20;
    private static final Function<Player, Double> MAX_HEALTH = player -> player.getAttribute(Attribute.MAX_HEALTH)
            .getBaseValue();

    @ConfigValue(comment = "Enable or disable this module")
    private Boolean enabled = false;

    @ConfigValue(comment = "If true, players cannot place blocks in protected worlds")
    private Boolean blockBlockPlace = true;

    @ConfigValue(comment = "If true, players cannot break blocks in protected worlds")
    private Boolean blockBlockBreak = true;

    @ConfigValue(comment = "If true, players will not take damage in protected worlds")
    private Boolean alwaysGod = true;

    @ConfigValue(comment = "List of worlds where protection is active")
    private List<String> protectedWorlds = new ArrayList<>();

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        var world = event.getBlock().getWorld();
        if (!protectedWorlds.contains(world.getName().toLowerCase())) {
            return;
        }

        if (blockBlockPlace && !event.getPlayer().hasPermission("common.protection.build.bypass")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        var world = event.getBlock().getWorld();
        if (!protectedWorlds.contains(world.getName().toLowerCase())) {
            return;
        }

        if (blockBlockBreak && !event.getPlayer().hasPermission("common.protection.build.bypass")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        var world = event.getEntity().getWorld();
        if (!protectedWorlds.contains(world.getName().toLowerCase())) {
            return;
        }

        if (alwaysGod && !event.getEntity().hasPermission("common.protection.god.bypass")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        var world = event.getEntity().getWorld();
        if (!protectedWorlds.contains(world.getName().toLowerCase())) {
            return;
        }

        if (event.getEntity() instanceof Player player && alwaysGod
                && !player.hasPermission("common.protection.god.bypass")) {
            player.setFoodLevel(MAX_FOOD_LEVEL);
            player.setHealth(MAX_HEALTH.apply(player));
            event.setCancelled(true);
        }
    }

}
