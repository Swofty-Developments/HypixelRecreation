package net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer;

import java.util.List;
import java.util.UUID;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.timer.Task;
import net.swofty.commons.rates.RateCategory;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.ability.SlayerBossAbility;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.ability.SlayerBossAbilityFactory;
import net.swofty.type.skyblockgeneric.loottable.OtherLoot;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import org.jetbrains.annotations.Nullable;

public final class SlayerBossMob extends SkyBlockMob {
    private static final ThreadLocal<SlayerBossProfile> IN_CONSTRUCTION = new ThreadLocal<>();

    private final SlayerBossProfile profile;
    private final UUID ownerUuid;
    private final SlayerBossAbility ability;
    private final List<Task> abilityTasks = new java.util.concurrent.CopyOnWriteArrayList<>();

    private SlayerBossMob(UUID ownerUuid, SlayerBossProfile profile) {
        super(profile.entityType());
        this.ownerUuid = ownerUuid;
        this.profile = profile;
        this.ability = SlayerBossAbilityFactory.create(profile.type());
    }

    public static SlayerBossMob create(UUID ownerUuid, SlayerBossProfile profile) {
        IN_CONSTRUCTION.set(profile);
        try {
            return new SlayerBossMob(ownerUuid, profile);
        } finally {
            IN_CONSTRUCTION.remove();
        }
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public SlayerBossProfile getProfile() {
        return live();
    }

    public SlayerBossAbility getAbility() {
        return ability;
    }

    public void trackAbilityTask(Task task) {
        abilityTasks.add(task);
    }

    @Override public String getDisplayName() { return live().displayName(); }
    @Override public Integer getLevel() { return live().tier().bossLevel(); }
    @Override public RateCategory getDropRateCategory() { return RateCategory.SLAYER; }
    @Override public List<MobType> getMobTypes() { return live().mobTypes(); }
    @Override public ItemStatistics getBaseStatistics() { return live().asBaseStatistics(); }
    @Override public OtherLoot getOtherLoot() { return live().asOtherLoot(); }
    @Override public long damageCooldown() { return 500L; }
    @Override public @Nullable SkyBlockLootTable getLootTable() { return live().asLootTable(); }
    @Override public SkillCategories getSkillCategory() { return SkillCategories.COMBAT; }

    @Override
    public List<GoalSelector> getGoalSelectors() {
        return SlayerBossBehaviour.goals(this);
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        return SlayerBossBehaviour.targets(this);
    }

    @Override
    public void onSpawn() {
        ability.onSpawn(this);
    }

    @Override
    public boolean damage(@org.jetbrains.annotations.NotNull Damage damage) {
        float amount = damage.getAmount();
        float modified = ability.modifyIncomingDamage(this, damage, amount);
        if (modified <= 0F) {
            return false;
        }

        Damage applied = modified == amount
            ? damage
            : new Damage(damage.getType(), damage.getSource(), damage.getAttacker(), damage.getSourcePosition(), modified);
        boolean result = super.damage(applied);
        ability.onDamaged(this, applied, modified);
        return result;
    }

    @Override
    public void kill() {
        ability.onDeath(this);
        abilityTasks.forEach(Task::cancel);
        abilityTasks.clear();
        super.kill();
    }

    private SlayerBossProfile live() {
        return profile != null ? profile : IN_CONSTRUCTION.get();
    }
}
