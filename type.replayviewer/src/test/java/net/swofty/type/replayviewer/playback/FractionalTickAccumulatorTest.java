package net.swofty.type.replayviewer.playback;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FractionalTickAccumulatorTest {
    @Test
    void delaysQuarterAndHalfSpeedTicks() {
        FractionalTickAccumulator accumulator = new FractionalTickAccumulator();
        assertEquals(List.of(0, 0, 0, 1), frames(accumulator, 0.25f, 4));
        accumulator.reset();
        assertEquals(List.of(0, 1, 0, 1), frames(accumulator, 0.5f, 4));
    }

    @Test
    void advancesEveryInterveningFastTick() {
        FractionalTickAccumulator accumulator = new FractionalTickAccumulator();
        assertEquals(List.of(4, 4, 4), frames(accumulator, 4.0f, 3));
    }

    private List<Integer> frames(FractionalTickAccumulator accumulator, float speed, int count) {
        List<Integer> result = new ArrayList<>(count);
        for (int index = 0; index < count; index++) result.add(accumulator.advance(speed));
        return result;
    }
}
