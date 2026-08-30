package io.github.term4.polyp.platform.compatibility;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.platform.compatibility.CompatConfig.SwimSuppression;
import io.github.term4.polyp.platform.player.OptimizedPlayer;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.entity.EntityPose;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.network.packet.server.play.EntityEffectPacket;
import net.minestom.server.network.packet.server.play.RemoveEntityEffectPacket;
import net.minestom.server.network.packet.server.play.UpdateHealthPacket;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Drives {@code CompatConfig.suppressSwim}: while an affected client's feet are in water, one of the client's two
 * wire-drivable sprint gates is held shut so it can never enter the swim pose; released the moment it leaves.
 * {@code FOOD} rides the {@code OptimizedPlayer} outgoing clamp (any server food update stays clamped mid-water);
 * {@code BLINDNESS} refreshes a hidden effect every tick ({@code swimBlindnessTicks} long - the default stays above
 * the 20-tick fog saturation knee so the fog factor can't sawtooth).
 * All state lives on {@link CompatState}, so disconnect/relog cleanup is free. Installed once; inert unless configured.
 */
public final class CompatSwim {

    private CompatSwim() {}

    public static void install(Polyp polyp) {
        EventNode<@NotNull PlayerEvent> node = EventNode.type("polyp:compat-swim", EventFilter.PLAYER);
        node.addListener(PlayerTickEvent.class, e -> {
            if (e.getPlayer() instanceof OptimizedPlayer op) tick(op);
        });
        polyp.install(node);
    }

    static void tick(@NotNull OptimizedPlayer player) {
        CompatState compat = player.compat();
        SwimSuppression want = wanted(player, compat);
        SwimSuppression active = compat.activeSwimFix();
        if (want != active) {
            compat.setActiveSwimFix(want); // before the sends: the food clamp keys off it
            if (active == SwimSuppression.BLINDNESS && !player.hasEffect(PotionEffect.BLINDNESS)) {
                player.sendPacket(new RemoveEntityEffectPacket(player.getEntityId(), PotionEffect.BLINDNESS));
            }
            if (active == SwimSuppression.FOOD || want == SwimSuppression.FOOD) resendFood(player);
        }
        // a real blindness effect already gates sprint; riding it also keeps its client duration honest
        if (want == SwimSuppression.BLINDNESS && !player.hasEffect(PotionEffect.BLINDNESS)) {
            player.sendPacket(new EntityEffectPacket(player.getEntityId(),
                    new Potion(PotionEffect.BLINDNESS, 0, compat.swimBlindnessTicks(), Potion.AMBIENT_FLAG)));
        }
        // the gate takes a round trip; a pose slipped in before it landed is forced back (isPoseDisabled covers later ones)
        if (want != null && player.getPose() == EntityPose.SWIMMING) player.setPose(EntityPose.STANDING);
    }

    private static @Nullable SwimSuppression wanted(OptimizedPlayer player, CompatState compat) {
        SwimSuppression configured = compat.suppressSwim();
        if (configured == null || player.isDead() || player.getInstance() == null) return null;
        GameMode gm = player.getGameMode();
        // mayfly bypasses the client's food gate anyway, and fogging a builder helps no one
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return null;
        return ClientEye.feetInWater(player, MechanicsWorld.viewed(player)) ? configured : null;
    }

    /** Through {@code sendPacket}, so the clamp applies exactly when a fix is active. */
    private static void resendFood(OptimizedPlayer player) {
        player.sendPacket(new UpdateHealthPacket(player.getHealth(), player.getFood(), player.getFoodSaturation()));
    }
}
