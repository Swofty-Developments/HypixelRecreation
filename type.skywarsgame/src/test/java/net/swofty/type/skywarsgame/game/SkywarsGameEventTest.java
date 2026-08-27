package net.swofty.type.skywarsgame.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkywarsGameEventTest {
    @Test
    void followsTheInGameEventOrder() {
        assertEquals(SkywarsGame.GameEvent.FIRST_REFILL, SkywarsGame.GameEvent.GAME_START.getNext());
        assertEquals(SkywarsGame.GameEvent.SECOND_REFILL, SkywarsGame.GameEvent.FIRST_REFILL.getNext());
        assertEquals(SkywarsGame.GameEvent.DRAGON_SPAWN, SkywarsGame.GameEvent.SECOND_REFILL.getNext());
        assertEquals(SkywarsGame.GameEvent.GAME_END, SkywarsGame.GameEvent.DRAGON_SPAWN.getNext());
        assertEquals(SkywarsGame.GameEvent.GAME_END, SkywarsGame.GameEvent.GAME_END.getNext());
    }
}
