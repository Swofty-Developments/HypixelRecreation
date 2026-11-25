package net.swofty.type.bedwarsgame.game.listener;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.sound.SoundEventKeys;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.ServerType;
import net.swofty.commons.bedwars.BedwarsLevelUtil;
import net.swofty.commons.text.Text;
import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.bedwarsgame.game.BedWarsGameStat;
import net.swofty.type.bedwarsgame.game.BedWarsTeam;
import net.swofty.type.bedwarsgame.stats.BedWarsStatsRecorder;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.game.game.event.GameTeamWinConditionEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.guild.GuildManager;
import org.tinylog.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class GameEndListener implements HypixelEventClass {

    private static final Text THICK_BAR = Text.of("<a><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false)
    public void onGameEnd(GameTeamWinConditionEvent<BedWarsTeam> event) {
        BedWarsGame game = (BedWarsGame) event.game();
        String gameId = game.getGameId();

        // Show results to all players
        for (BedWarsPlayer player : game.getPlayers()) {
            player.playSound(Sound.sound(SoundEventKeys.UI_TOAST_CHALLENGE_COMPLETE.key(),
                Sound.Source.MASTER, 1f, 1f), Sound.Emitter.self());

            // Record win
            event.team().ifPresent(team -> {
                if (team.hasPlayer(player.getUuid())) {
                    if (player.allowsPersistentProgress()) {
                        BedWarsStatsRecorder.recordWin(player, game.getGameType());
                        player.getAchievementHandler().addProgressByTrigger("bedwars.wins", 1);
                        GuildManager.recordProgress(player, 0, true);
                    }
                }
            });

            player.setGameMode(GameMode.ADVENTURE);
            if (player.allowsPersistentProgress()) GuildManager.recordProgress(player, 20, false);
        }

        boolean isRecording = game.getReplayManager().isRecording();
        game.getReplayManager().stopRecording();

        game.getGeneratorManager().stopAllGenerators();
        game.getGameEventManager().stop();
        game.getSwappageManager().stop();
        game.getOneBlockManager().stop();

        Logger.info("Ending game " + gameId);
        game.end();

        for (BedWarsPlayer player : game.getPlayers()) {
            player.sendMessage(THICK_BAR);
            player.sendMessage("<center><f><l>Bed Wars</center>");
            player.sendMessage(Text.empty());

            event.team().ifPresent(team -> {
                List<Text> playerNames = team.getPlayerIds().stream()
                    .map(game::getPlayer)
                    .flatMap(Optional::stream)
                    .map(p -> Text.of("{}", p.getColouredName()))
                    .toList();
                player.sendMessage("<center><f>{} <7>- {}</center>",
                    Text.of("<color:{}>{}", team.getColor(), team.getName()),
                    Text.join(Text.of("<7>,"), playerNames));
                player.sendMessage(Text.empty());
            });

            List<BedWarsPlayer> killers = game.getPlayers().stream()
                    .sorted(Comparator.comparingLong((BedWarsPlayer killer) ->
                                    game.getGameStats().get(killer.getUuid(), BedWarsGameStat.KILLS)).reversed()
                            .thenComparing(BedWarsPlayer::getUsername, String.CASE_INSENSITIVE_ORDER))
                    .limit(3)
                    .toList();
            String[] places = {"<e><l>1st Killer", "<6><l>2nd Killer", "<c><l>3rd Killer"};
            for (int index = 0; index < killers.size(); index++) {
                BedWarsPlayer killer = killers.get(index);
                player.sendMessage("<center>{} <7>- {} <7>- {}</center>",
                        Text.of(places[index]), killer.getColouredName(),
                        game.getGameStats().get(killer.getUuid(), BedWarsGameStat.KILLS));
            }
            player.sendMessage(Text.empty());
            player.sendMessage(THICK_BAR);
        }

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            for (BedWarsPlayer player : game.getPlayers()) {
                player.sendMessage(THICK_BAR);
                player.sendMessage("<center><f><l>Reward Summary</center>");
                player.sendMessage(Text.empty());
                player.sendMessage("   <7>You earned:");
                player.sendMessage("    <f>• <2>{} Bed Wars Tokens",
                        game.getGameStats().get(player.getUuid(), BedWarsGameStat.TOKENS));
                player.sendMessage("    <f>• <3>{} Hypixel Experience",
                        game.getGameStats().get(player.getUuid(), BedWarsGameStat.HYPIXEL_EXPERIENCE));
                player.sendMessage("    <f>• <7>0 Guild Experience");
                player.sendMessage(Text.empty());
                player.sendMessage("<center><b>Bed Wars XP</center>");

                long currentLevel = player.getCurrentBedWarsLevel();
                player.sendMessage("<f>          <b>Level {}                                     <b>Level {}", currentLevel, currentLevel + 1);

                long experience = player.getCurrentBedWarsExperience();
                int progress = BedwarsLevelUtil.calculateExperienceSinceLastLevel(experience);
                int maxExperience = BedwarsLevelUtil.calculateMaxExperienceFromExperience(experience);

                double percentage = Math.min(1.0, (double) progress / maxExperience);
                int filledSquares = (int) Math.round(percentage * 34);
                StringBuilder progressBar = new StringBuilder("<8>[");
                for (int i = 0; i < 34; i++) {
                    if (i < filledSquares) {
                        progressBar.append("<b>■");
                    } else {
                        progressBar.append("<7>■");
                    }
                }
                progressBar.append("<8>]");
                player.sendMessage(Text.of("<f>          ").append(Text.of(progressBar.toString())));

                String prettyExperience = String.format("%,d", experience);
                String prettyMaxExperience = String.format("%,d", maxExperience);

                String percentageString = String.format("%.1f", percentage * 100);

                player.sendMessage("<center><b>{} <7>/ <a>{} <7>({}%)</center>", prettyExperience, prettyMaxExperience, percentageString);

                player.sendMessage(Text.empty());
                player.sendMessage("<7>You earned <b>{} Bed Wars XP",
                        game.getGameStats().get(player.getUuid(), BedWarsGameStat.BED_WARS_EXPERIENCE));
                player.sendMessage(Text.empty());
                // xp multipliers shown here
                player.sendMessage(THICK_BAR);
                if (isRecording) {
                    player.sendMessage(Text.of("<click:run:'/replay {}'><a>This game has been recorded. <6>Click here to watch the Replay!",
                        game.getReplayManager().getRecorder().getReplayId()));
                }
                player.sendMessage(Text.empty());
            }
        }).delay(TaskSchedule.seconds(2)).schedule();

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            game.getPlayers().forEach(p -> p.sendTo(ServerType.BEDWARS_LOBBY));
            game.dispose();
        }).delay(TaskSchedule.seconds(7)).schedule();
    }

}
