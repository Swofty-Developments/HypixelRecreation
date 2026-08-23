package net.swofty.type.murdermysterygame.game;

import lombok.Getter;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.utility.Titles;

import java.time.Duration;

public class GameCountdown {
    private final Game game;
    @Getter
    private boolean active = false;
    private Task countdownTask;
    @Getter
    private int secondsRemaining = 30;

    public GameCountdown(Game game) {
        this.game = game;
    }

    public void startCountdown() {
        if (active) return;

        active = true;
        secondsRemaining = 30;

        countdownTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (!active) return;

            if (secondsRemaining <= 0) {
                active = false;
                game.startGame();
                return;
            }

            // Announce at specific intervals
            if (secondsRemaining == 30 || secondsRemaining == 20 || secondsRemaining == 15 ||
                    secondsRemaining == 10 || secondsRemaining <= 5) {
                announceCountdown(secondsRemaining);
            }

            secondsRemaining--;
        }).repeat(TaskSchedule.seconds(1)).schedule();
    }

    public void stopCountdown() {
        if (!active) return;

        active = false;
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }

        game.getPlayers().forEach(player -> player.sendMessage("<c>Countdown cancelled - not enough players!"));
    }

    public void checkCountdownConditions() {
        if (active && game.getPlayers().size() < game.getGameType().getMinPlayers()) {
            stopCountdown();
        }
    }

    public void forceStart(int seconds) {
        if (countdownTask != null) {
            countdownTask.cancel();
        }
        active = true;
        secondsRemaining = seconds;

        game.getPlayers().forEach(player -> player.sendMessage("<a>Game force started! Starting in {} seconds!", seconds));

        countdownTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (!active) return;

            if (secondsRemaining <= 0) {
                active = false;
                game.startGame();
                return;
            }

            announceCountdown(secondsRemaining);
            secondsRemaining--;
        }).repeat(TaskSchedule.seconds(1)).schedule();
    }

    private void announceCountdown(int seconds) {
        String word = seconds == 1 ? "second" : "seconds";

        Text message = seconds <= 5
                ? Text.of("<e>The game is starting in <c>{}<e> {}!", seconds, word)
                : Text.of("<e>The game is starting in <b>{}<e> {}!", seconds, word);
        game.getPlayers().forEach(player -> player.sendMessage(message));

        if (seconds <= 5) {
            Title title = Titles.title(
                    Text.of("<c>{}", seconds),
                    Text.empty(),
                    Title.Times.times(Duration.ZERO, Duration.ofMillis(800), Duration.ofMillis(200))
            );
            game.getPlayers().forEach(p -> p.showTitle(title));
        }
    }
}
