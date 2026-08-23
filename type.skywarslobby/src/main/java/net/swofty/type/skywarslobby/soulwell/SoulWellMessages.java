package net.swofty.type.skywarslobby.soulwell;

import net.swofty.commons.StringUtility;
import net.swofty.type.generic.user.HypixelPlayer;

/**
 * Utility class for sending centered chat messages for Soul Well upgrades.
 */
public class SoulWellMessages {
    private static final int CENTER_PX = 154; // Minecraft chat center pixel

    /**
     * Send a purchase confirmation message to the player
     * Format:
     * [blank line]
     * [centered purple upgrade name]
     * [centered white description]
     * [blank line]
     * [centered yellow "Rewards"]
     * [centered effect description]
     */
    public static void sendPurchaseMessage(HypixelPlayer player, SoulWellUpgrade upgrade,
                                           SoulWellUpgrade.SoulWellUpgradeTier tier, int newLevel) {
        player.sendMessage("");

        // Centered purple upgrade name with level
        String upgradeName = "<5><l>" + upgrade.name().toUpperCase() + " " + StringUtility.getAsRomanNumeral(newLevel);
        player.sendMessage(centerMessage(upgradeName));

        // Centered white description
        String description = "<f>" + upgrade.baseDescription();
        player.sendMessage(centerMessage(description));

        player.sendMessage("");

        // Centered yellow "Rewards"
        player.sendMessage(centerMessage("<e><l>Rewards"));

        // Centered effect description
        String effectLine = "<7>" + tier.previousEffect() + " <l>→ </l><a>" + tier.newEffect() + " <7>" + tier.effectDescription();
        player.sendMessage(centerMessage(effectLine));
    }

    /**
     * Center a message in Minecraft chat
     */
    public static String centerMessage(String message) {
        if (message == null || message.isEmpty()) return message;

        int messagePxSize = getMessagePixelWidth(message);
        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = 4; // Default space width
        int compensated = 0;

        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }

        return sb + message;
    }

    /**
     * Calculate the pixel width of a message (accounting for markup tags)
     */
    private static int getMessagePixelWidth(String message) {
        int width = 0;
        boolean isBold = false;

        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);

            if (c == '<') {
                int end = message.indexOf('>', i);
                if (end > i) {
                    String tag = message.substring(i + 1, end);
                    if (tag.equals("l") || tag.equals("bold")) {
                        isBold = true;
                    } else if (tag.equals("/l") || tag.equals("/bold") || tag.equals("r") || tag.equals("reset")) {
                        isBold = false;
                    }
                    i = end;
                    continue;
                }
            }

            width += getCharWidth(c, isBold);
        }

        return width;
    }

    /**
     * Get the pixel width of a character
     */
    private static int getCharWidth(char c, boolean bold) {
        int width;
        switch (c) {
            case ' ' -> width = 4;
            case '!' -> width = 2;
            case '"' -> width = 5;
            case '\'' -> width = 3;
            case '(' -> width = 5;
            case ')' -> width = 5;
            case '*' -> width = 5;
            case ',' -> width = 2;
            case '.' -> width = 2;
            case ':' -> width = 2;
            case ';' -> width = 2;
            case '<' -> width = 5;
            case '>' -> width = 5;
            case '@' -> width = 7;
            case 'I' -> width = 4;
            case '[' -> width = 4;
            case ']' -> width = 4;
            case '`' -> width = 3;
            case 'f' -> width = 5;
            case 'i' -> width = 2;
            case 'k' -> width = 5;
            case 'l' -> width = 3;
            case 't' -> width = 4;
            case '{' -> width = 5;
            case '|' -> width = 2;
            case '}' -> width = 5;
            case '~' -> width = 7;
            default -> width = 6;
        }
        if (bold) width += 1;
        return width;
    }

}
