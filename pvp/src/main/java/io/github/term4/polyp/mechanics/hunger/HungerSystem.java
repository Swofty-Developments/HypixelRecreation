package io.github.term4.polyp.mechanics.hunger;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsModule;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.damage.DamageSnapshot;
import io.github.term4.polyp.mechanics.damage.DamageSystem;
import io.github.term4.polyp.mechanics.damage.types.starvation.StarvationDamage;
import io.github.term4.polyp.util.tick.TickContext;
import io.github.term4.polyp.util.tick.TickPhase;
import io.github.term4.polyp.util.tick.TickScaler;
import io.github.term4.polyp.util.tick.TickSystem;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerRespawnEvent;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Hunger: the vanilla food tick (exhaustion drain, natural regen, starvation - {@code FoodMetaData.a} /
 * {@code FoodData.tick}) plus every exhaustion source, per-scope config via {@code HUNGER}. Action sources ride
 * {@link ExhaustionSources}, the hunger effect the attribute catalog; docs/exhaustion-sources.md has the vanilla
 * catalog. Difficulty gates are dropped (Minestom has no server difficulty).
 */
public final class HungerSystem implements MechanicsModule {

    /** Per-module TPS scaling identity. */
    public static final Key KEY = Key.key("polyp:hunger");
    // Source keys, priced per preset. Lib sources charge ONLY when the scope prices them (no entry = inert).
    /** The 80-tick regen heal; quantity 1 per heal (1.8 preset: {@code flat(3)}, modern: {@code flat(6)}). */
    public static final Key REGEN_COST = Key.key("polyp:regen");
    /** The modern saturation fast-regen heal; quantity = the spent saturation (modern preset: {@code dynamic()}). */
    public static final Key SATURATION_REGEN_COST = Key.key("polyp:regen-saturation");
    /** A damaging hit taken; quantity = the damage type's {@code exhaustion} value (both presets: {@code dynamic()}). */
    public static final Key DAMAGE_TAKEN_COST = Key.key("polyp:damage-taken");
    /** A landed melee attack, charged to the attacker (1.8: {@code flat(0.3)}, modern: {@code flat(0.1)}). */
    public static final Key ATTACK_COST = Key.key("polyp:attack");
    /** A jump (1.8: {@code flat(0.2)}, modern: {@code flat(0.05)}). */
    public static final Key JUMP_COST = Key.key("polyp:jump");
    /** A sprint-jump (1.8: {@code flat(0.8)}, modern: {@code flat(0.2)}). */
    public static final Key SPRINT_JUMP_COST = Key.key("polyp:sprint-jump");
    /** Eye-underwater movement, quantity = 3D meters (1.8: {@code scaled(0.015)}, modern: {@code scaled(0.01)}). */
    public static final Key DIVE_COST = Key.key("polyp:dive");
    /** Surface water movement, quantity = horizontal meters (1.8: {@code scaled(0.015)}, modern: {@code scaled(0.01)}). */
    public static final Key SWIM_COST = Key.key("polyp:swim");
    /** Ground sprint, quantity = horizontal meters ({@code scaled(0.1)} both). */
    public static final Key SPRINT_COST = Key.key("polyp:sprint");
    /** Ground walk/sneak, quantity = horizontal meters (1.8: {@code scaled(0.01)}; modern free - unpriced). */
    public static final Key WALK_COST = Key.key("polyp:walk");
    /** A survival block harvest (1.8: {@code flat(0.025)}, modern: {@code flat(0.005)}). */
    public static final Key BLOCK_BREAK_COST = Key.key("polyp:block-break");
    /** The hunger effect, quantity = amplifier+1 per tick (1.8: {@code scaled(0.025)}, modern: {@code scaled(0.005)}). */
    public static final Key HUNGER_EFFECT_COST = Key.key("polyp:hunger-effect");

    /** Accumulated exhaustion (vanilla {@code foodExhaustionLevel}); absent = 0. */
    private static final Tag<Float> EXHAUSTION = Tag.Transient("polyp:exhaustion");
    /** The shared regen/starvation timer (vanilla {@code foodTickTimer}); absent = 0. */
    private static final Tag<Integer> FOOD_TIMER = Tag.Transient("polyp:food-timer");

