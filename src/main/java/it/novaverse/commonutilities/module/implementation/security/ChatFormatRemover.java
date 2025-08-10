package it.novaverse.commonutilities.module.implementation.security;

import io.papermc.paper.event.player.AsyncChatEvent;
import it.novaverse.commonutilities.annotation.ConfigValue;
import it.novaverse.commonutilities.annotation.RegisterListeners;
import it.novaverse.commonutilities.module.Module;
import it.novaverse.commonutilities.service.PluginService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@RegisterListeners
public class ChatFormatRemover implements Module, Listener {

    @ConfigValue
    private Boolean enabled = false;

    private PluginService service;

    @Override
    public void onLoad(String name, PluginService service) {
        this.service = service;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncChatEvent event) {
        if (event.getPlayer().hasPermission("common.chatformat.allow")) return;


        String plainTextMessage = PlainTextComponentSerializer.plainText().serialize(event.message());
        Component newMessage = service.transformComponent(plainTextMessage).color(NamedTextColor.WHITE);
        event.message(newMessage);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
