package net.swofty.type.replayviewer.playback.display;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.swofty.type.generic.i18n.I18n;

import java.util.ArrayList;
import java.util.List;

public class CountdownTextSource implements DynamicTextSource {

    private final String identifier;
    private final int startTick;
    private final int endTick;
    private final String eventName;
    private final List<String> completedText;

    public CountdownTextSource(DynamicTextConfig config) {
        this.identifier = config.identifier();
        this.startTick = config.getMeta("startTick", 0);
        this.endTick = config.getMeta("endTick", 0);
        this.eventName = config.getMeta("eventName", "");
        this.completedText = config.getMeta("completedText", List.of());
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getDisplayType() {
        return "countdown";
    }

    @Override
    public List<String> getTextAt(int currentTick) {
        if (currentTick >= endTick) {
            return completedText.isEmpty()
                    ? List.of(ReplayDisplayTranslations.toLegacy(I18n.t("replays.countdown_completed")))
                    : completedText;
        }

        int remainingTicks = endTick - currentTick;
        int remainingSeconds = remainingTicks / 20;

        List<String> result = new ArrayList<>();
        Component event = eventName.isEmpty()
                ? I18n.t("replays.countdown_default_event")
                : LegacyComponentSerializer.legacySection().deserialize(eventName);
        result.add(ReplayDisplayTranslations.toLegacy(I18n.t(
                "replays.countdown_event", Argument.component("event", event))));
        result.add(ReplayDisplayTranslations.toLegacy(I18n.t(
                "replays.countdown_in", Argument.string("time", formatTime(remainingSeconds)))));

        return result;
    }

    @Override
    public boolean hasChangedSince(int lastTick, int currentTick) {
        // Changes every second
        int lastSecond = lastTick / 20;
        int currentSecond = currentTick / 20;
        return lastSecond != currentSecond;
    }

    @Override
    public boolean isActiveAt(int currentTick) {
        return currentTick >= startTick;
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
