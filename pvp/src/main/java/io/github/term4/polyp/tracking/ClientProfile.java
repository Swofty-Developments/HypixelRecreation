package io.github.term4.polyp.tracking;

import io.github.term4.polyp.platform.compatibility.AnimatiumFeature;
import io.github.term4.polyp.platform.player.OptimizedPlayer;
import net.minestom.server.entity.Player;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player view of client-side info: protocol version, Animatium status, and a typed store keyed by
 * {@link ClientKey}. Obtained from {@code polyp.client(player)}; a fresh view each call, state lives on the player in a
 * transient tag and drops on disconnect.
 */
public final class ClientProfile {

    private static final Tag<Map<ClientKey<?>, Object>> DATA = Tag.Transient("polyp:client-data");

    private final Player player;
    private final ClientInfoTracker tracker;

    ClientProfile(Player player, ClientInfoTracker tracker) {
        this.player = player;
        this.tracker = tracker;
    }

    public Player player() { return player; }

    /** {@code 47} = 1.8, or {@link ClientVersion#UNKNOWN_PROTOCOL} until the proxy/Via handshake resolves it. */
    public int protocol() { return tracker.getProtocol(player); }

    public boolean isAnimatium() {
        return player instanceof OptimizedPlayer op && op.compat().isAnimatiumClient();
    }

    /** Features this client applies natively (empty for non-Animatium clients). */
    public Set<AnimatiumFeature> animatiumFeatures() {
        return player instanceof OptimizedPlayer op ? op.compat().nativeFeatures() : Set.of();
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T get(ClientKey<T> key) {
        Map<ClientKey<?>, Object> data = player.getTag(DATA);
        return data != null ? (T) data.get(key) : null;
    }

    /** {@code null} removes. */
    public <T> void set(ClientKey<T> key, @Nullable T value) {
        Map<ClientKey<?>, Object> data = player.getTag(DATA);
        if (data == null) {
            synchronized (player) {
                data = player.getTag(DATA);
                if (data == null) {
                    data = new ConcurrentHashMap<>();
                    player.setTag(DATA, data);
                }
            }
        }
        if (value == null) data.remove(key); else data.put(key, value);
    }
}
