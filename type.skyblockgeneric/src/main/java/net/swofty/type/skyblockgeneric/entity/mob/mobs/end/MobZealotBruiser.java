package net.swofty.type.skyblockgeneric.entity.mob.mobs.end;

import net.minestom.server.entity.EntityType;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.region.RegionType;

import java.util.List;

public class MobZealotBruiser extends EndMob {
    public MobZealotBruiser() {
        super(EntityType.ENDERMAN);
    }

    @Override
    protected String endDisplayName() {
        return "Zealot Bruiser";
    }

    @Override
    protected int endLevel() {
        return 100;
    }

    @Override
    protected double endHealth() {
        return 65_000;
    }

    @Override
    protected double endDamage() {
        return 2_500;
    }

    @Override
    protected String endMobId() {
        return "ZEALOT_BRUISER";
    }

    @Override
    protected RegionType endRegion() {
        return RegionType.THE_END_NEST;
    }

    @Override
    protected int endBestiaryTier() {
        return 10;
    }

    @Override
    protected int endBestiaryBracket() {
        return 2;
    }

    @Override
    protected List<SkyBlockLootTable.LootRecord> endLoot() {
        return List.of(
                drop(ItemType.ENDER_PEARL, 2, 4, 100),
                drop(ItemType.ENCHANTED_ENDER_PEARL, 1, 1, 4),
                drop(ItemType.PEARLESCENT_DYE, 1, 1, 0.00001)
        );
    }

    @Override
    protected long endSkillXp() {
        return 78;
    }

    @Override
    protected int endCoins() {
        return 24;
    }

    @Override
    protected int endXpOrbs() {
        return 20;
    }
}
