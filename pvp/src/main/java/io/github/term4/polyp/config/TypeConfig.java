package io.github.term4.polyp.config;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/** Base for per-type configs (damage types, projectile types): a {@link Config} keyed by the registry type it configures. */
public abstract class TypeConfig<CTX, SELF extends TypeConfig<CTX, SELF>> extends Config<CTX, SELF> {

    private final Key key;

    protected TypeConfig(Key key, @Nullable Function<CTX, SELF> subConfig) {
        super(subConfig);
        this.key = key;
    }

    public final Key key() { return key; }
}
