package net.swofty.type.skyblockgeneric.entity.mob.mobs.end;

import net.minestom.server.entity.EntityType;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.region.RegionType;

import java.util.List;

public class MobEnderman_42 extends EndMob {
    public MobEnderman_42() {
        super(EntityType.ENDERMAN);
    }

    @Override
    protected String endDisplayName() {
        return "Enderman";
    }

    @Override
    protected int endLevel() {
        return 42;
    }

    @Override
    protected double endHealth() {
        return 4_500;
    }

    @Override
    protected double endDamage() {
        return 500;
    }

    @Override
    protected String endMobId() {
        return "ENDERMAN_42";
    }

    @Override
    protected RegionType endRegion() {
        return RegionType.THE_END;
    }

    @Override
    protected int endBestiaryTier() {
        return 10;
    }

    @Override
    protected int endBestiaryBracket() {
        return 1;
    }

    @Override
    protected List<SkyBlockLootTable.LootRecord> endLoot() {
        return endermanLoot(1, 3);
    }

    @Override
    protected long endSkillXp() {
        return 40;
    }

    @Override
    protected int endCoins() {
        return 10;
    }

    @Override
    protected int endXpOrbs() {
        return 12;
    }
}
