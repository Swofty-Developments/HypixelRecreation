package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.swofty.type.skyblockgeneric.collection.CustomCollectionAward;
import net.swofty.type.skyblockgeneric.enchantment.abstr.DamageEventEnchant;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EnchFromTable;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.DamageIndicator;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentCleave implements Ench, EnchFromTable, DamageEventEnchant {

    public static final double[] DAMAGE_PERCENTAGES = new double[]{0.05, 0.10, 0.15, 0.20, 0.25, 0.30};

    public static final double[] RADIUS = new double[]{3.5, 4.0, 4.5, 5.0, 5.5, 6.0};

    @Override
    public String getDescription(int level) {
        int damagePercent = (int) (DAMAGE_PERCENTAGES[level - 1] * 100);
        double radius = RADIUS[level - 1];
        return "Deals <a>" + damagePercent + "%<7> of your damage to other monsters within <a>" + radius + " blocks<7>.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 10,
                2, 20,
                3, 30,
                4, 40,
                5, 50,
                6, 200
        ));

        if (player.hasCustomCollectionAward(CustomCollectionAward.CLEAVE_DISCOUNT)) {
            levels.replaceAll((k, v) -> (int) (v * 0.75));
        }

        return new ApplyLevels(levels);
    }

    @Override
    public List<EnchantItemGroups> getGroups() {
        return List.of(EnchantItemGroups.SWORD, EnchantItemGroups.FISHING_WEAPON, 
                EnchantItemGroups.GAUNTLET, EnchantItemGroups.LONG_SWORD);
    }

    @Override
    public TableLevels getLevelsFromTableToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 10,
                2, 20,
                3, 30,
                4, 40,
                5, 50
        ));

        if (player.hasCustomCollectionAward(CustomCollectionAward.CLEAVE_DISCOUNT)) {
            levels.replaceAll((k, v) -> (int) (v * 0.75));
        }

        return new TableLevels(levels);
    }

    @Override
    public int getRequiredBookshelfPower() {
        return 3;
    }

    @Override
    public void onDamageDealt(SkyBlockPlayer player, LivingEntity target, double damageDealt, int level) {
        if (level < 1 || level > 6) return;

        Pos targetPos = target.getPosition();
        double radius = RADIUS[level - 1];
        double damagePercentage = DAMAGE_PERCENTAGES[level - 1];
        double cleaveDamage = damageDealt * damagePercentage;

        if (cleaveDamage <= 0) return;

        List<Entity> nearbyEntities = new ArrayList<>(target.getInstance().getNearbyEntities(targetPos, radius));

        for (Entity entity : nearbyEntities) {

            if (entity.equals(target)) continue;

            if (!(entity instanceof SkyBlockMob nearbyEntity)) continue;
            if (entity.getEntityType() == EntityType.PLAYER) continue;
            if (entity.getEntityType() == EntityType.VILLAGER) continue;

            nearbyEntity.damage(new Damage(DamageType.PLAYER_ATTACK, player, player, player.getPosition(), (float) cleaveDamage));

            new DamageIndicator()
                    .damage((float) cleaveDamage)
                    .pos(nearbyEntity.getPosition())
                    .critical(false)
                    .display(nearbyEntity.getInstance());

        }
    }
}

