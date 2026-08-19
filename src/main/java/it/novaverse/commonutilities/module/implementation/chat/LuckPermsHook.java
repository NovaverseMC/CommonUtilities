package it.novaverse.commonutilities.module.implementation.chat;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.platform.PlayerAdapter;
import org.bukkit.entity.Player;

import java.util.SortedMap;

/**
 * Isolated LuckPerms access, this class is loaded only when LuckPerms is installed.
 */
class LuckPermsHook {

    private final PlayerAdapter<Player> adapter;

    LuckPermsHook() {
        LuckPerms api = LuckPermsProvider.get();
        adapter = api.getPlayerAdapter(Player.class);
    }

    private CachedMetaData getMetaData(Player player) {
        return adapter.getMetaData(player);
    }

    String getPrefix(Player player) {
        return orEmpty(getMetaData(player).getPrefix());
    }

    String getSuffix(Player player) {
        return orEmpty(getMetaData(player).getSuffix());
    }

    String getPrefixes(Player player) {
        return join(getMetaData(player).getPrefixes());
    }

    String getSuffixes(Player player) {
        return join(getMetaData(player).getSuffixes());
    }

    String getPrimaryGroup(Player player) {
        var user = getUser(player);
        return user == null ? getMetaData(player).getPrimaryGroup() : user.getPrimaryGroup();
    }

    private User getUser(Player player) {
        return LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
    }

    private static String join(SortedMap<Integer, String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        var builder = new StringBuilder();
        values.values().forEach(builder::append);
        return builder.toString();
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
