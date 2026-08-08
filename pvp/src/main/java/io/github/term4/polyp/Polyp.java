package io.github.term4.polyp;

import io.github.term4.polyp.mechanics.attack.FakeHits;
import io.github.term4.polyp.platform.SharedTeam;
import io.github.term4.polyp.platform.compatibility.CompatAnimatium;
import io.github.term4.polyp.platform.compatibility.CompatCreativeGuard;
import io.github.term4.polyp.platform.compatibility.CompatMovement;
import io.github.term4.polyp.platform.compatibility.CompatOffhand;
import io.github.term4.polyp.platform.compatibility.CompatPlacement;
import io.github.term4.polyp.platform.compatibility.LegacyVelocityBridge;
import io.github.term4.polyp.platform.compatibility.ViaBridgeRpc;
import io.github.term4.polyp.platform.fixes.client.MetaFix;
import io.github.term4.polyp.platform.player.OptimizedPlayer;
import io.github.term4.polyp.platform.player.PlayerConfigApplier;
import io.github.term4.polyp.tracking.ClientInfoTracker;
import io.github.term4.polyp.tracking.ClientProfile;
import io.github.term4.polyp.tracking.motion.MotionTracker;
import io.github.term4.polyp.tracking.SprintTracker;
import io.github.term4.polyp.tracking.Tracker;
import io.github.term4.polyp.world.WorldSounds;
import io.github.term4.polyp.util.tick.TickSystem;
import io.github.term4.polyp.util.tick.TickScaler;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * Main initialization class for the library: server-level options (trackers, metaFix), the node tree
 * ({@code polyp:root} -&gt; trackers / systems / api-events), the system registry, and scoped config profiles.
 */
public final class Polyp {

    private static final Logger LOG = LoggerFactory.getLogger(Polyp.class);
    private static final Polyp INSTANCE = new Polyp();

    /** Listens for player details sent via the ViaVersion proxy message. */
    public boolean viaProxyDetails = true;

    public boolean installSprintTracker = true;
    /** Tracks per-entity air-time, launch state, and position-delta motion (drives knockback velocity). */
    public boolean installMotionTracker = true;
    /** Removes the pose-change stutter (sneak/sprint/...) 1.9+ clients show under high ping. Requires {@link #installPlayerProvider}. */
    public boolean metaFix = true;
    /**
     * Installs {@link #playerFactory} as the player provider + scoped {@code PlayerConfig} application. To customize
     * the player class set {@link #playerFactory} instead of disabling this - the packet-level compat keys on it.
     */
    public boolean installPlayerProvider = true;

    /** Builds each connecting player. Swap for an {@code OptimizedPlayer} subclass to keep the whole
     *  {@code instanceof}-gated compat/fixes layer; read per-connect, so setting it after init works too. */
    public BiFunction<PlayerConnection, GameProfile, ? extends OptimizedPlayer> playerFactory = OptimizedPlayer::new;

    private final EventNode<@NotNull Event> root = EventNode.all("polyp:root");
    private final EventNode<@NotNull Event> apiEvents = EventNode.all("polyp:api-events");
    private EventNode<@NotNull Event> trackersNode;

    private ClientInfoTracker clientInfo;
    private final MechanicsProfiles profiles = new MechanicsProfiles();

    private @Nullable SprintTracker sprintTracker;
    private @Nullable MotionTracker motionTracker;

    /** Installed systems, keyed by concrete type. */
    private final Map<Class<? extends MechanicsModule>, MechanicsModule> modules = new ConcurrentHashMap<>();

    private volatile boolean initialized = false;

    private Polyp() {}

    /** The JVM-wide library instance; the lookup for code that can't be handed {@link Services} (entity factories). */
    public static Polyp getInstance() { return INSTANCE; }

    /** Registers an installed system; later retrievable via {@link #module(Class)}. Called from each system's {@code install}. */
    public <M extends MechanicsModule> void register(M module) {
        MechanicsModule previous = modules.putIfAbsent(module.getClass(), module);
        if (previous != null) throw new IllegalStateException(
                module.getClass().getSimpleName() + " is already installed - install once, or unregister(...) first to swap configs");
    }

    /** Tears down an installed system (its node comes off the tree) so a fresh install may run. */
    public void unregister(Class<? extends MechanicsModule> type) {
        MechanicsModule previous = modules.remove(type);
        if (previous != null && previous.node() != null) uninstall(previous.node());
    }

    /** {@link #unregister} for every installed system (full teardown; the test harness resets per class). */
    public void unregisterAll() {
        for (Class<? extends MechanicsModule> type : List.copyOf(modules.keySet())) unregister(type);
    }

    public <M extends MechanicsModule> @Nullable M module(Class<M> type) {
        return type.cast(modules.get(type));
    }

