package net.swofty.type.skyblockgeneric.entity.mob.mobs.end;

import net.minestom.server.entity.EntityType;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.region.RegionType;

import java.util.List;

public class MobSeer extends EndMob {
    public MobSeer() {
        super(EntityType.SKELETON);
    }

    @Override
    protected String endDisplayName() {
        return "Seer";
    }

    @Override
    protected int endLevel() {
        return 55;
    }

    @Override
    protected double endHealth() {
        return 9_500;
    }

    @Override
    protected double endDamage() {
        return 500;
    }

    @Override
    protected String endMobId() {
        return "SEER";
    }

    @Override
    protected RegionType endRegion() {
        return RegionType.DRAGONS_NEST;
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
                drop(ItemType.ENDER_PEARL, 1, 2, 100),
                drop(ItemType.FLINT_ARROW, 1, 1, 100),
                drop(ItemType.FLINT_ARROW, 1, 1, 50),
                drop(ItemType.FLINT_ARROW, 1, 1, 50),
                drop(ItemType.FLINT_ARROW, 1, 1, 50),
                drop(ItemType.ENDER_CLOAK, 1, 1, 1),
                drop(ItemType.END_STONE_BOW, 1, 1, 0.1),
                drop(ItemType.PEARLESCENT_DYE, 1, 1, 0.00001)
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
    protected boolean endNaturallyHostile() {
        return true;
    }
}
