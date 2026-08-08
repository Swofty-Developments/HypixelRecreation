package io.github.term4.polyp.fx;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.Services;
import io.github.term4.polyp.api.event.fx.FxEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.ListenerHandle;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

/**
 * The fx layer's entry points: the built-in {@link Key keys}, the vanilla {@link FxRegistry} factories a
 * preset installs, and the generic {@link #play} the library's mechanics call. A server customizes feedback by putting a
 * different registry on the {@code MechanicsKeys.FX} profile member - no event listeners required ({@link FxEvent}
 * is the optional dynamic hook). Sound ids are the modern ones (Via translates for 1.8 clients).
 *
 * <p>Per-player preferences ride PLAYER-scoped profiles: the registry resolves against the fx SOURCE, so
 * {@code profiles().setPlayer(p, MechanicsKeys.FX, registry)} re-flavors what {@code p}'s own actions emit.
 * For an fx only that player should hear, register a handler using {@link FxContext#sourceSound}.
 */
public final class Fx {

    private Fx() {}

    private static final ListenerHandle<FxEvent> FX_EVENT = EventDispatcher.getHandle(FxEvent.class);

    public static final Key CRIT = Key.key("polyp:crit");
    /** Enchantment ("magic") critical. */
    public static final Key MAGIC_CRIT = Key.key("polyp:magic_crit");
    /** {@link #CRIT} on a server-filled swing hit: the attacker's client never saw the hit, so the default includes it. */
    public static final Key FAKE_CRIT = Key.key("polyp:fake_crit");
    /** {@link #MAGIC_CRIT} on a server-filled swing hit. */
    public static final Key FAKE_MAGIC_CRIT = Key.key("polyp:fake_magic_crit");
    /** Food chewed this tick - on the vanilla eating cadence (every 4 ticks). */
    public static final Key EAT = Key.key("polyp:eat");
    public static final Key DRINK = Key.key("polyp:drink");
    /** Food finished. */
    public static final Key BURP = Key.key("polyp:burp");
    public static final Key ITEM_PICKUP = Key.key("polyp:item_pickup");
    /** A critical arrow's flight-trail particles. */
    public static final Key ARROW_CRIT = Key.key("polyp:arrow_crit");
    public static final Key THROW_SNOWBALL = Key.key("polyp:throw_snowball");
    public static final Key THROW_EGG = Key.key("polyp:throw_egg");
    public static final Key THROW_PEARL = Key.key("polyp:throw_pearl");
    /** Pearl landed and moved its thrower. 1.8 has no pearl sound, so {@link #vanilla18()} leaves it unregistered. */
    public static final Key PEARL_TELEPORT = Key.key("polyp:pearl_teleport");
    /** Fire charge thrown from the hand. */
    public static final Key THROW_FIREBALL = Key.key("polyp:throw_fireball");
    /** Bow released (arrow shot). */
    public static final Key BOW_SHOOT = Key.key("polyp:bow_shoot");
    public static final Key ROD_CAST = Key.key("polyp:rod_cast");
    /** Fishing rod reeled in. */
    public static final Key ROD_RETRIEVE = Key.key("polyp:rod_retrieve");
    /** Arrow struck a block or entity. */
    public static final Key ARROW_HIT = Key.key("polyp:arrow_hit");
    /** Arrow struck a target - the hit-marker "ding" to the SHOOTER only. Unregistered by default; a PvP preset registers it. */
    public static final Key ARROW_HIT_PLAYER = Key.key("polyp:arrow_hit_player");
    /** A burning entity doused by water (1.8 {@code random.fizz}); heard by viewers, never the doused player. */
    public static final Key FIRE_EXTINGUISH = Key.key("polyp:fire_extinguish");
    /** TNT ignited (primed TNT spawned). */
    public static final Key TNT_PRIME = Key.key("polyp:tnt_prime");
    /** The big explosion flash; played for power &gt;= 2 (the 1.8 client's hugeexplosion-vs-explode gate). */
    public static final Key EXPLOSION_EMITTER = Key.key("polyp:explosion_emitter");

