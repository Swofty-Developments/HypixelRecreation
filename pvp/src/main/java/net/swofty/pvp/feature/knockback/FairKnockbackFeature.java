package net.swofty.pvp.feature.knockback;

import net.minestom.server.property.ServerProperties;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.swofty.pvp.events.EntityKnockbackEvent;
import net.swofty.pvp.feature.FeatureType;
import net.swofty.pvp.feature.config.DefinedFeature;
import net.swofty.pvp.feature.config.FeatureConfiguration;
import net.swofty.pvp.player.CombatPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Extension of {@link VanillaKnockbackFeature} which tries to make the playing field more even for players with high latency.
 * <p>
 * Rising knockback is applied usually when a player is on the ground.
 * This feature determines if the player would be on the ground <i>client side</i> instead of just server side.
 * To have just this change, use {@link FairKnockbackFeature#ONLY_RISING}.
 * <p>
 * The other option is {@link FairKnockbackFeature#RISING_AND_FALLING}, which, along with rising knockback,
 * also compensates for latency with falling knockback. It will use the (estimated) velocity of when the packet will arrive at the client,
 * possibly making falling knockback feel more natural.
 * <p>
 * The changes made by this feature only apply to players with more than 25 ms ping.
 */
public class FairKnockbackFeature extends VanillaKnockbackFeature {
	/**
	 * @see FairKnockbackFeature
	 */
	public static final DefinedFeature<FairKnockbackFeature> ONLY_RISING = new DefinedFeature<>(
			FeatureType.KNOCKBACK, configuration -> new FairKnockbackFeature(configuration, false),
			FeatureType.VERSION
	);
	/**
	 * @see FairKnockbackFeature
	 */
	public static final DefinedFeature<FairKnockbackFeature> RISING_AND_FALLING = new DefinedFeature<>(
			FeatureType.KNOCKBACK, configuration -> new FairKnockbackFeature(configuration, true),
			FeatureType.VERSION
	);

	private static final int PING_OFFSET = 25;

	protected final boolean compensateFallKnockback;

	public FairKnockbackFeature(FeatureConfiguration configuration, boolean compensateFallKnockback) {
		super(configuration);
		this.compensateFallKnockback = compensateFallKnockback;
	}

	@Override
	protected boolean applyKnockback(LivingEntity target, Entity attacker, @Nullable Entity source,
	                                 EntityKnockbackEvent.KnockbackType type, int extraKnockback,
	                                 double dx, double dz, boolean legacy) {
		if (!(target instanceof Player player) || player.getLatency() < PING_OFFSET)
			return super.applyKnockback(target, attacker, source, type, extraKnockback, dx, dz, legacy);

		KnockbackValues values = prepareKnockback(target, attacker, source, type, extraKnockback, dx, dz, legacy);
		if (values == null) return false;

		Vec velocity = target.getVelocity();
		if (legacy && type == EntityKnockbackEvent.KnockbackType.ATTACK) {
			// For legacy versions, extra knockback is added directly on top of the original velocity
			target.setVelocity(velocity.add(
					-values.horizontalModifier().x(),
					values.vertical(),
					-values.horizontalModifier().z()
			));
		} else {
			// For modern versions and legacy non-attack knockback, the velocity is first divided by 2

			int latencyTicks = getLatencyTicks(player.getLatency());
			double vertical;
			if (isOnGroundClientSide(player, latencyTicks)) {
				vertical = Math.min(values.verticalLimit(), velocity.y() / 2d + values.vertical());
			} else if (compensateFallKnockback) {
				vertical = getCompensatedVerticalVelocity(player.getAerodynamics(), velocity.y(), latencyTicks);
			} else {
				vertical = velocity.y();
			}

			target.setVelocity(new Vec(
					velocity.x() / 2d - values.horizontalModifier().x(),
					vertical,
					velocity.z() / 2d - values.horizontalModifier().z()
			));
		}

        if (values.animationType() == EntityKnockbackEvent.AnimationType.DIRECTIONAL) {
            // Send player a packet with its hurt direction
            sendDirectionalEvent(player, dx, dz);
        }

		return true;
	}

	protected boolean isOnGroundClientSide(Player player, int latencyTicks) {
		if (player.isOnGround() || !(player instanceof CombatPlayer combatPlayer)) return true;
		if (player.getGravityTickCount() > 30) return false; // Very uncertain, default to false

		// These are all cases in which isOnGroundAfterTicks() will not be accurate
		Block block = player.getInstance().getBlock(player.getPosition());
		if (player.isFlyingWithElytra()
				|| block.compare(Block.WATER)
				|| block.compare(Block.LAVA)
				|| block.compare(Block.COBWEB)
				|| block.compare(Block.SCAFFOLDING))
			return false;

		return combatPlayer.isOnGroundAfterTicks(latencyTicks);
	}

	/**
	 * Compensates the given vertical velocity for gravity calculations for a given amount of ticks.
	 * This means for every tick, it will be affected by gravity and vertical air resistance.
	 *
	 * @param aerodynamics the aerodynamics of the player
	 * @param velocity the velocity to compensate
	 * @param ticks the amount of ticks to compensate for
	 * @return the compensated vertical velocity
	 */
	protected static double getCompensatedVerticalVelocity(Aerodynamics aerodynamics, double velocity, int ticks) {
		for (int i = 0; i < ticks; i++) {
			velocity -= aerodynamics.gravity();
			velocity *= aerodynamics.verticalAirResistance();
		}

		return velocity;
	}

	private static int getLatencyTicks(int latencyMillis) {
		return Math.ceilDiv(latencyMillis * ServerProperties.SERVER_TICKS_PER_SECOND.get(), 1000) + 2;
	}
}
