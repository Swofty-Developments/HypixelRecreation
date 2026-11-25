package net.swofty.type.skywarsgame.game;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.swofty.type.game.game.team.SimpleGameTeam;

@Getter
public final class SkywarsTeam extends SimpleGameTeam {
    private final int teamId;

    public SkywarsTeam(int teamId) {
        super(String.valueOf(teamId), "Team " + (teamId + 1), NamedTextColor.WHITE);
        this.teamId = teamId;
    }
}
