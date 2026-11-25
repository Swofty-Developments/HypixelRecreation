package net.swofty.type.murdermysterygame.events;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.swofty.type.game.game.event.CountdownCancelledEvent;
import net.swofty.type.game.game.event.CountdownTickEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.murdermysterygame.game.Game;
import net.swofty.type.murdermysterygame.user.MurderMysteryPlayer;

import java.time.Duration;

public class ActionGameCountdown implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false)
    public void onTick(CountdownTickEvent event) {
        if (!(event.game() instanceof Game game)
                || (!event.shouldAnnounce() && !game.isForceCountdownAnnouncements())) return;

        int seconds = event.remainingSeconds();
        if (seconds <= 0) return;

        Component message = Component.empty()
                .append(Component.text("The game is starting in ", NamedTextColor.YELLOW))
                .append(Component.text(seconds, seconds <= 5 ? NamedTextColor.RED : NamedTextColor.AQUA))
                .append(Component.text(seconds == 1 ? " second!" : " seconds!", NamedTextColor.YELLOW));

        for (MurderMysteryPlayer player : game.getPlayers()) {
            player.sendMessage(message);
            player.playSound(Sound.sound(Key.key("minecraft:block.note_block.pling"), Sound.Source.MASTER, 1f, 1f));
            if (seconds <= 5) {
                player.showTitle(Title.title(
                        Component.text(String.valueOf(seconds), NamedTextColor.RED),
                        Component.empty(),
                        Title.Times.times(Duration.ZERO, Duration.ofMillis(800), Duration.ofMillis(200))));
            }
        }
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false)
    public void onCancelled(CountdownCancelledEvent event) {
        if (event.game() instanceof Game game) {
            game.broadcastMessage(Component.text(event.reason()));
        }
    }
}
