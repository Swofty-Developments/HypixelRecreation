package io.github.term4.polyp.mechanics.attribute.catalog.effect;

import io.github.term4.polyp.mechanics.attribute.source.Behavior;
import io.github.term4.polyp.mechanics.attribute.source.EntitySource;
import io.github.term4.polyp.mechanics.attribute.source.Source;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;

/** Invisibility - Minestom only stores the effect, so the flag is toggled here. */
public final class Invisibility {

    public static final Key KEY = Key.key("minecraft:invisibility");

    private Invisibility() {}

    private static final Behavior HIDE = new Behavior() {
        @Override public void onApply(Entity entity, int level) { entity.setInvisible(true); }
        @Override public void onRemove(Entity entity, int level) { entity.setInvisible(false); }
    };

    public static final Source INSTANCE = new EntitySource(KEY) {
        @Override public Behavior behavior() { return HIDE; }
    };
}
