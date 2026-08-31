package io.github.term4.polyp.tracking;

import io.github.term4.polyp.util.tick.TickSystem;
import io.github.term4.polyp.util.tick.TickState;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerStartSprintingEvent;
import net.minestom.server.event.player.PlayerStopSprintingEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

// Combat/knockback reads sprint through here; MotionTracker's raw isSprinting() reads are physics, not combat timing,
// so they stay raw by design.
public final class SprintTracker implements Tracker {

    private static final Tag<TickState> LAST_SPRINT_STATE = Tag.Transient("polyp:last-sprint-state");
    private static final Tag<TickState> LAST_CLIENT_START_SPRINT = Tag.Transient("polyp:client-sprint-start");
    private static final Tag<TickState> LAST_CLIENT_STOP_SPRINT  = Tag.Transient("polyp:client-sprint-stop");
    private static final AtomicBoolean CLOCK_RESET = new AtomicBoolean();

    public SprintTracker() {
        if (CLOCK_RESET.compareAndSet(false, true)) {
            TickSystem.onClockChange(e -> {
                if (e instanceof Player p) clearTransient(p);
            });
        }
    }

    public void markStopSprint(Player player) {
        player.setTag(LAST_SPRINT_STATE, new TickState(TickSystem.tick(player), 0));
    }

    @Override
    public EventNode<@NotNull PlayerEvent> node() {
        EventNode<@NotNull PlayerEvent> node = EventNode.type("polyp:sprint-tracker", EventFilter.PLAYER);

        node.addListener(PlayerStartSprintingEvent.class, e -> {
            Player p = e.getPlayer();
            p.setTag(LAST_CLIENT_START_SPRINT, new TickState(TickSystem.tick(p), 0));
        });

        node.addListener(PlayerStopSprintingEvent.class, e -> {
            Player p = e.getPlayer();
            markStopSprint(p);
            p.setTag(LAST_CLIENT_STOP_SPRINT, new TickState(TickSystem.tick(p), 0));
        });

        // instance change reseeds the clock these stamps use, and isClientSprinting compares raw eventTicks (no
        // TickState future-guard), so drop them or it misreads across instances
        node.addListener(PlayerSpawnEvent.class, e -> clearTransient(e.getPlayer()));

        return node;
    }

    public static void clearTransient(Player p) {
        p.removeTag(LAST_SPRINT_STATE);
        p.removeTag(LAST_CLIENT_START_SPRINT);
        p.removeTag(LAST_CLIENT_STOP_SPRINT);
    }

    /** Falls back to raw sprint state without a tracker. */
    public static boolean wasRecentlySprinting(@Nullable SprintTracker t, Entity e, long ticks) {
        if (!(e instanceof Player p)) return e.isSprinting();
        if (t == null || p.isSprinting()) return p.isSprinting();
        TickState state = p.getTag(LAST_SPRINT_STATE);
        if (state == null) return false;
        return state.isActiveWithin(TickSystem.tick(p), (int) ticks);
    }

    /** Whether the client believes it is sprinting, even when the server does not. */
    public static boolean isClientSprinting(@Nullable SprintTracker t, Entity e) {
        if (t == null) return e.isSprinting();
        TickState start = e.getTag(LAST_CLIENT_START_SPRINT);
        TickState stop  = e.getTag(LAST_CLIENT_STOP_SPRINT);
        if (start == null) return e.isSprinting();
        if (stop == null) return true;
        return start.eventTick() > stop.eventTick();
    }

    public static boolean wasClientRecentlySprinting(@Nullable SprintTracker t, Entity e, int ticks) {
        if (t == null) return e.isSprinting();
        if (isClientSprinting(t, e)) return true;
        TickState stop = e.getTag(LAST_CLIENT_STOP_SPRINT);
        // no stop ever recorded = never sprinted (fresh join)
        if (stop == null) return false;
        return stop.isActiveWithin(TickSystem.tick(e), ticks);
    }
}
