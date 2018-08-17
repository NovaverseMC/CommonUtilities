package it.feargames.commonutilities.module.implementation.fix;

import com.comphenix.packetwrapper.WrapperPlayServerScoreboardTeam;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
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
        wrapper.getProtocolService().ifPresent(protocol -> {
            // Protocol docs: http://wiki.vg/Protocol#Teams
            protocol.addSendingListener(LISTENER_ID, ListenerPriority.HIGH, PacketType.Play.Server.SCOREBOARD_TEAM, event -> {
                final WrapperPlayServerScoreboardTeam wrapper = new WrapperPlayServerScoreboardTeam(event.getPacket());
                if(wrapper.getMode() != WrapperPlayServerScoreboardTeam.Mode.TEAM_CREATED
                        && wrapper.getMode() != WrapperPlayServerScoreboardTeam.Mode.TEAM_UPDATED) {
                    return;
                }

                // Reparse, prevent mixed legacy-component formats
                String scoreBuilder = BaseComponent.toLegacyText(ComponentConverter.fromWrapper(wrapper.getPrefix())) +
                        BaseComponent.toLegacyText(ComponentConverter.fromWrapper(wrapper.getSuffix()));
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
                wrapper.setPrefix(WrappedChatComponent.fromText(prefixBuilder.toString()));
                wrapper.setSuffix(WrappedChatComponent.fromText(suffixBuilder.toString()));
            });
        });
    }

    @Override
    public void onDisable() {
        wrapper.getProtocolService().ifPresent(protocol -> protocol.removePacketListener(LISTENER_ID));
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
