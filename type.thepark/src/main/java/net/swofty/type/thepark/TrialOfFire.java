package net.swofty.type.thepark;

import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointInteger;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.mission.missions.thepark.darkthicket.MissionCompleteTrialOfFireOne;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrialOfFire {

	private static final Map<UUID, Integer> competingPlayers = new HashMap<>();

	public static void init() {
		MinecraftServer.getSchedulerManager().scheduleTask(() -> {
			for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
				if (!player.getMissionData().isCurrentlyActive(MissionCompleteTrialOfFireOne.class)) {
					return;
				}

				Block block = player.getInstance().getBlock(player.getPosition().sub(0, 0.3, 0));
				if (block.key() == Block.CAMPFIRE.key()) {
					start(player);
				}
			}
		}, TaskSchedule.seconds(1), TaskSchedule.millis(200));
	}

	public static void start(SkyBlockPlayer player) {
		if (isCompeting(player)) {
			return;
		}

		SkyBlockDataHandler dataHandler = player.getSkyblockDataHandler();
		int level = dataHandler.get(SkyBlockDataHandler.Data.TRIAL_OF_FIRE_LEVEL, DatapointInteger.class).getValue();
		Trial trial = Arrays.stream(Trial.values())
				.filter(t -> (t.ordinal() + 1) == (level + 1))
				.findFirst()
				.orElse(null);

		if (trial == null) {
			sendMessage(player, "<c>You have already completed the <6>Trial of Fire<f>!");
			return;
		}

		// TODO: remove this
		if (trial == Trial.II) {
			sendMessage(player, "<c>Trials are not finished.");
			player.notImplemented();
			return;
		}

		competingPlayers.put(player.getUuid(), 0);
		MinecraftServer.getSchedulerManager().submitTask(() -> {
			int iterations = competingPlayers.get(player.getUuid());
			if (iterations == 0) {
				sendMessage(player, "Started <6>Trial of Fire<f>!");
			}

			player.damage(new Damage(DamageType.CAMPFIRE, null, null, player.getPosition(), trial.dps));
			if (player.getInstance().getBlock(player.getPosition().sub(0, 0.3, 0)).key() != Block.CAMPFIRE.key()) {
				sendMessage(player, "<c>Trial cancelled: <f>You stepped out of the campfire!");
				competingPlayers.remove(player.getUuid());
				return TaskSchedule.stop();
			}

			competingPlayers.put(player.getUuid(), iterations + 1);

			if (iterations + 1 >= 10) {
				player.showTitle(
						Text.of("<a>Complete!"),
						Text.literal(" "),
						Title.Times.times(Duration.ofSeconds(1), Duration.ZERO, Duration.ofSeconds(1))
				);
				sendMessage(player, "<a>Completed <6>Trial of Fire {}", trial.name());
				player.setVelocity(new Vec(12, 18, 12));
				competingPlayers.remove(player.getUuid());

				SkyBlockDataHandler handler = player.getSkyblockDataHandler();
				DatapointInteger levelData = handler.get(SkyBlockDataHandler.Data.TRIAL_OF_FIRE_LEVEL, DatapointInteger.class);
				levelData.setValue(levelData.getValue() + 1);

				switch (levelData.getValue()) {
					case 1:
						player.getMissionData().endMission(MissionCompleteTrialOfFireOne.class);
						break;
				}

				return TaskSchedule.stop();
			}

			return TaskSchedule.seconds(1);
		}, ExecutionType.TICK_END);

	}

	public static boolean isCompeting(SkyBlockPlayer player) {
		return competingPlayers.containsKey(player.getUuid());
	}

	private static void sendMessage(SkyBlockPlayer player, String message, Object... args) {
		player.sendMessage("<6><l>TRIAL OF FIRE! </l><f>" + message, args);
	}

	enum Trial {
		I(10),
		II(15),
		III(25),
		IV(35),
		V(50),
		VI(65),
		VII(85),
		VIII(110),
		IX(140),
		X(185),
		XI(240),
		XII(315),
		XIII(410),
		XIV(530),
		;

		private final int dps;

		Trial(int dps) {
			this.dps = dps;
		}
	}

}
