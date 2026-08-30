package io.github.term4.polyp.tracking;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/** Stateless protocol-version logic behind {@link ClientInfoTracker} (which owns the per-player store). */
public final class ClientVersion {

    public static final int UNKNOWN_PROTOCOL = -1;
    /** 1.8.x = 47. */
    public static final int LEGACY_PROTOCOL_MAX = 47;

    private ClientVersion() {}

    /** Parses {@code {"version": <protocol>, ...}} from the ViaVersion proxy-details JSON. */
    public static int parse(String json) {
        if (json == null || json.isEmpty()) return UNKNOWN_PROTOCOL;
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            var version = obj.get("version");
            return (version != null && version.isJsonPrimitive()) ? version.getAsInt() : UNKNOWN_PROTOCOL;
        } catch (Exception ignored) {
            return UNKNOWN_PROTOCOL;
        }
    }

    /** {@link #UNKNOWN_PROTOCOL} counts as not legacy. */
    public static boolean isLegacy(int protocol) {
        return protocol != UNKNOWN_PROTOCOL && protocol <= LEGACY_PROTOCOL_MAX;
    }
}
