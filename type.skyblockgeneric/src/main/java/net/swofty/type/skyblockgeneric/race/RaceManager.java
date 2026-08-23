package net.swofty.type.skyblockgeneric.race;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.data.datapoints.DatapointMapStringLong;
import net.swofty.type.generic.entity.hologram.PlayerHolograms;
import net.swofty.type.generic.event.HypixelEventHandler;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.event.custom.ActionPlayerFinishRace;
import net.swofty.type.skyblockgeneric.user.SkyBlockActionBar;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
public class RaceManager {

	@Getter
	private final Race race;

	@Getter
	private final Map<UUID, RunData> perPlayerStartTime = new ConcurrentHashMap<>();

	@Getter
	private static final Map<UUID, HologramCache> perPlayerHolo = new HashMap<>();

	public void updateForPlayer(Instance instance, SkyBlockPlayer player) {
		List<Point> checkpoints = race.getCheckpoints();
		if (checkpoints.isEmpty()) return;

		Point start = race.startPosition();
		Point end = race.endPosition();

		boolean isSame = start.sameBlock(end);

		Thread.startVirtualThread(() -> {
			boolean playerHasHolograms = perPlayerHolo.containsKey(player.getUuid()) &&
					!perPlayerHolo.get(player.getUuid()).getHolograms().isEmpty();

			if (!playerHasHolograms) {
				HologramCache cache = new HologramCache();
				String titleMarkup = titleMarkup();
				String[] startHolograms = new String[]{
						titleMarkup,
						"<a>Start"
				};

				if (perPlayerStartTime.containsKey(player.getUuid())) {
					RunData runData = perPlayerStartTime.get(player.getUuid());
					if (runData.lastCheckpointIndex() == -1) {
						startHolograms = new String[]{
								titleMarkup,
								"<a>Reset Timer",
						};
					} else if (isSame) {
						startHolograms = new String[]{
								titleMarkup,
								"<a>Finish"
						};
					}
				}

				PlayerHolograms.ExternalPlayerHologram startHolo = PlayerHolograms.ExternalPlayerHologram.builder()
						.pos(start.asPos().add(0.5, 0, 0.5))
						.text(startHolograms)
						.player(player)
						.instance(instance)
						.build();
				PlayerHolograms.addExternalPlayerHologram(startHolo);
				cache.add(startHolo);

				for (Point checkpointPos : checkpoints) {
					String[] checkpointHolograms = new String[]{
							titleMarkup,
							"<a>Checkpoint"
					};
					PlayerHolograms.ExternalPlayerHologram checkpointHolo = PlayerHolograms.ExternalPlayerHologram.builder()
							.pos(checkpointPos.asPos().add(0.5, 0, 0.5))
							.text(checkpointHolograms)
							.player(player)
							.instance(instance)
							.build();
					PlayerHolograms.addExternalPlayerHologram(checkpointHolo);
					cache.add(checkpointHolo);
				}

				if (!isSame) {
					String[] endHolograms = new String[]{
							titleMarkup,
							"<a>Finish"
					};
					PlayerHolograms.ExternalPlayerHologram endHolo = PlayerHolograms.ExternalPlayerHologram.builder()
							.pos(end.asPos().add(0.5, 0, 0.5))
							.text(endHolograms)
							.player(player)
							.instance(instance)
							.build();
					PlayerHolograms.addExternalPlayerHologram(endHolo);
					cache.add(endHolo);
				}

				perPlayerHolo.put(player.getUuid(), cache);
				return;
			}
			HologramCache cache = perPlayerHolo.get(player.getUuid());
			cache.getHolograms().forEach(PlayerHolograms::removeExternalPlayerHologram);
			perPlayerHolo.remove(player.getUuid());
			updateForPlayer(instance, player);
		});
	}

