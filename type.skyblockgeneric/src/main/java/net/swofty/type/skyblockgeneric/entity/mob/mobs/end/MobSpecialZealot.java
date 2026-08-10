package net.swofty.type.skyblockgeneric.entity.mob.mobs.end;

import net.minestom.server.entity.EntityType;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.region.RegionType;

import java.util.List;

public class MobSpecialZealot extends EndMob {
    public MobSpecialZealot() {
        super(EntityType.ENDERMAN);
    }

    @Override
    protected String endDisplayName() {
        return "Special Zealot";
    }

    @Override
    protected int endLevel() {
        return 55;
    }

    @Override
    protected double endHealth() {
        return 2_000;
    }

    @Override
    protected double endDamage() {
        return 1_250;
    }

    @Override
    protected String endMobId() {
        return "SPECIAL_ZEALOT";
    }

    @Override
    protected RegionType endRegion() {
        return RegionType.DRAGONS_NEST;
    }

    @Override
    protected int endBestiaryTier() {
        return 25;
    }

    @Override
    protected int endBestiaryBracket() {
        return 4;
    }

    @Override
    protected List<SkyBlockLootTable.LootRecord> endLoot() {
        return List.of(
                drop(ItemType.ENDER_PEARL, 3, 4, 100),
                drop(ItemType.ENCHANTED_ENDER_PEARL, 1, 1, 5),
                drop(ItemType.PEARLESCENT_DYE, 1, 1, 0.001)
        );
    }

    @Override
    protected long endSkillXp() {
        return 40;
    }

    @Override
    protected int endCoins() {
        return 15;
    }

    @Override
    protected int endXpOrbs() {
        return 10;
    }

    @Override
    protected int endMinimumSpawnCount() {
        return 0;
    }
}
