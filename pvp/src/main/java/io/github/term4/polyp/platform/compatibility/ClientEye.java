package io.github.term4.polyp.platform.compatibility;

import io.github.term4.polyp.platform.player.OptimizedPlayer;
import io.github.term4.polyp.tracking.ClientVersion;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityPose;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;

/**
 * The eye height a client perceives itself at - value (a) of the compat eye model; value (b), the server eye, is
 * {@link CompatState#eyeHeight}.
 * {@link #candidates} = every eye the client could have (reach takes the MIN, so the pose needn't be pinned down);
 * {@link #perceived} = one value from the client's believed pose.
 */
public final class ClientEye {

    private ClientEye() {}

    public static final double STANDING = 1.62;
    public static final double LEGACY_SNEAKING = 1.54;
    public static final double MODERN_CROUCH = 1.27;
    /** Crawl / swim / elytra / riptide eye. */
    public static final double POSE_EYE = 0.4;

    /**
     * Every eye height {@code player}'s client could believe it has (unknown protocol = modern). Animatium's
     * {@link AnimatiumFeature#OLD_SNEAK_HEIGHT} rewrites the real crouch dimensions client-side, which feed the pick
     * raytrace, so such a client's crouch candidate is {@link #LEGACY_SNEAKING}, not {@link #MODERN_CROUCH}.
     */
    public static double[] candidates(Player player, int protocol) {
        if (ClientVersion.isLegacy(protocol)) return new double[]{STANDING, LEGACY_SNEAKING};
        return new double[]{STANDING, oldSneakHeight(player) ? LEGACY_SNEAKING : MODERN_CROUCH, POSE_EYE};
    }

    /** Single best-guess client-perceived eye for {@code player} from its protocol + the pose the client believes it's in. */
    public static double perceived(Player player, int protocol) {
        if (ClientVersion.isLegacy(protocol)) return player.isSneaking() ? LEGACY_SNEAKING : STANDING; // 1.8 has no lower pose
        // believed pose: the disabled pose we intercepted, even though the server pose is forced to STANDING
        EntityPose pose = player instanceof OptimizedPlayer op ? op.compat().clientPose(op.getPose()) : player.getPose();
        return switch (pose) {
            case FALL_FLYING, SWIMMING, SPIN_ATTACK -> POSE_EYE; // crawl / elytra / riptide
            case SNEAKING -> oldSneakHeight(player) ? LEGACY_SNEAKING : MODERN_CROUCH;
            // Minestom never computes the swim pose (no client signal), so detect sprint-swimming directly
            default -> player.isSprinting() && player.getInstance() != null
                    && feetInWater(player, MechanicsWorld.viewed(player)) ? POSE_EYE : STANDING;
        };
    }

    private static boolean oldSneakHeight(Player player) {
        return player instanceof OptimizedPlayer op && op.compat().handlesNatively(AnimatiumFeature.OLD_SNEAK_HEIGHT);
    }

    /** Feet cell (not eye): 1.8 slows wading too, and the swim pose starts at the surface. */
    static boolean feetInWater(Player player, MechanicsWorld world) {
        Pos p = player.getPosition();
        try {
            Block block = world.getBlock(p.blockX(), p.blockY(), p.blockZ(), Block.Getter.Condition.TYPE);
            return block != null && block.compare(Block.WATER);
        } catch (Exception ignored) {
            return false; // unloaded chunk
        }
    }
}
