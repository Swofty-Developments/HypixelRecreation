package io.github.term4.polyp;

import io.github.term4.polyp.mechanics.attack.AttackConfig;
import io.github.term4.polyp.mechanics.attribute.AttributeConfig;
import io.github.term4.polyp.mechanics.blocking.BlockingConfig;
import io.github.term4.polyp.mechanics.cooldown.CooldownConfig;
import io.github.term4.polyp.mechanics.consumable.ConsumableConfig;
import io.github.term4.polyp.mechanics.damage.DamageConfig;
import io.github.term4.polyp.mechanics.death.DeathConfig;
import io.github.term4.polyp.mechanics.durability.DurabilityConfig;
import io.github.term4.polyp.fx.FxRegistry;
import io.github.term4.polyp.mechanics.explosion.ExplosionConfig;
import io.github.term4.polyp.mechanics.hunger.HungerConfig;
import io.github.term4.polyp.mechanics.knockback.KnockbackConfig;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfig;
import io.github.term4.polyp.item.ItemRegistry;
import io.github.term4.polyp.platform.compatibility.CompatConfig;
import io.github.term4.polyp.platform.fixes.FixesConfig;
import io.github.term4.polyp.platform.player.PlayerConfig;
import io.github.term4.polyp.tracking.motion.VelocityRule;
import io.github.term4.polyp.entity.DroppedItemEntity;
import io.github.term4.polyp.mechanics.item.ItemDamageConfig;
import io.github.term4.polyp.vri.VriConfig;
import io.github.term4.polyp.util.tick.TickScalingConfig;

/** Catalog of the built-in {@link MechanicsProfile} config keys. */
public final class MechanicsKeys {

    private MechanicsKeys() {}

    public static final ConfigKey<AttackConfig> ATTACK = ConfigKey.of("polyp:attack", AttackConfig.class);
    public static final ConfigKey<DamageConfig> DAMAGE = ConfigKey.of("polyp:damage", DamageConfig.class);
    public static final ConfigKey<DeathConfig> DEATH = ConfigKey.of("polyp:death", DeathConfig.class);
    public static final ConfigKey<KnockbackConfig> KNOCKBACK = ConfigKey.of("polyp:knockback", KnockbackConfig.class);
    public static final ConfigKey<PlayerConfig> PLAYER = ConfigKey.of("polyp:player", PlayerConfig.class);
    public static final ConfigKey<VelocityRule> VELOCITY = ConfigKey.of("polyp:velocity", VelocityRule.class);
    public static final ConfigKey<ProjectileConfig> PROJECTILES = ConfigKey.of("polyp:projectiles", ProjectileConfig.class);
    public static final ConfigKey<FixesConfig> FIXES = ConfigKey.of("polyp:fixes", FixesConfig.class);
    public static final ConfigKey<AttributeConfig> ATTRIBUTES = ConfigKey.of("polyp:attributes", AttributeConfig.class);
    public static final ConfigKey<TickScalingConfig> TICK_SCALING = ConfigKey.of("polyp:tick-scaling", TickScalingConfig.class);
    public static final ConfigKey<DurabilityConfig> DURABILITY = ConfigKey.of("polyp:durability", DurabilityConfig.class);
    public static final ConfigKey<HungerConfig> HUNGER = ConfigKey.of("polyp:hunger", HungerConfig.class);
    public static final ConfigKey<ConsumableConfig> CONSUMABLES = ConfigKey.of("polyp:consumables", ConsumableConfig.class);
    public static final ConfigKey<FxRegistry> FX = ConfigKey.of("polyp:fx", FxRegistry.class);
    public static final ConfigKey<BlockingConfig> BLOCKING = ConfigKey.of("polyp:blocking", BlockingConfig.class);
    /** Server-authoritative item-use cooldowns. */
    public static final ConfigKey<CooldownConfig> COOLDOWNS = ConfigKey.of("polyp:item-cooldowns", CooldownConfig.class);
    /** Vanilla behaviors Minestom omits (crack overlay, block drops, item pickup/drop). */
    public static final ConfigKey<VriConfig> VRI = ConfigKey.of("polyp:vri", VriConfig.class);
    /** Dropped-item environment physics: 1.8 sink vs 26.1 float. */
    public static final ConfigKey<DroppedItemEntity.Model> ITEM_PHYSICS = ConfigKey.of("polyp:item-physics", DroppedItemEntity.Model.class);
    /** What destroys a dropped item: explosions, fire, lava, cactus, the void. */
    public static final ConfigKey<ItemDamageConfig> ITEM_DAMAGE = ConfigKey.of("polyp:item-damage", ItemDamageConfig.class);
    public static final ConfigKey<ExplosionConfig> EXPLOSION = ConfigKey.of("polyp:explosion", ExplosionConfig.class);
    public static final ConfigKey<CompatConfig> COMPAT = ConfigKey.of("polyp:compat", CompatConfig.class);
    public static final ConfigKey<ItemRegistry> ITEMS = ConfigKey.of("polyp:items", ItemRegistry.class);
}
