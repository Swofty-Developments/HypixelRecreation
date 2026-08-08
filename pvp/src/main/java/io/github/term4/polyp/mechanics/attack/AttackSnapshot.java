package io.github.term4.polyp.mechanics.attack;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * A detected hit. Attacks are melee by definition - the processing ruleset stamps that onto the
 * damage/knockback snapshots it produces.
 *
 * @param config the attack config, or {@code null} to resolve the scope chain (profile -> install config)
 * @param aim    for a server-filled swing hit ({@link FakeHits}), the attacker eye whose look intersected the target:
 *               knockback follows it instead of the attacker's live look, and hit effects route to the fake-hit
 *               endpoints. {@code null} = the client's own hit.
 */
public record AttackSnapshot(Entity attacker, @Nullable Entity target, @Nullable AttackConfig config, @Nullable Pos aim) {

    public AttackSnapshot(Entity attacker, @Nullable Entity target, @Nullable AttackConfig config) {
        this(attacker, target, config, null);
    }

    public AttackSnapshot withAttacker(Entity e) { return new AttackSnapshot(e, target, config, aim); }
    public AttackSnapshot withTarget(Entity e) { return new AttackSnapshot(attacker, e, config, aim); }
    public AttackSnapshot withConfig(AttackConfig c) { return new AttackSnapshot(attacker, target, c, aim); }
    public AttackSnapshot withAim(Pos p) { return new AttackSnapshot(attacker, target, config, p); }

    public Builder toBuilder() { return new Builder(attacker, target, config, aim); }

    public static final class Builder {
        private Entity attacker;
        private @Nullable Entity target;
        private @Nullable AttackConfig config;
        private @Nullable Pos aim;

        Builder(Entity a, @Nullable Entity t, @Nullable AttackConfig cfg, @Nullable Pos aim) {
            attacker = a; target = t; config = cfg; this.aim = aim;
        }

        public Builder attacker(Entity e) { attacker = e; return this; }
        public Builder target(Entity e) { target = e; return this; }
        public Builder config(AttackConfig cfg) { config = cfg; return this; }
        public Builder aim(Pos p) { aim = p; return this; }

        public AttackSnapshot build() {
            return new AttackSnapshot(attacker, target, config, aim);
        }
    }
}
