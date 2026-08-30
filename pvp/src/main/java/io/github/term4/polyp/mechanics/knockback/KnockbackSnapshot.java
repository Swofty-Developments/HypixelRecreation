package io.github.term4.polyp.mechanics.knockback;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * A knockback instance to apply.
 *
 * @param melee          gates melee-only logic (sprint "extra" KB, melee components)
 * @param source         the entity dealing the knockback, or {@code null} (direction from origin/direction)
 * @param origin         explicit knockback origin, or {@code null}
 * @param direction      explicit knockback direction override, or {@code null}
 * @param config         the knockback config, or {@code null} to resolve the scope chain (profile -> install config)
 * @param extraKnockback explicit extra-knockback <em>levels</em> (vanilla's {@code i}): the melee Knockback enchant
 *                       (set by the attack ruleset, which has the weapon) or a projectile's Punch; a melee sprint adds a
 *                       further {@code +1} in the calculator. Each level scales the config's {@code extra}* knobs. {@code 0} = none.
 */
public record KnockbackSnapshot(Entity target, boolean melee, @Nullable Entity source,
                                @Nullable Point origin, @Nullable Vec direction, @Nullable KnockbackConfig config,
                                int extraKnockback) {

    /** Convenience: a snapshot with no explicit extra-knockback ({@code extraKnockback = 0}). */
    public KnockbackSnapshot(Entity target, boolean melee, @Nullable Entity source,
                             @Nullable Point origin, @Nullable Vec direction, @Nullable KnockbackConfig config) {
        this(target, melee, source, origin, direction, config, 0);
    }

    public KnockbackSnapshot withTarget(Entity e) { return new KnockbackSnapshot(e, melee, source, origin, direction, config, extraKnockback); }
    public KnockbackSnapshot withMelee(boolean m) { return new KnockbackSnapshot(target, m, source, origin, direction, config, extraKnockback); }
    public KnockbackSnapshot withSource(Entity e) { return new KnockbackSnapshot(target, melee, e, origin, direction, config, extraKnockback); }
    public KnockbackSnapshot withOrigin(Point p) { return new KnockbackSnapshot(target, melee, source, p, direction, config, extraKnockback); }
    public KnockbackSnapshot withDirection(Vec v) { return new KnockbackSnapshot(target, melee, source, origin, v, config, extraKnockback); }
    public KnockbackSnapshot withConfig(KnockbackConfig c) { return new KnockbackSnapshot(target, melee, source, origin, direction, c, extraKnockback); }
    public KnockbackSnapshot withExtraKnockback(int level) { return new KnockbackSnapshot(target, melee, source, origin, direction, config, level); }

    public Builder toBuilder() { return new Builder(target, melee, source, origin, direction, config, extraKnockback); }

    public static final class Builder {
        private Entity target;
        private boolean melee;
        private Entity source;
        private Point origin;
        private Vec direction;
        private KnockbackConfig config;
        private int extraKnockback;

        Builder(Entity t, boolean m, Entity s, Point o, Vec d, KnockbackConfig cfg, int extra) {
            target = t; melee = m; source = s; origin = o; direction = d; config = cfg; extraKnockback = extra;
        }

        public Builder target(Entity e) { this.target = e; return this; }
        public Builder melee(boolean m) { this.melee = m; return this; }
        public Builder source(Entity e) { this.source = e; return this; }
        public Builder origin(Point p) { this.origin = p; return this; }
        public Builder direction(Vec v) { this.direction = v; return this; }
        public Builder config(KnockbackConfig c) { this.config = c; return this; }
        public Builder extraKnockback(int level) { this.extraKnockback = level; return this; }

        public KnockbackSnapshot build() {
            return new KnockbackSnapshot(target, melee, source, origin, direction, config, extraKnockback);
        }
    }
}
