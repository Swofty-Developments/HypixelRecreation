package net.swofty.type.lobby.parkour;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.datapoints.DatapointParkourData;
import net.swofty.type.generic.entity.hologram.PlayerHolograms;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.lobby.LobbyTypeLoader;
import net.swofty.type.lobby.item.LobbyItem;
import net.swofty.type.lobby.item.impl.CancelParkour;
import net.swofty.type.lobby.item.impl.LastCheckpoint;
import net.swofty.type.lobby.item.impl.ResetParkour;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@AllArgsConstructor
public class LobbyParkourManager {

	private final Parkour parkour;
	private final Map<UUID, RunData> perPlayerStartTime = new ConcurrentHashMap<>();
	private static final Map<UUID, HologramCache> perPlayerHolo = new HashMap<>();

	public void updateForPlayer(Instance instance, HypixelPlayer player) {
		List<Point> checkpoints = parkour.getCheckpoints();
		if (checkpoints.isEmpty()) return;

		Point start = checkpoints.getFirst();
		Point end = checkpoints.getLast();

		Thread.startVirtualThread(() -> {
			boolean playerHasHolograms = perPlayerHolo.containsKey(player.getUuid()) &&
					!perPlayerHolo.get(player.getUuid()).getHolograms().isEmpty();

			if (!playerHasHolograms) {
				HologramCache cache = new HologramCache();

				Map<DatapointParkourData.ParkourType, Long> time = player.getDataHandler().get(HypixelDataHandler.Data.PARKOUR_DATA, DatapointParkourData.class).getValue();
				Long bestTime = time.getOrDefault(parkour.getId(), null);
				Logger.info("Best time for player " + player.getUsername() + " on parkour " + parkour.getId() + " is " + bestTime);
				String bestTimeString = "00:00.000";
				if (bestTime != null) {
					bestTimeString = String.format("%02d:%02d.%03d",
							(bestTime / 60000),
							(bestTime % 60000) / 1000,
							(bestTime % 1000));
				}

				String[] startHolograms = new String[]{
						"<e><l>Parkour Challenge",
						"<6><l>Your best time: <e>" + bestTimeString,
						"<a><l>Start"
				};
				PlayerHolograms.ExternalPlayerHologram startHolo = PlayerHolograms.ExternalPlayerHologram.builder()
					.pos(start.asPos().add(0.5, 1, 0.5))
						.text(startHolograms)
						.player(player)
						.instance(instance)
						.build();
				PlayerHolograms.addExternalPlayerHologram(startHolo);
				cache.add(startHolo);

				for (int i = 1; i < checkpoints.size() - 1; i++) {
					Point checkpointPos = checkpoints.get(i);
					String[] checkpointHolograms = new String[]{
							"<e><l>Checkpoint",
							"<b><l>#" + i
					};
					PlayerHolograms.ExternalPlayerHologram checkpointHolo = PlayerHolograms.ExternalPlayerHologram.builder()
						.pos(checkpointPos.asPos().add(0.5, 1, 0.5))
							.text(checkpointHolograms)
							.player(player)
							.instance(instance)
							.build();
					PlayerHolograms.addExternalPlayerHologram(checkpointHolo);
					cache.add(checkpointHolo);
				}

				if (checkpoints.size() > 1) {
					String[] endHolograms = new String[]{
							"<e><l>Parkour Challenge",
							"<c><l>End"
					};
					PlayerHolograms.ExternalPlayerHologram endHolo = PlayerHolograms.ExternalPlayerHologram.builder()
						.pos(end.asPos().add(0.5, 1, 0.5))
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

	public void startParkour(HypixelPlayer player) {
		if (perPlayerStartTime.containsKey(player.getUuid())) {
			perPlayerStartTime.remove(player.getUuid());
			perPlayerStartTime.put(player.getUuid(), new RunData(System.currentTimeMillis(), 0));
			player.sendMessage("<a><l>Reset your timer to 00:00! Get to the finish line!");
			return;
		}
		perPlayerStartTime.put(player.getUuid(), new RunData(System.currentTimeMillis(), 0));
		player.sendMessage("<a><l>Parkour challenge started!");
		player.sendMessage("<a>Use <e>/parkour checkpoint <a>to teleport to the last checkpoint or <e>/parkour cancel <a>to cancel!");

		player.getInventory().setItemStack(3, new LastCheckpoint().getItemStack(player));
		player.getInventory().setItemStack(4, new ResetParkour().getItemStack(player));
		player.getInventory().setItemStack(5, new CancelParkour().getItemStack(player));
	}

	public void finishedParkour(HypixelPlayer player) {
		RunData runData = perPlayerStartTime.get(player.getUuid());
		if (runData == null || runData.startTime() == null) {
			player.sendMessage("<c>You haven't started the parkour challenge yet! Use <e>/parkour start <c>to start!");
			return;
		}

		long startTime = runData.startTime();

		int lastCheckpointIndex = perPlayerStartTime.get(player.getUuid()).lastCheckpointIndex();
		if (lastCheckpointIndex != parkour.getCheckpoints().size() - 2) {
			player.sendMessage("<c>You must go through all checkpoints before finishing the parkour challenge!");
			return;
		}

		long timeTaken = System.currentTimeMillis() - startTime;
		DatapointParkourData datapoint = player.getDataHandler().get(HypixelDataHandler.Data.PARKOUR_DATA, DatapointParkourData.class);
		Map<DatapointParkourData.ParkourType, Long> data = datapoint.getValue();

		long previousTimeTaken = data.getOrDefault(parkour.getId(), 0L);
		boolean newRecord = false;
		if (data.containsKey(parkour.getId())) {
			long previousBest = data.get(parkour.getId());
			if (timeTaken < previousBest) {
				data.put(parkour.getId(), timeTaken);
				newRecord = true;
			}
		} else {
			data.put(parkour.getId(), timeTaken);
			newRecord = true;
		}
		datapoint.setValue(data);
		String timeString = String.format("%02d:%02d.%03d",
				(timeTaken / 60000),
				(timeTaken % 60000) / 1000,
				(timeTaken % 1000));

		if (newRecord) {
			player.sendMessage("<a><l>That's a new record of <e>{}<a>! Try again to get an even better record!", timeString);
		} else {
			String previousTimeString = String.format("%02d:%02d.%03d",
					(previousTimeTaken / 60000),
					(previousTimeTaken % 60000) / 1000,
					(previousTimeTaken % 1000));
			player.sendMessage("<a><l>Your time of <e>{}<a> did not beat your previous record of <e>{}<a>! Try again to beat your old record!", timeString, previousTimeString);
		}

		if (HypixelConst.getTypeLoader() instanceof LobbyTypeLoader lobbyLoader) {
			player.getInventory().clear();
			Map<Integer, LobbyItem> hotbarItems = lobbyLoader.getHotbarItems();
			for (Map.Entry<Integer, LobbyItem> entry : hotbarItems.entrySet()) {
				player.getInventory().setItemStack(
						entry.getKey(),
						entry.getValue().getItemStack(player)
				);
			}
		}

		updateForPlayer(player.getInstance(), player);
	}

	public void checkpointPlayer(HypixelPlayer player, int checkpointIndex) {
		RunData runData = perPlayerStartTime.get(player.getUuid());
		if (runData == null) {
			player.sendMessage("<c>You haven't started the parkour challenge yet! Use <e>/parkour start <c>to start!");
			return;
		}
		if (checkpointIndex <= runData.lastCheckpointIndex()) {
			return;
		}
		perPlayerStartTime.put(player.getUuid(), new RunData(runData.startTime(), checkpointIndex));
		String timeString = String.format("%02d:%02d.%03d",
				((System.currentTimeMillis() - runData.startTime()) / 60000),
				((System.currentTimeMillis() - runData.startTime()) % 60000) / 1000,
				((System.currentTimeMillis() - runData.startTime()) % 1000));
		player.sendMessage("<a><l>You reached <e>Checkpoint #{} <a>after <e>{}<a>.", checkpointIndex, timeString);
	}

	public void cancelParkour(HypixelPlayer player) {
		if (perPlayerStartTime.containsKey(player.getUuid())) {
			perPlayerStartTime.remove(player.getUuid());

			if (!player.isOnline()) return;
			player.sendMessage("<c><l>Parkour challenge cancelled!");

			if (HypixelConst.getTypeLoader() instanceof LobbyTypeLoader lobbyLoader) {
				player.getInventory().clear();
				Map<Integer, LobbyItem> hotbarItems = lobbyLoader.getHotbarItems();
				for (Map.Entry<Integer, LobbyItem> entry : hotbarItems.entrySet()) {
					player.getInventory().setItemStack(
							entry.getKey(),
							entry.getValue().getItemStack(player)
					);
				}
			}
		} else {
			if (!player.isOnline()) return;
			player.sendMessage("<c>You haven't started the parkour challenge yet! Use <e>/parkour start <c>to start!");
		}
	}

	public void resetPlayer(HypixelPlayer player) {
		player.teleport(parkour.getStartLocation());
		startParkour(player);
	}

	public static void removePlayerHolograms(HypixelPlayer player) {
		HologramCache cache = perPlayerHolo.remove(player.getUuid());
		if (cache != null) {
			for (PlayerHolograms.ExternalPlayerHologram holo : cache.getHolograms()) {
				PlayerHolograms.removeExternalPlayerHologram(holo);
			}
		}
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
