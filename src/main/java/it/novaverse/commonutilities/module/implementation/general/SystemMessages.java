package it.novaverse.commonutilities.module.implementation.general;

import it.novaverse.commonutilities.annotation.ConfigValue;
import it.novaverse.commonutilities.annotation.RegisterListeners;
import it.novaverse.commonutilities.module.Module;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@RegisterListeners
public class SystemMessages implements Module, Listener {

    @ConfigValue(comment = "Enable or disable this module")
    private Boolean enabled = false;
    @ConfigValue(comment = "If true, default join messages will be hidden")
    private Boolean hideJoinMessages = false;
    @ConfigValue(comment = "Custom join messages. Separate multiple random messages with ';'. Placeholders: <player>")
    private String customJoinMessage = "";
    @ConfigValue(comment = "If true, default quit/kick messages will be hidden")
    private Boolean hideLeaveMessages = false;
    @ConfigValue(comment = "Custom quit messages. Separate multiple random messages with ';'. Placeholders: <player>")
    private String customLeaveMessage = "";
    @ConfigValue(comment = "Custom kick messages when a player is kicked for being idle/AFK. Separate multiple random messages with ';'. Placeholders: <player>")
    private String customIdleKickMessage = "";

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (hideJoinMessages) {
            event.joinMessage(null);
            return;
        }

        if (!customJoinMessage.isEmpty()) {
            var template = getRandomMessageFromString(customJoinMessage);
            event.joinMessage(MiniMessage.miniMessage().deserialize(template,
                    Placeholder.unparsed("player", event.getPlayer().getName())));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (hideLeaveMessages) {
            event.quitMessage(null);
            return;
        }

        if (!customLeaveMessage.isEmpty()) {
            var template = getRandomMessageFromString(customLeaveMessage);
            event.quitMessage(MiniMessage.miniMessage().deserialize(template,
                    Placeholder.unparsed("player", event.getPlayer().getName())));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerKick(PlayerKickEvent event) {
        if (hideLeaveMessages) {
            event.leaveMessage(Component.empty());
        }

        var plainReason = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                .plainText().serialize(event.reason());

        if (!plainReason.toLowerCase().contains("idle") && !plainReason.toLowerCase().contains("afk")) {
            return;
        }

        if (!customIdleKickMessage.isEmpty()) {
            var template = getRandomMessageFromString(customIdleKickMessage);
            event.reason(MiniMessage.miniMessage().deserialize(template,
                    Placeholder.unparsed("player", event.getPlayer().getName())));
        }
    }

    private String getRandomMessageFromString(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        if (raw.contains(";")) {
            var split = raw.split(";");
            var index = new java.util.Random().nextInt(split.length);
            return split[index];
        }
        return raw;
    }
}
