package io.github.term4.polyp.tracking.motion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Takeoff motY = the jump_strength base plus Jump Boost's float-exact {@code +0.1/level} - vanilla
 * {@code bF()}/{@code getJumpPower} add a separate float term, not an attribute.
 */
class JumpVelocityTest {

    private static final double BASE = VelocityConfig.JUMP_VELOCITY; // 0.41999998688697815, the float-exact 0.42 takeoff

    @Test
    void noJumpBoostIsBareBase() {
        assertEquals(BASE, MotionTracker.jumpSeed(BASE, -1), 0.0);
    }

    @Test
    void jumpBoostAddsFloatExactTenthPerLevel() {
        // vanilla: motY += (double)((float)(amplifier + 1) * 0.1f)
        assertEquals(BASE + (double) (1f * 0.1f), MotionTracker.jumpSeed(BASE, 0), 0.0); // Jump Boost I
        assertEquals(BASE + (double) (2f * 0.1f), MotionTracker.jumpSeed(BASE, 1), 0.0); // Jump Boost II
        assertEquals(BASE + (double) (5f * 0.1f), MotionTracker.jumpSeed(BASE, 4), 0.0); // Jump Boost V
    }

    @Test
    void passesAnArbitraryBaseThrough() {
        // the +0.1/level term stays float-exact (0.1f widens to 0.10000000149...)
        assertEquals(0.5 + (double) (1f * 0.1f), MotionTracker.jumpSeed(0.5, 0), 0.0);
    }
}
