package net.swofty.type.skyblockgeneric.entity.mob.mobs.end;

import lombok.NonNull;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget;
import net.minestom.server.item.Material;
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
import net.swofty.type.skyblockgeneric.loottable.OtherLoot;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class EndMob extends BestiaryMob implements RegionPopulator {
    protected EndMob(EntityType entityType) {
        super(entityType);
    }

    protected abstract String endDisplayName();

    protected abstract int endLevel();

    protected abstract double endHealth();

    protected abstract double endDamage();

    protected abstract String endMobId();

    protected abstract RegionType endRegion();

    protected abstract int endBestiaryTier();

    protected abstract int endBestiaryBracket();

    protected List<SkyBlockLootTable.LootRecord> endLoot() {
        return List.of();
    }

    protected long endSkillXp() {
        return endLevel();
    }

    protected int endCoins() {
        return Math.max(1, endLevel() / 5);
    }

    protected int endXpOrbs() {
        return Math.max(1, endLevel() / 2);
    }

    protected boolean endNaturallyHostile() {
        return false;
    }

    protected int endMinimumSpawnCount() {
        return 10;
    }

    @Override
    public String getDisplayName() {
        return endDisplayName();
    }

    @Override
    public Integer getLevel() {
        return endLevel();
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return List.of(
                new MeleeAttackWithinRegionGoal(this, 1.6, 20, TimeUnit.SERVER_TICK, endRegion()),
                new RandomRegionStrollGoal(this, 15, endRegion())
        );
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        if (endNaturallyHostile()) {
            return List.of(
                    new LastEntityDamagerTarget(this, 16),
                    new ClosestEntityRegionTarget(this, 16,
                            entity -> entity instanceof SkyBlockPlayer,
                            endRegion())
            );
        }

        return List.of(new LastEntityDamagerTarget(this, 16));
    }

    @Override
    public ItemStatistics getBaseStatistics() {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, endHealth())
                .withBase(ItemStatistic.DAMAGE, endDamage())
                .withBase(ItemStatistic.SPEED, 100D)
                .build();
    }

    @Override
    public @Nullable SkyBlockLootTable getLootTable() {
        List<SkyBlockLootTable.LootRecord> loot = endLoot();
        return new SkyBlockLootTable() {
            @Override
            public @NonNull List<LootRecord> getLootTable() {
                return loot;
            }

            @Override
            public @NotNull CalculationMode getCalculationMode() {
                return CalculationMode.CALCULATE_INDIVIDUAL;
            }
        };
    }

    protected SkyBlockLootTable.LootRecord drop(ItemType itemType, int amount, double chancePercent) {
        return new SkyBlockLootTable.LootRecord(itemType, amount, chancePercent);
    }

    protected SkyBlockLootTable.LootRecord drop(ItemType itemType, int minAmount, int maxAmount, double chancePercent) {
        return new SkyBlockLootTable.LootRecord(itemType, (int) (Math.random() * (maxAmount - minAmount + 1)) + minAmount,
                chancePercent);
    }

    protected List<SkyBlockLootTable.LootRecord> endermanLoot(int minPearls, int maxPearls) {
        return List.of(
                drop(ItemType.ENDER_PEARL, minPearls, maxPearls, 100),
                drop(ItemType.ENCHANTED_ENDER_PEARL, 1, 1, 1),
                drop(ItemType.END_HELMET, 1, 1, 0.25),
                drop(ItemType.END_CHESTPLATE, 1, 1, 0.25),
                drop(ItemType.END_LEGGINGS, 1, 1, 0.25),
                drop(ItemType.END_BOOTS, 1, 1, 0.25),
                drop(ItemType.ENDER_NECKLACE, 1, 1, 0.5),
                drop(ItemType.PEARLESCENT_DYE, 1, 1, 0.00001)
        );
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
        return new OtherLoot(endSkillXp(), endCoins(), endXpOrbs());
    }

    @Override
    public List<MobType> getMobTypes() {
        return List.of(MobType.ENDER);
    }

    @Override
    public int getMaxBestiaryTier() {
        return endBestiaryTier();
    }

    @Override
    public int getBestiaryBracket() {
        return endBestiaryBracket();
    }

    @Override
    public String getMobID() {
        return endMobId();
    }

    @Override
    public GUIMaterial getGuiMaterial() {
        return new GUIMaterial(Material.END_STONE);
    }

    @Override
    public List<Populator> getPopulators() {
        return List.of(new Populator(endRegion(), endMinimumSpawnCount()));
    }
}
