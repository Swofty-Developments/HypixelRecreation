package io.github.term4.polyp.util.tick;

import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntitySpawnEvent;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.event.instance.InstanceUnregisterEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * The library's tick system in one place: the server-wide clock, the per-instance clocks, and the per-tick update loop.
 * {@link #serverTick()} is a server-wide counter for state no instance owns; {@link #instanceTick(Instance)} advances once
 * per {@link InstanceTickEvent} and backs everything timed per entity/world (the two run on different phases, so a value
 * stamped on one clock must not be judged against the other). Per-tick work installs as a {@link Tickable}
 * ({@link #register}); each instance tick advances its clock then dispatches in {@link TickPhase} + registration order,
 * gated by interval. {@link #start()} once at init.
 */
public final class TickSystem {

    private static final Logger LOG = LoggerFactory.getLogger(TickSystem.class);

    private TickSystem() {}

    private static final AtomicBoolean STARTED = new AtomicBoolean();

    private static final AtomicLong serverTick = new AtomicLong();
    /** Per-instance tick counters (removed when an instance unregisters). */
    private static final Map<Instance, AtomicLong> CLOCKS = new ConcurrentHashMap<>();
    /** Registered work, bucketed by phase; insertion order within a phase. */
    private static final Map<TickPhase, List<Tickable>> BY_PHASE = new EnumMap<>(TickPhase.class);
    private static final TickPhase[] PHASES = TickPhase.values(); // values() clones per call; dispatch is per-tick

    static { for (TickPhase p : PHASES) BY_PHASE.put(p, new CopyOnWriteArrayList<>()); }

    /** Starts the clocks and the dispatch loop. Idempotent; call once at startup. */
    public static void start() {
        if (!STARTED.compareAndSet(false, true)) return;
        MinecraftServer.getSchedulerManager()
                .buildTask(serverTick::incrementAndGet).repeat(TaskSchedule.tick(1)).schedule();
        EventNode<InstanceEvent> node = EventNode.type("polyp:tick", EventFilter.INSTANCE);
        node.addListener(InstanceTickEvent.class, e -> { advance(e.getInstance()); dispatch(e.getInstance()); });
        node.addListener(InstanceUnregisterEvent.class, e -> CLOCKS.remove(e.getInstance()));
        // an instance swap lands the entity on a different counter; an externally ticked one keeps its own clock,
        // so its world bridge owns that call instead
        node.addListener(EntitySpawnEvent.class, e -> {
            if (MechanicsWorld.externalTick(e.getEntity()) < 0) clockChanged(e.getEntity());
        });
        MinecraftServer.getGlobalEventHandler().addChild(node);
    }

    /** The server-wide tick (state not owned by any one instance). */
    public static long serverTick() { return serverTick.get(); }

    /** The instance's current tick ({@code 0} if it has not ticked yet). */
    public static long instanceTick(@Nullable Instance instance) {
        if (instance == null) return 0L;
        AtomicLong c = CLOCKS.get(instance);
        return c != null ? c.get() : 0L;
    }

    /**
     * The clock owning {@code entity}: its external ticker's when one owns it, else its instance's. Stamp AND
     * judge tick-stamped entity state through THIS accessor - never against another clock.
     */
    public static long tick(@Nullable Entity entity) {
        if (entity == null) return 0L;
        long external = MechanicsWorld.externalTick(entity);
        return external >= 0 ? external : instanceTick(entity.getInstance());
    }

    private static final List<Consumer<Entity>> CLOCK_CHANGE = new CopyOnWriteArrayList<>();

    /** Registers a reset for tick-stamped entity state (i-frames, sprint windows, motion clocks); run by {@link #clockChanged}. */
    public static Runnable onClockChange(Consumer<Entity> reset) {
        CLOCK_CHANGE.add(reset);
        return () -> CLOCK_CHANGE.remove(reset);
    }

    /**
     * {@code entity} moved to a different clock, so stamps from the old one are meaningless and every registered
     * reset runs. Fired here on an instance swap; world bridges call it on external-ticker ownership changes.
     */
    public static void clockChanged(Entity entity) {
        for (Consumer<Entity> reset : CLOCK_CHANGE) {
            try {
                reset.accept(entity);
            } catch (Throwable e) {
                LOG.error("clock-change reset threw for {}", entity, e);
            }
        }
    }

    /** A registered {@link Tickable}'s handle; {@link #cancel()} removes it (safe mid-dispatch - the phase lists are copy-on-write). */
    public record Registration(Tickable tickable) {
        public void cancel() { BY_PHASE.get(tickable.phase()).remove(tickable); }
    }

    /** Installs a {@link Tickable}; it ticks from the next instance tick on. */
    public static Registration register(Tickable tickable) {
        BY_PHASE.get(tickable.phase()).add(tickable);
        return new Registration(tickable);
    }

    /** Installs a lambda in {@code phase} at every-tick cadence. */
    public static Registration register(TickPhase phase, Consumer<TickContext> fn) {
        return register(new Tickable() {
            @Override public void tick(TickContext ctx) { fn.accept(ctx); }
            @Override public TickPhase phase() { return phase; }
        });
    }

    private static void advance(Instance instance) {
        CLOCKS.computeIfAbsent(instance, k -> new AtomicLong()).incrementAndGet();
    }

    private static void dispatch(Instance instance) {
        dispatch(new TickContext(MechanicsWorld.of(instance), instanceTick(instance),
                ServerFlag.SERVER_TICKS_PER_SECOND, false));
    }

    /** One pass of the registered work scoped to {@code world}, on ITS clock - an external world ticker drives this from the world's own thread. */
    public static void tickWorld(MechanicsWorld world, long tick) {
        dispatch(new TickContext(world, tick, ServerFlag.SERVER_TICKS_PER_SECOND, true));
    }

    private static void dispatch(TickContext ctx) {
        for (TickPhase phase : PHASES) {
            for (Tickable t : BY_PHASE.get(phase)) {
                int iv = t.interval();
                if (iv <= 1 || ctx.tick() % iv == 0) {
                    // isolated: one broken tickable must not starve every later system of its tick
                    try {
                        t.tick(ctx);
                    } catch (Throwable e) {
                        LOG.error("tickable {} threw (phase {})", t.getClass().getName(), phase, e);
                    }
                }
            }
        }
    }
}
