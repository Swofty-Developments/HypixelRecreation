package net.swofty.type.murdermysterygame.role;

import lombok.Getter;
import net.swofty.commons.text.Text;

@Getter
public enum GameRole {
    MURDERER("Murderer", Text.of("<c>You are the Murderer"), Text.of("<7>Kill all innocents without being caught")),
    DETECTIVE("Detective", Text.of("<9>You are the Detective"), Text.of("<7>Find and eliminate the murderer")),
    INNOCENT("Innocent", Text.of("<a>You are the Innocent"), Text.of("<7>Survive and help identify the murderer")),
    ASSASSIN("Assassin", Text.of("<6>You are the Assassin"), Text.of("<7>Eliminate your assigned target"));

    private final String displayName;
    private final Text announcement;
    private final Text description;

    GameRole(String displayName, Text announcement, Text description) {
        this.displayName = displayName;
        this.announcement = announcement;
        this.description = description;
    }
}
