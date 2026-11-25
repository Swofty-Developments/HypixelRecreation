package io.github.term4.polyp.tracking.motion;

import net.minestom.server.instance.block.Block;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.registry.TagKey;

import java.util.OptionalDouble;

/**
 * Pluggable ladder/climb behavior for {@link MotionTracker}: built-ins {@link #LEGACY} (1.8) and {@link #MODERN}
 * (26.1), or a custom impl. Only the version-dependent parts live here; sneak-hold and the ladder clamp are
 * model-agnostic and stay in {@link MotionTracker}. Selected per preset via {@link VelocityConfig#climbModel}.
 */
public interface ClimbModel {

    /** Vanilla climb-up motY, pre-gravity ({@code -> ~0.1176} after it). */
    double CLIMB_UP = 0.2;

    boolean isClimbable(Block feet);

    /** Climb-up motY (b/t) to fold, or empty. {@code motClipped} = the server-tracked mot clipped a wall
     *  this step - vanilla {@code positionChanged}, which is the server move, never the client's WASD. */
    OptionalDouble climbUpMotY(double clientDy, boolean motClipped);

    /** 1.8: LADDER/VINE only. Normal climbing folds the slide value (measured); {@code 0.2} fires only on a
     *  server-mot clip ({@code g()}: {@code positionChanged && k_()}), e.g. knockback into the ladder wall. */
    ClimbModel LEGACY = new ClimbModel() {
        @Override public boolean isClimbable(Block feet) {
            return feet.compare(Block.LADDER) || feet.compare(Block.VINE);
        }
        @Override public OptionalDouble climbUpMotY(double clientDy, boolean motClipped) {
            return motClipped ? OptionalDouble.of(CLIMB_UP) : OptionalDouble.empty();
        }
    };

    /** 26.1: the full {@code minecraft:climbable} tag + climb-up folded while ascending. */
    ClimbModel MODERN = new ClimbModel() {
        /** {@code null} if absent from the bundled registry - then ladder/vine. */
        private final RegistryTag<Block> climbable = Block.staticRegistry().getTag(TagKey.ofHash("#minecraft:climbable"));
        /** Above this (b/t) the client is climbing; ignores float jitter. */
        private static final double CLIMB_MIN_DY = 0.01;

        @Override public boolean isClimbable(Block feet) {
            return climbable != null ? climbable.contains(feet) : feet.compare(Block.LADDER) || feet.compare(Block.VINE);
        }
        @Override public OptionalDouble climbUpMotY(double clientDy, boolean motClipped) {
            return clientDy > CLIMB_MIN_DY || motClipped ? OptionalDouble.of(CLIMB_UP) : OptionalDouble.empty();
        }
    };
}
