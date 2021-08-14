package fr.royalpha.bungeeannounce.util;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {
    private static final int CENTER_PX = 154;

    public static String centerMessage(String message) {
        Pattern pattern = Pattern.compile("<center>(.*)</center>");
        Matcher matcher = pattern.matcher(message);
        if (!matcher.find()) {
            return color(message);
        }

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;
        Pattern regex = Pattern.compile("(&#[A-Fa-f0-9]{6}.*?:)");
        matcher = regex.matcher(message);
        char[] var7;
        int toCompensate;
        int spaceLength;
        char c;
        DefaultFontInfo dFI;
        if (matcher.find()) {
            var7 = matcher.group().toCharArray();
            toCompensate = var7.length;

            for (spaceLength = 0; spaceLength < toCompensate; ++spaceLength) {
                c = var7[spaceLength];
                if (c != '&' && c != '#') {
                    dFI = DefaultFontInfo.getDefaultFontInfo(c);
                    messagePxSize += dFI.getLength();
                    ++messagePxSize;
                }
            }
        }

        message = color(message);
        var7 = message.toCharArray();
        toCompensate = var7.length;

        for (spaceLength = 0; spaceLength < toCompensate; ++spaceLength) {
            c = var7[spaceLength];
            if (c == 167) {
                previousCode = true;
            } else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            } else {
                dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                ++messagePxSize;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        toCompensate = 154 - halvedMessageSize;
        spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;

        StringBuilder sb;
        for (sb = new StringBuilder(); compensated < toCompensate; compensated += spaceLength) {
            sb.append(" ");
        }

        return sb + message.replace("<center>", "        ").replace("</center>", "         ");

    }

    public static String color(final String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}