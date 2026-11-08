package net.swofty.pvp.feature.food;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import net.swofty.pvp.utils.ViewUtil;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.world.DimensionType;

import java.util.concurrent.ThreadLocalRandom;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChorusFruitUtil {

	private static boolean randomTeleport(Entity entity, Pos to, boolean directionalParticles) {
		Instance instance = entity.getInstance();
		assert instance != null;
		
		boolean success = false;
		int lowestY = to.blockY();
		if (lowestY == 0) lowestY++;
		while (lowestY > MinecraftServer.getDimensionTypeRegistry().get(instance.getDimensionType()).minY()) {
			Block block = instance.getBlock(to.blockX(), lowestY - 1, to.blockZ());
			if (!block.air() && !block.liquid()) {
				Block above = instance.getBlock(to.blockX(), lowestY, to.blockZ());
				Block above2 = instance.getBlock(to.blockX(), lowestY + 1, to.blockZ());
				if (above.air() && above2.air()) {
					success = true;
					break;
				} else {
					lowestY--;
				}
			} else {
				lowestY--;
			}
		}
		
		if (!success) return false;
		
		entity.teleport(to.withY(lowestY));
		if (directionalParticles) entity.triggerStatus((byte) 46);
		
		return true;
	}
	
	public static void tryChorusTeleport(Entity entity, float diameter, boolean directionalParticles) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		Instance instance = entity.getInstance();
		assert instance != null;
		float radius = diameter / 2.0f;
		
		Pos prevPosition = entity.getPosition();
		double prevX = prevPosition.x();
		double prevY = prevPosition.y();
		double prevZ = prevPosition.z();
		
		float pitch = prevPosition.pitch();
		float yaw = prevPosition.yaw();
		
		DimensionType dimensionType = MinecraftServer.getDimensionTypeRegistry().get(instance.getDimensionType());
		assert dimensionType != null;
		
		// Max 16 tries
		for (int i = 0; i < 16; i++) {
			double x = prevX + (random.nextDouble() - 0.5) * radius;
			double y = MathUtils.clamp(prevY + (random.nextInt(16) - 8),
					dimensionType.minY(), dimensionType.minY()
							+ dimensionType.logicalHeight() - 1);
			double z = prevZ + (random.nextDouble() - 0.5) * radius;
			
			if (entity.getVehicle() != null) {
				entity.getVehicle().removePassenger(entity);
			}
			
			if (randomTeleport(entity, new Pos(x, y, z, yaw, pitch), directionalParticles)) {
				ViewUtil.packetGroup(entity).playSound(Sound.sound(
						SoundEvent.ITEM_CHORUS_FRUIT_TELEPORT, Sound.Source.PLAYER,
						1.0f, 1.0f
				), prevPosition);
				
				if (!entity.isSilent()) {
					entity.getViewersAsAudience().playSound(Sound.sound(
							SoundEvent.ITEM_CHORUS_FRUIT_TELEPORT, Sound.Source.PLAYER,
							1.0f, 1.0f
					), entity);
				}
				
				break;
			}
		}
	}
}
