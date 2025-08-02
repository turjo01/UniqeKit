package com.turjo.uniquekits.utils;

import com.turjo.uniquekits.UniqueKits;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MessageUtils {
    
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient:([^>]+)>([^<]+)</gradient>");
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    
    /**
     * Colorize a string with support for legacy codes, hex codes, gradients, and MiniMessage format
     */
    public static String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        
        // Handle MiniMessage format first
        if (message.contains("<") && message.contains(">")) {
            try {
                // Handle gradients
                message = handleGradients(message);
                
                // Handle other MiniMessage tags
                Component component = MINI_MESSAGE.deserialize(message);
                return LEGACY_SERIALIZER.serialize(component);
            } catch (Exception e) {
                // Fall back to legacy processing
            }
        }
        
        // Handle hex colors (&#RRGGBB format)
        message = translateHexColorCodes(message);
        
        // Handle legacy color codes
        message = ChatColor.translateAlternateColorCodes('&', message);
        
        return message;
    }
    
    private static String handleGradients(String message) {
        Matcher matcher = GRADIENT_PATTERN.matcher(message);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String colors = matcher.group(1);
            String text = matcher.group(2);
            String[] colorArray = colors.split(":");
            
            if (colorArray.length >= 2) {
                String gradient = createGradient(text, colorArray[0], colorArray[1]);
                matcher.appendReplacement(result, gradient);
            } else {
                matcher.appendReplacement(result, text);
            }
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    private static String createGradient(String text, String startColor, String endColor) {
        // Simple gradient implementation
        StringBuilder result = new StringBuilder();
        int length = text.length();
        
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                result.append(c);
                continue;
            }
            
            // Calculate color interpolation
            float ratio = (float) i / (length - 1);
            String color = interpolateColor(startColor, endColor, ratio);
            result.append("&#").append(color).append(c);
        }
        
        return result.toString();
    }
    
    private static String interpolateColor(String start, String end, float ratio) {
        // Remove # if present
        start = start.replace("#", "");
        end = end.replace("#", "");
        
        try {
            int startR = Integer.parseInt(start.substring(0, 2), 16);
            int startG = Integer.parseInt(start.substring(2, 4), 16);
            int startB = Integer.parseInt(start.substring(4, 6), 16);
            
            int endR = Integer.parseInt(end.substring(0, 2), 16);
            int endG = Integer.parseInt(end.substring(2, 4), 16);
            int endB = Integer.parseInt(end.substring(4, 6), 16);
            
            int r = (int) (startR + (endR - startR) * ratio);
            int g = (int) (startG + (endG - startG) * ratio);
            int b = (int) (startB + (endB - startB) * ratio);
            
            return String.format("%02x%02x%02x", r, g, b);
        } catch (Exception e) {
            return start;
        }
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
     * Send a message to a command sender with full color support
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
            
            // Use legacy format with color support
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
     * Send a title to a player with color support
     */
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (title == null) title = "";
        if (subtitle == null) subtitle = "";
        
        Component titleComponent = LEGACY_SERIALIZER.deserialize(colorize(title));
        Component subtitleComponent = LEGACY_SERIALIZER.deserialize(colorize(subtitle));
        
        UniqueKits.getInstance().getAdventure().player(player).showTitle(
            Title.title(
                titleComponent,
                subtitleComponent,
                Title.Times.times(
                    Duration.ofMillis(fadeIn * 50),
                    Duration.ofMillis(stay * 50),
                    Duration.ofMillis(fadeOut * 50)
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
            result.append("&e").append(days).append("&6d ");
            hours %= 24;
        }
        if (hours > 0) {
            result.append("&e").append(hours).append("&6h ");
            minutes %= 60;
        }
        if (minutes > 0) {
            result.append("&e").append(minutes).append("&6m ");
            seconds %= 60;
        }
        if (seconds > 0 || result.length() == 0) {
            result.append("&e").append(seconds).append("&6s");
        }
        
        return colorize(result.toString().trim());
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
    
    /**
     * Create a centered message
     */
    public static String centerMessage(String message) {
        if (message == null) return "";
        
        int maxWidth = 80;
        int messageLength = ChatColor.stripColor(message).length();
        
        if (messageLength >= maxWidth) {
            return message;
        }
        
        int spaces = (maxWidth - messageLength) / 2;
        StringBuilder centered = new StringBuilder();
        
        for (int i = 0; i < spaces; i++) {
            centered.append(" ");
        }
        
        centered.append(message);
        return centered.toString();
    }
    
    /**
     * Create a progress bar
     */
    public static String createProgressBar(double percentage, int length, String completeColor, String incompleteColor) {
        StringBuilder bar = new StringBuilder();
        int completed = (int) (percentage * length);
        
        bar.append(completeColor);
        for (int i = 0; i < completed; i++) {
            bar.append("█");
        }
        
        bar.append(incompleteColor);
        for (int i = completed; i < length; i++) {
            bar.append("█");
        }
        
        return colorize(bar.toString());
    }
}