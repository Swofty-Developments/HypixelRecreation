package net.swofty.type.theend.dragon;

import net.swofty.commons.skyblock.item.ItemType;

public enum EndDragonVariant {
    PROTECTOR(9_000_000, 0.72, 16, ItemType.PROTECTOR_DRAGON_FRAGMENT),
    OLD(9_000_000, 0.72, 16, ItemType.OLD_DRAGON_FRAGMENT),
    WISE(9_000_000, 0.82, 16, ItemType.WISE_DRAGON_FRAGMENT),
    UNSTABLE(9_000_000, 0.78, 16, ItemType.UNSTABLE_DRAGON_FRAGMENT),
    YOUNG(7_500_000, 1.05, 16, ItemType.YOUNG_DRAGON_FRAGMENT),
    STRONG(9_000_000, 0.86, 16, ItemType.STRONG_DRAGON_FRAGMENT),
    SUPERIOR(15_000_000, 0.9, 4, ItemType.SUPERIOR_DRAGON_FRAGMENT);

    private final double health;
    private final double movementSpeed;
    private final int weight;
    private final ItemType fragment;

    EndDragonVariant(double health, double movementSpeed, int weight, ItemType fragment) {
        this.health = health;
        this.movementSpeed = movementSpeed;
        this.weight = weight;
        this.fragment = fragment;
    }

    public double health() {
        return health;
    }

    public double movementSpeed() {
        return movementSpeed;
    }

    public int weight() {
        return weight;
    }

    public ItemType fragment() {
        return fragment;
    }
}