    public @Nullable SprintTracker sprintTracker() { return sprintTracker; }
    public @Nullable MotionTracker motionTracker() { return motionTracker; }

    /** Installs with the current options; a failed init resets the flag instead of latching a partial installation. */
    public synchronized void init() {
        if (initialized) return;
        initialized = true; // inside the lock; the internal mounts assert it
        try {
            doInit();
        } catch (RuntimeException | Error e) {
            initialized = false;
            throw e;
        }
    }

    private void doInit() {
        TickSystem.start();

        if (metaFix && !installPlayerProvider) {
            LOG.warn("metaFix is enabled but installPlayerProvider is not - the meta fix needs the OptimizedPlayer"
                    + " provider and will be inert (keep the provider on and set playerFactory instead).");
        }
        if (metaFix) MetaFix.installListeners();
        CompatMovement.install(this); // plain server API: covers foreign-provider players, so not provider-gated
        if (installPlayerProvider) {
            // reads the field per-connect, not bound at init: a factory set late still takes effect
            MinecraftServer.getConnectionManager().setPlayerProvider((conn, profile) -> playerFactory.apply(conn, profile));
            PlayerConfigApplier.install(this);
            // inert unless CompatConfig.disableOffhand
            CompatOffhand.install(this);
            // inert unless CompatConfig.blockPlaceReach
            CompatPlacement.install(this);
            // strips attack_range off creative-echoed items, so the client-view stamp never becomes server state
            CompatCreativeGuard.install(this);
        }
        // the one lib scoreboard team; features enroll, this cleans up on disconnect
        SharedTeam.install(this);
        // inert unless AttackConfig.fakeHits or CompatConfig.fistRayHits
        FakeHits.install(this);
        // block-place + footstep sounds Minestom doesn't emit
        WorldSounds.install(this);
        profiles.onChange(changed -> {
            if (changed != null) {
                if (installPlayerProvider) PlayerConfigApplier.apply(this, changed);
                return;
            }
            refreshGlobalScaling();
            if (installPlayerProvider) PlayerConfigApplier.applyAll(this);
        });
        refreshGlobalScaling();
        // per-subject scope: one world can run dilated (simulated TPS) while the rest doesn't
        TickScaler.resolver(subject -> profiles.resolve(subject, MechanicsKeys.TICK_SCALING));

        MinecraftServer.getGlobalEventHandler().addChild(root);
        root.addChild(apiEvents);

        trackersNode = EventNode.all("polyp:trackers");
        root.addChild(trackersNode);

        clientInfo = new ClientInfoTracker(viaProxyDetails);
        if (installSprintTracker) mountTracker(sprintTracker = new SprintTracker());
        if (installMotionTracker) mountTracker(motionTracker = new MotionTracker(profiles));
        // the client-info hub also routes Animatium handshakes
        if (viaProxyDetails || installPlayerProvider) mountTracker(clientInfo);
        if (installPlayerProvider) {
            CompatAnimatium.install(this);
            ViaBridgeRpc.install(this);
            LegacyVelocityBridge.install(this);
        }
    }

    /** Starts a tracker and mounts it under {@code polyp:trackers}. */
    public void mountTracker(Tracker tracker) {
        ensureInitialized();
        tracker.start();
        trackersNode.addChild(tracker.node());
    }

    public Services services() {
        ensureInitialized();
        return new Services(this);
    }

    public ClientInfoTracker clientInfo() {
        ensureInitialized();
        return clientInfo;
    }

    /**
     * A per-player view of client-side info: protocol version, Animatium status, and a typed store keyed by
     * {@code ClientKey} for custom data. A fresh lightweight view each call.
     */
    public ClientProfile client(@NotNull Player player) {
        ensureInitialized();
        return clientInfo.of(player);
    }

    /** Scoped config profiles; assignable before {@link #init()} and swappable at runtime. */
    public MechanicsProfiles profiles() { return profiles; }

    private void refreshGlobalScaling() {
        TickScaler.setGlobal(profiles.resolve(null, MechanicsKeys.TICK_SCALING));
    }

    /**
     * Convenience node for Polyp API event listeners. API events dispatch through the global handler, so
     * listening here or on {@code MinecraftServer.getGlobalEventHandler()} both work.
     */
    public EventNode<@NotNull Event> events() {
        ensureInitialized();
        return apiEvents;
    }

    /** Installs a node under the root Polyp node. */
    public void install(EventNode<? extends @NotNull Event> node) {
        ensureInitialized();
        root.addChild(node);
    }

    public void uninstall(EventNode<? extends @NotNull Event> node) {
        root.removeChild(node);
    }

    private void ensureInitialized() {
        if (!initialized) throw new IllegalStateException("Polyp has not been initialized");
    }

    public boolean isInitialized() {
        return initialized;
    }
}