	public void startRace(SkyBlockPlayer player) {
		if (perPlayerStartTime.containsKey(player.getUuid())) {
			perPlayerStartTime.remove(player.getUuid());
			perPlayerStartTime.put(player.getUuid(), new RunData(System.currentTimeMillis(), -1));
			player.sendMessage("{} <e>Timer reset to <a>0:00<e>!", title());
			return;
		}

		if (!getRace().canStart(player)) return;

		perPlayerStartTime.put(player.getUuid(), new RunData(System.currentTimeMillis(), -1));
		player.sendMessage("{} <e>Race started! Good luck!", title());
		updateForPlayer(HypixelConst.getInstanceContainer(), player);

		MinecraftServer.getSchedulerManager().submitTask(() -> {
			if (!perPlayerStartTime.containsKey(player.getUuid())) {
				return TaskSchedule.stop();
			}

			// Check for time limit
			if (race.timeLimit() > 0) {
				long timeElapsed = System.currentTimeMillis() - perPlayerStartTime.get(player.getUuid()).startTime();
				if (timeElapsed >= race.timeLimit() * 1000L) {
					perPlayerStartTime.remove(player.getUuid());
					player.sendMessage("{} <c>Race cancelled! Time limit reached!", title());
					player.playSound(Sound.sound(
							Key.key("block.note_block.pling"), Sound.Source.NEUTRAL,
							.3f, 0.75f
					));
					updateForPlayer(HypixelConst.getInstanceContainer(), player);
					return TaskSchedule.stop();
				}
			}

			long timeElapsed = System.currentTimeMillis() - perPlayerStartTime.get(player.getUuid()).startTime();
			String timeString = String.format("%02d:%05.2f", timeElapsed / 60000, (timeElapsed % 60000) / 1000.0);
			SkyBlockActionBar.getFor(player).addReplacement(
					SkyBlockActionBar.BarSection.HEALTH,
					Text.of(titleMarkup() + " <e>{}", timeString),
					25,
					5
			);
			return TaskSchedule.millis(200);
		}, ExecutionType.TICK_END);
	}

	public void checkpointPlayer(SkyBlockPlayer player, int checkpointIndex) {
		RunData runData = perPlayerStartTime.get(player.getUuid());
		if (runData == null) {
			player.sendMessage("{} <c>This is the end of a race, usually you start at the beginning!", title());
			return;
		}
		if (checkpointIndex <= runData.lastCheckpointIndex()) {
			return;
		}
		perPlayerStartTime.put(player.getUuid(), new RunData(runData.startTime(), checkpointIndex));
		race.onCheckpoint(player, checkpointIndex, System.currentTimeMillis() - runData.startTime());
		updateForPlayer(HypixelConst.getInstanceContainer(), player);
	}

	public void finishedRace(SkyBlockPlayer player) {
		RunData runData = perPlayerStartTime.get(player.getUuid());
		if (runData == null) {
			player.sendMessage("{} <c>This is the end of a race, usually you start at the beginning!", title());
			return;
		}

		if (runData.lastCheckpointIndex() < race.getCheckpoints().size() - 1) {
			player.sendMessage("{} <c>You haven't passed all the checkpoints yet!", title());
			return;
		}

		perPlayerStartTime.remove(player.getUuid());
		long totalTime = System.currentTimeMillis() - runData.startTime();

		SkyBlockDataHandler dataHandler = player.getSkyblockDataHandler();
		DatapointMapStringLong data = dataHandler.get(SkyBlockDataHandler.Data.RACE_BEST_TIME, DatapointMapStringLong.class);

		boolean isRecord = false;
		if (data.getValue().isEmpty()) {
			data.getValue().put(race.getId(), totalTime);
			isRecord = true;
		} else if (!data.getValue().containsKey(race.getId()) || totalTime < data.getValue().get(race.getId())) {
			data.getValue().put(race.getId(), totalTime);
			isRecord = true;
		}

		race.onFinish(player, totalTime, isRecord);
		updateForPlayer(HypixelConst.getInstanceContainer(), player);

		HypixelEventHandler.callCustomEvent(new ActionPlayerFinishRace(
				player,
				race,
				totalTime
		));
	}

	public void cancelRace(SkyBlockPlayer player) {
		if (!perPlayerStartTime.containsKey(player.getUuid())) return;
		perPlayerStartTime.remove(player.getUuid());
		updateForPlayer(HypixelConst.getInstanceContainer(), player);
	}

	private Text title() {
		String raw = race.getTitle();
		return Text.read(raw);
	}

	private String titleMarkup() {
		return title().serialize();
	}

	public record RunData(Long startTime, int lastCheckpointIndex) {
	}

	@Getter
	public static class HologramCache {
		private final List<PlayerHolograms.ExternalPlayerHologram> holograms = new ArrayList<>();

		public void add(PlayerHolograms.ExternalPlayerHologram hologram) {
			holograms.add(hologram);
		}
	}

}
