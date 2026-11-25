package io.github.term4.polyp.fx;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * An immutable registry of {@link FxHandler}s by {@link Key} - the {@code MechanicsKeys.FX} profile value, resolved
 * per scope. A preset provides one ({@link Fx#vanilla18()}); {@link #register} returns a NEW registry so overrides
 * compose without mutation, and {@link FxHandler#NONE} silences one key.
 */
public final class FxRegistry {

    private static final FxRegistry EMPTY = new FxRegistry(Map.of());

    private final Map<Key, FxHandler> handlers;

    private FxRegistry(Map<Key, FxHandler> handlers) { this.handlers = Map.copyOf(handlers); }

    /** A registry with no handlers - nothing plays until keys are {@link #register registered}. */
    public static @NotNull FxRegistry empty() { return EMPTY; }

    /** The handler registered for {@code key}, or {@code null}. */
    public @Nullable FxHandler get(@NotNull Key key) { return handlers.get(key); }

    /** A copy of this registry with {@code key} mapped to {@code fx} (added or replaced). */
    public @NotNull FxRegistry register(@NotNull Key key, @NotNull FxHandler fx) {
        Map<Key, FxHandler> map = new HashMap<>(handlers);
        map.put(key, fx);
        return new FxRegistry(map);
    }

    /** A copy of this registry with every entry of {@code overrides} laid on top ({@code overrides} wins). */
    public @NotNull FxRegistry withAll(@NotNull FxRegistry overrides) {
        Map<Key, FxHandler> map = new HashMap<>(handlers);
        map.putAll(overrides.handlers);
        return new FxRegistry(map);
    }

    public @NotNull Map<Key, FxHandler> handlers() { return handlers; }
}
