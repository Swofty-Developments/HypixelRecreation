package io.github.term4.polyp.platform;

import io.github.term4.polyp.Polyp;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.network.packet.server.play.TeamsPacket.CollisionRule;
import net.minestom.server.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * The ONE lib scoreboard team ({@code polyp_lib}). Client-side team membership is exclusive - a later add to another team
 * silently moves the entity off the previous one - so every team-borne fix shares this roster; separate lib teams would
 * erase each other's members on the client. Members = the union of per-feature enrollments. Cosmetically neutral: no
 * color/prefix, friendly fire off ({@link Reason#ARROW_VISIBILITY} requires it; harmless otherwise - server-side damage
 * ignores teams).
 *
 * <p>{@link Reason#NO_PUSH}: entity pushback is CLIENT-predicted, and the only vanilla-wire off switch is
 * {@code collisionRule NEVER}. Players only - the client computes every push against its LOCAL player
 * ({@code EntitySelector.pushableBy}), so the player's own NEVER membership blocks pushes both ways against anything;
 * mobs never need the team. The rule is NEVER while anyone is enrolled for it, ALWAYS otherwise (an arrow-vis-only
 * server keeps vanilla pushes).
 *
 * <p>An app that runs its own scoreboard teams (nametag colors, spectator groups) must keep players off this roster
 * (disable the knobs) and set {@code collisionRule NEVER} / friendly-fire flags on ITS teams instead. Scoreboard teams
 * cannot express per-pair behavior at all, only partitions - see the compat docs.
 */
public final class SharedTeam {

    /** Why a player is on the roster; membership is the union of reasons. */
    public enum Reason {
        /** 1.8 arrow-visibility fix: shooter and target must share a friendly-fire-off team. */
        ARROW_VISIBILITY,
        /** {@code CompatConfig.disableEntityPush}: collision rule NEVER while anyone is enrolled for this. */
        NO_PUSH
    }

    private static final String TEAM_NAME = "polyp_lib"; // <=16 chars (the 1.8 wire limit)

    private static Team team;
    private static final Map<String, EnumSet<Reason>> roster = new HashMap<>();

    private SharedTeam() {}

    /** Installs the disconnect cleanup. Called by {@code Polyp.init()}. */
    public static void install(Polyp polyp) {
        EventNode<@NotNull PlayerEvent> node = EventNode.type("polyp:shared-team", EventFilter.PLAYER);
        node.addListener(PlayerDisconnectEvent.class, e -> onDisconnect(e.getPlayer()));
        polyp.install(node);
    }

    /** Enrolls or withdraws {@code player} for {@code reason}; each feature calls this on its own config re-evaluation. */
    public static synchronized void set(@NotNull Player player, @NotNull Reason reason, boolean on) {
        String name = player.getUsername();
        EnumSet<Reason> reasons = roster.get(name);
        if (on) {
            if (reasons == null) roster.put(name, reasons = EnumSet.noneOf(Reason.class));
            if (!reasons.add(reason)) return;
        } else {
            if (reasons == null || !reasons.remove(reason)) return;
            if (reasons.isEmpty()) roster.remove(name);
        }
        sync(name);
    }

    private static synchronized void onDisconnect(Player player) {
        if (roster.remove(player.getUsername()) != null) sync(player.getUsername());
    }

    /** Reconciles the Minestom team with the roster: membership for {@code name}, collision rule for everyone. */
    private static void sync(String name) {
        boolean member = roster.containsKey(name);
        if (team == null) {
            if (!member) return;
            // default team flags = friendly fire off; registration broadcasts to everyone online, and Minestom
            // sends all registered teams (with members) to each later joiner in the play init
            team = MinecraftServer.getTeamManager().createBuilder(TEAM_NAME).collisionRule(wantedRule()).build();
        }
        CollisionRule rule = wantedRule();
        if (team.getCollisionRule() != rule) team.updateCollisionRule(rule);
        if (member && !team.getMembers().contains(name)) team.addMember(name);
        else if (!member && team.getMembers().contains(name)) team.removeMember(name);
    }

    private static CollisionRule wantedRule() {
        for (EnumSet<Reason> reasons : roster.values()) {
            if (reasons.contains(Reason.NO_PUSH)) return CollisionRule.NEVER;
        }
        return CollisionRule.ALWAYS;
    }
}
