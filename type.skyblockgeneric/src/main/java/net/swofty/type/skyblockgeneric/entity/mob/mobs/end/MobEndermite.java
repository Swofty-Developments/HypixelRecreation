package net.swofty.type.skyblockgeneric.entity.mob.mobs.end;

import net.minestom.server.entity.EntityType;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.region.RegionType;

import java.util.List;

public class MobEndermite extends EndMob {
    public MobEndermite() {
        super(EntityType.ENDERMITE);
    }

    @Override
    protected String endDisplayName() {
        return "Endermite";
    }

    @Override
    protected int endLevel() {
        return 37;
    }

    @Override
    protected double endHealth() {
        return 2_000;
    }

    @Override
    protected double endDamage() {
        return 400;
    }

    @Override
    protected String endMobId() {
        return "ENDERMITE_37";
    }

    @Override
    protected RegionType endRegion() {
        return RegionType.THE_END;
    }

    @Override
    protected int endBestiaryTier() {
        return 5;
    }

    @Override
    protected int endBestiaryBracket() {
        return 5;
    }

    @Override
    protected List<SkyBlockLootTable.LootRecord> endLoot() {
        return List.of(
                drop(ItemType.END_STONE, 1, 2, 100),
                drop(ItemType.PEARLESCENT_DYE, 1, 1, 0.00001)
        );
    }

    @Override
    protected long endSkillXp() {
        return 25;
    }

    @Override
    protected int endCoins() {
        return 10;
    }

    @Override
    protected int endXpOrbs() {
        return 8;
    }

    @Override
    protected boolean endNaturallyHostile() {
        return true;
    }

    @Override
    public List<MobType> getMobTypes() {
        return List.of(MobType.ARTHROPOD, MobType.ENDER);
    }
}
