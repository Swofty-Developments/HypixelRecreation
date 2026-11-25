package io.github.term4.polyp.util.tick;

import io.github.term4.polyp.mechanics.damage.DamageSystem;
import io.github.term4.polyp.mechanics.knockback.KnockbackSystem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** {@link TickScaler} duration math: {@code round(baseTicks × serverTps / referenceTps)}, identity at the default baseline. */
class TickScalerTest {

    @Test
    void stretchesDurationToServerTps() {
        assertEquals(10, TickScaler.scaleDuration(10, 20, 20));    // default 20/20: identity
        assertEquals(500, TickScaler.scaleDuration(10, 1000, 20)); // 0.5s stays 0.5s at 1000 TPS
        assertEquals(30, TickScaler.scaleDuration(10, 60, 20));    // 0.5s stays 0.5s at 60 TPS
        assertEquals(5, TickScaler.scaleDuration(10, 10, 20));     // below baseline TPS -> shorter
    }

    @Test
    void referenceTpsShiftsTheBaseline() {
        assertEquals(10, TickScaler.scaleDuration(10, 100, 100)); // authored at 100, run at 100: literal
        assertEquals(2, TickScaler.scaleDuration(10, 20, 100));   // authored at 100, run at 20: compressed
    }

    @Test
    void perModuleReferenceTpsOverridesTheDefault() {
        TickScalingConfig cfg = TickScalingConfig.builder()
                .referenceTps(100)                       // default feel baseline
                .referenceTps(KnockbackSystem.KEY, 20)   // knockback pinned to vanilla
                .build();
        assertEquals(20, cfg.referenceTps(KnockbackSystem.KEY));
        assertEquals(100, cfg.referenceTps(DamageSystem.KEY));
    }

    @Test
    void defaultIsNoScaling() {
        // both baselines default to the SERVER_TPS sentinel: track the server TPS, so nothing scales
        assertEquals(TickScalingConfig.SERVER_TPS, TickScalingConfig.DEFAULTS.clientTps());
        assertEquals(TickScalingConfig.SERVER_TPS, TickScalingConfig.DEFAULTS.referenceTps(DamageSystem.KEY));
        // the sentinel resolves to the live server TPS, so durations stay literal at any rate
        assertEquals(10, TickScaler.scaleDuration(10, 1000, 1000));
        assertEquals(10, TickScaler.scaleDuration(10, 20, 20));
    }
}
