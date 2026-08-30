package io.github.term4.polyp.mechanics.attribute;

import io.github.term4.polyp.mechanics.attribute.defense.Bypass;
import io.github.term4.polyp.mechanics.attribute.defense.MitigationRequest;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The staged mitigation pipeline: custom stages run in list order and the run short-circuits at 0. */
class MitigationPipelineTest extends HeadlessServerTest {

    private static final MitigationPipeline.Stage HALVE = s -> s.damage /= 2;
    private static final MitigationPipeline.Stage MINUS_THREE = s -> s.damage -= 3;

    private MitigationPipeline.State state(float damage) {
        return new MitigationPipeline.State(services.attributes(), zombie(new Pos(0, 64, 800)),
                MitigationRequest.of(Set.of(), Bypass.NONE, new Random(1)), AttributeConfig.builder().build(), damage);
    }

    @Test
    void stagesRunInListOrder() {
        assertEquals(2f, MitigationPipeline.run(List.of(HALVE, MINUS_THREE), state(10f)));   // 10/2 - 3
        assertEquals(3.5f, MitigationPipeline.run(List.of(MINUS_THREE, HALVE), state(10f))); // (10-3)/2
    }

    @Test
    void zeroShortCircuits() {
        assertEquals(0f, MitigationPipeline.run(List.of(MINUS_THREE, HALVE), state(3f)));
    }

    @Test
    void vanillaOrderNoOpsWithoutDefenseConfigs() {
        // armor/protection null + no resistance effect: the vanilla stages pass the damage through
        assertEquals(7f, MitigationPipeline.run(null, state(7f)));
    }
}
