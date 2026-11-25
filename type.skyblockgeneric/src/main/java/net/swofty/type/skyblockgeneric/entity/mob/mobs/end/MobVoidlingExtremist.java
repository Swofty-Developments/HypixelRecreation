package net.swofty.type.skyblockgeneric.entity.mob.mobs.end;

import net.minestom.server.entity.EntityType;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.region.RegionType;

import java.util.List;

public class MobVoidlingExtremist extends EndMob {
    public MobVoidlingExtremist() {
        super(EntityType.ENDERMAN);
    }

    @Override
    protected String endDisplayName() {
        return "Voidling Extremist";
    }

    @Override
    protected int endLevel() {
        return 100;
    }

    @Override
    protected double endHealth() {
        return 8_000_000;
    }

    @Override
    protected double endDamage() {
        return 13_500;
    }

    @Override
    protected String endMobId() {
        return "VOIDLING_EXTREMIST";
    }

    @Override
    protected RegionType endRegion() {
        return RegionType.VOID_SEPULTURE;
    }

    @Override
    protected int endBestiaryTier() {
        return 15;
    }

    @Override
    protected int endBestiaryBracket() {
        return 3;
    }

    @Override
    protected List<SkyBlockLootTable.LootRecord> endLoot() {
        return List.of(
                drop(ItemType.ENDER_PEARL, 32, 32, 100),
                drop(ItemType.ENCHANTED_ENDER_PEARL, 1, 1, 16),
                drop(ItemType.ENDERMAN_CORTEX_REWRITER, 1, 1, 0.1),
                drop(ItemType.SUMMONING_EYE, 1, 1, 0.04),
                drop(ItemType.PEARLESCENT_DYE, 1, 1, 0.00002)
        );
    }

    @Override
    protected long endSkillXp() {
        return 750;
    }

    @Override
    protected int endCoins() {
        return 100;
    }

    @Override
    protected int endXpOrbs() {
        return 35;
    }
}
