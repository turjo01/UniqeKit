package com.turjo.uniquekits.utils;

import com.turjo.uniquekits.UniqueKits;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MessageUtils {
    
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    
    /**
     * Colorize a string with support for legacy codes, hex codes, and MiniMessage format
     */
    public static String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        
        // Handle hex colors first (&#RRGGBB format)
        message = translateHexColorCodes(message);
        
        // Handle legacy color codes
        message = ChatColor.translateAlternateColorCodes('&', message);
        
        return message;
    }
    
    /**
     * Colorize a list of strings
     */
    public static List<String> colorizeList(List<String> messages) {
        if (messages == null) {
            return null;
        }
        
        return messages.stream()
            .map(MessageUtils::colorize)
            .collect(Collectors.toList());
    }
    
    /**
     * Send a message to a command sender
     */
    public static void sendMessage(CommandSender sender, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        
        if (sender instanceof Player) {
            Player player = (Player) sender;
            
            // Try MiniMessage format first
            if (message.contains("<") && message.contains(">")) {
                try {
                    Component component = MINI_MESSAGE.deserialize(message);
                    UniqueKits.getInstance().getAdventure().player(player).sendMessage(component);
                    return;
                } catch (Exception e) {
                    // Fall back to legacy format
                }
            }
            
            // Use legacy format
            Component component = LEGACY_SERIALIZER.deserialize(colorize(message));
            UniqueKits.getInstance().getAdventure().player(player).sendMessage(component);
        } else {
            // For console, strip colors and send plain text
            sender.sendMessage(ChatColor.stripColor(colorize(message)));
        }
    }
    
    /**
     * Send multiple messages to a command sender
     */
    public static void sendMessages(CommandSender sender, List<String> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        
        for (String message : messages) {
            sendMessage(sender, message);
        }
    }
    
    /**
     * Send a title to a player
     */
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (title == null) title = "";
        if (subtitle == null) subtitle = "";
        
        Component titleComponent = LEGACY_SERIALIZER.deserialize(colorize(title));
        Component subtitleComponent = LEGACY_SERIALIZER.deserialize(colorize(subtitle));
        
        UniqueKits.getInstance().getAdventure().player(player).showTitle(
            net.kyori.adventure.title.Title.title(
                titleComponent,
                subtitleComponent,
                net.kyori.adventure.title.Title.Times.times(
                    java.time.Duration.ofMillis(fadeIn * 50),
                    java.time.Duration.ofMillis(stay * 50),
                    java.time.Duration.ofMillis(fadeOut * 50)
                )
            )
        );
    }
    
    /**
     * Send an action bar message to a player
     */
    public static void sendActionBar(Player player, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        
        Component component = LEGACY_SERIALIZER.deserialize(colorize(message));
        UniqueKits.getInstance().getAdventure().player(player).sendActionBar(component);
    }
    
    /**
     * Translate hex color codes from &#RRGGBB to the appropriate format
     */
    private static String translateHexColorCodes(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, "§x"
                + "§" + group.charAt(0) + "§" + group.charAt(1)
                + "§" + group.charAt(2) + "§" + group.charAt(3)
                + "§" + group.charAt(4) + "§" + group.charAt(5));
        }
        
        return matcher.appendTail(buffer).toString();
    }
    
    /**
     * Strip all color codes from a message
     */
    public static String stripColor(String message) {
        if (message == null) {
            return null;
        }
        
        return ChatColor.stripColor(colorize(message));
    }
    
    /**
     * Check if a string contains MiniMessage formatting
     */
    public static boolean isMiniMessage(String message) {
        return message != null && message.contains("<") && message.contains(">");
    }
    
    /**
     * Convert legacy format to MiniMessage format
     */
    public static String legacyToMiniMessage(String legacy) {
        if (legacy == null) {
            return null;
        }
        
        // This is a basic conversion - you might want to implement a more sophisticated converter
        return legacy
            .replace("&0", "<black>")
            .replace("&1", "<dark_blue>")
            .replace("&2", "<dark_green>")
            .replace("&3", "<dark_aqua>")
            .replace("&4", "<dark_red>")
            .replace("&5", "<dark_purple>")
            .replace("&6", "<gold>")
            .replace("&7", "<gray>")
            .replace("&8", "<dark_gray>")
            .replace("&9", "<blue>")
            .replace("&a", "<green>")
            .replace("&b", "<aqua>")
            .replace("&c", "<red>")
            .replace("&d", "<light_purple>")
            .replace("&e", "<yellow>")
            .replace("&f", "<white>")
            .replace("&l", "<bold>")
            .replace("&m", "<strikethrough>")
            .replace("&n", "<underlined>")
            .replace("&o", "<italic>")
            .replace("&r", "<reset>");
    }
    
    /**
     * Format a long duration into a readable string
     */
    public static String formatDuration(long millis) {
        if (millis <= 0) {
            return "0s";
        }
        
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        StringBuilder result = new StringBuilder();
        
        if (days > 0) {
            result.append(days).append("d ");
            hours %= 24;
        }
        if (hours > 0) {
            result.append(hours).append("h ");
            minutes %= 60;
        }
        if (minutes > 0) {
            result.append(minutes).append("m ");
            seconds %= 60;
        }
        if (seconds > 0 || result.length() == 0) {
            result.append(seconds).append("s");
        }
        
        return result.toString().trim();
    }
    
    /**
     * Replace placeholders in a message
     */
    public static String replacePlaceholders(String message, String... replacements) {
        if (message == null || replacements.length % 2 != 0) {
            return message;
        }
        
        String result = message;
        for (int i = 0; i < replacements.length; i += 2) {
            result = result.replace(replacements[i], replacements[i + 1]);
        }
        
        return result;
    }
}