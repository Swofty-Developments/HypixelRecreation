package io.github.term4.polyp.api.event.fx;

import io.github.term4.polyp.api.event.CancellableMechanicsEvent;
import io.github.term4.polyp.Services;
import io.github.term4.polyp.fx.FxHandler;
import io.github.term4.polyp.fx.FxContext;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

/**
 * Fired before an {@link FxHandler} plays, per {@link #key()}. Cancel to suppress it, or {@link #fx(FxHandler) swap} it.
 * The resolved fx is the scope's override, else the built-in default.
 */
public final class FxEvent extends CancellableMechanicsEvent<FxContext> {

    private final Key key;
    private @NotNull FxHandler fx;

    public FxEvent(@NotNull Key key, @NotNull FxContext context, @NotNull FxHandler fx, Services services) {
        super(context, services);
        this.key = key;
        this.fx = fx;
    }

    /** A built-in {@code Fx} key or a custom one. */
    public @NotNull Key key() { return key; }

    /** Position / source / target. */
    public @NotNull FxContext context() { return snapshot(); }

    /** The fx that will play - the resolved override or built-in. */
    public @NotNull FxHandler fx() { return fx; }
    public void fx(@NotNull FxHandler fx) { this.fx = fx; }
}
