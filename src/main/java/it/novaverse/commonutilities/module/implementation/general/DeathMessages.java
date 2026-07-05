package it.novaverse.commonutilities.module.implementation.general;

import it.novaverse.commonutilities.annotation.ConfigValue;
import it.novaverse.commonutilities.annotation.RegisterListeners;
import it.novaverse.commonutilities.module.Module;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;

import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@RegisterListeners
public class DeathMessages implements Module, Listener {

    @ConfigValue
    private Boolean enabled = false;
    @ConfigValue
    private Boolean hideDeathMessages = false;
    @ConfigValue
    private Boolean useVanillaAsFallback = false;

    @ConfigValue(comment = "On-demand death messages by cause. Keys are DamageCause names, 'PLAYER', 'MOB', or 'default'. Separate multiple random messages with ';'.")
    private Map<String, String> deathMessagesByCause = new LinkedHashMap<>() {
        {
            put("PLAYER", "<red><player> was slain by <killer> using <item>");
            put("MOB", "<red><player> was slain by <killer>");
            put("FALL", "<red><player> fell from a high place");
            put("LAVA", "<red><player> tried to swim in lava");
            put("VOID", "<red><player> fell into the void");
            put("DROWNING", "<red><player> drowned");
            put("default", "<red><player> died");
        }
    };

    @ConfigValue(comment = "Name used when a player is killed with bare hands")
    private String bareHandsName = "their bare hands";

    @ConfigValue(comment = "Translations/custom names for entities. If empty, the entity type name is used.")
    private Map<String, String> entityNames = new LinkedHashMap<>() {
        {
            put("ZOMBIE", "Zombie");
            put("CREEPER", "Creeper");
            put("SKELETON", "Skeleton");
            put("SPIDER", "Spider");
        }
    };

    @ConfigValue(comment = "Translations/custom names for items/weapons. If empty, the item type name is used.")
    private Map<String, String> itemNames = new LinkedHashMap<>() {
        {
            put("DIAMOND_SWORD", "Diamond Sword");
            put("NETHERITE_SWORD", "Netherite Sword");
            put("BOW", "Bow");
        }
    };

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (hideDeathMessages) {
            event.deathMessage(null);
            return;
        }

        var victim = event.getEntity();
        var lastDamageEvent = victim.getLastDamageCause();

        var template = resolveDeathMessage(victim, lastDamageEvent);

        if (template != null) {
            var killerName = "";
            var itemName = bareHandsName;

            if (victim.getKiller() != null) {
                var killer = victim.getKiller();
                killerName = killer.getName();
                var weapon = killer.getInventory().getItemInMainHand();
                if (weapon != null && weapon.getType() != org.bukkit.Material.AIR) {
                    if (weapon.hasItemMeta() && weapon.getItemMeta().hasDisplayName()) {
                        itemName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                                .plainText().serialize(weapon.getItemMeta().displayName());
                    } else {
                        var typeName = weapon.getType().name();
                        var translatedName = getFromMapIgnoreCase(itemNames, typeName);
                        itemName = translatedName != null ? translatedName
                                : typeName.replace("_", " ").toLowerCase();
                    }
                }
            } else if (lastDamageEvent instanceof EntityDamageByEntityEvent entityEvent) {
                var damager = entityEvent.getDamager();
                if (damager instanceof org.bukkit.entity.Projectile projectile
                        && projectile.getShooter() instanceof Entity shooter) {
                    damager = shooter;
                }
                if (damager.customName() != null) {
                    killerName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                            .serialize(damager.customName());
                } else {
                    var typeName = damager.getType().name();
                    var translatedName = getFromMapIgnoreCase(entityNames, typeName);
                    killerName = translatedName != null ? translatedName : typeName.replace("_", " ").toLowerCase();
                }
            }

            event.deathMessage(MiniMessage.miniMessage().deserialize(template,
                    Placeholder.unparsed("player", victim.getName()),
                    Placeholder.unparsed("killer", killerName),
                    Placeholder.unparsed("item", itemName)));
        }
    }

    private String resolveDeathMessage(Player victim, EntityDamageEvent lastDamageEvent) {
        if (deathMessagesByCause == null || deathMessagesByCause.isEmpty()) {
            return null;
        }

        var cause = lastDamageEvent != null ? lastDamageEvent.getCause()
                : EntityDamageEvent.DamageCause.CUSTOM;
        var causeName = cause.name();

        var keysToCheck = new ArrayList<String>();

        if (victim.getKiller() != null) {
            if (lastDamageEvent instanceof EntityDamageByEntityEvent entityEvent) {
                var damager = entityEvent.getDamager();
                if (damager instanceof org.bukkit.entity.Projectile projectile
                        && projectile.getShooter() instanceof Entity shooter) {
                    damager = shooter;
                }
                if (damager instanceof Tameable tameable && tameable.isTamed()
                        && tameable.getOwner() != null && tameable.getOwner().equals(victim.getKiller())) {
                    keysToCheck.add("PLAYER_TAMEABLE_" + tameable.getType().name());
                    keysToCheck.add("TAMEABLE_" + tameable.getType().name());
                }
            }
            keysToCheck.add("PLAYER_" + causeName);
            keysToCheck.add("PLAYER");
        } else if (lastDamageEvent instanceof EntityDamageByEntityEvent entityEvent) {
            var damager = entityEvent.getDamager();
            if (damager instanceof org.bukkit.entity.Projectile projectile
                    && projectile.getShooter() instanceof Entity shooter) {
                damager = shooter;
            }
            keysToCheck.add(damager.getType().name()); // e.g., "CREEPER"
            if (damager instanceof Mob) {
                keysToCheck.add("MOB");
            }
        }

        keysToCheck.add(causeName); // e.g., "FALL", "LAVA"
        if (!useVanillaAsFallback) {
            keysToCheck.add("default");
        }

        for (var key : keysToCheck) {
            var value = getFromMapIgnoreCase(deathMessagesByCause, key);
            if (value != null && !value.isEmpty()) {
                var split = value.split(";");
                var index = new java.util.Random().nextInt(split.length);
                return split[index];
            }
        }

        return null;
    }

    private String getFromMapIgnoreCase(Map<String, String> map, String key) {
        for (var entry : map.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