    // modern saturation fast regen (FoodData.tick): fixed cadence + per-heal saturation cap
    private static final int SATURATION_REGEN_INTERVAL = 10;
    private static final float SATURATION_REGEN_CAP = 6.0f;

    private final Polyp polyp;
    private final HungerConfig config;
    private final EventNode<@NotNull Event> node;
    private static final AtomicBoolean TICK_HOOK = new AtomicBoolean();

    public HungerSystem(Polyp polyp, HungerConfig config) {
        this.polyp = polyp;
        this.config = config;
        this.node = EventNode.all("polyp:hunger");
        // vanilla respawns with a fresh FoodMetaData; Minestom resets food/saturation, these tags are ours to clear
        node.addListener(PlayerRespawnEvent.class, e -> {
            e.getPlayer().removeTag(EXHAUSTION);
            e.getPlayer().removeTag(FOOD_TIMER);
        });
    }

    public EventNode<@NotNull Event> node() { return node; }
    public HungerConfig config() { return config; }

    /** Effective config for {@code subject}: the scoped profile, else the install config. */
    public HungerConfig configFor(@Nullable Entity subject) {
        return polyp.profiles().resolveOr(subject, MechanicsKeys.HUNGER, config);
    }

    /** Active by default; only an explicit {@code enabled(false)} disables. */
    public boolean enabled(@Nullable Entity subject) {
        return !Boolean.FALSE.equals(configFor(subject).enabled());
    }

    /**
     * The entry point food consumables call on finish. Saturation caps at the food level (vanilla).
     */
    public void restore(Player player, int nutrition, float saturation) {
        if (!enabled(player)) return;
        player.setFood(Math.min(player.getFood() + nutrition, 20));
        player.setFoodSaturation(Math.min(player.getFoodSaturation() + saturation, player.getFood()));
    }

    /**
     * Adds exhaustion from {@code source}: its {@link ExhaustionCost} rule maps {@code quantity} to the cost (no entry
     * = as-is), times the global {@code exhaustionScale}. Custom sources use their own key and are scope-tunable like
     * the vanilla ones.
     */
    public void exhaust(Player player, Key source, float quantity) {
        if (!enabled(player)) return;
        exhaust(player, configFor(player), source, quantity);
    }

    private static void exhaust(Player player, HungerConfig cfg, Key source, float quantity) {
        // vanilla applyExhaustion / causeFoodExhaustion: invulnerable abilities (creative, spectator) never charge
        if (player.getGameMode().invulnerable()) return;
        ExhaustionCost rule = cfg.exhaustionCost(source);
        float global = cfg.exhaustionScale() != null ? cfg.exhaustionScale() : 1f;
        float cost = (rule != null ? rule.cost(quantity) : quantity) * global;
        if (!(cost > 0)) return; // negated form: a NaN cost (bad rule/scale) must not poison the accumulator
        player.setTag(EXHAUSTION, exhaustion(player) + cost);
    }

    /** Lib-source charge: only when the scope PRICES the key - the presets own the vanilla values, unpriced = inert. */
    public void chargePriced(Player player, Key source, float quantity) {
        if (!enabled(player)) return;
        HungerConfig cfg = configFor(player);
        if (cfg.exhaustionCost(source) != null) exhaust(player, cfg, source, quantity);
    }

    public static float exhaustion(Player player) {
        Float v = player.getTag(EXHAUSTION);
        return v != null ? v : 0f;
    }

    private void tick(TickContext ctx) {
        for (Player p : ctx.world().players()) {
            if (!ctx.owns(p) || !enabled(p)) continue;
            drainExhaustion(p);
            foodTick(p, configFor(p));
        }
    }

