package it.novaverse.commonutilities.module.implementation.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import it.novaverse.commonutilities.annotation.ConfigValue;
import it.novaverse.commonutilities.hook.PlaceholderHook;
import it.novaverse.commonutilities.annotation.RegisterListeners;
import it.novaverse.commonutilities.module.Module;
import it.novaverse.commonutilities.service.PluginService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Replacement for LuckPerms-Chat/LPC style chat formatting.
 * <p>
 * Unlike LPC, which resolves the placeholders once against the sender before the
 * renderer runs, this module resolves them inside the {@code ChatRenderer} for
 * <b>every viewer</b>, so relational placeholders (%rel_...%) can produce a
 * different result per receiver (e.g. resource pack glyphs only for who has the pack).
 */
@Log
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@RegisterListeners
public class ChatFormat implements Module, Listener {

    @ConfigValue(comment = "Enable or disable this module")
    private Boolean enabled = false;

    @ConfigValue(comment = {
            "Default chat format.",
            "Supported tokens: {prefix} {suffix} {prefixes} {suffixes} {name} {displayname} {world} {message}",
            "PlaceholderAPI placeholders (%...% and relational %rel_...%) are supported and resolved per viewer.",
            "MiniMessage tags are supported, legacy '&' codes and '&#RRGGBB' hex colors are converted automatically."
    })
    private String format = "{prefix} {name}<#606161> » <white>{message}";

    @ConfigValue(path = "group-formats", comment = {
            "Per LuckPerms primary group formats, overriding the default format.",
            "Key is the group name, value is the format string."
    })
    private Map<String, String> groupFormats = new LinkedHashMap<>();

    private PluginService service;
    private boolean placeholderApiPresent;
    private LuckPermsHook luckPerms;
    private boolean conflicting;

    @Override
    public void onLoad(String name, PluginService service) {
        this.service = service;
    }

    @Override
    public void onEnable() {
        conflicting = service.isPluginInstalled("LPC") || service.isPluginInstalled("LuckPerms-Chat");
        if (conflicting) {
            log.warning("LPC (LuckPerms-Chat) is installed: the chatFormat module will not format the chat. "
                    + "Remove LPC to enable the per-viewer placeholder resolution.");
            return;
        }

        placeholderApiPresent = service.isPluginInstalled("PlaceholderAPI");
        if (!placeholderApiPresent) {
            log.warning("PlaceholderAPI is not installed, placeholders will not be resolved.");
        }

        if (service.isPluginInstalled("LuckPerms")) {
            try {
                luckPerms = new LuckPermsHook();
            } catch (Throwable t) {
                log.log(Level.WARNING, "Unable to hook into LuckPerms, prefixes/suffixes will be empty!", t);
            }
        } else {
            log.warning("LuckPerms is not installed, prefixes/suffixes will be empty.");
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncChatEvent event) {
        if (conflicting) {
            return;
        }
        event.renderer((source, sourceDisplayName, message, viewer) -> render(source, sourceDisplayName, message,
                viewer instanceof Player player ? player : null));
    }

    private Component render(Player source, Component sourceDisplayName, Component message, Player viewer) {
        var raw = resolveFormat(source);

        raw = replaceTokens(raw, source, sourceDisplayName);

        if (placeholderApiPresent) {
            // Normal placeholders first, %rel_...% ones survive this pass
            raw = PlaceholderHook.setPlaceholders(source, raw);
            // A null viewer is fine: PlaceholderAPI hands both players to the expansion as they
            // are, and the conditional ones fall back to their no pack branch, which is the
            // readable one for the console and the logs.
            raw = PlaceholderHook.setRelationalPlaceholders(viewer, source, raw);
        }

        raw = LegacyColors.toMiniMessage(raw);

        // The player message is never parsed, it is inserted as a plain component
        var index = raw.indexOf(MESSAGE_TOKEN);
        if (index < 0) {
            return MiniMessage.miniMessage().deserialize(raw);
        }
        var before = raw.substring(0, index);
        var after = raw.substring(index + MESSAGE_TOKEN.length());
        return Component.empty()
                .append(MiniMessage.miniMessage().deserialize(before))
                .append(message)
                .append(MiniMessage.miniMessage().deserialize(after));
    }

    private static final String MESSAGE_TOKEN = "{message}";

    private String resolveFormat(Player source) {
        if (luckPerms != null && !groupFormats.isEmpty()) {
            var group = luckPerms.getPrimaryGroup(source);
            if (group != null) {
                var groupFormat = groupFormats.get(group);
                if (groupFormat != null) {
                    return groupFormat;
                }
            }
        }
        return format;
    }

    private String replaceTokens(String raw, Player source, Component sourceDisplayName) {
        raw = raw.replace("{prefix}", luckPerms == null ? "" : luckPerms.getPrefix(source));
        raw = raw.replace("{suffix}", luckPerms == null ? "" : luckPerms.getSuffix(source));
        raw = raw.replace("{prefixes}", luckPerms == null ? "" : luckPerms.getPrefixes(source));
        raw = raw.replace("{suffixes}", luckPerms == null ? "" : luckPerms.getSuffixes(source));
        raw = raw.replace("{name}", source.getName());
        raw = raw.replace("{displayname}",
                PlainTextComponentSerializer.plainText().serialize(sourceDisplayName));
        raw = raw.replace("{world}", source.getWorld().getName());
        return raw;
    }
}
