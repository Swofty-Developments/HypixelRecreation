package net.swofty.commons.text;

import net.swofty.commons.StringUtility;

import java.math.BigDecimal;

final class TextFormats {

    enum Kind {
        COMMA,
        SHORT,
        ROMAN,
        TIME,
        SIGNED,
        PERCENT,
        DECIMAL
    }

    record Spec(Kind kind, int digits) {
    }

    private TextFormats() {
    }

    static Spec parse(String raw, String markup, int index) {
        if (raw.isEmpty()) {
            throw new TextParseException(markup, index, "Empty format specifier");
        }
        return switch (raw) {
            case "," -> new Spec(Kind.COMMA, 0);
            case "short" -> new Spec(Kind.SHORT, 0);
            case "roman" -> new Spec(Kind.ROMAN, 0);
            case "time" -> new Spec(Kind.TIME, 0);
            case "+" -> new Spec(Kind.SIGNED, 0);
            case "%" -> new Spec(Kind.PERCENT, 0);
            default -> parseDecimal(raw, markup, index);
        };
    }

    private static Spec parseDecimal(String raw, String markup, int index) {
        if (raw.charAt(0) != '.') {
            throw new TextParseException(markup, index, "Unknown format specifier '" + raw + "'");
        }
        int digits;
        try {
            digits = Integer.parseInt(raw.substring(1));
        } catch (NumberFormatException exception) {
            throw new TextParseException(markup, index, "Unknown format specifier '" + raw + "'");
        }
        if (digits < 1) {
            throw new TextParseException(markup, index, "Decimal format needs at least one digit, got '" + raw + "'");
        }
        return new Spec(Kind.DECIMAL, digits);
    }

    static String apply(Spec spec, Object value) {
        return switch (spec.kind()) {
            case COMMA -> comma(value);
            case SHORT -> StringUtility.shortenNumber(asDouble(value, "short"));
            case ROMAN -> StringUtility.getAsRomanNumeral((int) asLong(value, "roman"));
            case TIME -> StringUtility.formatTimeLeft(asLong(value, "time"));
            case SIGNED -> signed(asLong(value, "+"));
            case PERCENT -> Math.round(asDouble(value, "%") * 100.0D) + "%";
            case DECIMAL -> StringUtility.decimalify(asDouble(value, "." + spec.digits()), spec.digits());
        };
    }

    private static String signed(long value) {
        return value >= 0 ? "+" + value : String.valueOf(value);
    }

    private static String comma(Object value) {
        if (value instanceof Double || value instanceof Float || value instanceof BigDecimal) {
            return StringUtility.commaify(((Number) value).doubleValue());
        }
        if (value instanceof Number number) {
            return StringUtility.commaify(number.longValue());
        }
        String raw = String.valueOf(value).trim();
        try {
            return StringUtility.commaify(Long.parseLong(raw));
        } catch (NumberFormatException ignored) {
            return StringUtility.commaify(asDouble(value, ","));
        }
    }

    private static double asDouble(Object value, String specifier) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value).trim());
        } catch (NumberFormatException exception) {
            throw new TextParseException(String.valueOf(value), 0,
                    "Format specifier '" + specifier + "' needs a numeric argument");
        }
    }

    private static long asLong(Object value, String specifier) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException exception) {
            return (long) asDouble(value, specifier);
        }
    }
}
