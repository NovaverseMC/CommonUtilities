package it.feargames.commonutilities.service;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import it.feargames.commonutilities.CommonUtilities;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class ProtocolServiceWrapper {

    @NonNull
    private final CommonUtilities plugin;
    @NonNull
    private final PluginService pluginService;

    private ProtocolService protocolService = null;

    // Can't return optional + lambdas as they would trigger a class lookup and cause an exception if ProtocolLib isn't installed
    public void handle(Consumer<ProtocolService> handler) {
        if (protocolService != null) handler.accept(protocolService);
    }

    public void initialize() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(plugin));
    }

    public void cleanup() {
        handle(ProtocolService :: unregisterAll);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public class ProtocolService {

        private final Map<String, PacketListenerAbstract> listeners = new HashMap<>();

        public void unregisterAll() {
            listeners.values().forEach(listener -> {
                PacketEvents.getAPI().getEventManager().unregisterListener(listener);
            });
            listeners.clear();
        }

        public void removePacketListener(String listenerId) {
            PacketListenerAbstract listener = listeners.get(listenerId);
            PacketEvents.getAPI().getEventManager().unregisterListeners(listener);
        }

        public void addSendingListener(String listenerId, PacketListenerPriority priority, PacketTypeCommon packetTypeCommon, Consumer<PacketSendEvent> consumer) {
            ensureSingleListener(listenerId);

            PacketListenerAbstract listener = new PacketListenerAbstract(priority) {
                @Override
                public void onPacketSend(PacketSendEvent event) {
                    if (event.getPacketType() != packetTypeCommon) return;
                    consumer.accept(event);
                }
            };

            registerListener(listenerId, listener);
        }

        public void addReceivingListener(String listenerId, PacketListenerPriority priority, PacketTypeCommon packetTypeCommon, Consumer<PacketReceiveEvent> consumer) {
            ensureSingleListener(listenerId);

            PacketListenerAbstract listener = new PacketListenerAbstract(priority) {
                @Override
                public void onPacketReceive(PacketReceiveEvent event) {
                    if (event.getPacketType() != packetTypeCommon) return;
                    consumer.accept(event);
                }
            };

            registerListener(listenerId, listener);
        }
    }

    private void registerListener(String listenerId, PacketListenerAbstract listener) {
        PacketEvents.getAPI().getEventManager().registerListener(listener);
        protocolService.listeners.put(listenerId, listener);
    }

    private void ensureSingleListener(String listenerId) {
        if (protocolService.listeners.containsKey(listenerId))
            throw new IllegalStateException("A listener with id " + listenerId + " is already registered!");
    }

}
