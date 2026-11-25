package net.swofty.type.skyblockgeneric.entity.mob.mobs.deepcaverns;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget;
import net.minestom.server.utils.time.TimeUnit;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.ai.MeleeAttackWithinRegionGoal;
import net.swofty.type.skyblockgeneric.entity.mob.ai.RandomRegionStrollGoal;
import net.swofty.type.skyblockgeneric.entity.mob.impl.RegionPopulator;
import net.swofty.type.skyblockgeneric.loottable.BestiaryDropRarity;
import net.swofty.type.skyblockgeneric.loottable.MobLootTable;
import net.swofty.type.skyblockgeneric.loottable.OtherLoot;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MobRedstonePigman extends BestiaryMob implements RegionPopulator {

	public MobRedstonePigman() {
		super(EntityType.ZOMBIFIED_PIGLIN);
	}

	@Override
	public String getDisplayName() {
		return "Redstone Pigman";
	}

	@Override
	public Integer getLevel() {
		return 3;
	}

	@Override
	public List<GoalSelector> getGoalSelectors() {
		return List.of(
				new MeleeAttackWithinRegionGoal(this,
						1.6,
						20,
						TimeUnit.SERVER_TICK,
                        RegionType.PIGMENS_DEN),
				new RandomRegionStrollGoal(this, 15, RegionType.PIGMENS_DEN)  // Walk around
		);
	}

	@Override
	public List<TargetSelector> getTargetSelectors() {
		return List.of(
                new LastEntityDamagerTarget(this, 12)
		);
	}

	@Override
	public ItemStatistics getBaseStatistics() {
		return ItemStatistics.builder()
				.withBase(ItemStatistic.HEALTH, 250D)
				.withBase(ItemStatistic.DAMAGE, 75D)
				.withBase(ItemStatistic.SPEED, 100D)
				.build();
	}

	@Override
	public @Nullable SkyBlockLootTable getLootTable() {
        return new MobLootTable("REDSTONE_PIGMAN",
                new MobLootTable.Drop(ItemType.GOLD_NUGGET, 2, 50, BestiaryDropRarity.COMMON),
                new MobLootTable.Drop(ItemType.REDSTONE, 5, 50, BestiaryDropRarity.COMMON),
                new MobLootTable.Drop(ItemType.FLAMING_SWORD, 1, 1, BestiaryDropRarity.UNCOMMON),
                new MobLootTable.Drop(ItemType.EXP_SHARE_CORE, 1, 0.01, BestiaryDropRarity.RNGESUS));
	}

	@Override
	public SkillCategories getSkillCategory() {
		return SkillCategories.COMBAT;
	}

	@Override
	public long damageCooldown() {
		return 500;
	}

	@Override
	public OtherLoot getOtherLoot() {
		return new OtherLoot(20, 12, 25);
	}

	@Override
	public List<MobType> getMobTypes() {
		return List.of(MobType.UNDEAD);
	}

	@Override
	public int getMaxBestiaryTier() {
		return 10;
	}

	@Override
	public int getBestiaryBracket() {
		return 1;
	}

	@Override
	public String getMobID() {
		return "REDSTONE_PIGMAN";
	}

	@Override
	public GUIMaterial getGuiMaterial() {
		return new GUIMaterial("74e9c6e98582ffd8ff8feb3322cd1849c43fb16b158abb11ca7b42eda7743eb");
	}

	@Override
	public List<Populator> getPopulators() {
		return List.of(new Populator(RegionType.PIGMENS_DEN, 20));
	}
}
