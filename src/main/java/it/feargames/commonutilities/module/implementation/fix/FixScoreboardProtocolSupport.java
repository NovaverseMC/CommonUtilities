package it.feargames.commonutilities.module.implementation.fix;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.InternalStructure;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.ComponentConverter;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import it.feargames.commonutilities.annotation.ConfigValue;
import it.feargames.commonutilities.module.Module;
import it.feargames.commonutilities.service.PluginService;
import it.feargames.commonutilities.service.ProtocolServiceWrapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Optional;

// FIXME: not working as expected in some scenarios

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class FixScoreboardProtocolSupport implements Module {

    private final static String LISTENER_ID = "FixScoreboardProtocolSupport";

    private ProtocolServiceWrapper wrapper;

    @ConfigValue
    private Boolean enabled = true;

    @Override
    public void onLoad(String name, PluginService service, ProtocolServiceWrapper wrapper) {
        this.wrapper = wrapper;
    }

    private static String getFormatString(final BaseComponent component) {
        StringBuilder formatBuilder = new StringBuilder();
        formatBuilder.append(component.getColor());
        if (component.isBold()) formatBuilder.append(ChatColor.BOLD);
        if (component.isItalic()) formatBuilder.append(ChatColor.ITALIC);
        if (component.isUnderlined()) formatBuilder.append(ChatColor.UNDERLINE);
        if (component.isStrikethrough()) formatBuilder.append(ChatColor.STRIKETHROUGH);
        if (component.isObfuscated()) formatBuilder.append(ChatColor.MAGIC);
        return formatBuilder.toString();
    }

    @Override
    public void onEnable() {
        wrapper.handle(protocol -> {
            // Protocol docs: http://wiki.vg/Protocol#Teams
            protocol.addSendingListener(LISTENER_ID, ListenerPriority.HIGH, PacketType.Play.Server.SCOREBOARD_TEAM, event -> {
                PacketContainer packet = event.getPacket();
                int mode = packet.getIntegers().read(0);

                // Created or updated
                if(mode != 0 && mode != 2) {
                    return;
                }

                InternalStructure structure = packet.getOptionalStructures().read(0)
                        .orElseThrow(() -> new RuntimeException("Invalid packet!"));

                WrappedChatComponent prefix = structure.getChatComponents().read(1);
                WrappedChatComponent suffix = structure.getChatComponents().read(2);

                // Reparse, prevent mixed legacy-component formats
                String scoreBuilder = BaseComponent.toLegacyText(ComponentConverter.fromWrapper(prefix)) +
                        BaseComponent.toLegacyText(ComponentConverter.fromWrapper(suffix));
                final BaseComponent[] messageComponents = TextComponent.fromLegacyText(scoreBuilder);

                final StringBuilder prefixBuilder = new StringBuilder(16);
                boolean useSuffix = false;
                final StringBuilder suffixBuilder = new StringBuilder(16);

                for (BaseComponent component : messageComponents) {
                    final String componentText = ChatColor.stripColor(component.toLegacyText());
                    final String componentFormat = getFormatString(component);
                    String remaining = componentText;
                    while (!remaining.isEmpty()) {
                        if (!useSuffix) {
                            final int availablePrefix = prefixBuilder.capacity() - prefixBuilder.length() - componentFormat.length();
                            if (availablePrefix < 1) {
                                // Ok, let's use suffix from now on!
                                useSuffix = true;
                                continue;
                            }
                            final int handled = Math.min(availablePrefix, remaining.length());
                            prefixBuilder.append(componentFormat).append(remaining, 0, handled);
                            remaining = remaining.substring(handled);
                        } else {
                            // Ok, time to overflow!
                            suffixBuilder.append(componentFormat).append(remaining);
                            break;
                        }
                    }
                }

                structure.getChatComponents().write(1, WrappedChatComponent.fromText(prefixBuilder.toString()));
                structure.getChatComponents().write(2, WrappedChatComponent.fromText(suffixBuilder.toString()));
            });
        });
    }

    @Override
    public void onDisable() {
        wrapper.handle(protocol -> protocol.removePacketListener(LISTENER_ID));
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