    /** Identical in 1.8 and modern; peaceful gate dropped. */
    private static void drainExhaustion(Player p) {
        float ex = exhaustion(p);
        if (ex <= 4.0f) return;
        p.setTag(EXHAUSTION, ex - 4.0f);
        float sat = p.getFoodSaturation();
        if (sat > 0) p.setFoodSaturation(Math.max(sat - 1.0f, 0f));
        else p.setFood(Math.max(p.getFood() - 1, 0));
    }

    /** The vanilla food tick: regen, else starvation, on the SHARED timer. */
    private void foodTick(Player p, HungerConfig cfg) {
        float max = (float) p.getAttributeValue(Attribute.MAX_HEALTH);
        boolean hurt = p.getHealth() < max;
        boolean regen = !Boolean.FALSE.equals(cfg.naturalRegen());
        if (regen && Boolean.TRUE.equals(cfg.saturationRegen()) && p.getFoodSaturation() > 0 && hurt && p.getFood() >= 20) {
            int t = timer(p) + 1;
            if (t >= scaled(p, SATURATION_REGEN_INTERVAL)) {
                float spent = Math.min(p.getFoodSaturation(), SATURATION_REGEN_CAP);
                p.setHealth(Math.min(p.getHealth() + spent / 6.0f, max));
                exhaust(p, cfg, SATURATION_REGEN_COST, spent);
                t = 0;
            }
            p.setTag(FOOD_TIMER, t);
        } else if (regen && hurt && p.getFood() >= (cfg.regenFoodThreshold() != null ? cfg.regenFoodThreshold() : 18)) {
            int t = timer(p) + 1;
            if (t >= scaled(p, interval(cfg))) {
                p.setHealth(Math.min(p.getHealth() + 1.0f, max));
                exhaust(p, cfg, REGEN_COST, 1.0f);
                t = 0;
            }
            p.setTag(FOOD_TIMER, t);
        } else if (p.getFood() <= 0) {
            int t = timer(p) + 1;
            if (t >= scaled(p, interval(cfg))) { // vanilla starves on the same cadence + timer as regen
                starve(p);
                t = 0;
            }
            p.setTag(FOOD_TIMER, t);
        } else {
            p.removeTag(FOOD_TIMER);
        }
    }

    private static int interval(HungerConfig cfg) {
        return cfg.regenInterval() != null ? cfg.regenInterval() : 80;
    }

    private int scaled(Player p, int ticks) {
        // floor 1: a non-positive interval (misconfig / extreme scaling) would otherwise fire every tick
        return Math.max(1, TickScaler.duration(ticks, polyp.profiles().resolve(p, MechanicsKeys.TICK_SCALING), KEY));
    }

    /** Difficulty floors are EASY 10 / NORMAL 1 / HARD none; no server difficulty, so NORMAL's applies. */
    private void starve(Player p) {
        DamageSystem damage = polyp.module(DamageSystem.class);
        if (damage == null || p.getHealth() <= 1.0f) return;
        DamageSnapshot snap = DamageSnapshot.of(p, StarvationDamage.INSTANCE);
        var ctx = damage.contextFor(snap);
        if (ctx.typeConfig().enabled(ctx)) damage.apply(snap);
    }

    private static int timer(Player p) {
        Integer v = p.getTag(FOOD_TIMER);
        return v != null ? v : 0;
    }

    /** Installs the system active (a per-scope {@code MechanicsProfile.hunger} config can disable it). */
    public static HungerSystem install(Polyp polyp) {
        return install(polyp, HungerConfig.builder().build());
    }

    public static HungerSystem install(Polyp polyp, HungerConfig cfg) {
        HungerSystem system = new HungerSystem(polyp, cfg);
        polyp.register(system);
        polyp.install(system.node);
        // Registered once for the JVM (TickSystem has no removal); dispatches through the live registry so a re-install is picked up.
        if (TICK_HOOK.compareAndSet(false, true)) {
            TickSystem.register(TickPhase.DEFAULT, ctx -> {
                HungerSystem live = polyp.module(HungerSystem.class);
                if (live != null) live.tick(ctx);
            });
        }
        ExhaustionSources.install(polyp);
        return system;
    }
}
