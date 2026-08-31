package net.swofty.type.generic.command.commands.replay;

import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.protocol.objects.replay.ChooseReplayProtocolObject;
import net.swofty.commons.protocol.objects.replay.ReplayListProtocolObject;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.gui.impl.replay.ReplayEntry;
import net.swofty.type.generic.gui.impl.replay.ReplaysListView;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.generic.utility.ScheduleUtility;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@CommandParameters(
	description = "View your game replays",
	usage = "/replays",
	permission = Rank.DEFAULT,
		allowsConsole = false,
		labels = "replays"
)
public class ReplaysCommand extends HypixelCommand {

	@Override
	public void registerUsage(MinestomCommand command) {
		command.setDefaultExecutor((sender, _) -> {
			final HypixelPlayer player = (HypixelPlayer) sender;

			player.sendMessage(Text.key("replays.loading_replays"));
			displaySendReplay(player);
		});
	}

	public static void displaySendReplay(final HypixelPlayer player) {
		fetchReplays(player).thenAccept(replays -> {
			player.openView(new ReplaysListView(replay -> {
				sendToReplayViewer(player, replay);
			}), new ReplaysListView.State(replays, 0));
		}).exceptionally(e -> {
			player.sendMessage(Text.key("replays.replays_load_failed"));
			return null;
		});
	}

	private static CompletableFuture<List<ReplayEntry>> fetchReplays(HypixelPlayer player) {
		var request = new ReplayListProtocolObject.ListRequest(player.getUuid(), 50, null);
		ProxyService replayService = new ProxyService(ServiceType.REPLAY);

		return replayService.<ReplayListProtocolObject.ListRequest, ReplayListProtocolObject.ListResponse>handleRequest(request)
			.thenApply(response -> {
				if (!response.success()) {
					return List.of();
				}

				return response.replays().stream()
					.map(summary -> ReplayEntry.builder()
						.replayId(summary.replayId())
						.gameId(summary.gameId())
						.serverType(summary.serverType())
						.serverId(summary.serverId())
						.gameTypeName(summary.gameTypeName())
						.mapName(summary.mapName())
						.startTime(summary.startTime())
						.endTime(summary.endTime())
						.durationTicks(summary.durationTicks())
						.players(summary.players())
						.winnerId(summary.winnerId())
						.winnerType(summary.winnerType())
						.dataSize(summary.dataSize())
						.build())
					.collect(Collectors.toList());
			});
	}

	private static void sendToReplayViewer(HypixelPlayer player, ReplayEntry replay) {
		player.sendMessage(Text.key("replays.loading_replay"));

		ProxyService replayService = new ProxyService(ServiceType.REPLAY);
		var request = new ChooseReplayProtocolObject.ChooseReplayMessage(player.getUuid(), replay.replayId().toString());
		replayService.<ChooseReplayProtocolObject.ChooseReplayMessage, ChooseReplayProtocolObject.ChooseReplayResponse>handleRequest(request).thenAccept(response -> {
			ScheduleUtility.nextTick(() -> {
				if (!response.error()) {
					player.sendMessage(Text.key("replays.sending_to_viewer"));
					player.sendTo(ServerType.REPLAY_VIEWER);
				} else {
					player.sendMessage(Text.key("replays.viewer_send_failed"));
				}
			});
		}).exceptionally(e -> {
			ScheduleUtility.nextTick(() -> player.sendMessage(Text.key("replays.replay_load_failed_with_error",
					String.valueOf(e.getMessage()))));
			return null;
		});
	}
}
