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
import net.swofty.type.skyblockgeneric.entity.mob.ai.ClosestEntityRegionTarget;
import net.swofty.type.skyblockgeneric.entity.mob.ai.MeleeAttackWithinRegionGoal;
import net.swofty.type.skyblockgeneric.entity.mob.ai.RandomRegionStrollGoal;
import net.swofty.type.skyblockgeneric.entity.mob.impl.RegionPopulator;
import net.swofty.type.skyblockgeneric.loottable.BestiaryDropRarity;
import net.swofty.type.skyblockgeneric.loottable.MobLootTable;
import net.swofty.type.skyblockgeneric.loottable.OtherLoot;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MobEmeraldSlime_05 extends BestiaryMob implements RegionPopulator {

	public MobEmeraldSlime_05() {
		super(EntityType.SLIME);
	}

	@Override
	public String getDisplayName() {
		return "Emerald Slime";
	}

	@Override
	public Integer getLevel() {
		return 5;
	}

	@Override
	public List<GoalSelector> getGoalSelectors() {
		return List.of(
				new MeleeAttackWithinRegionGoal(this,
						1.6,
						20,
						TimeUnit.SERVER_TICK,
						RegionType.SLIMEHILL),
				new RandomRegionStrollGoal(this, 15, RegionType.SLIMEHILL)  // Walk around
		);
	}

	@Override
	public List<TargetSelector> getTargetSelectors() {
		return List.of(
				new LastEntityDamagerTarget(this, 12), // First target the last entity which attacked you
				new ClosestEntityRegionTarget(this,
						6,
						entity -> entity instanceof SkyBlockPlayer,
						RegionType.SLIMEHILL) // If there is none, target the nearest player
		);
	}

	@Override
	public ItemStatistics getBaseStatistics() {
		return ItemStatistics.builder()
				.withBase(ItemStatistic.HEALTH, 80D)
				.withBase(ItemStatistic.DAMAGE, 70D)
				.withBase(ItemStatistic.SPEED, 100D)
				.build();
	}

	@Override
	public @Nullable SkyBlockLootTable getLootTable() {
		return switch (getLevel()) {
			case 10 -> new MobLootTable(getMobID(),
					new MobLootTable.Drop(ItemType.SLIME_BALL, 1, 100, BestiaryDropRarity.COMMON),
					new MobLootTable.Drop(ItemType.SLIME_BALL, 1, 50, BestiaryDropRarity.COMMON),
					new MobLootTable.Drop(ItemType.EXP_SHARE_CORE, 1, 0.01, BestiaryDropRarity.RNGESUS));
			case 15 -> new MobLootTable(getMobID(),
					new MobLootTable.Drop(ItemType.SLIME_BALL, 2, 100, BestiaryDropRarity.COMMON),
					new MobLootTable.Drop(ItemType.EXP_SHARE_CORE, 1, 0.01, BestiaryDropRarity.RNGESUS));
			default -> new MobLootTable(getMobID(),
					new MobLootTable.Drop(ItemType.SLIME_BALL, 1, 100, BestiaryDropRarity.COMMON),
					new MobLootTable.Drop(ItemType.EXP_SHARE_CORE, 1, 0.01, BestiaryDropRarity.RNGESUS));
		};
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
		return new OtherLoot(12, 5, 20);
	}

	@Override
	public List<MobType> getMobTypes() {
		return List.of(MobType.CUBIC);
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
		return "EMERALD_SLIME_05";
	}

	@Override
	public GUIMaterial getGuiMaterial() {
		return new GUIMaterial("895aeec6b842ada8669f846d65bc49762597824ab944f22f45bf3bbb941abe6c");
	}

	@Override
	public List<Populator> getPopulators() {
		return List.of(new Populator(RegionType.SLIMEHILL, 20));
	}
}