    /**
     * Plays the fx registered for {@code key} in {@code ctx.source()}'s scope, firing the cancellable
     * {@link FxEvent} first. A no-op when the scope has no registry, the key is unregistered, the fx is
     * {@link FxHandler#NONE}, or a listener cancels.
     */
    public static void play(@NotNull Services services, @NotNull Key key, @NotNull FxContext ctx) {
        FxRegistry registry = services.profiles().resolve(ctx.source(), MechanicsKeys.FX);
        if (registry == null) return;
        FxHandler fx = registry.get(key);
        if (fx == null) return;
        if (FX_EVENT.hasListener()) {
            FxEvent event = new FxEvent(key, ctx, fx, services);
            EventDispatcher.call(event);
            if (event.isCancelled()) return;
            fx = event.fx(); // listener may swap it (or re-enable a NONE)
        }
        if (fx == FxHandler.NONE) return;
        fx.play(ctx);
    }

    /**
     * The 1.8 vanilla fx - the {@code Vanilla18} preset sets this as its {@code MechanicsKeys.FX} member.
     * 1.8 has no melee attack sounds (those are 1.9+), so the crit is particle-only.
     */
    public static @NotNull FxRegistry vanilla18() {
        return FxRegistry.empty()
                .register(CRIT, FxHandler.hitAnimation(EntityAnimationPacket.Animation.CRITICAL_EFFECT))
                .register(MAGIC_CRIT, FxHandler.hitAnimation(EntityAnimationPacket.Animation.MAGICAL_CRITICAL_EFFECT))
                .register(FAKE_CRIT, FxHandler.hitAnimationAll(EntityAnimationPacket.Animation.CRITICAL_EFFECT))
                .register(FAKE_MAGIC_CRIT, FxHandler.hitAnimationAll(EntityAnimationPacket.Animation.MAGICAL_CRITICAL_EFFECT))
                // viewers only: the client self-predicts its own chew from the eating metadata
                .register(EAT, ctx -> ctx.viewerSound(SoundEvent.ENTITY_GENERIC_EAT, Sound.Source.PLAYER, eatVolume(), jitterPitch(0.2f)))
                .register(DRINK, ctx -> ctx.viewerSound(SoundEvent.ENTITY_GENERIC_DRINK, Sound.Source.PLAYER, 0.5f, drinkPitch()))
                // 1.8 random.burp
                .register(BURP, ctx -> ctx.sound(SoundEvent.ENTITY_PLAYER_BURP, Sound.Source.PLAYER,
                        0.5f, ThreadLocalRandom.current().nextFloat() * 0.1f + 0.9f))
                // 1.8 item pickup
                .register(ITEM_PICKUP, ctx -> ctx.sound(SoundEvent.ENTITY_ITEM_PICKUP, Sound.Source.PLAYER, 0.2f,
                        jitterPitch(0.7f) * 2.0f))
                // 1.8 random.fizz 0.7F / 1.6F +- 0.4 dual-rand
                .register(FIRE_EXTINGUISH, ctx -> ctx.viewerSound(SoundEvent.ENTITY_GENERIC_EXTINGUISH_FIRE,
                        Sound.Source.NEUTRAL, 0.7f, 0.6f + jitterPitch(0.4f)))
                .register(THROW_SNOWBALL, throwSound(SoundEvent.ENTITY_SNOWBALL_THROW, Sound.Source.NEUTRAL))
                .register(THROW_EGG, throwSound(SoundEvent.ENTITY_EGG_THROW, Sound.Source.PLAYER))
                .register(THROW_PEARL, throwSound(SoundEvent.ENTITY_ENDER_PEARL_THROW, Sound.Source.NEUTRAL))
                // vanilla fire-charge / ghast-shoot pitch
                .register(THROW_FIREBALL, ctx -> ctx.sound(SoundEvent.ENTITY_GHAST_SHOOT, Sound.Source.NEUTRAL, 1.0f, jitterPitch(0.2f)))
                .register(BOW_SHOOT, ctx -> ctx.sound(SoundEvent.ENTITY_ARROW_SHOOT, Sound.Source.PLAYER, 1.0f, bowPitch()))
                .register(ROD_CAST, throwSound(SoundEvent.ENTITY_FISHING_BOBBER_THROW, Sound.Source.NEUTRAL))
                .register(ROD_RETRIEVE, throwSound(SoundEvent.ENTITY_FISHING_BOBBER_RETRIEVE, Sound.Source.NEUTRAL))
                .register(ARROW_HIT, ctx -> ctx.sound(SoundEvent.ENTITY_ARROW_HIT, Sound.Source.NEUTRAL, 1.0f, arrowHitPitch()))
                .register(ARROW_CRIT, FxHandler.particle(Particle.CRIT, 2, 0.05, 0f))
                // 1.8 game.tnt.primed 1.0/1.0 on ignite
                .register(TNT_PRIME, FxHandler.sound(SoundEvent.ENTITY_TNT_PRIMED, Sound.Source.BLOCK, 1.0f, 1.0f))
                // the wire explosion packet carries no radius through Via, so the 1.8 client never picks its own hugeexplosion
                .register(EXPLOSION_EMITTER, FxHandler.particle(Particle.EXPLOSION_EMITTER, 1, 0, 0f));
    }

