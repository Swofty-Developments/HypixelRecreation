package net.swofty.type.skyblockgeneric.enchantment.impl;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.skyblockgeneric.collection.CustomCollectionAward;
import net.swofty.type.skyblockgeneric.enchantment.abstr.DamageEventEnchant;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EnchFromTable;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.DamageIndicator;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EnchantmentFireAspect implements Ench, EnchFromTable, DamageEventEnchant {

    public static final int[] DURATION_SECONDS = new int[]{3, 4, 4};

    public static final double[] DAMAGE_PERCENTAGES = new double[]{0.03, 0.06, 0.09};

    private static final Map<UUID, FireEffect> activeFireEffects = new ConcurrentHashMap<>();

    @Override
    public String getDescription(int level) {
        int duration = DURATION_SECONDS[level - 1];
        int damagePercent = (int) (DAMAGE_PERCENTAGES[level - 1] * 100);
        return "Ignites your enemies for <a>" + duration + "s<7>, dealing <a>" + damagePercent + "%<7> of your damage per second.";
    }

    @Override
    public ApplyLevels getLevelsToApply(@NotNull SkyBlockPlayer player) {
        HashMap<Integer, Integer> levels = new HashMap<>(Map.of(
                1, 15,
                2, 30,
                3, 0
        ));

        if (player.hasCustomCollectionAward(CustomCollectionAward.FIRE_ASPECT_DISCOUNT)) {
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
                1, 15,
                2, 30
        ));

        if (player.hasCustomCollectionAward(CustomCollectionAward.FIRE_ASPECT_DISCOUNT)) {
            levels.replaceAll((k, v) -> (int) (v * 0.75));
        }

        return new TableLevels(levels);
    }

    @Override
    public int getRequiredBookshelfPower() {
        return 0;
    }

    @Override
    public void onDamageDealt(SkyBlockPlayer player, LivingEntity target, double damageDealt, int level) {
        if (level < 1 || level > 3) return;
        if (!(target instanceof SkyBlockMob)) return;

        UUID targetId = target.getUuid();
        int durationSeconds = DURATION_SECONDS[level - 1];
        double damagePercentage = DAMAGE_PERCENTAGES[level - 1];
        double fireDamagePerSecond = damageDealt * damagePercentage;

        // Refresh duration if the mob is already on fire
        if (activeFireEffects.containsKey(targetId)) {
            FireEffect existing = activeFireEffects.get(targetId);
            if (existing != null) {
                existing.cancel();
            }
        }

        FireEffect fireEffect = new FireEffect(player, target, fireDamagePerSecond, durationSeconds);
        activeFireEffects.put(targetId, fireEffect);

        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            activeFireEffects.remove(targetId);
        }, TaskSchedule.seconds(durationSeconds), TaskSchedule.stop());
    }
    
    private static class FireEffect {
        private final SkyBlockPlayer player;
        private final LivingEntity target;
        private final double damagePerSecond;
        private int ticksRemaining;
        private boolean cancelled = false;
        
        public FireEffect(SkyBlockPlayer player, LivingEntity target, double damagePerSecond, int durationSeconds) {
            this.player = player;
            this.target = target;
            this.damagePerSecond = damagePerSecond;
            this.ticksRemaining = durationSeconds * 20;
            
            startDamageTicks();
        }
        
        private void startDamageTicks() {
            MinecraftServer.getSchedulerManager().scheduleTask(() -> {
                if (cancelled || target.isRemoved() || target.isDead()) {
                    return;
                }
                
                if (ticksRemaining <= 0) {
                    return;
                }

                target.damage(new Damage(DamageType.PLAYER_ATTACK, player, player, player.getPosition(), (float) damagePerSecond));

                new DamageIndicator()
                        .damage((float) damagePerSecond)
                        .pos(target.getPosition())
                        .critical(false)
                        .display(target.getInstance());

                Pos particlePos = target.getPosition().add(0, target.getEyeHeight(), 0);
                target.getInstance().sendGroupedPacket(new ParticlePacket(
                        Particle.FLAME,
                        false,
                        false,
                        particlePos.x(),
                        particlePos.y(),
                        particlePos.z(),
                        0.3f,
                        0.3f,
                        0.3f,
                        0.02f,
                        5
                ));
                
                ticksRemaining -= 20;
                
                if (ticksRemaining > 0) {
                    startDamageTicks();
                }
            }, TaskSchedule.tick(20), TaskSchedule.stop());
        }
        
        public void cancel() {
            this.cancelled = true;
        }
    }
}

