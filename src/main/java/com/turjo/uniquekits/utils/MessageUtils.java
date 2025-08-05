package com.turjo.uniquekits.utils;

import com.turjo.uniquekits.UniqueKits;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MessageUtils {
    
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    
    /**
     * Colorize a string with support for legacy codes and hex codes
     */
    public static String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        
        // Handle hex colors (&#RRGGBB format)
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
     * Send a message to a command sender with full color support
     */
    public static void sendMessage(CommandSender sender, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        
        sender.sendMessage(colorize(message));
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
        
        player.sendTitle(colorize(title), colorize(subtitle), fadeIn, stay, fadeOut);
    }
    
    /**
     * Send an action bar message to a player
     */
    public static void sendActionBar(Player player, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        
        // Use reflection for action bar since it's version dependent
        try {
            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(colorize(message)));
        } catch (Exception e) {
            // Fallback to regular message
            sendMessage(player, message);
        }
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