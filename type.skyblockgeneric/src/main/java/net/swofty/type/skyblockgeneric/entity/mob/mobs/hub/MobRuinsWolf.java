package net.swofty.type.skyblockgeneric.entity.mob.mobs.hub;

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

public class MobRuinsWolf extends BestiaryMob implements RegionPopulator {

    public MobRuinsWolf() {
        super(EntityType.WOLF);
    }

    @Override
    public String getDisplayName() {
        return "Wolf";
    }

    @Override
    public Integer getLevel() {
        return 15;
    }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return List.of(
                new MeleeAttackWithinRegionGoal(this,
                        1.6,
                        20,
                        TimeUnit.SERVER_TICK,
                        RegionType.RUINS), // Attack the target
                new RandomRegionStrollGoal(this, 15, RegionType.RUINS)  // Walk around
        );
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        return List.of(
                new LastEntityDamagerTarget(this, 16), // First target the last entity which attacked you
                new ClosestEntityRegionTarget(this,
                        16,
                        entity -> entity instanceof SkyBlockPlayer,
                        RegionType.RUINS) // If there is none, target the nearest player
        );
    }

    @Override
    public ItemStatistics getBaseStatistics() {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, 250D)
                .withBase(ItemStatistic.DAMAGE, 90D)
                .withBase(ItemStatistic.SPEED, 100D)
                .build();
    }

    @Override
    public @Nullable SkyBlockLootTable getLootTable() {
        return new SkyBlockLootTable(Key.key("skyblock", "mob/ruins_wolf")) {
            @Override
            public @NonNull List<LootRecord> getLootTable() {
                return List.of(
                        new LootRecord(ItemType.BONE, makeAmountBetween(1, 3), 20, BestiaryDropRarity.UNCOMMON)
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
    public List<Populator> getPopulators() {
        return List.of(
            new Populator(RegionType.RUINS, 20)
        );
    }

    @Override
    public OtherLoot getOtherLoot() {
        return new OtherLoot(15, 1, 4);
    }

    @Override
    public List<MobType> getMobTypes() {
        return List.of(MobType.ANIMAL);
    }

    @Override
    public int getMaxBestiaryTier() {
        return 15;
    }

    @Override
    public int getBestiaryBracket() {
        return 1;
    }

    @Override
    public String getMobID() {
        return "RUIN_WOLF";
    }

    @Override
    public GUIMaterial getGuiMaterial() {
        return new GUIMaterial("f4cb7a6bf6c32c49f2589147e6f0f888e9e35875dd1ea2a8af379ca710589e6b");
    }
}
