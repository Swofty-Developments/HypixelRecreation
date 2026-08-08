package io.github.term4.polyp.platform.compatibility;

import net.minestom.server.entity.EntityPose;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Immutable cross-version compatibility config: per-scope knobs that present consistent (typically 1.8-style)
 * mechanics to mixed-version clients. Scoped via {@code MechanicsProfile.compat}, pushed to {@code OptimizedPlayer}
 * by {@code PlayerConfigApplier}. Plain values (platform knobs, not per-hit); unset ({@code null}) = unmanaged.
 */
public final class CompatConfig {

    /** Poses the server forces back to {@code STANDING}; the gameplay half is {@link #restrictMovement}. Empty = none disabled. */
    public final @Nullable Set<EntityPose> disabledPoses;
    /** Reject a move that newly places the SERVER hitbox in block collision - a client rendering itself crawling can't traverse a gap its server hitbox can't fit. */
    public final @Nullable Boolean restrictMovement;
    /** Server box stays at standing dimensions (no crouch shrink) + 1.8 eye heights (1.54 sneaking); the client still renders its own pose. */
    public final @Nullable Boolean legacyHitbox;
    /** {@code attack_range.hitbox_margin} stamped on the client's view of held items (1.8 = {@code 0.1f}, modern 0.3). Held-item only - bare-hand hardcodes 0 client-side. */
    public final @Nullable Float attackHitboxMargin;
    /** F-swap + offhand-slot clicks cancelled; no effect on 1.8 clients. */
    public final @Nullable Boolean disableOffhand;
    /** Cancel the sprint speed boost while sneaking (1.8 can't sprint-sneak). */
    public final @Nullable Boolean restrictSprintSneak;
    /** Cancel the sprint speed boost while using an item (1.8 can't sprint-use). */
    public final @Nullable Boolean restrictSprintUse;
    /** Cancel the sprint speed boost while in water (caps modern fast-swim toward 1.8). */
    public final @Nullable Boolean restrictSwimSpeed;
    /** Horizontal divisor for the {@link #restrictSwimSpeed} dampen (higher = slower); {@code null} = 1.25. */
    public final @Nullable Double swimFactor;
    /** Vertical divisor for the {@link #restrictSwimSpeed} dampen (stronger - holding space re-adds swim-up); {@code null} = 3.0. */
    public final @Nullable Double swimVerticalFactor;
    /** {@code attack_range} reach stamped alongside {@link #attackHitboxMargin} (1.8 = 3 blocks); {@code null} = 3. */
    public final @Nullable Float attackReach;
    /** Max blocks from the SERVER eye to a placement's clicked point; farther is cancelled (modern sneak-bridge over-reach). Pair with {@link #legacyHitbox}. */
    public final @Nullable Double blockPlaceReach;
    /** No placing against an air cell. Enforced client-side (Animatium) AND server-side ({@code CompatPlacement}, any client). */
    public final @Nullable Boolean oldPlacement;
    /** Remove the modern attack cooldown + crosshair indicator (huge {@code ATTACK_SPEED}). Server-side, any client. */
    public final @Nullable Boolean removeAttackCooldown;
    /** No arm-swing when a modern client throws a projectile: its inventory VIEW shows a non-usable reskin; the server item stays the real snowball/egg/pearl. */
    public final @Nullable Boolean suppressThrowSwing;
    /** Server-side ray fill for a modern client's bare-fist swings: {@link #attackHitboxMargin} only rides items, so an empty hand still picks with margin 0. */
    public final @Nullable Boolean fistRayHits;
    /** {@code blocks_attacks} stamped on swords in a modern client's VIEW, so a right-click hold shows the native block animation; the server's blocking system stays damage-authoritative. */
    public final @Nullable Boolean swordBlockingPose;
    /** {@code use_cooldown} stripped from a modern client's item VIEW - the client self-applies it (ender pearl 1s) even without server packets. */
    public final @Nullable Boolean removeUseCooldowns;

