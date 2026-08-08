package io.github.term4.polyp.mechanics.explosion;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsModule;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.Services;
import io.github.term4.polyp.api.event.explosion.ExplosionEvent;
import io.github.term4.polyp.fx.Fx;
import io.github.term4.polyp.fx.FxContext;
import io.github.term4.polyp.mechanics.attribute.defense.Bypass;
import io.github.term4.polyp.mechanics.damage.DamageSnapshot;
import io.github.term4.polyp.mechanics.damage.DamageSystem;
import io.github.term4.polyp.mechanics.item.ItemDamageSystem;
import io.github.term4.polyp.mechanics.damage.types.explosion.ExplosionDamage;
import io.github.term4.polyp.mechanics.explosion.ExplosionConfigResolver.ExplosionContext;
import io.github.term4.polyp.mechanics.explosion.ExplosionConfigResolver.ResolvedExplosionConfig;
import io.github.term4.polyp.mechanics.knockback.KnockbackConfig;
import io.github.term4.polyp.mechanics.knockback.KnockbackSnapshot;
import io.github.term4.polyp.mechanics.knockback.KnockbackSystem;
import io.github.term4.polyp.presets.vanilla18.Knockback;
import io.github.term4.polyp.world.MechanicsWorld;
import io.github.term4.polyp.world.WorldPolicy;
import io.github.term4.polyp.tracking.motion.VelocityContext;
import io.github.term4.polyp.tracking.motion.VelocityRule;
import io.github.term4.polyp.util.Directions;
import net.kyori.adventure.key.Key;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.Explosion;
import net.minestom.server.instance.ExplosionSupplier;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ExplosionPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.WeightedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Explosion system: entity selection + exposure raytrace + the vanilla falloff curve, fired through {@link ExplosionEvent}.
 * Knockback rides the {@link KnockbackSystem}, damage the {@link DamageSystem}; block breaking is a listener's job
 * (off {@link ExplosionEvent#center()} + power).
 */
public final class ExplosionSystem implements MechanicsModule {

    public static final Key KEY = Key.key("polyp:explosion");

    /** Radius used when neither the call nor the config supplies one (vanilla TNT). */
    public static final float DEFAULT_POWER = 4.0f;

    private final @Nullable ExplosionConfig config;
    private final Services services;
    private final EventNode<@NotNull Event> node;

    public ExplosionSystem(Polyp polyp, @Nullable ExplosionConfig config) {
        this.config = config;
        this.services = polyp.services();
        this.node = EventNode.all("polyp:explosion");
    }

    /** Per-player explosion: every player in the world gets the visual, those in range their own falloff knockback. */
    public void explode(@NotNull Instance instance, @NotNull Point center, float power) {
        explode(instance, center, power, null);
    }

    public void explode(@NotNull Instance instance, @NotNull Point center, float power, @Nullable Entity source) {
        explode(instance, center, power, source, null);
    }

    public void explode(@NotNull Instance instance, @NotNull Point center, float power, @Nullable Entity source, @Nullable Entity directHit) {
        explode(MechanicsWorld.of(instance), center, power, source, directHit);
    }

    /** {@code directHit} = the entity the projectile struck (or {@code null}); only it is subject to the {@link ExplosionConfig#knockbackImpactFloor knockback impact gate}. */
    public void explode(@NotNull MechanicsWorld world, @NotNull Point center, float power, @Nullable Entity source, @Nullable Entity directHit) {
        detonate(world, center, power, source, directHit, resolve(world, center, source, (double) power));
    }

    /** Per-player explosion using the configured (or default) radius. */
    public void explode(@NotNull Instance instance, @NotNull Point center, @Nullable Entity source) {
        MechanicsWorld world = MechanicsWorld.of(instance);
        ResolvedExplosionConfig resolved = resolve(world, center, source);
        float power = resolved.power() != null ? resolved.power().floatValue() : DEFAULT_POWER;
        detonate(world, center, power, source, null, resolved);
    }

    private void detonate(MechanicsWorld world, Point center, float power,
                          @Nullable Entity source, @Nullable Entity directHit, ResolvedExplosionConfig resolved) {
        ExplosionEvent event = computeAndFire(world, center, power, source, resolved);
        if (event == null) return;
        applyEffects(world, center, power, source, directHit, event.targets(), resolved);
        // 1.8 gates hugeexplosion on strength >= 2 (Explosion.doExplosionB), but the field is gone from the 1.21.2+
        // packet and ViaBackwards hardcodes 0 downgrading it, so the client can only ever self-pick the small one
        if (power >= 2.0f) Fx.play(services, Fx.EXPLOSION_EMITTER, FxContext.at(world, center, source));
        // AFTER the damage pass, per vanilla (ServerExplosion.explode: select -> hurtEntities -> interactWithBlocks)
        if (resolved.blockBreaking() != null && !event.blocks().isEmpty()) {
            List<Point> broken = ExplosionBlocks.destroy(world, event.blocks(), power, resolved.blockBreaking());
            // BROKEN (Hypixel) lights only vacated cells; SELECTED (vanilla) may light any cell the blast reached
            if (resolved.fire()) ExplosionBlocks.placeFire(world,
                    resolved.fireScope() == ExplosionConfig.FireScope.BROKEN ? broken : event.blocks());
        }
    }

    private ExplosionContext context(MechanicsWorld world, Point center, @Nullable Entity source) {
        return ExplosionContext.of(world.instance(), center, source, services);
    }

    /**
     * One grouped explosion (single packet to everyone): cheaper, but the shared {@code knockback} is applied uniformly
     * by every client (pass {@code null} for none). Damage is still per-entity. Use {@link #explode} for falloff knockback.
     */
    public void explodeUniform(@NotNull Instance instance, @NotNull Point center, float power,
                               @Nullable Entity source, @Nullable Vec knockback) {
        MechanicsWorld world = MechanicsWorld.of(instance);
        ResolvedExplosionConfig resolved = resolve(world, center, source, (double) power);
        ExplosionEvent event = computeAndFire(world, center, power, source, resolved);
        if (event == null) return;
        applyDamage(source, center, event.targets(), resolved.damageBypass());
        world.broadcast(packet(center, power, knockback));
        if (power >= 2.0f) Fx.play(services, Fx.EXPLOSION_EMITTER, FxContext.at(world, center, source));
    }

    /** Config: the source's scope chain (player -&gt; world -&gt; instance -&gt; global) over the install config. A
     *  SOURCELESS blast still belongs to a world, so it resolves that chain rather than dropping to the install
     *  config - otherwise a world-scoped preset silently reverted to vanilla (one-shotting its own ground loot). */
    private ResolvedExplosionConfig resolve(MechanicsWorld world, Point center, @Nullable Entity source) {
        return resolve(world, center, source, null);
    }

    private ResolvedExplosionConfig resolve(MechanicsWorld world, Point center, @Nullable Entity source,
                                            @Nullable Double power) {
        ExplosionConfig scoped = source != null
                ? services.profiles().resolve(source, MechanicsKeys.EXPLOSION)
                : services.profiles().resolveWorld(world, MechanicsKeys.EXPLOSION);
        return ExplosionConfigResolver.resolve(scoped != null ? scoped : config,
                ExplosionContext.of(world.instance(), center, source, services, power));
    }

    /** Builds the per-entity result set, fires the event, and returns the (possibly listener-edited) targets, or {@code null} if cancelled. */
    private @Nullable ExplosionEvent computeAndFire(MechanicsWorld world, Point center, float power,
                                                    @Nullable Entity source, ResolvedExplosionConfig resolved) {
        List<ExplosionEvent.Target> targets = computeTargets(world, center, power, source, resolved);
        // selected against INTACT geometry, before any damage: exposure rays must meet the blocks the blast still has
        List<Point> blocks = resolved.blockBreaking() == null ? new ArrayList<>()
                : ExplosionBlocks.select(world, center, power, resolved.blockBreaking(), context(world, center, source));
        ExplosionEvent event = new ExplosionEvent(world, center, power, source, resolved.fire(), targets, blocks);
        EventDispatcher.call(event);
        return event.isCancelled() ? null : event;
    }

    private List<ExplosionEvent.Target> computeTargets(MechanicsWorld world, Point center, float power,
                                                       @Nullable Entity source, ResolvedExplosionConfig resolved) {
        List<ExplosionEvent.Target> targets = new ArrayList<>();
        double doubleRadius = power * 2.0;
        if (doubleRadius <= 0.0) return targets;
        for (Entity entity : world.nearbyEntities(center, doubleRadius + 1.0)) { // coarse query; the distance gate below is authoritative
            // sourceless = the exploding world itself acts
            if (source != null ? !WorldPolicy.canAffect(source, entity) : !MechanicsWorld.of(entity).equals(world)) continue;
            boolean living = entity instanceof LivingEntity;
            boolean kbTarget = isKnockbackTarget(entity, resolved);
            // reach is the DEFAULT rule, not the configured one: narrowing knockbackTargets says "don't push
            // this", not "the blast can't touch it" - such an entity still lands below with a null push
            if (!living && !defaultKnockbackTarget(entity)) continue;
            if (entity == source && !resolved.affectsSource()) continue;
            double distance = entity.getPosition().distance(center);
            if (distance > doubleRadius) continue;
            // default = STANDING eye even when sneaking (Hypixel); pushEye overrides (MineMen = sneak-aware getHeadHeight).
            // Non-living TNT = feet/0 (vanilla), not the 0.15 registry eye.
            double headHeight = !living ? 0.0
                    : resolved.pushEye() != null ? resolved.pushEye().apply(entity)
                    : entity.getEntityType().registry().eyeHeight();
            Point eyeOrigin = entity.getPosition().add(0, headHeight, 0);
            float exposure = switch (resolved.exposure()) {
                case NONE -> 1.0f;
                case MODERN -> ExplosionExposure.seenPercent(world, center, entity);
                case LEGACY_1_8 -> ExplosionExposure.seenPercent18(world, center, entity);
                case LEGACY_1_8_FULL_CUBE -> ExplosionExposure.seenPercent18FullCube(world, center, entity);
            };
            // TODO knockback reduction (Blast Protection / KB resistance) via the attribute layer
            ExplosionCalculator.Hit hit = ExplosionCalculator.compute(center, power, eyeOrigin, distance, exposure,
                    resolved.damageConstant(), resolved.floorDamage(), resolved.knockbackMultiplier());
            if (hit == null) continue;
            // a dropped item takes the blast like anything else (vanilla EntityItem.damageEntity); its own
            // health/pricing lives in the item-damage config, so the raw curve amount is what it receives
            float damage = entity instanceof ItemEntity ? hit.damage()
                    : !living ? 0f : (resolved.flatDamage() != null ? resolved.flatDamage().floatValue() : hit.damage());
            damage *= (float) resolved.damageScale(); // post-floor, so a scaled vanilla curve stays step-quantized (MineMen FBF)
            Vec push = kbTarget ? hit.knockback() : null; // a non-KB target (mob) still takes damage, no push
            targets.add(new ExplosionEvent.Target(entity, distance, exposure, push, damage));
        }
        return targets;
    }

    /** Default explosion-KB targeting: players + non-living physics entities (TNT, falling blocks, items).
     *  Compose with it rather than restating it when a preset only carves out a type. */
    public static boolean defaultKnockbackTarget(@NotNull Entity entity) {
        return entity instanceof Player || (!(entity instanceof LivingEntity) && entity.hasPhysics());
    }

    /** {@link #defaultKnockbackTarget} unless {@link ExplosionConfig#knockbackTargets} overrides it. */
    private static boolean isKnockbackTarget(Entity entity, ResolvedExplosionConfig resolved) {
        Predicate<Entity> targets = resolved.knockbackTargets();
        return targets != null ? targets.test(entity) : defaultKnockbackTarget(entity);
    }

    private void applyEffects(MechanicsWorld world, Point center, float power, @Nullable Entity source,
                              @Nullable Entity directHit, List<ExplosionEvent.Target> targets, ResolvedExplosionConfig resolved) {
        DamageSystem damage = services.damage();
        KnockbackSystem knockback = services.knockback();
        // Two phases so the visual explosion packet reaches everyone BEFORE the knockback velocity (Hypixel's order).
        Map<Player, Vec> packetPush = new HashMap<>();     // i-frame push, client-applied from each player's explosion packet
        List<Runnable> velocitySends = new ArrayList<>();  // fresh-hit knockback, sent after the packets
        for (ExplosionEvent.Target target : targets) {
            Entity entity = target.entity();
            Vec push = target.knockback();
            DamageSystem.DamageOutcome outcome = damageTarget(damage, target, source, center, resolved.damageBypass(), resolved);
            if (entity instanceof Player player) {
                if (DamageSystem.isImmune(player) || gatedByImpactFloor(player, directHit, target, power, resolved)) continue;
                queuePlayerKnockback(player, push, outcome, center, source, resolved, knockback, packetPush, velocitySends);
            } else if (push != null && knockback != null) {
                // vanilla ADDS the push; the ~0.7 seen on a landed TNT is its own ground friction, not a scale
                entity.setVelocity(entity.getVelocity().add(perSecond(push)));
            }
        }
        for (Player player : world.watchers()) player.sendPacket(packet(center, power, packetPush.get(player)));
        velocitySends.forEach(Runnable::run);
    }

    /** {@code BLOCKED} for a non-living target or no damage. */
    private DamageSystem.DamageOutcome damageTarget(@Nullable DamageSystem damage, ExplosionEvent.Target target,
                                                    @Nullable Entity source, Point center, @Nullable Bypass bypass) {
        return damageTarget(damage, target, source, center, bypass, null);
    }

    private DamageSystem.DamageOutcome damageTarget(@Nullable DamageSystem damage, ExplosionEvent.Target target,
                                                    @Nullable Entity source, Point center, @Nullable Bypass bypass,
                                                    @Nullable ResolvedExplosionConfig resolved) {
        if (target.entity() instanceof ItemEntity item && target.damage() > 0f) {
            ItemDamageSystem items = services.items();
            // per-blast item price (MineMen: fireball 2, TNT 3 against vanilla health 5); unset = the curve
            if (items != null) items.hurt(item, ItemDamageSystem.EXPLOSION,
                    resolved != null && resolved.itemDamage() != null ? resolved.itemDamage().floatValue() : target.damage());
            return DamageSystem.DamageOutcome.BLOCKED; // items never enter the living damage pipeline
        }
        if (damage == null || !(target.entity() instanceof LivingEntity) || target.damage() <= 0f) return DamageSystem.DamageOutcome.BLOCKED;
        return damage.apply(explosionDamage(target.entity(), source, center, target.damage(), bypass));
    }

    /** Hypixel gate: the direct-hit player below the falloff impact floor gets no explosion KB (bystanders + block hits are never gated). */
    private static boolean gatedByImpactFloor(Player player, @Nullable Entity directHit, ExplosionEvent.Target target,
                                              float power, ResolvedExplosionConfig resolved) {
        Double floor = resolved.knockbackImpactFloor();
        return player == directHit && floor != null
                && ExplosionCalculator.impact(target.distance(), power, target.exposure()) < floor;
    }

    /** The push always reaches the player (bypasses i-frames); only the wire path differs per config. */
    private void queuePlayerKnockback(Player player, @Nullable Vec push, DamageSystem.DamageOutcome outcome, Point center,
                                      @Nullable Entity source, ResolvedExplosionConfig resolved, @Nullable KnockbackSystem knockback,
                                      Map<Player, Vec> packetPush, List<Runnable> velocitySends) {
        if (knockback != null && resolved.baseKnockback() > 0.0) {
            // Hypixel: radial base (toward feet+baseHeight) + push as one velocity; the explosion packet stays motion-less
            Vec base = radialBase(player.getPosition(), center, resolved.baseKnockback(), resolved.baseHeight(), resolved.baseScale());
            Vec velocity = perSecond(push != null ? base.add(push) : base);
            velocitySends.add(() -> knockback.deliver(player, velocity));
        } else if (push != null) {
            if (outcome == DamageSystem.DamageOutcome.FRESH_DAMAGE && knockback != null && source != null) {
                KnockbackConfig damageKb = resolved.damageKnockback() != null ? resolved.damageKnockback() : Knockback.melee();
                Vec impulse = perSecond(push);
                velocitySends.add(() -> knockback.apply(new KnockbackSnapshot(player, false, source, null, null, damageKb), impulse)); // a() fold, then push
            } else if (resolved.packetPush()) {
                packetPush.put(player, push); // i-frame: rides the explosion packet, not a velocity packet
            } else if (knockback != null && outcome != DamageSystem.DamageOutcome.BLOCKED) {
                // velocity-only (MineMen): push ADDED to the tracked motion (a same-tick contact KB is already folded in)
                velocitySends.add(() -> knockback.deliver(player, perSecond(trackedMotion(player).add(push))));
            }
        }
    }

    /** Explosion model vectors are blocks/tick (vanilla); the {@link KnockbackSystem} wire boundary takes blocks/second. */
    private static Vec perSecond(Vec perTick) {
        return perTick.mul(ServerFlag.SERVER_TICKS_PER_SECOND);
    }

    /** The victim's server-tracked motion (b/t): players via the profile's {@link VelocityRule} (MotionTracker), non-players their own velocity. */
    private Vec trackedMotion(Entity entity) {
        VelocityRule rule = services.profiles().resolve(entity, MechanicsKeys.VELOCITY);
        return (rule != null ? rule : VelocityRule.DEFAULT).estimate(VelocityContext.of(entity, services.sprintTracker()));
    }

    /** Radial base toward {@code height} above {@code position} (the entity's feet), ×{@code magnitude}, per-axis-shaped by {@code scale}. */
    private static Vec radialBase(Point position, Point center, double magnitude, double height, RadialScale scale) {
        Point body = position.add(0, height, 0);
        Vec u = Directions.unit3D(body.x() - center.x(), body.y() - center.y(), body.z() - center.z(), 1.0e-7);
        return u == null ? Vec.ZERO : scale.apply(magnitude, u);
    }

    private void applyDamage(@Nullable Entity source, Point center, List<ExplosionEvent.Target> targets, @Nullable Bypass bypass) {
        DamageSystem damage = services.damage();
        for (ExplosionEvent.Target target : targets) damageTarget(damage, target, source, center, bypass);
    }

    private static DamageSnapshot explosionDamage(Entity target, @Nullable Entity source, Point center, float amount, @Nullable Bypass bypass) {
        DamageSnapshot snap = DamageSnapshot.of(target, ExplosionDamage.INSTANCE).withSource(source).withPoint(center).withAmount(amount);
        return bypass != null ? snap.withBypass(bypass) : snap;
    }

    private static ExplosionPacket packet(Point center, float power, @Nullable Point knockback) {
        return new ExplosionPacket(center, power, 0, knockback,
                Particle.EXPLOSION, SoundEvent.ENTITY_GENERIC_EXPLODE, WeightedList.of());
    }

    /** An {@link ExplosionSupplier} routing {@code instance.explode(...)} through this system (no source; breaks no blocks). */
    public ExplosionSupplier supplier() {
        return (x, y, z, strength, data) -> new RoutedExplosion(this, x, y, z, strength);
    }

    public EventNode<@NotNull Event> node() { return node; }
    public @Nullable ExplosionConfig config() { return config; }

    /** Installs at the global {@code EXPLOSION} profile, else the modern-vanilla baseline (never inert - a null config resolves to vanilla TNT). */
    public static ExplosionSystem install(Polyp polyp) {
        return install(polyp, polyp.profiles().resolve(null, MechanicsKeys.EXPLOSION));
    }

    public static ExplosionSystem install(Polyp polyp, @Nullable ExplosionConfig config) {
        var system = new ExplosionSystem(polyp, config);
        polyp.register(system);
        polyp.install(system.node);
        return system;
    }

    private static final class RoutedExplosion extends Explosion {
        private final ExplosionSystem system;

        RoutedExplosion(ExplosionSystem system, float x, float y, float z, float strength) {
            super(x, y, z, strength);
            this.system = system;
        }

        @Override protected List<Point> prepare(Instance instance) { return List.of(); }

        @Override public void apply(Instance instance) {
            system.explode(instance, new Vec(getCenterX(), getCenterY(), getCenterZ()), getStrength());
        }
    }
}
