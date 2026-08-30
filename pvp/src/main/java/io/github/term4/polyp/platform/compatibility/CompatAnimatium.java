package io.github.term4.polyp.platform.compatibility;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.platform.player.OptimizedPlayer;
import io.github.term4.polyp.tracking.ClientInfoTracker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.EntityPose;
import net.minestom.server.entity.Player;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.stream.Collectors;
import java.util.EnumSet;
import java.util.Set;

/**
 * Animatium integration: an Animatium client applies 1.8 behaviour natively, so instead of server-side hacks we tell it
 * which features to apply and skip those hacks. Detection is the {@code animatium:info} plugin message on join (routed by
 * {@link ClientInfoTracker}); on receipt the feature set for the player's {@link CompatConfig} is sent and recorded on
 * {@link CompatState} so the enforcers gate off for it.
 *
 * <p>The set is <em>derived</em> from the compat knobs ({@link #derive}) or overridden by {@code CompatConfig.animatiumFeatures}.
 * Features Animatium can't yet do natively (pose disabling, fluids, typed eye height) stay server-side hacks for everyone.
 */
public final class CompatAnimatium {

    /** C2S: sent by an Animatium client on play join ({@code double version} + optional dev string); our detection signal. */
    public static final String INFO_CHANNEL = "animatium:info";
    /** S2C: the {@link BitSet} of enabled {@link AnimatiumFeature} bits (raw bytes, no length prefix). */
    public static final String SET_FEATURES_CHANNEL = "animatium:set_server_features";

    private CompatAnimatium() {}

    /** Routes the {@code animatium:info} handshake through {@link ClientInfoTracker}. Needs the player provider. */
    public static void install(Polyp polyp) {
        polyp.clientInfo().onPluginMessage(INFO_CHANNEL, (player, data) -> onInfo(polyp, player, data));
    }

    private static void onInfo(Polyp polyp, Player player, byte[] info) {
        if (!(player instanceof OptimizedPlayer op)) return;
        op.compat().setAnimatiumClient(true);
        op.compat().setSupportedFeatures(parseSupportedFeatures(info));
        applyFeatures(polyp, player);
        // items sent in the join window (before the handshake) carried the stamp/reskin this client no longer gets
        player.getInventory().update(player);
    }

    /**
     * (Re)resolves and sends an Animatium client's feature set, recording it on {@link CompatState} for the enforcers to gate
     * on. Also called whenever {@code PlayerConfigApplier} re-applies the player's config - the client only handshakes once on
     * join, so the server must re-push the set itself. No-op for non-Animatium clients.
     */
    public static void applyFeatures(Polyp polyp, Player player) {
        if (!(player instanceof OptimizedPlayer op)) return;
        CompatConfig cfg = polyp.profiles().resolve(player, MechanicsKeys.COMPAT);
        boolean debug = cfg != null && Boolean.TRUE.equals(cfg.animatiumDebug);
        if (!op.compat().isAnimatiumClient()) {
            // on join one skip line is NORMAL even for an Animatium client - the first config apply
            // precedes the mod's handshake, which then re-applies
            if (debug) player.sendMessage(Component.text(
                    "[animatium] skipped: no animatium:info from this client (yet)", NamedTextColor.GRAY));
            return;
        }
        Set<AnimatiumFeature> features = cfg == null ? Set.of() : resolve(cfg);
        features = gateWireFeatures(features, op.compat());
        op.compat().setNativeFeatures(features);
        player.sendPluginMessage(SET_FEATURES_CHANNEL, encode(features));
        if (debug) {
            player.sendMessage(Component.text("[animatium] sent " + features.size() + " feature(s): "
                    + features.stream().sorted().map(Enum::name).collect(Collectors.joining(", ")),
                    NamedTextColor.GRAY));
        }
    }

    static Set<AnimatiumFeature> resolve(CompatConfig cfg) {
        return cfg.animatiumFeatures != null ? cfg.animatiumFeatures : derive(cfg);
    }

