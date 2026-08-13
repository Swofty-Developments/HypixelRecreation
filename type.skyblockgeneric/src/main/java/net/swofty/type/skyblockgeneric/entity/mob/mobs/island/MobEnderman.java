package net.swofty.type.skyblockgeneric.entity.mob.mobs.island;

import net.swofty.type.skyblockgeneric.entity.mob.impl.PrivateIslandBestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.impl.PrivateIslandMobDefinitions;

public class MobEnderman extends PrivateIslandBestiaryMob {
    private static final double[] HEALTH = {160, 184, 211, 243, 279, 321, 370, 425, 489, 562, 647, 744, 856, 984, 1132};
    private static final double[] DAMAGE = {40, 45, 51, 58, 65, 74, 83, 94, 106, 120, 136, 153, 173, 196, 221};

    public MobEnderman() {
        super(PrivateIslandMobDefinitions.ENDERMAN, HEALTH, DAMAGE,
                new int[]{2, 2, 2, 3, 3, 4, 4, 5, 6, 7, 8, 9, 10, 11, 12},
                new int[]{4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 10, 11, 12, 14});
    }
}
