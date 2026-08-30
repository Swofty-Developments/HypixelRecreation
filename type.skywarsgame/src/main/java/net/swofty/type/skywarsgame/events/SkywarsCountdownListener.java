package net.swofty.type.skywarsgame.events;

import net.kyori.adventure.title.Title;
import net.swofty.commons.text.Text;
import net.swofty.type.game.game.event.CountdownCancelledEvent;
import net.swofty.type.game.game.event.CountdownTickEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.utility.Titles;
import net.swofty.type.skywarsgame.game.SkywarsGame;

import java.time.Duration;

public final class SkywarsCountdownListener implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false)
    public void onCountdownTick(CountdownTickEvent event) {
        if (!(event.game() instanceof SkywarsGame game)
                || (!event.shouldAnnounce() && !game.isForceCountdownAnnouncements())) {
            return;
        }

        int seconds = event.remainingSeconds();
        if (seconds <= 0) return;

        Text message = createCountdownMessage(seconds);

        game.getPlayers().forEach(player -> {
            player.sendMessage(message);
            if (seconds <= 5) {
                player.showTitle(Titles.title(
                        Text.of("<c>{}", seconds),
                        Text.empty(),
                        Title.Times.times(Duration.ZERO, Duration.ofMillis(800), Duration.ofMillis(200))
                ));
            }
        });
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false)
    public void onCountdownCancelled(CountdownCancelledEvent event) {
        if (event.game() instanceof SkywarsGame game) {
            game.broadcastMessage(Text.of(event.reason()));
        }
    }

    private Text createCountdownMessage(int seconds) {
        if (seconds == 10) return Text.of("<e>The game starts in <6>10</6> seconds!");
        if (seconds > 10) return Text.of("<e>The game starts in {} seconds!", seconds);
        return Text.of("<e>The game starts in <c>{}</c> seconds!", seconds);
    }
}
