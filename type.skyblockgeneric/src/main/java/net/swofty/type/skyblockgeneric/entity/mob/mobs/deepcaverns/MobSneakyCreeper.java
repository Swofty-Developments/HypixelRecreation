package net.swofty.type.skyblockgeneric.entity.mob.mobs.deepcaverns;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.ai.ClosestEntityRegionTarget;
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

public class MobSneakyCreeper extends BestiaryMob implements RegionPopulator {
    private long fuseStarted = -1;

	public MobSneakyCreeper() {
		super(EntityType.CREEPER);
	}

	@Override
	public String getDisplayName() {
		return "Sneaky Creeper";
	}

	@Override
	public Integer getLevel() {
		return 3;
	}

	@Override
	public void onInit() {
		setInvisible(true);
	}

	@Override
	public List<GoalSelector> getGoalSelectors() {
		return List.of(
				// TODO: add explosion goal
				new RandomRegionStrollGoal(this, 15, RegionType.GUNPOWDER_MINES)  // Walk around
		);
	}

	@Override
	public List<TargetSelector> getTargetSelectors() {
		return List.of(
				new LastEntityDamagerTarget(this, 12), // First target the last entity which attacked you
				new ClosestEntityRegionTarget(this,
						6,
						entity -> entity instanceof SkyBlockPlayer,
						RegionType.GUNPOWDER_MINES) // If there is none, target the nearest player
		);
	}

    @Override
    public void tick(long time) {
        super.tick(time);

        if (getInstance() == null) return;

        Entity target = getInstance().getNearbyEntities(getPosition(), 3).stream()
                .filter(entity -> entity instanceof SkyBlockPlayer)
                .findFirst()
                .orElse(null);

        if (target == null) {
            fuseStarted = -1;
            return;
        }

        if (fuseStarted < 0) {
            fuseStarted = time;
            setInvisible(false);
        }

        if (time - fuseStarted >= 30) {
            getInstance().explode((float) getPosition().x(), (float) getPosition().y(), (float) getPosition().z(), 3);
            remove();
        }
    }

    @Override
    public boolean damage(net.minestom.server.entity.damage.Damage damage) {
        setInvisible(false);
        return super.damage(damage);
    }

	@Override
	public ItemStatistics getBaseStatistics() {
		return ItemStatistics.builder()
				.withBase(ItemStatistic.HEALTH, 120D)
				.withBase(ItemStatistic.DAMAGE, 80D)
				.withBase(ItemStatistic.SPEED, 100D)
				.build();
	}

	@Override
	public @Nullable SkyBlockLootTable getLootTable() {
        return new MobLootTable("SNEAKY_CREEPER",
                new MobLootTable.Drop(ItemType.GUNPOWDER, 1, 100, BestiaryDropRarity.COMMON),
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
        return new OtherLoot(0, 3, 3);
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
        return "SNEAKY_CREEPER";
	}

	@Override
	public GUIMaterial getGuiMaterial() {
		return new GUIMaterial(Material.CREEPER_HEAD);
	}

	@Override
	public List<Populator> getPopulators() {
		return List.of(new Populator(RegionType.GUNPOWDER_MINES, 10));
	}
}
