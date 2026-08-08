package io.github.term4.polyp.mechanics.attribute.catalog.effect;

import io.github.term4.polyp.mechanics.attribute.Attribute;
import io.github.term4.polyp.mechanics.attribute.source.EntitySource;
import io.github.term4.polyp.mechanics.attribute.source.Source;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.attribute.AttributeOperation;

import java.util.List;

/**
 * Jump Boost. {@link #MODERN} is 26's {@code +1 × level} safe-fall-distance. The jump-velocity part ({@code +0.1/level},
 * both versions) isn't an attribute - {@link io.github.term4.polyp.tracking.motion.MotionTracker#jumpSeed}
 * seeds it off the live effect, so there is no LEGACY source.
 */
public final class JumpBoost {

    public static final Key KEY = Key.key("minecraft:jump_boost");

    private JumpBoost() {}

    public static final Source MODERN = new EntitySource(KEY) {
        @Override public List<Mod> modifiers(int level) {
            return level <= 0 ? List.of()
                    : List.of(new Mod(Attribute.SAFE_FALL_DISTANCE, AttributeOperation.ADD_VALUE, 1.0 * level));
        }
    };
}
