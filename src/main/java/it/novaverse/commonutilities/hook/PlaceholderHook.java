package it.novaverse.commonutilities.hook;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

/**
 * Isolated PlaceholderAPI access, this class is loaded only when PlaceholderAPI is installed.
 */
public class PlaceholderHook {

    private PlaceholderHook() {
    }

    public static String setPlaceholders(Player source, String text) {
        return PlaceholderAPI.setPlaceholders(source, text);
    }

    public static String setRelationalPlaceholders(Player viewer, Player source, String text) {
        return PlaceholderAPI.setRelationalPlaceholders(viewer, source, text);
    }
}
