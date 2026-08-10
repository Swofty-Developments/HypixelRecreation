package net.swofty.type.skyblockgeneric.entity.mob.mobs.end;

import net.minestom.server.entity.EntityType;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.region.RegionType;

import java.util.List;

public class MobVoidlingFanatic extends EndMob {
    public MobVoidlingFanatic() {
        super(EntityType.ENDERMAN);
    }

    @Override
    protected String endDisplayName() {
        return "Voidling Fanatic";
    }

    @Override
    protected int endLevel() {
        return 85;
    }

    @Override
    protected double endHealth() {
        return 750_000;
    }

    @Override
    protected double endDamage() {
        return 3_500;
    }

    @Override
    protected String endMobId() {
        return "VOIDLING_FANATIC";
    }

    @Override
    protected RegionType endRegion() {
        return RegionType.VOID_SEPULTURE;
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
                drop(ItemType.ENDER_PEARL, 4, 5, 100),
                drop(ItemType.ENCHANTED_ENDER_PEARL, 1, 1, 1),
                drop(ItemType.PEARLESCENT_DYE, 1, 1, 0.00002)
        );
    }

    @Override
    protected long endSkillXp() {
        return 110;
    }

    @Override
    protected int endCoins() {
        return 20;
    }

    @Override
    protected int endXpOrbs() {
        return 10;
    }
}
