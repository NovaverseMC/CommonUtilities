package it.feargames.commonutilities.service;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.*;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class VaultServiceWrapper {

    @NonNull
    private final PluginService pluginService;

    private VaultService vaultService = null;

    public Optional<VaultService> getVaultService() {
        return Optional.ofNullable(vaultService);
    }

    public void initialize() {
        if (pluginService.isPluginEnabled("Vault")) {
            vaultService = new VaultService();
        }
    }

    public class VaultService {

        private Permission permission = null;

        private VaultService() {
            RegisteredServiceProvider<Permission> permissionProvider = pluginService.getService(Permission.class);
            if(permissionProvider != null) {
                permission = permissionProvider.getProvider();
            }
        }

        public List<String> getGroups() {
            return Arrays.asList(permission.getGroups());
        }

        public String getPrimaryGroup(String world, OfflinePlayer player) {
            return permission.getPrimaryGroup(world, player);
        }

    }

}
