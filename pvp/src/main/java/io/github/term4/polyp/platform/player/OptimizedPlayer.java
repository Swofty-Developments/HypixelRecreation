package io.github.term4.polyp.platform.player;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.knockback.KnockbackConfig;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfig;
import io.github.term4.polyp.platform.compatibility.CompatState;
import io.github.term4.polyp.platform.fixes.RefreshPositionFix;
import io.github.term4.polyp.platform.fixes.client.InventorySync;
import io.github.term4.polyp.platform.fixes.client.EquipmentSlotsFix;
import io.github.term4.polyp.platform.fixes.client.SelfMetaFilter;
import io.github.term4.polyp.util.tick.TickScaler;
import io.github.term4.polyp.world.ExternallyTickable;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityPose;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Chunk;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import net.minestom.server.network.packet.server.play.EntityAttributesPacket;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import net.minestom.server.utils.time.Cooldown;
import java.util.Map;

/**
 * The library's custom {@link Player}: self-echo suppression ({@link SelfMetaFilter}), a position-broadcast
 * throttle, the self-placement exclusion, and the cross-version compat overrides (pose/hitbox/eye/attack-box,
 * state in {@link CompatState}). Each behavior is opt-in and wired by {@code Polyp}.
 */
public class OptimizedPlayer extends Player implements ExternallyTickable {

    private @Nullable SelfMetaFilter selfMetaFilter = SelfMetaFilter.defaultPlayerFilter();
    private boolean processingClientInput = false;
    private int positionBroadcastInterval = 1;
    private boolean selfPlacing = false;
    private final CompatState compat = new CompatState();
    private final UseItemAimSync aimSync = new UseItemAimSync();
    private final AttackAimSync attackSync = new AttackAimSync();
    private final InventorySync inventorySync = new InventorySync();

    public OptimizedPlayer(PlayerConnection connection, GameProfile gameProfile) {
        super(connection, gameProfile);
        // vanilla scans item pickups every tick; Minestom's default 5-tick cooldown adds up to 250ms of pickup lag
        this.itemPickupCooldown = new Cooldown(Duration.ZERO);
        // slot refreshes must reach sendPacket bare or the per-client item rewrite skips them (see PerViewerInventory)
        this.inventory = new PerViewerInventory();
    }

    // @ApiStatus.Internal override: super is exactly this field write + dispatcher().updateElement (verified
    // 2026.07.12-26.2, re-verify on bumps) - an externally ticked player in the global dispatcher double-ticks
    @Override protected void refreshCurrentChunk(@NotNull Chunk chunk) {
        if (MechanicsWorld.externallyTicked(this)) {
            currentChunk = chunk;
            return;
        }
        super.refreshCurrentChunk(chunk);
    }

    @Override public void tick(long time) {
        if (!MechanicsWorld.ownsCurrentTick(this)) return;
        super.tick(time);
    }

    /** Set by {@code MetaFix} around the client-input listeners; while {@code true}, self-bound echoes are filtered. */
    public void setProcessingClientInput(boolean value) {
        this.processingClientInput = value;
    }

    /** The active self-metadata filter, or {@code null} when filtering is disabled. */
    public @Nullable SelfMetaFilter getSelfMetaFilter() {
        return selfMetaFilter;
    }

    public void setSelfMetaFilter(@Nullable SelfMetaFilter filter) {
        this.selfMetaFilter = filter;
    }

    /** Updates player state without sending the change to this player; viewers still receive it. */
    public void suppressSelf(@NotNull Runnable action) {
        processingClientInput = true;
        try {
            action.run();
        } finally {
            processingClientInput = false;
        }
    }

    @Override
    public void sendPacketToViewersAndSelf(@NotNull SendablePacket packet) {
        if (processingClientInput && selfMetaFilter != null) {
            // client-predicted metadata (crouch, use item, elytra): viewers get the full packet, self only the rest
            if (packet instanceof EntityMetaDataPacket(int entityId, Map<Integer, Metadata.Entry<?>> entries) && entityId == getEntityId()) {
                Map<Integer, Metadata.Entry<?>> filtered = selfMetaFilter.filter(entries);
                if (filtered != null) {
                    if (!filtered.isEmpty()) {
                        sendPacket(new EntityMetaDataPacket(entityId, filtered));
                    }
                    sendPacketToViewers(packet);
                    return;
                }
            }
            if (packet instanceof EntityAttributesPacket attr
                    && attr.entityId() == getEntityId()
                    && selfMetaFilter.suppressAttributes()) {
                sendPacketToViewers(packet);
                return;
            }
        }
        super.sendPacketToViewersAndSelf(packet);
    }

    /** Inert until {@code FixesSystem} arms {@link InventorySync}. */
    public @NotNull InventorySync inventorySync() { return inventorySync; }

    @Override
    public void sendPacket(@NotNull SendablePacket packet) {
        SendablePacket p = EquipmentSlotsFix.rewrite(compat.rewriteItems(packet));
        if (InventorySync.enabled()) {
            p = inventorySync.filter(p);
            if (p == null) return; // redundant slot echo: the client already shows it
        }
        super.sendPacket(p);
    }

