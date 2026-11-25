package io.github.term4.polyp.tracking.motion;

import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Vec;

/**
 * Snaps an outgoing velocity onto the 1.8 wire grid so it matches a native 1.8 server: each axis is
 * {@code (int)(blocksPerTick*8000)} (truncates toward zero), clamped to a per-axis cap (default {@link #DEFAULT_CAP},
 * vanilla's {@code PacketPlayOutEntityVelocity} limit). Gated by {@code KnockbackConfig.quantizeVelocity}.
 *
 * <p>{@link #snap} is byte-exact only for |v| &lt;= 2 b/t; above that the 26.1 LpVec3 wire is coarser than the 1.8 short
 * grid, so a snapped value drifts &lt;=1 wire-tick through Via. For the knocked player above that band, {@link #wireShorts}
 * + {@code LegacyVelocityBridge} send the exact short over ViaBridge; {@code snap} is the fallback.
 */
public final class LegacyVelocity {

    /** 1.8 wire unit: shorts per block-per-tick. */
    private static final double WIRE_SCALE = 8000.0;
    /** Vanilla 1.8's {@code PacketPlayOutEntityVelocity} clamp (b/t); the wire saturates near {@code +-4.0}. */
    public static final double DEFAULT_CAP = 3.9;
    private static final double LP_EXACT_BT = 2.0;

    private LegacyVelocity() {}

    public static Vec snap(Vec perSecond) {
        return snap(perSecond, DEFAULT_CAP);
    }

    public static Vec snap(Vec perSecond, double capBt) {
        double tps = ServerFlag.SERVER_TICKS_PER_SECOND;
        return new Vec(snapAxis(perSecond.x(), tps, capBt), snapAxis(perSecond.y(), tps, capBt), snapAxis(perSecond.z(), tps, capBt));
    }

    private static double snapAxis(double perSecond, double tps, double capBt) {
        return wireShort(perSecond, tps, capBt) * tps / WIRE_SCALE;   // multiply before divide stays on-grid
    }

    /** The {@code SET_ENTITY_MOTION} payload for the exact ViaBridge path. */
    public static short[] wireShorts(Vec perSecond) {
        return wireShorts(perSecond, DEFAULT_CAP);
    }

    public static short[] wireShorts(Vec perSecond, double capBt) {
        double tps = ServerFlag.SERVER_TICKS_PER_SECOND;
        return new short[]{wireShort(perSecond.x(), tps, capBt), wireShort(perSecond.y(), tps, capBt), wireShort(perSecond.z(), tps, capBt)};
    }

    private static short wireShort(double perSecond, double tps, double capBt) {
        double bt = Math.max(-capBt, Math.min(capBt, perSecond / tps));
        return (short) (int) (bt * WIRE_SCALE);   // vanilla truncates, not rounds
    }

    public static double snapAxisBt(double bt, double capBt) {
        double capped = Math.max(-capBt, Math.min(capBt, bt));
        return (short) (int) (capped * WIRE_SCALE) / WIRE_SCALE;
    }

    /** Above 2 b/t {@link #snap} drifts through Via; gates the exact ViaBridge path. */
    public static boolean exceedsLpExactBand(Vec perSecond) {
        double tps = ServerFlag.SERVER_TICKS_PER_SECOND;
        return Math.abs(perSecond.x() / tps) > LP_EXACT_BT
                || Math.abs(perSecond.y() / tps) > LP_EXACT_BT
                || Math.abs(perSecond.z() / tps) > LP_EXACT_BT;
    }
}
