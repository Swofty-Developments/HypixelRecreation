package net.swofty.commons.text;

import lombok.Getter;

@Getter
public final class TextParseException extends RuntimeException {

    private final String markup;
    private final int index;

    public TextParseException(String markup, int index, String reason) {
        super(describe(markup, index, reason));
        this.markup = markup;
        this.index = index;
    }

    private static String describe(String markup, int index, String reason) {
        if (markup == null) {
            return reason;
        }
        int safeIndex = Math.clamp(index, 0, markup.length());
        int from = Math.max(0, safeIndex - 12);
        int to = Math.min(markup.length(), safeIndex + 24);
        String snippet = (from > 0 ? "..." : "") + markup.substring(from, to) + (to < markup.length() ? "..." : "");
        return reason + " (at index " + safeIndex + " of \"" + snippet + "\")";
    }
}
