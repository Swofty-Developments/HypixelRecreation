package net.swofty.type.skyblockgeneric.entity.mob.impl;

import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.time.TimeUnit;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.ai.ClosestEntityRegionTarget;
import net.swofty.type.skyblockgeneric.entity.mob.ai.MeleeAttackWithinRegionGoal;
import net.swofty.type.skyblockgeneric.entity.mob.ai.RandomRegionStrollGoal;
import net.swofty.type.skyblockgeneric.entity.mob.ai.VanillaMeleeAttackGoal;
import net.swofty.type.skyblockgeneric.entity.mob.ai.VanillaRandomStrollGoal;
import net.swofty.type.skyblockgeneric.loottable.OtherLoot;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class ProfiledBestiaryMob extends BestiaryMob implements RegionPopulator {
    protected final MobDefinition definition;

    protected ProfiledBestiaryMob(MobDefinition definition) {
        this(definition, true);
    }

    protected ProfiledBestiaryMob(MobDefinition definition, boolean initialize) {
        super(definition.entityType(), false);
        this.definition = definition;
        if (initialize) initializeMob();
    }

    @Override public String getDisplayName() { return definition.displayName(); }
    @Override public Integer getLevel() { return definition.level(); }
    @Override public ItemStatistics getBaseStatistics() { return statistics(definition.health(), definition.damage(), definition.speed()); }
    @Override public @Nullable SkyBlockLootTable getLootTable() { return definition.lootTable(); }
    @Override public SkillCategories getSkillCategory() { return SkillCategories.COMBAT; }
    @Override public long damageCooldown() { return 500; }
    @Override public OtherLoot getOtherLoot() { return definition.otherLoot(); }
    @Override public List<MobType> getMobTypes() { return definition.mobTypes(); }

    @Override
    public List<RegionPopulator.Populator> getPopulators() {
        return definition.targetRegion() == null ? List.of()
                : List.of(new RegionPopulator.Populator(definition.targetRegion(), 1));
    }

    @Override
    public boolean canPopulate(Instance instance) {
        return true;
    }
    @Override public int getMaxBestiaryTier() { return definition.maxBestiaryTier(); }
    @Override public int getBestiaryBracket() { return definition.bestiaryBracket(); }
    @Override public String getMobID() { return definition.mobId(); }
    @Override public GUIMaterial getGuiMaterial() { return definition.guiMaterial(); }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        List<GoalSelector> goals = new ArrayList<>();
        if (definition.attacks()) {
            goals.add(definition.targetRegion() == null
                    ? new VanillaMeleeAttackGoal(this, 1.6, 20, TimeUnit.SERVER_TICK)
                    : new MeleeAttackWithinRegionGoal(this, 1.6, 20, TimeUnit.SERVER_TICK,
                    definition.targetRegion()));
        }
        goals.add(definition.targetRegion() == null
                ? new VanillaRandomStrollGoal(this, 15)
                : new RandomRegionStrollGoal(this, 15, definition.targetRegion()));
        return List.copyOf(goals);
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        if (!definition.attacks()) return List.of();
        List<TargetSelector> targets = new ArrayList<>();
        targets.add(new LastEntityDamagerTarget(this, 16));
        if (definition.targetsPlayers()) {
            targets.add(new ClosestEntityRegionTarget(this, 16, entity -> entity instanceof SkyBlockPlayer,
                    definition.targetRegion()));
        }
        return List.copyOf(targets);
    }

    protected final ItemStatistics statistics(double health, double damage, double speed) {
        return ItemStatistics.builder()
                .withBase(ItemStatistic.HEALTH, health)
                .withBase(ItemStatistic.DAMAGE, damage)
                .withBase(ItemStatistic.SPEED, speed)
                .build();
    }
}
