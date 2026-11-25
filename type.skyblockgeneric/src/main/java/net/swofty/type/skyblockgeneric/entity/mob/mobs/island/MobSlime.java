package net.swofty.type.skyblockgeneric.entity.mob.mobs.island;

import net.swofty.type.skyblockgeneric.entity.mob.impl.PrivateIslandBestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.impl.PrivateIslandMobDefinitions;

public class MobSlime extends PrivateIslandBestiaryMob {
    private static final double[] HEALTH = {80, 92, 105, 121, 139, 160, 185, 212, 244, 281, 323, 372, 428, 492, 566};
    private static final double[] DAMAGE = {15, 17, 19, 22, 24, 28, 31, 35, 40, 45, 51, 58, 65, 73, 83};

    public MobSlime() {
        super(PrivateIslandMobDefinitions.SLIME, HEALTH, DAMAGE,
                new int[]{1, 1, 1, 1, 1, 1, 2, 2, 2, 3, 3, 4, 5, 6, 7},
                new int[]{1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 4, 4, 5, 6, 7});
    }
}
