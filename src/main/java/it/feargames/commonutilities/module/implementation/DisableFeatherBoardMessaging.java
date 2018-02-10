package it.feargames.commonutilities.module.implementation;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import it.feargames.commonutilities.annotation.ConfigValue;
import it.feargames.commonutilities.module.Module;
import it.feargames.commonutilities.service.PluginService;
import it.feargames.commonutilities.service.ProtocolServiceWrapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class DisableFeatherBoardMessaging implements Module {

    private final static String LISTENER_ID = "disableFeatherBoardMessaging";

    @ConfigValue
    private boolean enabled = false;

    private ProtocolServiceWrapper wrapper;

    @Override
    public void onLoad(String name, PluginService service, ProtocolServiceWrapper wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public void onEnable() {
        wrapper.getProtocolService().ifPresent(protocol -> protocol.addSendingListener(LISTENER_ID, ListenerPriority.LOWEST, PacketType.Play.Server.CUSTOM_PAYLOAD, event -> {
            if (event.isCancelled()) {
                return;
            }
            PacketContainer packetContainer = event.getPacket();
            StructureModifier<String> strings = packetContainer.getStrings();
            String channel = strings.read(0);
            if (!channel.equals("BungeeCord")) {
                return;
            }

            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (StackTraceElement element : stackTrace) {
                if (element.getClassName().startsWith("be.maximvdw.featherboard")) {
                    event.setCancelled(true);
                    return;
                }
            }
        }));
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
