package net.swofty.type.skyblockgeneric.entity.mob.mobs.end;

import net.minestom.server.entity.EntityType;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.region.RegionType;

import java.util.List;

public class MobNestEndermite extends EndMob {
    public MobNestEndermite() {
        super(EntityType.ENDERMITE);
    }

    @Override
    protected String endDisplayName() {
        return "Nest Endermite";
    }

    @Override
    protected int endLevel() {
        return 50;
    }

    @Override
    protected double endHealth() {
        return 4_500;
    }

    @Override
    protected double endDamage() {
        return 750;
    }

    @Override
    protected String endMobId() {
        return "NEST_ENDERMITE";
    }

    @Override
    protected RegionType endRegion() {
        return RegionType.THE_END;
    }

    @Override
    protected int endBestiaryTier() {
        return 0;
    }

    @Override
    protected int endBestiaryBracket() {
        return 0;
    }

    @Override
    protected List<SkyBlockLootTable.LootRecord> endLoot() {
        return List.of(
                drop(ItemType.ENCHANTED_END_STONE, 1, 1, 100),
                drop(ItemType.MITE_GEL, 1, 1, 100),
                drop(ItemType.PEARLESCENT_DYE, 1, 1, 0.00002)
        );
    }

    @Override
    protected long endSkillXp() {
        return 38;
    }

    @Override
    protected int endCoins() {
        return 22;
    }

    @Override
    protected int endXpOrbs() {
        return 24;
    }

    @Override
    protected boolean endNaturallyHostile() {
        return true;
    }

    @Override
    public List<MobType> getMobTypes() {
        return List.of(MobType.ARTHROPOD, MobType.ENDER);
    }

    @Override
    protected int endMinimumSpawnCount() {
        return 2;
    }
}
