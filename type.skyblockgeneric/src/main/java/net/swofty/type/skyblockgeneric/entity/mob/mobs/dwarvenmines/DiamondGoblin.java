package net.swofty.type.skyblockgeneric.entity.mob.mobs.dwarvenmines;

import lombok.NonNull;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.loottable.BestiaryDropRarity;
import net.swofty.type.skyblockgeneric.loottable.OtherLoot;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DiamondGoblin extends BestiaryMob {

	public DiamondGoblin() {
		super(EntityType.PLAYER);
	}

	@Override
	public Integer getLevel() {
		return 500;
	}

	@Override
	public int getMaxBestiaryTier() {
		return 15;
	}

	@Override
	public int getBestiaryBracket() {
		return 7;
	}

	@Override
	public String getMobID() {
		return "GOBLIN_2";
	}

	@Override
	public void onInit() {
		setBoots(new SkyBlockItem(ItemType.DIAMOND_BOOTS).getItemStack());
		setLeggings(new SkyBlockItem(ItemType.DIAMOND_LEGGINGS).getItemStack());
		setChestplate(new SkyBlockItem(ItemType.DIAMOND_CHESTPLATE).getItemStack());
		setHelmet(new SkyBlockItem(ItemType.DIAMOND_HELMET).getItemStack());
	}

	@Override
	public GUIMaterial getGuiMaterial() {
		return new GUIMaterial("81d2116827a41a713660bb52c9ba3bc6dd038175afb74a473b85f0cf60ff70e2");
	}

	@Override
	public String getDisplayName() {
		return "Diamond Goblin";
	}

	@Override
	public List<GoalSelector> getGoalSelectors() {
		return List.of();
	}

	@Override
	public List<TargetSelector> getTargetSelectors() {
		return List.of();
	}

	@Override
	public ItemStatistics getBaseStatistics() {
		return ItemStatistics.builder()
				.withBase(ItemStatistic.HEALTH, 5D)
				.withBase(ItemStatistic.DAMAGE, 250D)
				.withBase(ItemStatistic.SPEED, 150D)
				.build();
	}

	@Override
	public @Nullable SkyBlockLootTable getLootTable() {
        return new SkyBlockLootTable(Key.key("skyblock", "mob/diamond_goblin")) {
			@Override
			public @NonNull List<LootRecord> getLootTable() {
				return List.of(
                        new LootRecord(ItemType.MITHRIL, 20, 100, BestiaryDropRarity.COMMON)
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
		return new OtherLoot(20, 250, 100);
	}

	@Override
	public List<MobType> getMobTypes() {
		return List.of(MobType.GLACIAL, MobType.HUMANOID);
	}

}
