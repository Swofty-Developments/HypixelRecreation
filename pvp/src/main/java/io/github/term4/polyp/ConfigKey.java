package io.github.term4.polyp;

/**
 * Type-safe identifier for a {@link MechanicsProfile} member. Built-in keys live in {@link MechanicsKeys}; a module
 * declares its own as a static constant. Keys are equal by {@link #id()}, so each id must be unique (convention
 * {@code "namespace:name"}) - two modules claiming one id with different types fail fast at key creation.
 */
public final class ConfigKey<C> extends TypedKey<C> {

    private static final Namespace NAMESPACE = new Namespace();

    private ConfigKey(String id, Class<C> type) { super(id, type, NAMESPACE); }

    public static <C> ConfigKey<C> of(String id, Class<C> type) {
        return new ConfigKey<>(id, type);
    }
}
