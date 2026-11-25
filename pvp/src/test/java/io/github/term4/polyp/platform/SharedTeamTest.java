package io.github.term4.polyp.platform;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.platform.compatibility.Compat18;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * No-push is CLIENT-predicted, so what matters is each client's replayed scoreboard: adds for unknown teams are
 * ignored, and an add MOVES the entity off its previous team. The union roster exists because two lib teams
 * (no-push + arrow-visibility) were erasing each other's members through exactly that move.
 */
class SharedTeamTest extends HeadlessServerTest {

    private static final String TEAM = "polyp_lib";

    @Test
    void unionRosterSurvivesBothFeaturesAndDowngradesRule() {
        Instance inst = flatInstance(MechanicsProfile.builder().set(MechanicsKeys.COMPAT, Compat18.config()).build());
        FakePlayer a = FakePlayer.connect(inst, new Pos(8.5, 64, 8.5), "NoPushA");
        FakePlayer b = FakePlayer.connect(inst, new Pos(10.5, 64, 8.5), "NoPushB");

        assertEquals(TeamsPacket.CollisionRule.NEVER, new ClientScoreboard(a).rule, "A sees the no-push rule");
        ClientScoreboard late = new ClientScoreboard(b);
        assertEquals(TeamsPacket.CollisionRule.NEVER, late.rule, "the late joiner sees the no-push rule");
        assertTrue(late.members(TEAM).containsAll(Set.of("NoPushA", "NoPushB")),
                "the late joiner knows both members - above all ITSELF, or it keeps predicting pushes");
        assertEquals((byte) 0, late.friendlyFlags, "friendly fire off (the arrow-visibility pairing)");

        // the regression: a second team-borne feature must not MOVE the player off the team
        SharedTeam.set(a.player, SharedTeam.Reason.ARROW_VISIBILITY, true);
        ClientScoreboard afterArrow = new ClientScoreboard(b);
        assertEquals(TEAM, afterArrow.teamOf("NoPushA"), "arrow-vis enrollment lands on the SAME team");
        assertTrue(afterArrow.members(TEAM).contains("NoPushB"), "and nobody else was displaced");

        polyp.profiles().setPlayer(b.player, MechanicsProfile.builder().set(MechanicsKeys.COMPAT, Compat18.off()).build());
        assertFalse(new ClientScoreboard(a).members(TEAM).contains("NoPushB"), "withdrawal reaches the other clients");
        assertEquals(TeamsPacket.CollisionRule.NEVER, new ClientScoreboard(a).rule);

        polyp.profiles().setPlayer(a.player, MechanicsProfile.builder().set(MechanicsKeys.COMPAT, Compat18.off()).build());
        ClientScoreboard downgraded = new ClientScoreboard(b);
        assertEquals(TEAM, downgraded.teamOf("NoPushA"), "arrow-vis keeps A on the team");
        assertEquals(TeamsPacket.CollisionRule.ALWAYS, downgraded.rule, "no NO_PUSH enrollee left -> vanilla pushes return");

        SharedTeam.set(a.player, SharedTeam.Reason.ARROW_VISIBILITY, false);
        polyp.profiles().setPlayer(a.player, null);
        polyp.profiles().setPlayer(b.player, null);
    }

    /** Replays captured team packets the way the vanilla client does. */
    private static final class ClientScoreboard {
        final Map<String, Set<String>> teams = new HashMap<>();
        TeamsPacket.CollisionRule rule;
        byte friendlyFlags = -1;

        ClientScoreboard(FakePlayer p) {
            for (TeamsPacket packet : p.sent(TeamsPacket.class)) {
                switch (packet.action()) {
                    case TeamsPacket.CreateTeamAction c -> {
                        teams.put(packet.teamName(), new HashSet<>());
                        c.entities().forEach(e -> move(e, packet.teamName()));
                        settings(packet.teamName(), c.settings());
                    }
                    case TeamsPacket.UpdateTeamAction u -> {
                        if (teams.containsKey(packet.teamName())) settings(packet.teamName(), u.settings());
                    }
                    case TeamsPacket.AddEntitiesToTeamAction add -> {
                        if (teams.containsKey(packet.teamName())) add.entities().forEach(e -> move(e, packet.teamName()));
                    }
                    case TeamsPacket.RemoveEntitiesToTeamAction rm -> {
                        Set<String> members = teams.get(packet.teamName());
                        if (members != null) rm.entities().forEach(members::remove);
                    }
                    case TeamsPacket.RemoveTeamAction ignored -> teams.remove(packet.teamName());
                    default -> { }
                }
            }
        }

        private void settings(String teamName, TeamsPacket.Settings settings) {
            if (!TEAM.equals(teamName)) return;
            rule = settings.collisionRule();
            friendlyFlags = settings.friendlyFlags();
        }

        private void move(String entity, String to) {
            teams.values().forEach(members -> members.remove(entity));
            teams.get(to).add(entity);
        }

        Set<String> members(String teamName) {
            return teams.getOrDefault(teamName, Set.of());
        }

        String teamOf(String entity) {
            return teams.entrySet().stream()
                    .filter(e -> e.getValue().contains(entity))
                    .map(Map.Entry::getKey).findFirst().orElse(null);
        }
    }
}
