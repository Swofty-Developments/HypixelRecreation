package net.swofty.type.skyblockgeneric.user;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.text.Text;
import net.swofty.commons.text.TextBody;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SkyBlockActionBar {
    private static final Text SEPARATOR = Text.literal("     ");
    private static final Map<UUID, SkyBlockActionBar> playerBars = new ConcurrentHashMap<>();

    private final EnumMap<BarSection, PriorityQueue<DisplayReplacement>> replacements = new EnumMap<>(BarSection.class);
    private final Map<BarSection, Text> defaultDisplays = new EnumMap<>(BarSection.class);

    public static SkyBlockActionBar getFor(SkyBlockPlayer player) {
        return playerBars.computeIfAbsent(player.getUuid(), k -> new SkyBlockActionBar());
    }

    private SkyBlockActionBar() {
        for (BarSection section : BarSection.VALUES) {
            replacements.put(section, new PriorityQueue<>(Comparator.comparingInt(DisplayReplacement::priority).reversed()));
        }
    }

    public void setDefaultDisplay(BarSection section, Text display) {
        defaultDisplays.put(section, display);
    }

    public @Nullable DisplayReplacement getReplacement(BarSection section) {
        return replacements.get(section).peek();
    }

    public void addReplacement(BarSection section, Text display, int duration, int priority) {
        addReplacement(section, new DisplayReplacement(display, duration, priority));
    }

    public void addReplacement(BarSection section, DisplayReplacement replacement) {
        PriorityQueue<DisplayReplacement> sectionReplacements = replacements.get(section);
        sectionReplacements.offer(replacement);

        if (replacement.duration > 0) {
            scheduleRemoval(section, replacement);
        }
    }

    private void scheduleRemoval(BarSection section, DisplayReplacement replacement) {
        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            replacements.get(section).remove(replacement);
        }, TaskSchedule.tick(replacement.duration()), TaskSchedule.stop());
    }

    public Text render() {
        TextBody body = new TextBody();
        for (BarSection section : BarSection.VALUES) {
            Text display = getDisplayForSection(section);
            body.section(section.id(), section.ordinal())
                    .when(() -> !display.isEmpty())
                    .line(display);
        }
        return body.renderJoined(SEPARATOR);
    }

    private Text getDisplayForSection(BarSection section) {
        PriorityQueue<DisplayReplacement> sectionReplacements = replacements.get(section);
        if (!sectionReplacements.isEmpty()) {
            return sectionReplacements.peek().display;
        }
        return defaultDisplays.getOrDefault(section, Text.empty());
    }

    public enum BarSection {
        HEALTH("health"),
        DEFENSE("defense"),
        MANA("mana"),
        ;

        private static final BarSection[] VALUES = values();

        private final String id;

        BarSection(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }

    public record DisplayReplacement(Text display, int duration, int priority) { }
}
