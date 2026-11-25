package io.github.term4.polyp.mechanics.damage;

import io.github.term4.polyp.mechanics.attribute.defense.Bypass;
import io.github.term4.polyp.mechanics.damage.types.DamageType;
import io.github.term4.polyp.mechanics.damage.types.DamageTypeConfig;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable input describing a single instance of damage to apply. Mirrors the knockback
 * snapshot inputs but for damage.
 *
 * @param source  the entity responsible (attacker, projectile), if any
 * @param item    the item involved (melee weapon), if any
 * @param detail  type-specific payload attached by the producer (e.g. the fall distance), if any
 * @param amount  optional amount override; {@code null} uses {@link DamageTypeConfig#baseAmount(io.github.term4.polyp.mechanics.damage.DamageConfigResolver.DamageContext)}
 * @param bypass  targeted attribute/effect/enchant skips the attacking item/attack contributes (e.g. a god-killer
 *                ignoring resistance); merged with the damage type's broad bypass
 */
public record DamageSnapshot(Entity target, DamageType type, @Nullable Entity source,
                             @Nullable Point point, @Nullable ItemStack item, @Nullable Object detail,
                             @Nullable Float amount, @Nullable DamageConfig config, @Nullable Bypass bypass) {

    public static DamageSnapshot of(Entity target, DamageType type) {
        return new DamageSnapshot(target, type, null, null, null, null, null, null, null);
    }

    public DamageSnapshot withTarget(Entity e) { return new DamageSnapshot(e, type, source, point, item, detail, amount, config, bypass); }
    public DamageSnapshot withType(DamageType t) { return new DamageSnapshot(target, t, source, point, item, detail, amount, config, bypass); }
    public DamageSnapshot withSource(@Nullable Entity e) { return new DamageSnapshot(target, type, e, point, item, detail, amount, config, bypass); }
    public DamageSnapshot withPoint(@Nullable Point p) { return new DamageSnapshot(target, type, source, p, item, detail, amount, config, bypass); }
    public DamageSnapshot withItem(@Nullable ItemStack i) { return new DamageSnapshot(target, type, source, point, i, detail, amount, config, bypass); }
    public DamageSnapshot withDetail(@Nullable Object d) { return new DamageSnapshot(target, type, source, point, item, d, amount, config, bypass); }
    public DamageSnapshot withAmount(@Nullable Float a) { return new DamageSnapshot(target, type, source, point, item, detail, a, config, bypass); }
    public DamageSnapshot withConfig(@Nullable DamageConfig c) { return new DamageSnapshot(target, type, source, point, item, detail, amount, c, bypass); }
    public DamageSnapshot withBypass(@Nullable Bypass b) { return new DamageSnapshot(target, type, source, point, item, detail, amount, config, b); }

    public Builder toBuilder() { return new Builder(target, type, source, point, item, detail, amount, config, bypass); }

    public static final class Builder {
        private Entity target;
        private DamageType type;
        private @Nullable Entity source;
        private @Nullable Point point;
        private @Nullable ItemStack item;
        private @Nullable Object detail;
        private @Nullable Float amount;
        private @Nullable DamageConfig config;
        private @Nullable Bypass bypass;

        Builder(Entity target, DamageType type, @Nullable Entity source, @Nullable Point point,
                @Nullable ItemStack item, @Nullable Object detail,
                @Nullable Float amount, @Nullable DamageConfig config, @Nullable Bypass bypass) {
            this.target = target;
            this.type = type;
            this.source = source;
            this.point = point;
            this.item = item;
            this.detail = detail;
            this.amount = amount;
            this.config = config;
            this.bypass = bypass;
        }

        public Builder target(Entity e) { this.target = e; return this; }
        public Builder type(DamageType t) { this.type = t; return this; }
        public Builder source(@Nullable Entity e) { this.source = e; return this; }
        public Builder point(@Nullable Point p) { this.point = p; return this; }
        public Builder item(@Nullable ItemStack i) { this.item = i; return this; }
        public Builder detail(@Nullable Object d) { this.detail = d; return this; }
        public Builder amount(@Nullable Float a) { this.amount = a; return this; }
        public Builder config(@Nullable DamageConfig c) { this.config = c; return this; }
        public Builder bypass(@Nullable Bypass b) { this.bypass = b; return this; }

        public DamageSnapshot build() {
            return new DamageSnapshot(target, type, source, point, item, detail, amount, config, bypass);
        }
    }
}
