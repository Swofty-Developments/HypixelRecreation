package net.swofty.type.skyblockgeneric.entity.mob.mobs.island;

import net.swofty.type.skyblockgeneric.entity.mob.impl.PrivateIslandBestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.impl.PrivateIslandMobDefinitions;

public class MobZombie_02 extends PrivateIslandBestiaryMob {
    private static final double[] HEALTH = {100, 115, 132, 152, 174, 201, 231, 266, 305, 351, 404, 465, 535, 615, 707};
    private static final double[] DAMAGE = {20, 23, 26, 29, 33, 37, 42, 47, 53, 60, 68, 77, 87, 98, 111};

    public MobZombie_02() {
        super(PrivateIslandMobDefinitions.zombie(2, "ZOMBIE_02"), HEALTH, DAMAGE,
                new int[]{1, 1, 1, 1, 1, 1, 2, 2, 3, 3, 4, 5, 6, 7, 8},
                new int[]{1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 4, 4, 5, 6});
    }
}
