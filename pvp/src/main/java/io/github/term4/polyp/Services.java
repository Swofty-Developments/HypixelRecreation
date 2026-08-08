package io.github.term4.polyp;

import io.github.term4.polyp.mechanics.attack.AttackSystem;
import io.github.term4.polyp.mechanics.attribute.AttributeSystem;
import io.github.term4.polyp.mechanics.blocking.BlockingSystem;
import io.github.term4.polyp.mechanics.consumable.ConsumableSystem;
import io.github.term4.polyp.mechanics.damage.DamageSystem;
import io.github.term4.polyp.mechanics.item.ItemDamageSystem;
import io.github.term4.polyp.mechanics.durability.DurabilitySystem;
import io.github.term4.polyp.mechanics.explosion.ExplosionSystem;
import io.github.term4.polyp.mechanics.hunger.HungerSystem;
import io.github.term4.polyp.platform.fixes.FixesSystem;
import io.github.term4.polyp.mechanics.knockback.KnockbackSystem;
import io.github.term4.polyp.mechanics.projectile.ProjectileSystem;
import io.github.term4.polyp.tracking.motion.MotionTracker;
import io.github.term4.polyp.tracking.SprintTracker;
import org.jetbrains.annotations.Nullable;

/** Access to the systems registered on Polyp. */
public record Services(Polyp polyp) {

    // live registry lookups: install order doesn't matter, null = not installed
    public @Nullable SprintTracker sprintTracker() { return polyp.sprintTracker(); }
    public @Nullable MotionTracker motionTracker() { return polyp.motionTracker(); }
    public @Nullable AttackSystem attack() { return polyp.module(AttackSystem.class); }
    public @Nullable KnockbackSystem knockback() { return polyp.module(KnockbackSystem.class); }
    public @Nullable DamageSystem damage() { return polyp.module(DamageSystem.class); }
    public @Nullable ItemDamageSystem items() { return polyp.module(ItemDamageSystem.class); }
    public @Nullable ProjectileSystem projectiles() { return polyp.module(ProjectileSystem.class); }
    public @Nullable ExplosionSystem explosion() { return polyp.module(ExplosionSystem.class); }
    public @Nullable FixesSystem fixes() { return polyp.module(FixesSystem.class); }
    public @Nullable AttributeSystem attributes() { return polyp.module(AttributeSystem.class); }
    public @Nullable DurabilitySystem durability() { return polyp.module(DurabilitySystem.class); }
    public @Nullable HungerSystem hunger() { return polyp.module(HungerSystem.class); }
    public @Nullable ConsumableSystem consumables() { return polyp.module(ConsumableSystem.class); }
    public @Nullable BlockingSystem blocking() { return polyp.module(BlockingSystem.class); }
    public MechanicsProfiles profiles() { return polyp.profiles(); }

}
