package io.github.term4.polyp.mechanics.damage;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsModule;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.Services;
import io.github.term4.polyp.mechanics.death.DeathConfig;
import io.github.term4.polyp.mechanics.death.DeathConfig.DeathContext;
import io.github.term4.polyp.presets.vanilla18.Knockback;
import io.github.term4.polyp.mechanics.blocking.BlockingSystem;
import io.github.term4.polyp.mechanics.attribute.AttributeSystem;
import io.github.term4.polyp.mechanics.attribute.defense.Bypass;
import io.github.term4.polyp.mechanics.attribute.defense.MitigationRequest;
import io.github.term4.polyp.mechanics.attribute.defense.ProtectionCategory;
import io.github.term4.polyp.api.event.damage.DamageEvent;
import io.github.term4.polyp.api.event.damage.PreDamageEvent;
import io.github.term4.polyp.api.event.damage.DamageAppliedEvent;
import io.github.term4.polyp.mechanics.damage.DamageCalculator.DamageResult;
import io.github.term4.polyp.mechanics.damage.DamageConfigResolver.DamageContext;
import io.github.term4.polyp.mechanics.damage.DamageConfigResolver.ResolvedDamageConfig;
import io.github.term4.polyp.mechanics.damage.silent.HurtSuppression;
import io.github.term4.polyp.mechanics.damage.silent.SilentDamage;
import io.github.term4.polyp.mechanics.damage.types.breathing.DrowningDamage;
import io.github.term4.polyp.mechanics.damage.types.DamageType;
import io.github.term4.polyp.mechanics.damage.types.DamageTypeConfig;
import io.github.term4.polyp.mechanics.damage.types.explosion.ExplosionDamage;
import io.github.term4.polyp.mechanics.damage.types.melee.MeleeDamage;
import io.github.term4.polyp.mechanics.damage.types.projectile.ProjectileDamage;
import io.github.term4.polyp.mechanics.knockback.KnockbackConfig;
import io.github.term4.polyp.mechanics.knockback.KnockbackSnapshot;
import io.github.term4.polyp.mechanics.knockback.KnockbackSystem;
import io.github.term4.polyp.mechanics.projectile.entities.arrow.StuckArrows;
import io.github.term4.polyp.util.tick.TickSystem;
import io.github.term4.polyp.util.tick.TickScaler;
import io.github.term4.polyp.util.tick.TickState;
import io.github.term4.polyp.presets.vanilla18.Vanilla18;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.item.ItemStack;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.ListenerHandle;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.player.PlayerRespawnEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main damage system: resolves config, computes the amount, fires {@link DamageEvent}, applies the 1.8 overdamage
 * rule, and applies the damage. Mirrors KnockbackSystem.
 *
 * <p>Every fresh hit except drowning broadcasts the victim's server-tracked velocity. Non-melee hits route it through
 * the {@link KnockbackSystem} with {@link DamageConfig#hurtKnockback}; melee's broadcast is its own knockback.
 */
public final class DamageSystem implements MechanicsModule {

    /** This system's identity for per-module TPS scaling (its {@code referenceTps} feel-baseline). */
    public static final Key KEY = Key.key("polyp:damage");

    private static final Tag<TickState> INVUL_DAMAGE = Tag.Transient("polyp:invul-damage");
    /** Amount of the hit that opened the current invulnerability window (for overdamage replacement). */
    private static final Tag<Float> LAST_DAMAGE = Tag.Transient("polyp:last-damage");
    private static final Tag<DamageType> LAST_DAMAGE_TYPE = Tag.Transient("polyp:last-damage-type");
    /** Melee weapon that opened the current invulnerability window ({@code null} = fist / non-melee). */
    private static final Tag<ItemStack> OPENING_ITEM = Tag.Transient("polyp:opening-hit-item");

    /** Default invul ticks (vanilla 1.8) when nothing resolves; scaled to live TPS at the stamp site. */
    public static final int DEFAULT_INVUL_TICKS = 10;

    /** Fallback death-animation length when a scoped {@link DeathConfig#deathAnimationTicks} is unset (vanilla {@code deathTime} 20). */
    private static final int DEATH_ANIMATION_TICKS = 20;

    // Pre/Applied fire only when listened to; main always fires
    private static final ListenerHandle<PreDamageEvent> PRE_DAMAGE = EventDispatcher.getHandle(PreDamageEvent.class);
    private static final ListenerHandle<DamageAppliedEvent> DAMAGE_APPLIED = EventDispatcher.getHandle(DamageAppliedEvent.class);
    private static final AtomicBoolean CLOCK_RESET = new AtomicBoolean();

    private final Polyp polyp;
    private final DamageConfig config;
    private final DamageCalculator calc;
    private final DamageTypeRegistry registry;
    private final Services services;

    public Services services() { return services; }
    private final EventNode<@NotNull Event> node;

    public DamageSystem(Polyp polyp, DamageConfig config) {
        this.polyp = polyp;
        this.node = EventNode.all("polyp:damage");
        this.config = config;
        this.services = polyp.services();
        this.calc = new DamageCalculator(this.services, Vanilla18.damage());
        this.registry = new DamageTypeRegistry(this, polyp).registerVanillaDefaults();
        // window stamps ride the victim's per-instance clock; the TickState future-guard misses a coinciding long-lived instance
        this.node.addListener(PlayerSpawnEvent.class, e -> clearDamageWindow(e.getPlayer()));
        // vanilla sends the hurt sound to everyone EXCEPT the victim, whose own client predicts it
        // (EntityPlayerSP/LocalPlayer override playSound; a remote entity's playSound is a client no-op in both
        // eras). Minestom sends viewers AND self - re-emit to viewers only, or the victim doubles / attackers hear nothing
        this.node.addListener(EntityDamageEvent.class, e -> {
            SoundEvent sound = e.getSound();
            if (sound == null || !e.shouldAnimate()) return;
            e.setSound(null);
            Entity victim = e.getEntity();
            Pos at = victim.getPosition();
            Sound.Source category = victim instanceof Player ? Sound.Source.PLAYER : Sound.Source.HOSTILE;
            victim.sendPacketToViewers(AdventurePacketConvertor.createSoundPacket(
                    Sound.sound(sound, category, 1.0f, 1.0f), at.x(), at.y(), at.z()));
        });
        if (CLOCK_RESET.compareAndSet(false, true)) {
            TickSystem.onClockChange(e -> {
                if (e instanceof LivingEntity le) clearDamageWindow(le);
            });
        }
        // vanilla clears effects + transient combat state on death; Minestom's kill() does not and the Player object
        // survives respawn. An unset DeathConfig knob defaults to vanilla (on / 20-tick animation).
        this.node.addListener(EntityDeathEvent.class, e -> {
            if (!(e.getEntity() instanceof LivingEntity dead)) return;
            DeathContext ctx = new DeathContext(dead);
            DeathConfig death = effectiveDeath(polyp.profiles().resolve(dead, MechanicsKeys.DEATH), ctx);
            if (deathFlag(death != null ? death.clearEffects(ctx) : null)) dead.clearEffects();
            if (deathFlag(death != null ? death.resetMechanicsState(ctx) : null)) resetMechanicsState(dead);
            // Minestom keeps the health-0 entity in the world (1.8/Via replays the death smoke on chunk reload); hide it
            // only AFTER the animation plays, and guard so a fast respawn doesn't hide the live player
            if (deathFlag(death != null ? death.hideCorpse(ctx) : null) && dead instanceof Player p) {
                Integer knob = death != null ? death.deathAnimationTicks(ctx) : null;
                int ticks = knob != null ? knob : DEATH_ANIMATION_TICKS;
                p.scheduler().buildTask(() -> { if (p.isDead()) p.setAutoViewable(false); })
                        .delay(TaskSchedule.tick(TickScaler.duration(p, ticks, KEY))).schedule();
            }
        });
        // re-show the respawned player NEXT tick (after the respawn teleport, else viewers re-see them at the death spot)
        this.node.addListener(PlayerRespawnEvent.class, e -> {
            DeathContext ctx = new DeathContext(e.getPlayer());
            DeathConfig death = effectiveDeath(polyp.profiles().resolve(e.getPlayer(), MechanicsKeys.DEATH), ctx);
            if (deathFlag(death != null ? death.hideCorpse(ctx) : null)) e.getPlayer().scheduleNextTick(p -> p.setAutoViewable(true));
        });
    }

    /** Effective config for a snapshot carrying none: the victim's scoped profile, else the install config. */
    private DamageConfig configFor(@Nullable Entity target) {
        return polyp.profiles().resolveOr(target, MechanicsKeys.DAMAGE, config);
    }

    /**
     * Resolution context for a snapshot, applying the config chain (snapshot -> victim scope -> install) when the
     * snapshot carries none. Producers use this to read per-type knobs before emitting.
     */
    public DamageContext contextFor(DamageSnapshot snap) {
        DamageSnapshot working = snap.config() != null ? snap : snap.withConfig(configFor(snap.target()));
        return DamageContext.of(working, services);
    }

    /** Whether {@code type} is enabled for {@code target} under its effective config chain. */
    public boolean typeEnabled(DamageType type, Entity target) {
        DamageContext ctx = contextFor(DamageSnapshot.of(target, type));
        return ctx.typeConfig().enabled(ctx);
    }

    /**
     * Outcome of {@link #apply}, mirroring vanilla {@code damageEntity}: rulesets gate effects on it (knockback on
     * {@link #FRESH_DAMAGE}, sprint reset on {@link #landed()}).
     */
    public enum DamageOutcome {
        /** Absorbed by the i-frame window, or cancelled / zero / disabled. Distinct from {@link #IMMUNE}. */
        BLOCKED,
        /** Fundamentally immune (creative/spectator): no damage and no knockback. Kept distinct from {@link #BLOCKED} so a
         *  projectile can react differently - a 1.8 arrow passes through an immune target but deflects off an i-frame one. */
        IMMUNE,
        /** Overdamage replacement inside the i-frame window: damage dealt, but the fresh effects are skipped. */
        OVERDAMAGE,
        /** Fresh damage - full effects (knockback, opens the i-frame window). */
        FRESH_DAMAGE;

        /** Damage was dealt (fresh or replacement). */
        public boolean landed() { return this == OVERDAMAGE || this == FRESH_DAMAGE; }
    }

    /**
     * Applies damage from a snapshot. Base amount comes from the {@link DamageCalculator}; type-specific modifiers are
     * baked into the snapshot beforehand. Returns the {@link DamageOutcome} so rulesets can gate effects.
     */
    public DamageOutcome apply(DamageSnapshot snap) {
        if (!(snap.target() instanceof LivingEntity)) return DamageOutcome.BLOCKED;

        // config: snapshot -> victim scope -> install (none = inert, empty = vanilla floor)
        DamageConfig effective = snap.config() != null ? snap.config() : configFor(snap.target());
        if (effective == null) return DamageOutcome.BLOCKED;

        DamageSnapshot working = snap.config() != null ? snap : snap.withConfig(effective);

        if (PRE_DAMAGE.hasListener()) {
            PreDamageEvent pre = new PreDamageEvent(working, services);
            EventDispatcher.call(pre);
            if (pre.isCancelled()) return DamageOutcome.BLOCKED;
            working = pre.finalSnap();
            if (!(working.target() instanceof LivingEntity)) return DamageOutcome.BLOCKED;
        }

        DamageResult result = calc.compute(working);

        float amount = result.amount();

        DamageEvent event = new DamageEvent(working, amount, services);
        EventDispatcher.call(event);
        if (event.isCancelled()) return DamageOutcome.BLOCKED;

        DamageSnapshot finalSnap = event.finalSnap();
        if (!(finalSnap.target() instanceof LivingEntity living)) return DamageOutcome.BLOCKED;

        DamageType type = finalSnap.type();
        DamageContext typeCtx = contextFor(finalSnap);
        DamageTypeConfig typeCfg = typeCtx.typeConfig();
        // per-scope kill switch; read off the final snap so a listener can swap in an enabled config
        if (!typeCfg.enabled(typeCtx)) return DamageOutcome.BLOCKED;
        amount = event.amount();
        boolean bypassImmune = event.bypassImmune() || typeCfg.bypassImmune(typeCtx);
        boolean bypassInvul = event.bypassInvul() || typeCfg.bypassInvul(typeCtx);

        if (!bypassImmune && isImmune(living)) return DamageOutcome.IMMUNE;

        // Fire Resistance blocks at the hit entry (1.8 damageEntity / 26 hurtServer return false): before i-frames and
        // mitigation, so it consumes no invul window and never flashes. Not gated by the i-frame bypass.
        AttributeSystem fireAttrs = services.attributes();
        if (fireAttrs != null && type.protectionCategories().contains(ProtectionCategory.FIRE) && fireAttrs.fireResistant(living)) {
            return DamageOutcome.BLOCKED;
        }

        ResolvedDamageConfig resolved = calc.resolveConfig(typeCtx.snap());

        boolean overdamage = Boolean.TRUE.equals(pick(typeCfg.overdamage(typeCtx), resolved.enableOverdamage()));
        boolean generalSilent = Boolean.TRUE.equals(pick(typeCfg.silent(typeCtx), resolved.silent()));

        // vanilla mitigates the FULL amount first, THEN takes the overdamage remainder - a replacement hit is not
        // "true damage" (a fall landing mid-i-frame still gets resistance / Feather Falling)
        boolean replacement = event.invulnerable() && !bypassInvul;
        if (replacement && !overdamage) return DamageOutcome.BLOCKED;

        amount = applyComponents(typeCtx, event, amount, replacement);
        // Blocking (sword/shield): vanilla reduces a blocked hit BEFORE armor (1.8 EntityHuman.damageEntity). The
        // BlockingSystem owns the decision entirely (is the player blocking, the behavior, what's blockable).
        BlockingSystem blocking = services.blocking();
        if (blocking != null) amount = blocking.reduce(living, typeCtx, amount);
        amount = applyMitigation(living, type, typeCfg, typeCtx, amount);
        boolean triggersInvul = typeCfg.triggersInvul(typeCtx);

        if (replacement) {
            // overdamage: only the mitigated amount above the window's stored highwater lands
            float applied = amount > event.stored() ? amount - event.stored() : 0f;
            if (applied <= 0) return DamageOutcome.BLOCKED;
            Boolean odSilent = pick(typeCfg.overdamageSilent(typeCtx), resolved.overdamageSilent());
            boolean replacementSilent = odSilent != null ? odSilent : generalSilent;
            living.setTag(LAST_DAMAGE, Math.max(event.stored(), amount));
            living.setTag(LAST_DAMAGE_TYPE, type);
            applyDamage(living, type, finalSnap, applied, replacementSilent);
            // vanilla runs the post-hit enchant effects on ANY landed hit - an overdamage refresh included
            // (EntityHuman.attack applies fire aspect whenever damageEntity returns true)
            dispatchWeaponOnHit(living, finalSnap);
            fireDamageApplied(finalSnap, applied, DamageOutcome.OVERDAMAGE);
            return DamageOutcome.OVERDAMAGE;
        }

        // fresh hit: a 0-damage hit still lands when its type triggers invul (snowball/egg); only negative or non-invul 0 is dropped
        if (amount < 0 || (amount == 0f && !triggersInvul)) return DamageOutcome.BLOCKED;

        storeOpeningItem(living, finalSnap.item());
        living.setTag(LAST_DAMAGE, amount);
        living.setTag(LAST_DAMAGE_TYPE, type);
        applyDamage(living, type, finalSnap, amount, generalSilent);
        Boolean ownsFlag = typeCfg.ownsVelocityBroadcast(typeCtx);
        boolean ownsVelocity = ownsFlag != null ? ownsFlag : knockbackOwnsVelocity(type);
        if (Boolean.TRUE.equals(resolved.syncHurtVelocity())
                && !ownsVelocity
                && !DROWN_KEY.equals(type.key())) {
            applyHurtKnockback(living, resolved.hurtKnockback());
        }
        Integer invulTicks = pick(typeCfg.invulTicks(typeCtx), resolved.invulTicks());
        if (triggersInvul && invulTicks != null && invulTicks > 0) {
            // i-frame window is a server-authoritative duration: stretch the vanilla-tick count to live TPS (identity at 20)
            setDamageInvulnerable(living, TickScaler.duration(invulTicks, polyp.profiles().resolve(living, MechanicsKeys.TICK_SCALING), KEY));
        }
        dispatchWeaponOnHit(living, finalSnap);
        fireDamageApplied(finalSnap, amount, DamageOutcome.FRESH_DAMAGE);
        return DamageOutcome.FRESH_DAMAGE;
    }

    private void fireDamageApplied(DamageSnapshot snap, float dealt, DamageOutcome outcome) {
        if (DAMAGE_APPLIED.hasListener()) EventDispatcher.call(new DamageAppliedEvent(snap, dealt, outcome, services));
    }

    /** Weapon on-hit enchant side effects (Fire Aspect, ...): defined in the attribute catalog, triggered here. */
    private void dispatchWeaponOnHit(LivingEntity victim, DamageSnapshot snap) {
        AttributeSystem attrs = services.attributes();
        if (attrs == null || !(snap.source() instanceof LivingEntity attacker)) return;
        ItemStack weapon = snap.item();
        if (weapon == null || weapon.isAir()) return;
        attrs.dispatchWeaponOnHit(attacker, victim, weapon);
    }

    /** Default for the per-type {@code ownsVelocityBroadcast} knob: these deliver their own KB, so the generic hurt velocity isn't also sent. */
    private static boolean knockbackOwnsVelocity(DamageType type) {
        return MeleeDamage.KEY.equals(type.key()) || ProjectileDamage.KEY.equals(type.key()) || ExplosionDamage.KEY.equals(type.key());
    }

    private static <T> @Nullable T pick(@Nullable T typeValue, @Nullable T globalValue) {
        return typeValue != null ? typeValue : globalValue;
    }

    /** A nullable {@link DeathConfig} toggle: unset (or true) is on; only an explicit {@code false} disables. */
    private static boolean deathFlag(@Nullable Boolean v) { return !Boolean.FALSE.equals(v); }

    private static @Nullable DeathConfig effectiveDeath(@Nullable DeathConfig cfg, DeathContext ctx) {
        return cfg != null ? cfg.withOverlay(ctx) : null;
    }

    /** Vanilla {@code damageEntity}: drowning is the one source that never triggers {@code ac()}. */
    private static final Key DROWN_KEY = Key.key("minecraft:drown");
    /** Fallback hurt knockback when no config sets one (built once - it is immutable). */
    private static final KnockbackConfig DEFAULT_HURT_KB = Knockback.hurt();

    /** Hands the hit to {@link AttributeSystem#mitigate} (armor → resistance → EPF, vanilla order; absorption is Minestom's). */
    private float applyMitigation(LivingEntity living, DamageType type, DamageTypeConfig typeCfg, DamageContext ctx, float amount) {
        AttributeSystem attrs = services.attributes();
        if (attrs == null || amount <= 0) return amount;
        // the damage type contributes the broad stage flags; the snapshot (item/attack) contributes targeted bypass
        Bypass bypass = Bypass.builder()
                .all(typeCfg.bypassAll(ctx))
                .armor(typeCfg.bypassArmor(ctx))
                .effects(typeCfg.bypassEffects(ctx))
                .enchants(typeCfg.bypassEnchants(ctx))
                .build()
                .merge(ctx.snap().bypass());
        MitigationRequest req = MitigationRequest.of(type.protectionCategories(), bypass, ThreadLocalRandom.current());
        return attrs.mitigate(living, amount, req);
    }

    /** The hurt velocity broadcast, routed through the {@link KnockbackSystem} with a zero-impulse config so all velocity sends share one path. */
    private void applyHurtKnockback(LivingEntity living, @Nullable KnockbackConfig cfg) {
        KnockbackSystem kb = services.knockback();
        if (kb == null) return;
        // LEGACY rolls to negate (1:1 with vanilla); MODERN's ×(1-resistance) scale folds in with the KB stage list later.
        AttributeSystem attrs = services.attributes();
        double resistance = attrs != null ? attrs.knockbackResistance(living)
                : living.getAttributeValue(Attribute.KNOCKBACK_RESISTANCE);
        if (ThreadLocalRandom.current().nextDouble() < resistance) return;
        kb.apply(new KnockbackSnapshot(living, false, null,
                living.getPosition(), living.getPosition().direction(),
                cfg != null ? cfg : DEFAULT_HURT_KB));
    }

    private float applyComponents(DamageContext ctx, DamageEvent event, float amount, boolean overdamage) {
        DamageConfig cfg = event.config();
        if (cfg == null) cfg = configFor(event.target());
        if (cfg == null) return amount;
        List<DamageComponent> components = cfg.withOverlay(ctx).customComponents;
        if (components == null || components.isEmpty()) return amount;
        for (DamageComponent component : components) {
            Float next = component.apply(ctx, event, amount, overdamage);
            if (next != null) amount = next;
        }
        return amount;
    }

    private static void storeOpeningItem(LivingEntity living, @Nullable ItemStack item) {
        if (item != null && !item.isAir()) living.setTag(OPENING_ITEM, item);
        else living.removeTag(OPENING_ITEM);
    }

    /** Melee weapon that opened the target's current damage-invul window, or {@code null} (fist / none). */
    public static @Nullable ItemStack openingHitItem(LivingEntity target) {
        return target.getTag(OPENING_ITEM);
    }

    private void applyDamage(LivingEntity living, DamageType type, DamageSnapshot snap, float amount, boolean silent) {
        // lethal hits fall through to living.damage() so Minestom handles death
        if (silent && living instanceof Player p) {
            // absorption absorbs first (Minestom's damage() does this; the silent path sets health directly, so replicate it)
            float absorb = p.getAdditionalHearts();
            float absorbed = Math.min(absorb, amount);
            float newHealth = p.getHealth() - (amount - absorbed);
            if (newHealth > 0) {
                if (absorbed > 0) p.setAdditionalHearts(absorb - absorbed);
                SilentDamage.setHealthWithoutHurtEffect(p, newHealth, polyp.clientInfo());
                return;
            }
        }
        Entity source = snap.source();
        Damage damage = new Damage(type.minecraftType(), source, source, snap.point(), amount);
        living.damage(damage);
    }

    public DamageConfig config() { return config; }

    /**
     * Effective invul ticks for a type: per-type override, else the global value, else {@link #DEFAULT_INVUL_TICKS}.
     * A {@code null} type resolves the global value only.
     */
    public int defaultInvulTicks(@Nullable DamageType type) {
        // only constant invul values resolve without a snapshot
        if (type != null) {
            DamageTypeConfig tcfg = config != null ? config.typeConfig(type.key()) : null;
            if (tcfg == null) tcfg = type.defaultConfig();
            Integer v = tcfg.invulTicksConstant();
            if (v != null) return v;
        }
        Integer v = config != null && config.invulTicks != null ? config.invulTicks.constantOrNull() : null;
        return v != null ? v : DEFAULT_INVUL_TICKS;
    }

    /** The standard hit i-frame window (the {@code player_attack} type's invul), used to align other systems' windows. */
    public int defaultInvulTicks() {
        return defaultInvulTicks(registry.get(MeleeDamage.KEY));
    }

    /** Registry of damage types and their handlers. */
    public DamageTypeRegistry registry() { return registry; }

    public EventNode<@NotNull Event> node() { return node; }

    /**
     * Installs reading the GLOBAL profile's {@link DamageConfig}: its {@code typeConfigs} start the self-driven
     * producers (fall/fire/cactus/...). Set the profile before installing. With no global profile this is inert
     * (a hit with no scoped or snapshot config is a no-op). {@code extraTypes} registers + enables custom types.
     */
    public static DamageSystem install(Polyp polyp, DamageType... extraTypes) {
        return install(polyp, polyp.profiles().resolve(null, MechanicsKeys.DAMAGE), extraTypes);
    }

    /** Installs from an explicit config (the modular path): enables its {@code typeConfigs} producers. */
    public static DamageSystem install(Polyp polyp, DamageConfig cfg, DamageType... extraTypes) {
        var system = new DamageSystem(polyp, cfg);
        polyp.register(system);
        EnvironmentalDamageTicker.instance().bind(system);
        HurtSuppression.install(system.node);
        polyp.install(system.node);
        for (DamageType type : extraTypes) {
            if (!system.registry.contains(type.key())) system.registry.register(type);
        }
        if (cfg != null) {
            for (Key key : cfg.typeConfigs.keySet()) {
                if (system.registry.contains(key)) system.registry.enable(key);
            }
        }
        for (DamageType type : extraTypes) system.registry.enable(type.key());
        return system;
    }

    /**
     * Pre-event early-out: while the target's window is active, an attempt that can't beat the stored highwater is
     * dropped before any {@link DamageEvent}. Repeating producers (fire, cactus) use this to avoid event spam.
     */
    public static boolean absorbedByWindow(LivingEntity target, float amount) {
        return isInvulnerableToDamage(target) && amount <= lastDamage(target);
    }

    /** The "last damage" highwater stored for the target's current invul window ({@code 0} if none). */
    public static float lastDamage(LivingEntity le) {
        Float v = le.getTag(LAST_DAMAGE);
        return v != null ? v : 0f;
    }

    /** Type of the target's most recent LANDED damage (fresh or overdamage - a replacement overwrites it), or {@code null}. */
    public static @Nullable DamageType lastDamageType(LivingEntity le) {
        return le.getTag(LAST_DAMAGE_TYPE);
    }

    /** Opens (or re-opens) the target's damage-invulnerability window (i-frames) for {@code duration} ticks. */
    public static void setDamageInvulnerable(Entity e, int duration) {
        if (!(e instanceof LivingEntity le) || duration <= 0) return;
        // stamp against the instance-local combat clock so the window is opened and checked on one phase (see TickSystem)
        le.setTag(INVUL_DAMAGE, new TickState(TickSystem.tick(le), duration));
    }

    /** Whether the target is inside its i-frame window. Not fundamental immunity (creative/spectator). */
    public static boolean isInvulnerableToDamage(Entity e) {
        if (!(e instanceof LivingEntity le)) return false;
        TickState s = getDamageInvul(le);
        return s != null && s.isActive(TickSystem.tick(le));
    }

    /** Fundamental immunity - creative/spectator, which take no damage AND no knockback (vanilla {@code abilities.isInvulnerable}). Distinct from the i-frame window {@link #isInvulnerableToDamage}. */
    public static boolean isImmune(Entity e) {
        return e instanceof Player p && (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR);
    }

    /** Ticks left in the target's damage-invulnerability window ({@code 0} if none). */
    public static int remainingDamageInvul(LivingEntity le) {
        TickState s = getDamageInvul(le);
        return s != null ? s.remainingTicks(TickSystem.tick(le)) : 0;
    }

    /** Clears the i-frame window state (the window, its overdamage highwater, its type, and its opening item) as one unit. */
    public static void clearDamageWindow(LivingEntity le) {
        le.removeTag(INVUL_DAMAGE);
        le.removeTag(LAST_DAMAGE);
        le.removeTag(LAST_DAMAGE_TYPE);
        le.removeTag(OPENING_ITEM);
    }

    /**
     * Clears the transient state vanilla starts fresh after death - fire, residual velocity, drowning air, stuck arrows,
     * in-progress item use. Minestom reuses the Player object, so none of it resets on its own.
     *
     * <p>Also the reset seam for a round restart / kit swap / tick-domain move (a timer armed on one clock means nothing
     * on another). Fx are NOT touched - {@link DeathConfig#clearEffects} owns those.
     */
    public static void resetMechanicsState(@NotNull LivingEntity entity) {
        entity.setFireTicks(0);
        entity.setVelocity(Vec.ZERO);
        DrowningDamage.resetAir(entity);
        StuckArrows.clear(entity);
        // vanilla die() calls stopUsingItem; Minestom's kill() leaves the use timer armed, so an eat/drink
        // finishes on the corpse (effects applied, item consumed) and a block survives the death
        if (entity instanceof Player p && p.isUsingItem()) {
            p.refreshActiveHand(false, p.getItemUseHand() == PlayerHand.OFF, false);
            p.clearItemUse();
        }
    }

    private static @Nullable TickState getDamageInvul(LivingEntity le) {
        return le.getTag(INVUL_DAMAGE);
    }
}
