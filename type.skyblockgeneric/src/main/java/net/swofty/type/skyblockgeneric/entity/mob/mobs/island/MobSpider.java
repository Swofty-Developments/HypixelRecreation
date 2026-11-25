package net.swofty.type.skyblockgeneric.entity.mob.mobs.island;

import net.swofty.type.skyblockgeneric.entity.mob.impl.PrivateIslandBestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.impl.PrivateIslandMobDefinitions;

public class MobSpider extends PrivateIslandBestiaryMob {
    private static final double[] HEALTH = {120, 138, 158, 182, 209, 241, 277, 319, 367, 422, 485, 558, 642, 738, 849};
    private static final double[] DAMAGE = {35, 40, 45, 51, 57, 64, 73, 82, 93, 105, 119, 134, 152, 171, 194};

    public MobSpider() {
        super(PrivateIslandMobDefinitions.SPIDER, HEALTH, DAMAGE,
                new int[]{1, 1, 1, 1, 1, 1, 2, 2, 3, 3, 4, 5, 6, 7, 8},
                new int[]{1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 4, 4, 5, 6});
    }
}
