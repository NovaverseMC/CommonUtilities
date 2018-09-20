package it.feargames.commonutilities.service;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import it.feargames.commonutilities.CommonUtilities;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
        if(protocolService != null) {
            handler.accept(protocolService);
        }
    }

    public void initialize() {
        if (pluginService.isPluginEnabled("ProtocolLib")) {
            protocolService = new ProtocolService();
        }
    }

    public void cleanup() {
        handle(ProtocolService::unregisterAll);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public class ProtocolService {

        private Map<String, PacketListener> listeners = new HashMap<>();

        public void unregisterAll() {
            listeners.values().forEach(listener -> {
                ProtocolLibrary.getProtocolManager().removePacketListener(listener);
            });
            listeners.clear();
        }

        public void removePacketListener(String listenerId) {
            PacketListener listener = listeners.get(listenerId);
            ProtocolLibrary.getProtocolManager().removePacketListener(listener);
        }

        public void addSendingListener(String listenerId, ListenerPriority priority, PacketType packetType, Consumer<PacketEvent> consumer) {
            if (listeners.containsKey(listenerId)) {
                throw new IllegalStateException("A listener with id " + listenerId + " is already registered!");
            }
            PacketListener listener = new PacketAdapter(plugin, priority, packetType) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    consumer.accept(event);
                }
            };
            ProtocolLibrary.getProtocolManager().addPacketListener(listener);
            listeners.put(listenerId, listener);
        }

        public void addReceivingListener(String listenerId, ListenerPriority priority, PacketType packetType, Consumer<PacketEvent> consumer) {
            if (listeners.containsKey(listenerId)) {
                throw new IllegalStateException("A listener with id " + listenerId + " is already registered!");
            }
            PacketListener listener = new PacketAdapter(plugin, priority, packetType) {
                @Override
                public void onPacketReceiving(PacketEvent event) {
                    consumer.accept(event);
                }
            };
            ProtocolLibrary.getProtocolManager().addPacketListener(listener);
            listeners.put(listenerId, listener);
        }
    }

}
