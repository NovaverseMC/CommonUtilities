package it.novaverse.commonutilities.module.implementation.gameplay;

import it.novaverse.commonutilities.annotation.ConfigValue;
import it.novaverse.commonutilities.annotation.RegisterListeners;
import it.novaverse.commonutilities.module.Module;
import it.novaverse.commonutilities.service.PluginService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.CampfireRecipe;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@RegisterListeners
public class DisableCrafting implements Module, Listener {

    private PluginService service;

    @ConfigValue(comment = "Enable or disable this module")
    private Boolean enabled = false;

    @ConfigValue(comment = "Blacklist of materials that cannot be crafted/processed. Keep empty to blacklist nothing.")
    private List<String> blacklist = new ArrayList<>();

    @ConfigValue(comment = "Whitelist of materials that are allowed to be crafted/processed. Keep empty to allow everything.")
    private List<String> whitelist = new ArrayList<>();

    @ConfigValue(comment = "Whether to block all crafting/processing. Whitelist still acts as override if not empty.")
    private Boolean blockAll = false;

    @ConfigValue(comment = "List of worlds where crafting is disabled (blocked). If empty, crafting is blocked in all worlds.")
    private List<String> disabledWorlds = new ArrayList<>();

    @ConfigValue(comment = "Message sent to player when their craft/process is blocked. Keep empty for no message.")
    private String message = "<red>You are not allowed to craft or process this item!";

    @Override
    public void onLoad(String name, PluginService service) {
        this.service = service;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    private boolean isWorldDisabled(org.bukkit.World world) {
        if (world == null) {
            return true;
        }
        if (disabledWorlds.isEmpty()) {
            return true;
        }
        for (var currentWorld : disabledWorlds) {
            if (currentWorld.equalsIgnoreCase(world.getName())) {
                return true;
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getSlotType() != InventoryType.SlotType.RESULT) {
            return;
        }

        var player = (Player) event.getWhoClicked();
        if (!isWorldDisabled(player.getWorld())) {
            return;
        }

        var invType = event.getInventory().getType();

        // Skip villager trading
        if (invType == InventoryType.MERCHANT) {
            return;
        }

        var item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        var material = item.getType();
        var matName = material.name();

        // Check general bypass permission or material-specific bypass permission
        if (player.hasPermission("common.bypass.craft")
                || player.hasPermission("common.bypass.craft." + matName.toLowerCase())) {
            return;
        }

        var blocked = isMaterialBlocked(matName);

        if (blocked) {
            event.setCancelled(true);
            if (!message.isEmpty()) {
                service.sendMessage(player, message);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getViewers().isEmpty() || !isWorldDisabled(event.getViewers().get(0).getWorld())) {
            return;
        }

        var result = event.getInventory().getResult();
        if (result == null || result.getType() == Material.AIR) {
            return;
        }

        if (shouldBlock(result.getType(), event.getViewers())) {
            event.getInventory().setResult(new ItemStack(Material.AIR));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepareResult(PrepareResultEvent event) {
        if (event.getViewers().isEmpty() || !isWorldDisabled(event.getViewers().get(0).getWorld())) {
            return;
        }

        var result = event.getResult();
        if (result == null || result.getType() == Material.AIR) {
            return;
        }

        if (shouldBlock(result.getType(), event.getViewers())) {
            event.setResult(new ItemStack(Material.AIR));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        if (!isWorldDisabled(event.getBlock().getWorld())) {
            return;
        }

        if (event.getBlock().getState() instanceof org.bukkit.block.Furnace furnace) {
            var smelting = furnace.getInventory().getSmelting();
            if (isSmeltingResultBlocked(smelting)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockCook(BlockCookEvent event) {
        if (!isWorldDisabled(event.getBlock().getWorld())) {
            return;
        }

        var result = event.getResult();
        if (result == null || result.getType() == Material.AIR) {
            return;
        }

        if (isMaterialBlocked(result.getType().name())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBrew(BrewEvent event) {
        if (!isWorldDisabled(event.getBlock().getWorld())) {
            return;
        }

        for (var result : event.getResults()) {
            if (result == null || result.getType() == Material.AIR) {
                continue;
            }
            if (isMaterialBlocked(result.getType().name())) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCrafterCraft(CrafterCraftEvent event) {
        if (!isWorldDisabled(event.getBlock().getWorld())) {
            return;
        }

        var result = event.getResult();
        if (result == null || result.getType() == Material.AIR) {
            return;
        }

        if (isMaterialBlocked(result.getType().name())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRecipeDiscover(PlayerRecipeDiscoverEvent event) {
        var player = event.getPlayer();
        if (!isWorldDisabled(player.getWorld())) {
            return;
        }

        var recipe = Bukkit.getRecipe(event.getRecipe());
        if (recipe == null) {
            return;
        }

        var result = recipe.getResult();
        if (result == null || result.getType() == Material.AIR) {
            return;
        }

        var matName = result.getType().name();
        if (player.hasPermission("common.bypass.craft")
                || player.hasPermission("common.bypass.craft." + matName.toLowerCase())) {
            return;
        }

        if (isMaterialBlocked(matName)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        var block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        if (block.getType() != Material.CAMPFIRE && block.getType() != Material.SOUL_CAMPFIRE) {
            return;
        }

        var player = event.getPlayer();
        if (!isWorldDisabled(player.getWorld())) {
            return;
        }

        var item = event.getItem();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        for (var recipe : Bukkit.getRecipesFor(item)) {
            if (recipe instanceof CampfireRecipe campfireRecipe) {
                var resultName = campfireRecipe.getResult().getType().name();
                if (player.hasPermission("common.bypass.craft")
                        || player.hasPermission("common.bypass.craft." + resultName.toLowerCase())) {
                    continue;
                }
                if (isMaterialBlocked(resultName)) {
                    event.setCancelled(true);
                    if (!message.isEmpty()) {
                        service.sendMessage(player, message);
                    }
                    return;
                }
            }
        }
    }

    private boolean isSmeltingResultBlocked(ItemStack source) {
        if (source == null || source.getType() == Material.AIR) {
            return false;
        }
        for (var recipe : Bukkit.getRecipesFor(source)) {
            if (recipe instanceof CookingRecipe<?> cookingRecipe) {
                if (isMaterialBlocked(cookingRecipe.getResult().getType().name())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean shouldBlock(Material material, List<HumanEntity> viewers) {
        var matName = material.name();
        var blocked = isMaterialBlocked(matName);

        if (blocked) {
            // If at least one viewer has bypass permission, don't block
            for (var viewer : viewers) {
                if (viewer instanceof Player player) {
                    if (player.hasPermission("common.bypass.craft")
                            || player.hasPermission("common.bypass.craft." + matName.toLowerCase())) {
                        return false;
                    }
                }
            }
            return true;
        }

        return false;
    }

    private boolean isMaterialBlocked(String matName) {
        var blocked = false;

        if (blockAll) {
            blocked = true;
        }

        if (!blacklist.isEmpty() && containsIgnoreCase(blacklist, matName)) {
            blocked = true;
        }

        if (!whitelist.isEmpty()) {
            if (containsIgnoreCase(whitelist, matName)) {
                blocked = false;
            } else {
                blocked = true;
            }
        }

        return blocked;
    }

    private boolean containsIgnoreCase(List<String> list, String val) {
        for (var s : list) {
            if (s.equalsIgnoreCase(val)) {
                return true;
            }
        }
        return false;
    }
}