    // bulk equipment resends (respawn/teleport) group into a CachedPacket the per-viewer transform can't unwrap,
    // so strip empty slots before grouping - else BODY=AIR reaches ViaBackwards and hides the chestplate
    @Override
    public void sendPacketToViewers(@NotNull SendablePacket packet) {
        SendablePacket rewritten = EquipmentSlotsFix.rewrite(packet);
        // The blocking-pose stamp is per-VIEWER (a modern client poses the block off ITS copy of the held item), so
        // equipment must arrive bare for each viewer's own item view to apply - grouping would share one CachedPacket.
        if (rewritten instanceof EntityEquipmentPacket && anyViewerRewritesItems()) {
            for (Player viewer : getViewers()) viewer.sendPacket(rewritten);
            return;
        }
        super.sendPacketToViewers(rewritten);
    }

    private boolean anyViewerRewritesItems() {
        for (Player viewer : getViewers()) {
            if (viewer instanceof OptimizedPlayer op && op.compat().rewritesItems()) return true;
        }
        return false;
    }

    /**
     * {@link Player#updatePose()} recomputes the pose every tick (crawl enter/exit), not only in packet listeners -
     * without arming the echo guard here the self-bound pose echo slips through (the crawl/stand stutter).
     * A direct {@code setPose} doesn't route here, so server-authoritative poses still echo.
     */
    @Override
    protected void updatePose() {
        boolean previous = processingClientInput;
        processingClientInput = true;
        compat.resetInterceptedPose(); // setPose (via super.updatePose) re-captures any disabled pose computed this tick
        try {
            super.updatePose();
        } finally {
            processingClientInput = previous;
        }
    }

    /**
     * A compat-disabled pose is rewritten to {@code STANDING} before it reaches metadata (1.8 parity). Sent with the
     * echo guard cleared so the correction reaches the mispredicting client; the metadata layer dedups the repeat.
     */
    @Override
    public void setPose(@NotNull EntityPose pose) {
        if (compat.isPoseDisabled(pose)) {
            compat.recordInterceptedPose(pose); // what the client believes it's in
            boolean previous = processingClientInput;
            processingClientInput = false;
            try {
                super.setPose(EntityPose.STANDING);
            } finally {
                processingClientInput = previous;
            }
            return;
        }
        super.setPose(pose);
    }

    /** This player's cross-version compat state + logic (pose/hitbox/eye/attack-box); pushed from {@code CompatConfig}. */
    public @NotNull CompatState compat() { return compat; }

    // legacyHitbox: the server box stays standing regardless of pose (1.8 parity; CompatMovement blocks the sneak gap)
    @Override
    public BoundingBox getBoundingBox() {
        return compat.legacyHitbox() ? boundingBox : super.getBoundingBox();
    }

    /** The SERVER-treated eye height (projectile spawn, drowning); what the client believes is {@code ClientEye}. */
    @Override
    public double getEyeHeight() {
        return compat.eyeHeight(super.getEyeHeight(), getPose());
    }

    /** Position-broadcast cadence to viewers: 1 = every tick (Minestom), 2 = every other (Spigot). */
    public void setPositionBroadcastInterval(int interval) {
        if (interval < 1) throw new IllegalArgumentException("interval must be >= 1");
        this.positionBroadcastInterval = interval;
    }

    public int getPositionBroadcastInterval() {
        return positionBroadcastInterval;
    }

    @Override
    public void refreshPosition(Pos newPosition, boolean ignoreView, boolean sendPackets) {
        if (sendPackets) {
            // identity at 20 TPS, throttled toward ~client Hz as server TPS rises
            int cadence = TickScaler.clientCadence(positionBroadcastInterval);
            if (cadence > 1 && getAliveTicks() % cadence != 0) sendPackets = false;
        }
        super.refreshPosition(newPosition, ignoreView, sendPackets); // api-internal override
        Pos retry = RefreshPositionFix.swallowedRetry(this, newPosition);
        if (retry != null) super.refreshPosition(retry, ignoreView, sendPackets);
    }

    // aim sync (see UseItemAimSync / AttackAimSync); runs on the connection's read thread. Independent packet types
    // (a client can't use-item and attack in one tick), so chaining is order-free.
    @Override
    public void addPacketToQueue(ClientPacket packet) {
        attackSync.intercept(packet, this::attackAimSyncEnabled,
                p -> aimSync.intercept(p, this::useItemAimSyncEnabled, super::addPacketToQueue));
    }

    /** {@link ProjectileConfig#useItemAimSync} + a legacy client (a modern use packet already carries the click aim). */
    private boolean useItemAimSyncEnabled() {
        Polyp polyp = Polyp.getInstance();
        if (!polyp.isInitialized() || !polyp.clientInfo().isLegacy(this)) return false;
        ProjectileConfig cfg = polyp.profiles().resolve(this, MechanicsKeys.PROJECTILES);
        return cfg != null && Boolean.TRUE.equals(cfg.useItemAimSync);
    }

    /** {@link KnockbackConfig#experimentalAimSync} - all client versions (the attack packet never carried rotation). */
    private boolean attackAimSyncEnabled() {
        Polyp polyp = Polyp.getInstance();
        if (!polyp.isInitialized()) return false;
        KnockbackConfig cfg = polyp.profiles().resolve(this, MechanicsKeys.KNOCKBACK);
        return cfg != null && Boolean.TRUE.equals(cfg.experimentalAimSync);
    }

    /** Armed by {@code LegacySelfPlacementFix} while this player's own placement is processed; lets a passable block into their body. */
    public void setSelfPlacing(boolean value) {
        this.selfPlacing = value;
    }

    @Override
    public boolean preventBlockPlacement() {
        return !selfPlacing && super.preventBlockPlacement();
    }
}
