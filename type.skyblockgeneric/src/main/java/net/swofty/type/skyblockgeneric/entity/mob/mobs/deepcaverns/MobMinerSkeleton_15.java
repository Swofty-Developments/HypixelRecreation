package net.swofty.type.skyblockgeneric.entity.mob.mobs.deepcaverns;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.ai.ClosestEntityRegionTarget;
import net.swofty.type.skyblockgeneric.entity.mob.ai.RandomRegionStrollGoal;
import net.swofty.type.skyblockgeneric.entity.mob.impl.RegionPopulator;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.loottable.BestiaryDropRarity;
import net.swofty.type.skyblockgeneric.loottable.MobLootTable;
import net.swofty.type.skyblockgeneric.loottable.OtherLoot;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MobMinerSkeleton_15 extends BestiaryMob implements RegionPopulator {

	public MobMinerSkeleton_15() {
		super(EntityType.SKELETON);
	}

	@Override
	public String getDisplayName() {
		return "Miner Skeleton";
	}

	@Override
	public Integer getLevel() {
		return 15;
	}

	@Override
	public void onInit() {
		setBoots(new SkyBlockItem(ItemType.MINER_ARMOR_BOOTS).getItemStack());
		setLeggings(new SkyBlockItem(ItemType.MINER_ARMOR_LEGGINGS).getItemStack());
		setChestplate(new SkyBlockItem(ItemType.MINER_ARMOR_CHESTPLATE).getItemStack());
		setHelmet(new SkyBlockItem(ItemType.MINER_ARMOR_HELMET).getItemStack());
	}

	@Override
	public List<GoalSelector> getGoalSelectors() {
		return List.of(
				new RandomRegionStrollGoal(this, 15, RegionType.DIAMOND_RESERVE)
		);
	}

	@Override
	public List<TargetSelector> getTargetSelectors() {
		return List.of(
				new LastEntityDamagerTarget(this, 12), // First target the last entity which attacked you
				new ClosestEntityRegionTarget(this,
						6,
						entity -> entity instanceof SkyBlockPlayer,
						RegionType.DIAMOND_RESERVE) // If there is none, target the nearest player
		);
	}

	@Override
	public ItemStatistics getBaseStatistics() {
		return ItemStatistics.builder()
				.withBase(ItemStatistic.HEALTH, 250D)
                .withBase(ItemStatistic.DAMAGE, 150D)
				.withBase(ItemStatistic.SPEED, 100D)
				.build();
	}

	@Override
	public @Nullable SkyBlockLootTable getLootTable() {
        return MobLootTable.withPools(getMobID(),
                MobLootTable.Pool.independent("drops",
                        new MobLootTable.Drop(ItemType.BONE, 3, 100, BestiaryDropRarity.COMMON),
                        new MobLootTable.Drop(ItemType.EXP_SHARE_CORE, 1, 0.01, BestiaryDropRarity.RNGESUS),
                        new MobLootTable.Drop(ItemType.BONE_DYE, 1, 0.000033, BestiaryDropRarity.RNGESUS)),
                MobLootTable.Pool.weighted("diamond_tank", 0.96,
                        new MobLootTable.Drop(ItemType.MINER_ARMOR_BOOTS, 1, 1, BestiaryDropRarity.RARE),
                        new MobLootTable.Drop(ItemType.MINER_ARMOR_LEGGINGS, 1, 1, BestiaryDropRarity.RARE),
                        new MobLootTable.Drop(ItemType.MINER_ARMOR_CHESTPLATE, 1, 1, BestiaryDropRarity.RARE),
                        new MobLootTable.Drop(ItemType.MINER_ARMOR_HELMET, 1, 1, BestiaryDropRarity.RARE)));
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
		return new OtherLoot(20, 12, 30);
	}

	@Override
	public List<MobType> getMobTypes() {
		return List.of(MobType.SKELETAL);
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
		return "DIAMOND_SKELETON_15";
	}

	@Override
	public GUIMaterial getGuiMaterial() {
		return new GUIMaterial("8de8bbd7f6d77a1614865ef6a1d31f53f797550d14ee21d107a8415c14b48ca6");
	}

	@Override
	public List<Populator> getPopulators() {
		return List.of(
				new Populator(RegionType.DIAMOND_RESERVE, 20)
		);
	}
}
