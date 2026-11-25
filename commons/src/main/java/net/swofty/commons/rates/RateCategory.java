package net.swofty.commons.rates;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum RateCategory {
    ALL_DROPS("drops.all", Kind.DROP),
    MOB("drops.mob", Kind.DROP),
    MINION("drops.minion", Kind.DROP),
    PET("drops.pet", Kind.DROP),
    SLAYER("drops.slayer", Kind.DROP),
    FISHING("drops.fishing", Kind.DROP),
    SEA_CREATURE("drops.sea_creature", Kind.DROP),
    FISHING_TREASURE("drops.fishing_treasure", Kind.DROP),
    BLOCK("drops.block", Kind.DROP),
    FORAGING("drops.foraging", Kind.DROP),
    HUNTING("drops.hunting", Kind.DROP),
    DUNGEON("drops.dungeon", Kind.DROP),
    NUCLEUS("drops.nucleus", Kind.DROP),
    KUUDRA("drops.kuudra", Kind.DROP),

    ALL_XP("xp.all", Kind.XP),
    SKILL("xp.skill", Kind.XP),
    PET_XP("xp.pet", Kind.XP),
    SLAYER_XP("xp.slayer", Kind.XP),
    HOTM("xp.hotm", Kind.XP),
    POWDER("xp.powder", Kind.XP),
    VANILLA("xp.vanilla", Kind.XP),
    DUNGEON_XP("xp.dungeon", Kind.XP);

    private final String key;
    private final Kind kind;

    RateCategory(String key, Kind kind) {
        this.key = key;
        this.kind = kind;
    }

    public boolean isGlobal() {
        return this == ALL_DROPS || this == ALL_XP;
    }

    public RateCategory global() {
        return kind == Kind.DROP ? ALL_DROPS : ALL_XP;
    }

    public static Optional<RateCategory> fromKey(String key) {
        return Arrays.stream(values()).filter(category -> category.key.equalsIgnoreCase(key)).findFirst();
    }

    public enum Kind {
        DROP,
        XP
    }
}
