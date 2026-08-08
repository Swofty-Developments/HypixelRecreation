package net.swofty.type.bedwarsgame.game.v2.listener;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.sound.SoundEventKeys;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.ChatUtility;
import net.swofty.commons.ServerType;
import net.swofty.commons.bedwars.BedwarsLevelUtil;
import net.swofty.type.bedwarsgame.game.v2.BedWarsGame;
import net.swofty.type.bedwarsgame.game.v2.BedWarsTeam;
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

    private static final String THICK_BAR = "§a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬";

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
            player.sendMessage(Component.text(THICK_BAR));
            player.sendMessage(Component.text(ChatUtility.FontInfo.center("§lBed Wars"), NamedTextColor.WHITE));
            player.sendMessage(Component.empty());

            event.team().ifPresent(team -> {
                String playerNamesConcatenated = team.getPlayerIds().stream()
                    .map(game::getPlayer)
                    .filter(Optional::isPresent)
                    .map(p -> LegacyComponentSerializer.legacySection().serialize(p.get().getColouredName()))
                    .reduce((a, b) -> a + "§7," + b)
                    .orElse("");
                player.sendMessage(Component.text(ChatUtility.FontInfo.center(team.getColorCode() + team.getName() + " §7- " + playerNamesConcatenated), NamedTextColor.WHITE));
                player.sendMessage(Component.empty());
            });

            List<BedWarsPlayer> killers = game.getPlayers().stream()
                    .sorted(Comparator.comparingInt(BedWarsPlayer::getKillsThisGame).reversed()
                            .thenComparing(BedWarsPlayer::getUsername, String.CASE_INSENSITIVE_ORDER))
                    .limit(3)
                    .toList();
            String[] places = {"§e§l1st Killer", "§6§l2nd Killer", "§c§l3rd Killer"};
            for (int index = 0; index < killers.size(); index++) {
                BedWarsPlayer killer = killers.get(index);
                String name = LegacyComponentSerializer.legacySection().serialize(killer.getColouredName());
                player.sendMessage(Component.text(ChatUtility.FontInfo.center(
                        places[index] + " §7- " + name + " §7- " + killer.getKillsThisGame())));
            }
            player.sendMessage(Component.empty());
            player.sendMessage(Component.text(THICK_BAR));
        }

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            for (BedWarsPlayer player : game.getPlayers()) {
                player.sendMessage(Component.text(THICK_BAR));
                player.sendMessage(Component.text(ChatUtility.FontInfo.center("§lReward Summary"), NamedTextColor.WHITE));
                player.sendMessage(Component.empty());
                player.sendMessage(Component.text("   §7You earned:"));
                player.sendMessage(Component.text("    §f• §2" + player.getTokensThisGame() + " Bed Wars Tokens"));
                player.sendMessage(Component.text("    §f• §3" + player.getHypixelXpThisGame() + " Hypixel Experience"));
                player.sendMessage(Component.text("    §f• §20 Guild Experience"));
                player.sendMessage(Component.empty());
                player.sendMessage(Component.text(ChatUtility.FontInfo.center("Bed Wars XP"), NamedTextColor.AQUA));

                long currentLevel = player.getCurrentBedWarsLevel();
                player.sendMessage(Component.text(
                    "§f          §bLevel " + currentLevel + "                                     §bLevel " + (currentLevel + 1)
                ));

                long experience = player.getCurrentBedWarsExperience();
                int progress = BedwarsLevelUtil.calculateExperienceSinceLastLevel(experience);
                int maxExperience = BedwarsLevelUtil.calculateMaxExperienceFromExperience(experience);

                double percentage = Math.min(1.0, (double) progress / maxExperience);
                int filledSquares = (int) Math.round(percentage * 34);
                StringBuilder progressBar = new StringBuilder("§8[");
                for (int i = 0; i < 34; i++) {
                    if (i < filledSquares) {
                        progressBar.append("§b■");
                    } else {
                        progressBar.append("§7■");
                    }
                }
                progressBar.append("§8]");
                player.sendMessage(Component.text("§f          " + progressBar));

                String prettyExperience = String.format("%,d", experience);
                String prettyMaxExperience = String.format("%,d", maxExperience);

                String percentageString = String.format("%.1f", percentage * 100);

                player.sendMessage(ChatUtility.FontInfo.center(
                    "§b" + prettyExperience + " §7/ §a" + prettyMaxExperience + " §7(" + percentageString + "%)"
                ));

                player.sendMessage(Component.empty());
                player.sendMessage("§7You earned §b" + player.getXpThisGame() + " §bBed Wars XP");
                player.sendMessage(Component.empty());
                // xp multipliers shown here
                player.sendMessage(Component.text(THICK_BAR));
                if (isRecording) {
                    player.sendMessage(Component.text("§aThis game has been recorded. §6Click here to watch the Replay!").clickEvent(
                        ClickEvent.runCommand("/replay " + game.getReplayManager().getRecorder().getReplayId())
                    ));
                }
                player.sendMessage(Component.empty());
            }
        }).delay(TaskSchedule.seconds(2)).schedule();

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            game.getPlayers().forEach(p -> p.sendTo(ServerType.BEDWARS_LOBBY));
            game.dispose();
        }).delay(TaskSchedule.seconds(7)).schedule();
    }

}