    /** 1.8 water/lava movement (drag/gravity, no swim sprint/buoyancy, no lava current). */
    public final @Nullable Boolean legacyFluids;
    /** {@code glider} stripped from every modern client's item VIEW, so the client never attempts a glide (Animatium also disables natively - harmless doubled). */
    public final @Nullable Boolean disableElytraFlight;
    /** Opt-out for the hook prediction escort (default ON): on a silent hook wire, a viewer that can't predict the hook's water physics gets a dense server wire through the water window. */
    public final @Nullable Boolean hookPredictionEscort;
    /** 1.8 creative/spectator flight (sneaking while flying slows horizontal). */
    public final @Nullable Boolean oldFlight;
    /** Allow starting an item use while mining (modern MC blocks use-item during destroy; 1.8 doesn't). */
    public final @Nullable Boolean leftClickItemUsage;
    /** No auto-crouch under a low ceiling (1.8 sneak is shift-only). */
    public final @Nullable Boolean disableAutoSneak;
    /** Bundle for the four physics aspects below: {@code true} enables all; each per-aspect knob overrides it. */
    public final @Nullable Boolean oldPhysics;
    /** 1.8 parkour momentum threshold; {@code null} follows {@link #oldPhysics}. */
    public final @Nullable Boolean oldMomentum;
    /** 1.8 beds don't bounce; {@code null} follows {@link #oldPhysics}. */
    public final @Nullable Boolean disableBedBounce;
    /** Honey acts like a plain block (no slide/slowdown); {@code null} follows {@link #oldPhysics}. */
    public final @Nullable Boolean disableHoneyPhysics;
    /** No bubble-column push; {@code null} follows {@link #oldPhysics}. */
    public final @Nullable Boolean disableBubbleColumn;
    /** No entity-collision shove: a shared no-push TEAM for any modern client + the Animatium native feature. An app running its own teams must disable this and set collisionRule NEVER on them. */
    public final @Nullable Boolean disableEntityPush;
    /** Byte-exact 1.8 velocity (3 shorts) for clients advertising {@code SHORTS_VELOCITY}; never sent to clients without the decoder. */
    public final @Nullable Boolean nativeShortVelocity;
    /** Explicit override for the {@link AnimatiumFeature} set sent; {@code null} = derive from the knobs above. */
    public final @Nullable Set<AnimatiumFeature> animatiumFeatures;
    /** Chat-message the player each time their {@code set_server_features} payload is sent (mod-dev debugging). */
    public final @Nullable Boolean animatiumDebug;

    private CompatConfig(Builder b) {
        disabledPoses = b.disabledPoses;
        restrictMovement = b.restrictMovement;
        legacyHitbox = b.legacyHitbox;
        attackHitboxMargin = b.attackHitboxMargin;
        disableOffhand = b.disableOffhand;
        restrictSprintSneak = b.restrictSprintSneak;
        restrictSprintUse = b.restrictSprintUse;
        restrictSwimSpeed = b.restrictSwimSpeed;
        swimFactor = b.swimFactor;
        swimVerticalFactor = b.swimVerticalFactor;
        attackReach = b.attackReach;
        blockPlaceReach = b.blockPlaceReach;
        legacyFluids = b.legacyFluids;
        disableElytraFlight = b.disableElytraFlight;
        hookPredictionEscort = b.hookPredictionEscort;
        oldFlight = b.oldFlight;
        leftClickItemUsage = b.leftClickItemUsage;
        disableAutoSneak = b.disableAutoSneak;
        oldPhysics = b.oldPhysics;
        oldMomentum = b.oldMomentum;
        disableBedBounce = b.disableBedBounce;
        disableHoneyPhysics = b.disableHoneyPhysics;
        disableBubbleColumn = b.disableBubbleColumn;
        disableEntityPush = b.disableEntityPush;
        oldPlacement = b.oldPlacement;
        removeAttackCooldown = b.removeAttackCooldown;
        suppressThrowSwing = b.suppressThrowSwing;
        fistRayHits = b.fistRayHits;
        swordBlockingPose = b.swordBlockingPose;
        removeUseCooldowns = b.removeUseCooldowns;
        nativeShortVelocity = b.nativeShortVelocity;
        animatiumFeatures = b.animatiumFeatures;
        animatiumDebug = b.animatiumDebug;
    }

