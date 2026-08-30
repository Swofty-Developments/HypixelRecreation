package io.github.term4.polyp.tracking;

import io.github.term4.polyp.TypedKey;

/**
 * Type-safe identifier for a custom value on a player's {@link ClientProfile}. Keys are equal by {@link #id()}, so a
 * re-created key matches; ids must be unique ({@code "namespace:name"}) and a same-id different-type collision throws.
 */
public final class ClientKey<T> extends TypedKey<T> {

    private static final Namespace NAMESPACE = new Namespace();

    private ClientKey(String id, Class<T> type) { super(id, type, NAMESPACE); }

    public static <T> ClientKey<T> of(String id, Class<T> type) {
        return new ClientKey<>(id, type);
    }
}
