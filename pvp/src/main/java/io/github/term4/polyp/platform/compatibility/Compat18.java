package io.github.term4.polyp.platform.compatibility;

import net.minestom.server.entity.EntityPose;

/**
 * Cross-version compatibility presets that make a modern (26.1) client behave like 1.8. General - not tied to any
 * mechanics preset, usable from anywhere. {@link #config()} is the full 1.8 compat set; {@link #off()} is the inverse
 * (every knob OFF) for toggling back to vanilla 26.1. Applied via {@code MechanicsProfile.compat} (pushed to
 * {@code OptimizedPlayer} by {@code PlayerConfigApplier}).
 */
public final class Compat18 {

    private Compat18() {}

    public static CompatConfig config() {
        return CompatConfig.builder()
                .disabledPoses(EntityPose.SWIMMING, EntityPose.FALL_FLYING)
                .restrictMovement(true)
                .legacyHitbox(true)
                .attackHitboxMargin(0.1f) // 1.8 attack-target box grow (EntityPlayer.attackTargetEntityWithCurrentItem 0.1)
                .disableOffhand(true)
                .restrictSprintSneak(true)
                .restrictSprintUse(true)
                .restrictSwimSpeed(true)
                .blockPlaceReach(4.5) // 1.8 survival block reach
                .legacyFluids(true)   // Animatium only: also suppresses the modern swim state
                .disableElytraFlight(true) // Animatium only
                .oldFlight(true)           // Animatium only: 1.8 creative/spectator flight (sneak slows horizontal)
                .leftClickItemUsage(true)  // Animatium only: modern blocks use-item mid-destroy
                .disableAutoSneak(true)    // Animatium only: no auto-crouch-to-fit under a low ceiling
                .oldPhysics(true)          // Animatium only: momentum threshold, no bed bounce, no honey/bubble
                .disableEntityPush(true)   // all clients: the shared no-push collision team; disable if the app runs its own teams
                .oldPlacement(true)        // Animatium only: no same-tick break+place refill
                .nativeShortVelocity(true) // Animatium only: gated on the advertised decoder
                .removeAttackCooldown(true) // server-side, any client
                .suppressThrowSwing(true)  // modern only: 1.8 doesn't swing on use
                .fistRayHits(true)         // modern only: attack_range can't ride an empty hand
                .swordBlockingPose(true)   // modern only: blocks_attacks view stamp
                .removeUseCooldowns(true)  // modern only: 1.8 has none - pearls spam-throw
                .build();
    }

    /**
     * The inverse of {@link #config()} - every cross-version knob OFF (vanilla 26.1), for a {@code /compat} toggle. Each
     * boolean knob is {@code false} and the pose set is empty so {@code PlayerConfigApplier} actively resets them (it only
     * writes non-null fields). {@code attackHitboxMargin}/{@code blockPlaceReach} stay null - the layered apply can't null
     * them, so a caller clears those (and restores {@code ATTACK_SPEED}) directly.
     */
    public static CompatConfig off() {
        return CompatConfig.builder()
                .disabledPoses()
                .restrictMovement(false)
                .legacyHitbox(false)
                .disableOffhand(false)
                .restrictSprintSneak(false)
                .restrictSprintUse(false)
                .restrictSwimSpeed(false)
                .legacyFluids(false)
                .disableElytraFlight(false)
                .oldFlight(false)
                .leftClickItemUsage(false)
                .disableAutoSneak(false)
                .oldPhysics(false)
                .disableEntityPush(false)
                .oldPlacement(false)
                .nativeShortVelocity(false)
                .removeAttackCooldown(false)
                .suppressThrowSwing(false)
                .fistRayHits(false)
                .swordBlockingPose(false)
                .removeUseCooldowns(false)
                .build();
    }
}
