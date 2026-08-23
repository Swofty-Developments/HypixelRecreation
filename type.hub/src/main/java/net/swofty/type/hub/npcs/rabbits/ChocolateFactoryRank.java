package net.swofty.type.hub.npcs.rabbits;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.swofty.commons.text.Text;

@Getter
@AllArgsConstructor
public enum ChocolateFactoryRank {
    UNEMPLOYED(0, "Unemployed", NamedTextColor.RED),
    INTERN(1, "Intern", NamedTextColor.GRAY),
    EMPLOYEE(20, "Employee", NamedTextColor.GREEN),
    ASSISTANT(120, "Assistant", NamedTextColor.BLUE),
    MANAGER(140, "Manager", NamedTextColor.DARK_PURPLE),
    DIRECTOR(180, "Director", NamedTextColor.GOLD),
    EXECUTIVE(200, "Executive", NamedTextColor.LIGHT_PURPLE),
    BOARD_MEMBER(220, "Board Member", NamedTextColor.AQUA);

    private final int level;
    private final String name;
    private final TextColor color;

    public Text getHologramLine() {
        return getHologramLine(level);
    }

    public Text getHologramLine(int actualLevel) {
        if (this == UNEMPLOYED) {
            return Text.of("<color:{}>{}", color, name);
        }
        return Text.of("<7>[{}] <color:{}>{}", actualLevel, color, name);
    }

    public static ChocolateFactoryRank fromLevel(int level) {
        ChocolateFactoryRank result = UNEMPLOYED;
        for (ChocolateFactoryRank rank : values()) {
            if (level >= rank.level) {
                result = rank;
            }
        }
        return result;
    }
}
