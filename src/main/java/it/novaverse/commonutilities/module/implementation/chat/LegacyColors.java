package it.novaverse.commonutilities.module.implementation.chat;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Converts legacy '&' color codes and '&#RRGGBB' hex colors to MiniMessage tags,
 * keeping compatibility with the formats used by LPC configurations.
 */
final class LegacyColors {

    private static final Pattern HEX = Pattern.compile("[&§]#([0-9a-fA-F]{6})");
    private static final Map<Character, String> CODES = Map.ofEntries(
            Map.entry('0', "black"),
            Map.entry('1', "dark_blue"),
            Map.entry('2', "dark_green"),
            Map.entry('3', "dark_aqua"),
            Map.entry('4', "dark_red"),
            Map.entry('5', "dark_purple"),
            Map.entry('6', "gold"),
            Map.entry('7', "gray"),
            Map.entry('8', "dark_gray"),
            Map.entry('9', "blue"),
            Map.entry('a', "green"),
            Map.entry('b', "aqua"),
            Map.entry('c', "red"),
            Map.entry('d', "light_purple"),
            Map.entry('e', "yellow"),
            Map.entry('f', "white"),
            Map.entry('k', "obfuscated"),
            Map.entry('l', "bold"),
            Map.entry('m', "strikethrough"),
            Map.entry('n', "underlined"),
            Map.entry('o', "italic"),
            Map.entry('r', "reset"));

    private LegacyColors() {
    }

    static String toMiniMessage(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        if (input.indexOf('&') < 0 && input.indexOf('§') < 0) {
            return input;
        }

        var text = HEX.matcher(input).replaceAll("<#$1>");

        var builder = new StringBuilder(text.length());
        for (var i = 0; i < text.length(); i++) {
            var current = text.charAt(i);
            if ((current == '&' || current == '§') && i + 1 < text.length()) {
                var tag = CODES.get(Character.toLowerCase(text.charAt(i + 1)));
                if (tag != null) {
                    builder.append('<').append(tag).append('>');
                    i++;
                    continue;
                }
            }
            builder.append(current);
        }
        return builder.toString();
    }
}
