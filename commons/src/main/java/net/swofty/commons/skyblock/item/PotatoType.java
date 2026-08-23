package net.swofty.commons.skyblock.item;

import lombok.Getter;
import net.swofty.commons.skyblock.statistics.ItemStatistic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public enum PotatoType {
    ARMOR(List.of(
            "<7>When applied to armor, grants <a>+2<glyph:stat_defense>",
            "<a>Defense <7>and <c>+4<glyph:stat_health> Health<7>."
    ), Map.of(ItemStatistic.DEFENSE, 2.0, ItemStatistic.HEALTH, 4.0)),
    WEAPONS(List.of(
            "<7>When applied to weapons, grants",
            "<c>+2<glyph:stat_strength> Strength <7>and <c>+2<glyph:stat_ability_damage> Damage<7>."
    ), Map.of(ItemStatistic.STRENGTH, 2.0, ItemStatistic.DAMAGE, 2.0)),
    ;

    public final List<String> display;
    public final Map<ItemStatistic, Double> stats;

    PotatoType(List<String> display, Map<ItemStatistic, Double> stats) {
        this.display = display;
        this.stats = stats;
    }

    public static List<String> allLores() {
        List<String> lores = new ArrayList<>(ARMOR.display);
        lores.add("");
        lores.addAll(WEAPONS.display);
        return lores;
    }
}
