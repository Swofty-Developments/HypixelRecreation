package net.swofty.type.skyblockgeneric.entity.mob.mobs.dwarvenmines;

import lombok.NonNull;
import net.kyori.adventure.key.Key;
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
import net.swofty.type.skyblockgeneric.loottable.OtherLoot;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MobTreasureHoarder extends BestiaryMob implements RegionPopulator {

	public MobTreasureHoarder() {
		super(EntityType.PLAYER);
	}

	@Override
	public int getMaxBestiaryTier() {
		return 15;
	}

	@Override
	public int getBestiaryBracket() {
		return 4;
	}

	@Override
	public String getMobID() {
		return "TREASURE_HOARDER";
	}

	@Override
	public GUIMaterial getGuiMaterial() {
		return null;
	}

	@Override
	public String getDisplayName() {
		return "Treasure Hoarder";
	}

	@Override
	public Integer getLevel() {
		return 70;
	}

	@Override
	public List<GoalSelector> getGoalSelectors() {
		return List.of(
				new MeleeAttackWithinRegionGoal(this,
						1.6,
						20,
						TimeUnit.SERVER_TICK,
						RegionType.UPPER_MINES),
				new RandomRegionStrollGoal(this, 15, RegionType.UPPER_MINES)
		);
	}

	@Override
	public List<TargetSelector> getTargetSelectors() {
		return List.of(
				new LastEntityDamagerTarget(this, 16),
				new ClosestEntityRegionTarget(this,
						6,
						entity -> entity instanceof SkyBlockPlayer,
						RegionType.UPPER_MINES)
		);
	}

	@Override
	public ItemStatistics getBaseStatistics() {
		return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, 22000D)
                .withBase(ItemStatistic.DAMAGE, 750D)
				.withBase(ItemStatistic.SPEED, 100D)
				.build();
	}


	@Override
	public @Nullable SkyBlockLootTable getLootTable() {
        return new SkyBlockLootTable(Key.key("skyblock", "mob/treasure_hoarder")) {
			@Override
			public @NonNull List<LootRecord> getLootTable() {
				return List.of(
                        new LootRecord(ItemType.STARFALL, 1, 100, BestiaryDropRarity.COMMON),
                        new LootRecord(ItemType.STARFALL, 1, 50, BestiaryDropRarity.COMMON),
                        new LootRecord(ItemType.TREASURITE, 1, 0.5, BestiaryDropRarity.RARE)
				);
			}

			@Override
			public @NotNull CalculationMode getCalculationMode() {
				return CalculationMode.CALCULATE_INDIVIDUAL;
			}
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
		return new OtherLoot(70, 0, 0);
	}

	@Override
	public List<MobType> getMobTypes() {
		return List.of(MobType.HUMANOID);
	}

	@Override
	public List<Populator> getPopulators() {
		return List.of(new Populator(RegionType.UPPER_MINES, 20));
	}
}
