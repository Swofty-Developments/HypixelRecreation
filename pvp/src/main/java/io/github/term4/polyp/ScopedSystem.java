package io.github.term4.polyp;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Base for systems with a scope-resolved config: the install config is the fallback of the
 * {@link MechanicsProfiles} chain under the system's {@link ConfigKey}.
 */
public abstract class ScopedSystem<C> implements MechanicsModule {

    protected final Polyp polyp;
    /** The install-level fallback; prefer {@link #configFor} for per-subject reads. */
    protected final C config;
    private final ConfigKey<C> key;

    protected ScopedSystem(Polyp polyp, ConfigKey<C> key, C config) {
        this.polyp = polyp;
        this.key = key;
        this.config = config;
    }

    /** Effective config for {@code subject}: the scoped profile, else the install config. */
    public final C configFor(@Nullable Entity subject) {
        return polyp.profiles().resolveOr(subject, key, config);
    }

    public final C config() { return config; }

    public final Polyp polyp() { return polyp; }

    public final Services services() { return polyp.services(); }
}