    public Builder toBuilder() { return new Builder(this); }

    public static Builder builder() { return builder(null); }
    public static Builder builder(@Nullable CompatConfig base) { return base != null ? new Builder(base) : new Builder(); }

    /** Sets are defensively copied. */
    public static final class Builder {
        private @Nullable Set<EntityPose> disabledPoses;
        private @Nullable Boolean restrictMovement;
        private @Nullable Boolean legacyHitbox;
        private @Nullable Float attackHitboxMargin;
        private @Nullable Boolean disableOffhand;
        private @Nullable Boolean restrictSprintSneak;
        private @Nullable Boolean restrictSprintUse;
        private @Nullable Boolean restrictSwimSpeed;
        private @Nullable Double swimFactor;
        private @Nullable Double swimVerticalFactor;
        private @Nullable Float attackReach;
        private @Nullable Double blockPlaceReach;
        private @Nullable Boolean legacyFluids;
        private @Nullable Boolean disableElytraFlight;
        private @Nullable Boolean hookPredictionEscort;
        private @Nullable Boolean oldFlight;
        private @Nullable Boolean leftClickItemUsage;
        private @Nullable Boolean disableAutoSneak;
        private @Nullable Boolean oldPhysics;
        private @Nullable Boolean oldMomentum;
        private @Nullable Boolean disableBedBounce;
        private @Nullable Boolean disableHoneyPhysics;
        private @Nullable Boolean disableBubbleColumn;
        private @Nullable Boolean disableEntityPush;
        private @Nullable Boolean oldPlacement;
        private @Nullable Boolean removeAttackCooldown;
        private @Nullable Boolean suppressThrowSwing;
        private @Nullable Boolean fistRayHits;
        private @Nullable Boolean swordBlockingPose;
        private @Nullable Boolean removeUseCooldowns;
        private @Nullable Boolean nativeShortVelocity;
        private @Nullable Set<AnimatiumFeature> animatiumFeatures;
        private @Nullable Boolean animatiumDebug;

        Builder() {}

        Builder(CompatConfig c) {
            disabledPoses = c.disabledPoses;
            restrictMovement = c.restrictMovement;
            legacyHitbox = c.legacyHitbox;
            attackHitboxMargin = c.attackHitboxMargin;
            disableOffhand = c.disableOffhand;
            restrictSprintSneak = c.restrictSprintSneak;
            restrictSprintUse = c.restrictSprintUse;
            restrictSwimSpeed = c.restrictSwimSpeed;
            swimFactor = c.swimFactor;
            swimVerticalFactor = c.swimVerticalFactor;
            attackReach = c.attackReach;
            blockPlaceReach = c.blockPlaceReach;
            legacyFluids = c.legacyFluids;
            disableElytraFlight = c.disableElytraFlight;
            oldFlight = c.oldFlight;
            leftClickItemUsage = c.leftClickItemUsage;
            disableAutoSneak = c.disableAutoSneak;
            oldPhysics = c.oldPhysics;
            oldMomentum = c.oldMomentum;
            disableBedBounce = c.disableBedBounce;
            disableHoneyPhysics = c.disableHoneyPhysics;
            disableBubbleColumn = c.disableBubbleColumn;
            disableEntityPush = c.disableEntityPush;
            oldPlacement = c.oldPlacement;
            removeAttackCooldown = c.removeAttackCooldown;
            suppressThrowSwing = c.suppressThrowSwing;
            fistRayHits = c.fistRayHits;
            swordBlockingPose = c.swordBlockingPose;
            removeUseCooldowns = c.removeUseCooldowns;
            nativeShortVelocity = c.nativeShortVelocity;
            animatiumDebug = c.animatiumDebug;
            animatiumFeatures = c.animatiumFeatures;
        }

