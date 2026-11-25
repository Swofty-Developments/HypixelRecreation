package net.swofty.type.skyblockgeneric.entity.mob.mobs.end;

import net.minestom.server.entity.EntityType;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.region.RegionType;

import java.util.List;

public class MobObsidianDefender extends EndMob {
    public MobObsidianDefender() {
        super(EntityType.WITHER_SKELETON);
    }

    @Override
    protected String endDisplayName() {
        return "Obsidian Defender";
    }

    @Override
    protected int endLevel() {
        return 55;
    }

    @Override
    protected double endHealth() {
        return 10_000;
    }

    @Override
    protected double endDamage() {
        return 200;
    }

    @Override
    protected String endMobId() {
        return "OBSIDIAN_DEFENDER";
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
        return 5;
    }

    @Override
    protected List<SkyBlockLootTable.LootRecord> endLoot() {
        return List.of(
                drop(ItemType.OBSIDIAN, 6, 6, 100),
                drop(ItemType.OBSIDIAN, 1, 1, 50),
                drop(ItemType.ENCHANTED_OBSIDIAN, 1, 1, 1),
                drop(ItemType.OBSIDIAN_CHESTPLATE, 1, 1, 0.1),
                drop(ItemType.ENDER_BELT, 1, 1, 1),
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

    @Override
    public List<MobType> getMobTypes() {
        return List.of(MobType.ENDER, MobType.WITHER);
    }
}