    /** The modern (26.1) fx - the {@code Vanilla} preset sets this. {@link #vanilla18()} plus the 1.9+ melee attack sound. */
    public static @NotNull FxRegistry modern() {
        return vanilla18()
                .register(CRIT, FxHandler.hitAnimation(EntityAnimationPacket.Animation.CRITICAL_EFFECT)
                        .and(FxHandler.sound(SoundEvent.ENTITY_PLAYER_ATTACK_CRIT, Sound.Source.PLAYER, 1.0f, 1.0f)))
                // ThrownEnderpearl.playSound: positional at the destination, PLAYERS category
                .register(PEARL_TELEPORT, pearlTeleport());
    }

    /** The vanilla pearl landing: positional at the destination, so it fades with distance. */
    public static @NotNull FxHandler pearlTeleport() {
        return ctx -> ctx.sound(SoundEvent.ENTITY_PLAYER_TELEPORT, Sound.Source.PLAYER, 1.0f, 1.0f);
    }

    /** Hypixel BEDWARS only (their other modes stay positional): the landing reaches the whole game at full
     *  volume regardless of distance, so it rides a per-mode registry, not the shared Hypixel one. */
    public static @NotNull FxHandler pearlTeleportGameWide() {
        return ctx -> ctx.globalSound(SoundEvent.ENTITY_PLAYER_TELEPORT, Sound.Source.PLAYER, 1.0f, 1.0f);
    }

    /**
     * The arrow hit-marker "ding" a PvP preset registers under {@link #ARROW_HIT_PLAYER}: a sound to the SHOOTER only
     * when their arrow hits a player. Vanilla (26.1): {@code entity.arrow.hit_player} vol 0.18 pitch 0.45, which
     * ViaVersion maps to 1.8 {@code random.successful_hit}. Vanilla 1.8 has none; mmc18/hypixel backport it - capture
     * to confirm their pitch.
     */
    public static @NotNull FxHandler arrowHitMarker() {
        return ctx -> ctx.sourceSound(SoundEvent.ENTITY_ARROW_HIT_PLAYER, Sound.Source.PLAYER, 0.18f, 0.45f);
    }

    private static float eatVolume() { return ThreadLocalRandom.current().nextBoolean() ? 0.5f : 1.0f; }

    /** Vanilla {@code (rand - rand) * spread + 1}. */
    private static float jitterPitch(float spread) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        return (r.nextFloat() - r.nextFloat()) * spread + 1.0f;
    }

    private static float drinkPitch() { return ThreadLocalRandom.current().nextFloat() * 0.1f + 0.9f; }

    /** A throwable's launch sound: server-driven to everyone - the 1.8 client does NOT self-predict the throw. */
    private static FxHandler throwSound(SoundEvent sound, Sound.Source src) {
        return ctx -> ctx.sound(sound, src, 0.5f, throwPitch());
    }

    private static float throwPitch() { return 0.4f / (ThreadLocalRandom.current().nextFloat() * 0.4f + 0.8f); }

    // vanilla BowItem: 1/(rand*0.4+1.2) + power*0.5; the power term is approximated at full draw (the common PvP release)
    private static float bowPitch() { return 1.0f / (ThreadLocalRandom.current().nextFloat() * 0.4f + 1.2f) + 0.5f; }

    // vanilla AbstractArrow hit pitch
    private static float arrowHitPitch() { return 1.2f / (ThreadLocalRandom.current().nextFloat() * 0.2f + 0.9f); }
}
