package io.github.term4.polyp.mechanics.attribute.catalog.effect;

import io.github.term4.polyp.mechanics.attribute.source.Behavior;
import io.github.term4.polyp.mechanics.attribute.source.EntitySource;
import io.github.term4.polyp.mechanics.attribute.source.Source;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;

/**
 * Absorption - Player-only; Minestom's {@code damage()} consumes the buffer before health. {@link #LEGACY} is 1.8's
 * additive {@code +4 × level}, removed on expiry; {@link #MODERN} is 26's {@code max(cur, 4×level)}, which persists past
 * the effect.
 */
public final class Absorption {

    public static final Key KEY = Key.key("minecraft:absorption");

    private Absorption() {}

    public static final Source LEGACY = new EntitySource(KEY) {
        @Override public Behavior behavior() {
            return new Behavior() {
                @Override public void onApply(Entity entity, int level) {
                    if (entity instanceof Player p) p.setAdditionalHearts(p.getAdditionalHearts() + 4f * level);
                }
                @Override public void onRemove(Entity entity, int level) {
                    if (entity instanceof Player p) p.setAdditionalHearts(Math.max(0f, p.getAdditionalHearts() - 4f * level));
                }
            };
        }
    };

    public static final Source MODERN = new EntitySource(KEY) {
        @Override public Behavior behavior() {
            return new Behavior() {
                @Override public void onApply(Entity entity, int level) {
                    if (entity instanceof Player p) p.setAdditionalHearts(Math.max(p.getAdditionalHearts(), 4f * level));
                }
            };
        }
    };
}
