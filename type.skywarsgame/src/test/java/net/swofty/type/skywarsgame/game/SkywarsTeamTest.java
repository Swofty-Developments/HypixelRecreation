package net.swofty.type.skywarsgame.game;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkywarsTeamTest {
    @Test
    void tracksPlayersUsingTheSharedTeamContract() {
        SkywarsTeam team = new SkywarsTeam(3);
        UUID player = UUID.randomUUID();

        assertEquals("3", team.getId());
        assertEquals("Team 4", team.getName());
        assertFalse(team.hasPlayers());

        team.addPlayer(player);

        assertTrue(team.hasPlayers());
        assertTrue(team.hasPlayer(player));
        assertEquals(1, team.getPlayerCount());

        team.removePlayer(player);

        assertFalse(team.hasPlayers());
    }
}
