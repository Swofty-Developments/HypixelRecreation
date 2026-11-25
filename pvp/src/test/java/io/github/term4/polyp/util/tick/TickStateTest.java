package io.github.term4.polyp.util.tick;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A stamp made under another clock (domain transfer) must read expired, never as a huge remaining window. */
class TickStateTest {

    @Test
    void foreignClockStampsReadExpired() {
        TickState s = new TickState(1_000, 10);
        assertTrue(s.isActive(1_005));
        assertFalse(s.isActive(50));
        assertFalse(s.isActiveWithin(50, 100));
        assertTrue(s.isStaleAfter(50, 100));
        assertEquals(0, s.remainingTicks(50));
    }
}
