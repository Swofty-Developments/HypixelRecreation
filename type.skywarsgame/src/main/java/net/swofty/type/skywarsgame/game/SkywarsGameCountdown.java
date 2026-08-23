package net.swofty.type.skywarsgame.game;

import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.NamedTextColor;
import lombok.Getter;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.utility.Titles;

import java.time.Duration;

public class SkywarsGameCountdown {
    private final SkywarsGame game;
    @Getter
    private boolean active = false;
    private Task countdownTask;
    @Getter
    private int secondsRemaining;

    public SkywarsGameCountdown(SkywarsGame game) {
        this.game = game;
    }

    public void startCountdown() {
        if (active) return;
        active = true;
        secondsRemaining = 30;
        game.setGameStatus(SkywarsGameStatus.STARTING);

        countdownTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (secondsRemaining <= 0) {
                active = false;
                countdownTask.cancel();
                game.startGame();
                return;
            }

            if (secondsRemaining <= 5 || secondsRemaining == 10 || secondsRemaining == 15 || secondsRemaining == 20 || secondsRemaining == 30) {
                broadcastCountdownMessage(secondsRemaining);

                if (secondsRemaining <= 5) {
                    showCountdownTitle(secondsRemaining);
                }
            }

            secondsRemaining--;
        }).repeat(TaskSchedule.seconds(1)).schedule();
    }

    private void broadcastCountdownMessage(int seconds) {
        String word = seconds == 1 ? "second" : "seconds";

        TextColor numberColor;
        if (seconds <= 5) {
            numberColor = NamedTextColor.RED;
        } else if (seconds == 10) {
            numberColor = NamedTextColor.GOLD;
        } else {
            numberColor = NamedTextColor.GREEN;
        }

        game.broadcastMessage(Text.of("<e>The game starts in <color:{}>{}<e> {}!", numberColor, seconds, word));
    }

    private void showCountdownTitle(int seconds) {
        Title title = Titles.title(
                Text.of("<c>{}", seconds),
                Text.empty(),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(900), Duration.ofMillis(100))
        );

        game.getPlayers().forEach(p -> p.showTitle(title));
    }

    public void forceStart(int seconds) {
        if (active) {
            countdownTask.cancel();
        }
        active = true;
        secondsRemaining = seconds;
        game.setGameStatus(SkywarsGameStatus.STARTING);

        countdownTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (secondsRemaining <= 0) {
                active = false;
                countdownTask.cancel();
                game.startGame();
                return;
            }

            broadcastCountdownMessage(secondsRemaining);
            showCountdownTitle(secondsRemaining);
            secondsRemaining--;
        }).repeat(TaskSchedule.seconds(1)).schedule();
    }

    public void checkCountdownConditions() {
        if (!active) return;

        if (!game.hasMinimumPlayers()) {
            cancelCountdown();
        }
    }

    private void cancelCountdown() {
        if (!active) return;
        active = false;
        if (countdownTask != null) {
            countdownTask.cancel();
        }
        game.setGameStatus(SkywarsGameStatus.WAITING);
        game.broadcastMessage(Text.of("<c>Not enough players! Countdown cancelled."));
    }
}
