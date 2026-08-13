package net.swofty.type.skyblockgeneric.entity.mob.mobs.deepcaverns;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.loottable.OtherLoot;

import java.util.List;

public class MobEmeraldSlime_15 extends MobEmeraldSlime_05 {
    @Override
    public Integer getLevel() {
        return 15;
    }

    @Override
    public ItemStatistics getBaseStatistics() {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, 250D)
                .withBase(ItemStatistic.DAMAGE, 150D)
                .withBase(ItemStatistic.SPEED, 100D)
                .build();
    }

    @Override
    public OtherLoot getOtherLoot() {
        return new OtherLoot(20, 12, 50);
    }

    @Override
    public String getMobID() {
        return "EMERALD_SLIME_15";
    }

    @Override
    public List<MobType> getMobTypes() {
        return List.of(MobType.CUBIC);
    }
}
