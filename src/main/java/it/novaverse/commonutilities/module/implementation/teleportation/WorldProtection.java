package it.novaverse.commonutilities.module.implementation.teleportation;

import com.google.common.collect.Lists;
import it.novaverse.commonutilities.annotation.ConfigValue;
import it.novaverse.commonutilities.annotation.RegisterListeners;
import it.novaverse.commonutilities.module.Module;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import java.util.List;
import java.util.function.Function;

@RegisterListeners
public class WorldProtection implements Module, Listener {
    private static final int MAX_FOOD_LEVEL = 20;
    private static final Function<Player, Double> MAX_HEALTH = player -> player.getAttribute(Attribute.GENERIC_MAX_HEALTH)
            .getBaseValue();

    @ConfigValue
    private Boolean enabled = false;

    @ConfigValue
    private Boolean blockBlockPlace = true;

    @ConfigValue
    private Boolean blockBlockBreak = true;

    @ConfigValue
    private Boolean alwaysGod = true;

    @ConfigValue
    private List<String> protectedWorlds = Lists.newArrayList();

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        World world = event.getBlock().getWorld();
        if (!protectedWorlds.contains(world.getName().toLowerCase())) return;

        if (blockBlockPlace && !event.getPlayer().hasPermission("common.hub.build.bypass")) event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        World world = event.getBlock().getWorld();
        if (!protectedWorlds.contains(world.getName().toLowerCase())) return;

        if (blockBlockBreak && !event.getPlayer().hasPermission("common.hub.build.bypass")) event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        World world = event.getEntity().getWorld();
        if (!protectedWorlds.contains(world.getName().toLowerCase())) return;

        if (alwaysGod && !event.getEntity().hasPermission("common.hub.god.bypass")) event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        World world = event.getEntity().getWorld();
        if (!protectedWorlds.contains(world.getName().toLowerCase())) return;

        if (event.getEntity() instanceof Player player && alwaysGod && !player.hasPermission("common.hub.god.bypass")) {
            player.setFoodLevel(MAX_FOOD_LEVEL);
            player.setHealth(MAX_HEALTH.apply(player));
            event.setCancelled(true);
        }
    }

}
