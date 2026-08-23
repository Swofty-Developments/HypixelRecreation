package net.swofty.type.thepark;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.utility.ScheduleUtility;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.missions.thepark.darkthicket.MissionSneakUpOnRyan;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.thepark.npcs.NPCRyan;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RyanScene {

	private static final Map<UUID, Long> lastTriggered = new HashMap<>();

	public static void init() {
		MinecraftServer.getSchedulerManager().scheduleTask(() -> {
			for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
				handlePlayer(player);
			}
		}, TaskSchedule.seconds(1), TaskSchedule.tick(5));
	}

	private static void handlePlayer(SkyBlockPlayer player) {
		if (lastTriggered.containsKey(player.getUuid())) {
			long lastTime = lastTriggered.get(player.getUuid());
			if (System.currentTimeMillis() - lastTime < 5000) {
				return;
			}
		}

		MissionData missionData = player.getMissionData();
		if (missionData.hasCompleted(MissionSneakUpOnRyan.class)) {
			lastTriggered.remove(player.getUuid());
			return;
		}

		if (NPCRyan.hasInteractedMap.get(player.getUuid()) != null && NPCRyan.hasInteractedMap.get(player.getUuid())) {
			lastTriggered.remove(player.getUuid());
			return;
		}

		NPCRyan ryan = HypixelNPC.getRegisteredNPCs().stream()
				.filter(npc -> npc instanceof NPCRyan)
				.map(npc -> (NPCRyan) npc)
				.findFirst()
				.orElse(null);
		if (ryan == null) {
			return;
		}

		HypixelNPC.PlayerNPCCache npcCache = HypixelNPC.getPerPlayerNPCs().get(player.getUuid());
		Entity entity = npcCache.getEntityImpls().get(ryan);

		Pos pos = ryan.getParameters().position(player);
		if (entity == null || entity.getInstance() == null) return;

		if (isInView(entity.getInstance(), pos, player)) {
			ryan.sendNPCMessage(player, "Hey, you're not part of our <c>Cult<f>! Get out of here!");

			player.setSpeedManaged(true);

			float previousGravity = (float) player.getAttribute(Attribute.GRAVITY).getBaseValue();
			float previousFlyingSpeed = (float) player.getAttribute(Attribute.FLYING_SPEED).getBaseValue();

			player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0);
			player.getAttribute(Attribute.FLYING_SPEED).setBaseValue(0);
			player.getAttribute(Attribute.GRAVITY).setBaseValue(0);

			long startTime = System.currentTimeMillis();
			MinecraftServer.getSchedulerManager().submitTask(() -> {
				if ((System.currentTimeMillis() - startTime) > (20 * 50 * 3)) return TaskSchedule.stop();
				player.lookAt(pos.add(0, player.getEyeHeight(), 0));
				entity.lookAt(player.getPosition().add(0, player.getEyeHeight(), 0));
				return TaskSchedule.tick(1);
			}, ExecutionType.TICK_END);
			ScheduleUtility.delay(() -> {
				player.addEffect(new Potion(PotionEffect.BLINDNESS, 1, 20));
				player.teleport(new Pos(-380, 99, -45));
				player.getAttribute(Attribute.GRAVITY).setBaseValue(previousGravity);
				player.getAttribute(Attribute.FLYING_SPEED).setBaseValue(previousFlyingSpeed);
				player.setSpeedManaged(false);
			}, 20 * 3);
			lastTriggered.put(player.getUuid(), System.currentTimeMillis());
		}
	}

	private static boolean isInView(Instance instance, Pos viewer, SkyBlockPlayer player) {
		Pos targetPos = player.getPosition();

		double dx = targetPos.x() - viewer.x();
		double dz = targetPos.z() - viewer.z();
		double distSq = dx * dx + dz * dz;
		if (distSq > 225) return false;

		double distance = Math.sqrt(distSq);
		if (distance < 1e-6) return true;

		double nx = dx / distance;
		double nz = dz / distance;

		double yawRad = Math.toRadians(viewer.yaw());
		double fx = -Math.sin(yawRad);
		double fz = Math.cos(yawRad);

		double dot = fx * nx + fz * nz;
		if (dot > 1.0) dot = 1.0;
		if (dot < -1.0) dot = -1.0;

		double angleDeg = Math.toDegrees(Math.acos(dot));
		if (angleDeg > 90) return false;

		double startY = viewer.y() + 1.8;
		double endY = targetPos.y() + player.getEyeHeight();

		double startX = viewer.x();
		double startZ = viewer.z();

		double endX = targetPos.x();
		double endZ = targetPos.z();

		double vx = endX - startX;
		double vy = endY - startY;
		double vz = endZ - startZ;
		double fullDistance = Math.sqrt(vx * vx + vy * vy + vz * vz);
		if (fullDistance < 1e-6) return true;

		double step = 0.2;
		int steps = (int) Math.ceil(fullDistance / step);

		double dirX = vx / fullDistance;
		double dirY = vy / fullDistance;
		double dirZ = vz / fullDistance;

		for (int i = 1; i < steps; i++) {
			double t = i * step;
			double px = startX + dirX * t;
			double py = startY + dirY * t;
			double pz = startZ + dirZ * t;

			int bx = (int) Math.floor(px);
			int by = (int) Math.floor(py);
			int bz = (int) Math.floor(pz);

			Block block = instance.getBlock(bx, by, bz);
			if (block.air()) continue;
			if (!block.solid()) continue;
			String name = block.toString().toUpperCase();
			if (name.contains("LEAVES")) continue;

			return false;
		}

		return true;
	}

}
