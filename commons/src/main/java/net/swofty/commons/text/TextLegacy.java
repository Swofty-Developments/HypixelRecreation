package net.swofty.commons.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

final class TextLegacy {

    private TextLegacy() {
    }

    static Component parse(String legacy) {
        List<Component> parts = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        Set<TextDecoration> decorations = EnumSet.noneOf(TextDecoration.class);
        TextColor colour = null;
        int index = 0;
        while (index < legacy.length()) {
            char current = legacy.charAt(index);
            if ((current == '§' || current == '&') && index + 1 < legacy.length()) {
                char code = Character.toLowerCase(legacy.charAt(index + 1));
                TextColor hex = current == '§' && code == 'x' ? readHex(legacy, index) : null;
                if (hex != null) {
                    flush(parts, buffer, colour, decorations);
                    colour = hex;
                    decorations.clear();
                    index += 14;
                    continue;
                }
                if (TextTags.isLegacyColorCode(code)) {
                    flush(parts, buffer, colour, decorations);
                    colour = TextTags.legacyColor(code);
                    decorations.clear();
                    index += 2;
                    continue;
                }
                TextDecoration decoration = TextTags.legacyDecoration(code);
                if (decoration != null) {
                    flush(parts, buffer, colour, decorations);
                    decorations.add(decoration);
                    index += 2;
                    continue;
                }
                if (code == 'r') {
                    flush(parts, buffer, colour, decorations);
                    colour = null;
                    decorations.clear();
                    index += 2;
                    continue;
                }
            }
            buffer.append(current);
            index++;
        }
        flush(parts, buffer, colour, decorations);
        if (parts.isEmpty()) {
            return Component.empty();
        }
        return parts.size() == 1 ? parts.getFirst() : Component.empty().children(parts);
    }

    private static TextColor readHex(String legacy, int index) {
        if (index + 13 >= legacy.length()) {
            return null;
        }
        StringBuilder hex = new StringBuilder("#");
        for (int i = 0; i < 6; i++) {
            int at = index + 2 + i * 2;
            if (legacy.charAt(at) != '§') {
                return null;
            }
            char digit = legacy.charAt(at + 1);
            if (Character.digit(digit, 16) < 0) {
                return null;
            }
            hex.append(digit);
        }
        return TextColor.fromHexString(hex.toString());
    }

    private static void flush(List<Component> parts, StringBuilder buffer, TextColor colour,
                              Set<TextDecoration> decorations) {
        if (buffer.isEmpty()) {
            return;
        }
        Style.Builder style = Style.style();
        if (colour != null) {
            style.color(colour);
        }
        for (TextDecoration decoration : decorations) {
            style.decoration(decoration, true);
        }
        parts.add(Component.text(buffer.toString(), style.build()));
        buffer.setLength(0);
    }
}