        public Builder disabledPoses(@Nullable Set<EntityPose> v) { disabledPoses = v != null ? Set.copyOf(v) : null; return this; }
        public Builder disabledPoses(EntityPose... poses) { disabledPoses = Set.of(poses); return this; }
        public Builder restrictMovement(@Nullable Boolean v) { restrictMovement = v; return this; }
        public Builder legacyHitbox(@Nullable Boolean v) { legacyHitbox = v; return this; }
        public Builder attackHitboxMargin(@Nullable Float v) { attackHitboxMargin = v; return this; }
        public Builder disableOffhand(@Nullable Boolean v) { disableOffhand = v; return this; }
        public Builder restrictSprintSneak(@Nullable Boolean v) { restrictSprintSneak = v; return this; }
        public Builder restrictSprintUse(@Nullable Boolean v) { restrictSprintUse = v; return this; }
        public Builder restrictSwimSpeed(@Nullable Boolean v) { restrictSwimSpeed = v; return this; }
        public Builder swimFactor(@Nullable Double v) { swimFactor = v; return this; }
        public Builder swimVerticalFactor(@Nullable Double v) { swimVerticalFactor = v; return this; }
        public Builder attackReach(@Nullable Float v) { attackReach = v; return this; }
        public Builder blockPlaceReach(@Nullable Double v) { blockPlaceReach = v; return this; }
        public Builder legacyFluids(@Nullable Boolean v) { legacyFluids = v; return this; }
        public Builder disableElytraFlight(@Nullable Boolean v) { disableElytraFlight = v; return this; }
        public Builder hookPredictionEscort(@Nullable Boolean v) { hookPredictionEscort = v; return this; }
        public Builder oldFlight(@Nullable Boolean v) { oldFlight = v; return this; }
        public Builder leftClickItemUsage(@Nullable Boolean v) { leftClickItemUsage = v; return this; }
        public Builder disableAutoSneak(@Nullable Boolean v) { disableAutoSneak = v; return this; }
        public Builder oldPhysics(@Nullable Boolean v) { oldPhysics = v; return this; }
        public Builder oldMomentum(@Nullable Boolean v) { oldMomentum = v; return this; }
        public Builder disableBedBounce(@Nullable Boolean v) { disableBedBounce = v; return this; }
        public Builder disableHoneyPhysics(@Nullable Boolean v) { disableHoneyPhysics = v; return this; }
        public Builder disableBubbleColumn(@Nullable Boolean v) { disableBubbleColumn = v; return this; }
        public Builder disableEntityPush(@Nullable Boolean v) { disableEntityPush = v; return this; }
        public Builder oldPlacement(@Nullable Boolean v) { oldPlacement = v; return this; }
        public Builder removeAttackCooldown(@Nullable Boolean v) { removeAttackCooldown = v; return this; }
        public Builder suppressThrowSwing(@Nullable Boolean v) { suppressThrowSwing = v; return this; }
        public Builder fistRayHits(@Nullable Boolean v) { fistRayHits = v; return this; }
        public Builder swordBlockingPose(@Nullable Boolean v) { swordBlockingPose = v; return this; }
        public Builder removeUseCooldowns(@Nullable Boolean v) { removeUseCooldowns = v; return this; }
        public Builder nativeShortVelocity(@Nullable Boolean v) { nativeShortVelocity = v; return this; }
        public Builder animatiumFeatures(@Nullable Set<AnimatiumFeature> v) { animatiumFeatures = v != null ? Set.copyOf(v) : null; return this; }
        public Builder animatiumFeatures(AnimatiumFeature... features) { animatiumFeatures = Set.of(features); return this; }
        public Builder animatiumDebug(@Nullable Boolean v) { animatiumDebug = v; return this; }

        public CompatConfig build() { return new CompatConfig(this); }
    }
}
