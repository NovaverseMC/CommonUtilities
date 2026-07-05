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
import org.bukkit.event.inventory.PrepareInventoryResultEvent;
import org.bukkit.inventory.ItemStack;

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

    @ConfigValue(comment = "Whether to block furnace, blast furnace, and smoker processing")
    private Boolean blockFurnace = false;

    @ConfigValue(comment = "Whether to block brewing stand processing")
    private Boolean blockBrewing = false;

    @ConfigValue(comment = "Message sent to player when their craft/process is blocked. Keep empty for no message.")
    private String message = "&cYou are not allowed to craft or process this item!";

    @Override
    public void onLoad(String name, PluginService service) {
        this.service = service;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getSlotType() != InventoryType.SlotType.RESULT) {
            return;
        }

        InventoryType invType = event.getInventory().getType();

        // Skip villager trading
        if (invType == InventoryType.MERCHANT) {
            return;
        }

        // Handle furnace processing optionally
        if (!blockFurnace && (invType == InventoryType.FURNACE || invType == InventoryType.BLAST_FURNACE || invType == InventoryType.SMOKER)) {
            return;
        }

        // Handle brewing stands optionally
        if (!blockBrewing && invType == InventoryType.BREWING) {
            return;
        }

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Material material = item.getType();
        String matName = material.name();

        // Check general bypass permission or material-specific bypass permission
        if (player.hasPermission("common.bypass.craft") || player.hasPermission("common.bypass.craft." + matName.toLowerCase())) {
            return;
        }

        boolean blocked = false;

        // Check blacklist
        if (!blacklist.isEmpty() && containsIgnoreCase(blacklist, matName)) {
            blocked = true;
        }

        // Check whitelist (if not empty, only items in whitelist are allowed)
        if (!whitelist.isEmpty() && !containsIgnoreCase(whitelist, matName)) {
            blocked = true;
        }

        if (blocked) {
            event.setCancelled(true);
            if (!message.isEmpty()) {
                service.sendMessage(player, message);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        ItemStack result = event.getInventory().getResult();
        if (result == null || result.getType() == Material.AIR) {
            return;
        }

        if (shouldBlock(result.getType(), event.getViewers())) {
            event.getInventory().setResult(new ItemStack(Material.AIR));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepareResult(PrepareInventoryResultEvent event) {
        ItemStack result = event.getResult();
        if (result == null || result.getType() == Material.AIR) {
            return;
        }

        if (shouldBlock(result.getType(), event.getViewers())) {
            event.setResult(new ItemStack(Material.AIR));
        }
    }

    private boolean shouldBlock(Material material, List<HumanEntity> viewers) {
        String matName = material.name();
        boolean blocked = false;

        // Check blacklist
        if (!blacklist.isEmpty() && containsIgnoreCase(blacklist, matName)) {
            blocked = true;
        }

        // Check whitelist
        if (!whitelist.isEmpty() && !containsIgnoreCase(whitelist, matName)) {
            blocked = true;
        }

        if (blocked) {
            // If at least one viewer has bypass permission, don't block
            for (HumanEntity viewer : viewers) {
                if (viewer instanceof Player player) {
                    if (player.hasPermission("common.bypass.craft") || player.hasPermission("common.bypass.craft." + matName.toLowerCase())) {
                        return false;
                    }
                }
            }
            return true;
        }

        return false;
    }

    private boolean containsIgnoreCase(List<String> list, String val) {
        for (String s : list) {
            if (s.equalsIgnoreCase(val)) {
                return true;
            }
        }
        return false;
    }
}
