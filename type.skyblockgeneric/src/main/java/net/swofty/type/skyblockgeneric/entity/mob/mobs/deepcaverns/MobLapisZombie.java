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

public class MobLapisZombie extends BestiaryMob implements RegionPopulator {

	public MobLapisZombie() {
		super(EntityType.ZOMBIE);
	}

	@Override
	public String getDisplayName() {
		return "Lapis Zombie";
	}

	@Override
	public Integer getLevel() {
		return 7;
	}

	@Override
	public void onInit() {
		setBoots(new SkyBlockItem(ItemType.LAPIS_ARMOR_BOOTS).getItemStack());
		setLeggings(new SkyBlockItem(ItemType.LAPIS_ARMOR_LEGGINGS).getItemStack());
		setChestplate(new SkyBlockItem(ItemType.LAPIS_ARMOR_CHESTPLATE).getItemStack());
		setHelmet(new SkyBlockItem(ItemType.LAPIS_ARMOR_HELMET).getItemStack());
	}

	@Override
	public List<GoalSelector> getGoalSelectors() {
		return List.of(
				new MeleeAttackWithinRegionGoal(this,
						1.6,
						20,
						TimeUnit.SERVER_TICK,
						RegionType.LAPIS_QUARRY), // Attack the target
				new RandomRegionStrollGoal(this, 15, RegionType.LAPIS_QUARRY)  // Walk around
		);
	}

	@Override
	public List<TargetSelector> getTargetSelectors() {
		return List.of(
				new LastEntityDamagerTarget(this, 16), // First target the last entity which attacked you
				new ClosestEntityRegionTarget(this,
						6,
						entity -> entity instanceof SkyBlockPlayer,
						RegionType.LAPIS_QUARRY) // If there is none, target the nearest player
		);
	}

	@Override
	public ItemStatistics getBaseStatistics() {
		return ItemStatistics.builder()
				.withBase(ItemStatistic.HEALTH, 200D)
				.withBase(ItemStatistic.DAMAGE, 50D)
				.withBase(ItemStatistic.SPEED, 100D)
				.build();
	}

	@Override
	public @Nullable SkyBlockLootTable getLootTable() {
		return MobLootTable.withPools("LAPIS_ZOMBIE",
				MobLootTable.Pool.independent("drops",
						new MobLootTable.Drop(ItemType.ROTTEN_FLESH, 1, 100, BestiaryDropRarity.COMMON),
						new MobLootTable.Drop(ItemType.LAPIS_CRYSTAL, 1, 1, BestiaryDropRarity.RARE),
						new MobLootTable.Drop(ItemType.EXP_SHARE_CORE, 1, 0.01, BestiaryDropRarity.RNGESUS)),
				MobLootTable.Pool.weighted("lapis_armor", 0.96,
						new MobLootTable.Drop(ItemType.LAPIS_ARMOR_BOOTS, 1, 1, BestiaryDropRarity.RARE),
						new MobLootTable.Drop(ItemType.LAPIS_ARMOR_LEGGINGS, 1, 1, BestiaryDropRarity.RARE),
						new MobLootTable.Drop(ItemType.LAPIS_ARMOR_CHESTPLATE, 1, 1, BestiaryDropRarity.RARE),
						new MobLootTable.Drop(ItemType.LAPIS_ARMOR_HELMET, 1, 1, BestiaryDropRarity.RARE)));
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
		return new OtherLoot(12, 5, 10);
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
		return "lapis_zombie";
	}

	@Override
	public GUIMaterial getGuiMaterial() {
		return new GUIMaterial("e9f7979b25001087969d58c06e14d00b8dab57dab060b4c8b483c1b7f869940");
	}

	@Override
	public List<Populator> getPopulators() {
		return List.of(new Populator(RegionType.LAPIS_QUARRY, 20));
	}
}
