package io.github.term4.polyp.util.tick;

import net.kyori.adventure.key.Key;
import net.minestom.server.ServerFlag;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Scales tick-based values to the live server TPS. Both baselines default to {@link TickScalingConfig#SERVER_TPS}
 * (track the server TPS -> <b>no scaling</b>); opt in with a fixed baseline. Durations scale against {@code referenceTps}
 * (per-scope and per-module, a system's {@code KEY}); client-coupled physics + velocity against {@code clientTps}.
 * Identity whenever a baseline equals the live server TPS.
 *
 * <p>Setting {@code clientTps} per scope is time dilation - see {@link TickScalingConfig#simulated}.
 */
public final class TickScaler {

    private TickScaler() {}

    /** Fallback when a subject's own scope chain sets none. */
    private static volatile TickScalingConfig global = TickScalingConfig.DEFAULTS;

    /** Per-subject scope walk (player/world/instance); {@code null} = that chain sets none. */
    private static volatile Function<@Nullable Entity, @Nullable TickScalingConfig> resolver = e -> null;

    public static void setGlobal(@Nullable TickScalingConfig cfg) {
        global = cfg != null ? cfg : TickScalingConfig.DEFAULTS;
    }

    /** Installs the scope resolver, so physics reads the SUBJECT's scaling rather than global only. */
    public static void resolver(@Nullable Function<@Nullable Entity, @Nullable TickScalingConfig> fn) {
        resolver = fn != null ? fn : e -> null;
    }

    private record Memo(long aliveTicks, TickScalingConfig cfg) {}

    private static final Tag<Memo> MEMO = Tag.Transient("polyp:tick-scaling-memo");

    /**
     * {@code subject}'s effective scaling, memoized per subject-tick. Keyed on alive-ticks, not a profile generation:
     * shards share one instance, so only re-resolving catches a transfer between them.
     */
    private static TickScalingConfig scaling(@Nullable Entity subject) {
        if (subject == null) return global;
        long alive = subject.getAliveTicks();
        Memo memo = subject.getTag(MEMO);
        if (memo != null && memo.aliveTicks() == alive) return memo.cfg();
        TickScalingConfig scoped = resolver.apply(subject);
        TickScalingConfig out = scoped != null ? scoped : global;
        subject.setTag(MEMO, new Memo(alive, out));
        return out;
    }

    private static int serverTps() { return ServerFlag.SERVER_TICKS_PER_SECOND; }

    /** The {@link TickScalingConfig#SERVER_TPS} sentinel means "track the server TPS" (-> no scaling). */
    private static int resolve(int baseline) {
        return baseline == TickScalingConfig.SERVER_TPS ? serverTps() : baseline;
    }

    // ---- durations: armed once, then counted down in SERVER ticks, so they stretch at the arm point ----

    /** {@code module}'s duration from an already-resolved config ({@code null} = defaults). */
    public static int duration(int baseTicks, @Nullable TickScalingConfig cfg, Key module) {
        int ref = resolve((cfg != null ? cfg : TickScalingConfig.DEFAULTS).referenceTps(module));
        return scaleDuration(baseTicks, serverTps(), ref);
    }

    /** {@code module}'s duration in {@code subject}'s scope - prefer this wherever a subject exists. */
    public static int duration(@Nullable Entity subject, int baseTicks, Key module) {
        return scaleDuration(baseTicks, serverTps(), resolve(scaling(subject).referenceTps(module)));
    }

    static int scaleDuration(int baseTicks, int serverTps, int referenceTps) {
        return Math.round(baseTicks * (float) serverTps / referenceTps);
    }

    // ---- physics: per-server-tick values, re-rated so the arc matches the rate the client integrates at ----

    /** Native-rate steps one server tick covers ({@code simTps / serverTps}); also the physics fraction. */
    public static double stepsPerTick(@Nullable Entity subject) {
        return resolve(scaling(subject).clientTps()) / (double) serverTps();
    }

    /** Per-server-tick drag, so {@code drag^elapsed} matches the client-rate decay exactly. */
    public static double dragPerTick(@Nullable Entity subject, double drag) {
        return Math.pow(drag, stepsPerTick(subject));
    }

    /** Re-rates a blocks/tick value (impulse, cap, set-value) to the server rate. */
    public static double impulse(@Nullable Entity subject, double valueBt) {
        return valueBt * stepsPerTick(subject);
    }

    /** Per-server-tick gravity for a blocks/tick velocity: {@code gravity × s²}. */
    public static double gravityPerTick(@Nullable Entity subject, double gravity) {
        double s = stepsPerTick(subject);
        return gravity * s * s;
    }

    /** {@code base} with gravity and both drags re-rated - one resolve for a whole physics step. */
    public static Aerodynamics aerodynamics(@Nullable Entity subject, Aerodynamics base) {
        double s = stepsPerTick(subject);
        if (s == 1.0) return base;
        return new Aerodynamics(base.gravity() * s * s,
                Math.pow(base.horizontalAirResistance(), s), Math.pow(base.verticalAirResistance(), s));
    }

    // ---- cadences + the wire ----

    /** An "every N client-ticks" broadcast throttle, in server ticks: past 20 TPS a client cannot use the extra packets.
     *  Divides by the FIXED client ceiling, not any scope's rate - one packet serves viewers at mixed rates, so it must
     *  be sized for the fastest; a dilated subject throttling its own position broadcasts teleports for every watcher.
     *  Per-scope tuning is {@code positionBroadcastInterval}. */
    public static int clientCadence(int baseTicks) {
        return Math.max(1, Math.round(baseTicks * (float) serverTps() / TickScalingConfig.VANILLA_CLIENT_TPS));
    }

    /** A resync interval authored in client ticks. Rides the prediction baseline, not the duration one;
     *  {@code <= 0} passes through (a silent-wire sentinel, never a rate). */
    public static int clientTicks(@Nullable Entity subject, int baseTicks) {
        if (baseTicks <= 0) return baseTicks;
        return Math.max(1, Math.round(baseTicks * (float) serverTps() / resolve(scaling(subject).clientTps())));
    }

    /** What a client can ACTUALLY simulate at: {@code min(clientTps, 20)}. Vanilla floors its own tick period at
     *  50ms ({@code Minecraft.getTickTargetMillis}), so it follows a tick rate downward only, never faster than 20. */
    public static int effectiveClientTps(@Nullable Entity subject) {
        return Math.min(resolve(scaling(subject).clientTps()), TickScalingConfig.VANILLA_CLIENT_TPS);
    }

    /** A server per-tick displacement as wire b/t. Above 20 the capped client gets a proportionally BIGGER step,
     *  so it keeps pace instead of falling behind and being resynced forward (vanilla's own /tick rate 40 stutter). */
    public static Vec wireVelocity(@Nullable Entity subject, Vec perServerTickDisplacement) {
        return perServerTickDisplacement.mul(serverTps() / (double) effectiveClientTps(subject));
    }

    /** Inverse of {@link #wireVelocity} for a single step: a client/vanilla b/t velocity to the server rate. */
    public static Vec fromClientVelocity(@Nullable Entity subject, Vec clientBt) {
        return clientBt.mul(stepsPerTick(subject));
    }
}