    /** Maps the enabled compat knobs to their native Animatium equivalents. */
    static Set<AnimatiumFeature> derive(CompatConfig cfg) {
        EnumSet<AnimatiumFeature> set = EnumSet.noneOf(AnimatiumFeature.class);
        if (cfg.attackHitboxMargin != null) set.add(AnimatiumFeature.PICK_INFLATION);
        if (Boolean.TRUE.equals(cfg.legacyHitbox)) set.add(AnimatiumFeature.OLD_SNEAK_HEIGHT);
        if (Boolean.TRUE.equals(cfg.restrictSprintUse)) set.add(AnimatiumFeature.FIX_SPRINT_ITEM_USE);
        if (Boolean.TRUE.equals(cfg.restrictSprintSneak)) set.add(AnimatiumFeature.FIX_SPRINT_SNEAKING);
        if (cfg.disabledPoses != null) {
            // in-water swim AND the squeeze-crawl are both Pose.SWIMMING, so disabling it maps to both client bits
            if (cfg.disabledPoses.contains(EntityPose.SWIMMING)) {
                set.add(AnimatiumFeature.DISABLE_SWIM_POSE);
                set.add(AnimatiumFeature.DISABLE_CRAWL_POSE);
            }
            if (cfg.disabledPoses.contains(EntityPose.FALL_FLYING)) set.add(AnimatiumFeature.DISABLE_ELYTRA_POSE);
        }
        if (Boolean.TRUE.equals(cfg.legacyFluids)) set.add(AnimatiumFeature.OLD_FLUID_PHYSICS);
        if (Boolean.TRUE.equals(cfg.disableElytraFlight)) set.add(AnimatiumFeature.DISABLE_ELYTRA_FLIGHT);
        if (Boolean.TRUE.equals(cfg.oldFlight)) set.add(AnimatiumFeature.OLD_FLIGHT);
        if (Boolean.TRUE.equals(cfg.leftClickItemUsage)) set.add(AnimatiumFeature.LEFT_CLICK_ITEM_USAGE);
        if (Boolean.TRUE.equals(cfg.disableAutoSneak)) set.add(AnimatiumFeature.DISABLE_AUTO_SNEAK);
        // oldPhysics is the bundle default; each per-aspect knob overrides it (null = follow the bundle)
        boolean physics = Boolean.TRUE.equals(cfg.oldPhysics);
        if (physicsAspect(cfg.oldMomentum, physics)) set.add(AnimatiumFeature.OLD_MOMENTUM);
        if (physicsAspect(cfg.disableBedBounce, physics)) set.add(AnimatiumFeature.DISABLE_BED_BOUNCE);
        if (physicsAspect(cfg.disableHoneyPhysics, physics)) set.add(AnimatiumFeature.DISABLE_HONEY_PHYSICS);
        if (physicsAspect(cfg.disableBubbleColumn, physics)) set.add(AnimatiumFeature.DISABLE_BUBBLE_COLUMN);
        if (Boolean.TRUE.equals(cfg.disableEntityPush)) set.add(AnimatiumFeature.DISABLE_ENTITY_PUSH);
        if (Boolean.TRUE.equals(cfg.oldPlacement)) set.add(AnimatiumFeature.OLD_PLACEMENT);
        if (Boolean.TRUE.equals(cfg.disableOffhand)) set.add(AnimatiumFeature.DISABLE_OFFHAND);
        if (Boolean.TRUE.equals(cfg.nativeShortVelocity)) set.add(AnimatiumFeature.SHORTS_VELOCITY);
        return set;
    }

    private static boolean physicsAspect(@Nullable Boolean knob, boolean bundle) {
        return knob != null ? knob : bundle;
    }

    /** Drops wire-format features the client didn't advertise - a client that can't decode them gets a corrupt stream. */
    private static Set<AnimatiumFeature> gateWireFeatures(Set<AnimatiumFeature> features, CompatState state) {
        if (!features.contains(AnimatiumFeature.SHORTS_VELOCITY) || state.supports(AnimatiumFeature.SHORTS_VELOCITY)) {
            return features;
        }
        EnumSet<AnimatiumFeature> gated = EnumSet.copyOf(features);
        gated.remove(AnimatiumFeature.SHORTS_VELOCITY);
        return gated;
    }

    /**
     * The features a client advertised, from its {@code animatium:info} payload ({@code double version}, optional dev string,
     * then a length-prefixed {@code BitSet.toByteArray()}). Empty for an upstream/old Animatium or a malformed payload.
     */
    private static Set<AnimatiumFeature> parseSupportedFeatures(byte[] info) {
        try {
            NetworkBuffer buf = NetworkBuffer.wrap(info, 0, info.length);
            buf.read(NetworkBuffer.DOUBLE);
            if (buf.read(NetworkBuffer.BOOLEAN)) buf.read(NetworkBuffer.STRING);
            if (buf.readableBytes() <= 0) return Set.of();   // upstream/old client: no capability field
            // byte[]-backed, NOT NetworkBuffer.BITSET (long[]-backed)
            BitSet bits = BitSet.valueOf(buf.read(NetworkBuffer.BYTE_ARRAY));
            EnumSet<AnimatiumFeature> supported = EnumSet.noneOf(AnimatiumFeature.class);
            for (AnimatiumFeature f : AnimatiumFeature.values()) {
                if (bits.get(f.bit)) supported.add(f);
            }
            return supported;
        } catch (RuntimeException malformed) {
            return Set.of();
        }
    }

    /** {@code BitSet.toByteArray} - the inverse of Animatium's {@code BitSet.valueOf}. */
    private static byte[] encode(Set<AnimatiumFeature> features) {
        BitSet bits = new BitSet();
        for (AnimatiumFeature f : features) bits.set(f.bit);
        return bits.toByteArray();
    }
}
