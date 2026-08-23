package net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.ability;

import java.util.List;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.EntityDamage;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerBossMob;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerMinionMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.DamageIndicator;

abstract class SlayerAbilitySupport implements SlayerBossAbility {
    protected Task repeating(SlayerBossMob boss, int delayTicks, int repeatTicks, Runnable runnable) {
        Task task = boss.scheduler().buildTask(() -> {
            if (boss.isRemoved() || boss.getInstance() == null) {
                return;
            }
            runnable.run();
        }).delay(TaskSchedule.tick(delayTicks)).repeat(TaskSchedule.tick(repeatTicks)).schedule();
        boss.trackAbilityTask(task);
        return task;
    }

    protected Task delayed(SlayerBossMob boss, int delayTicks, Runnable runnable) {
        Task task = boss.scheduler().buildTask(() -> {
            if (boss.isRemoved() || boss.getInstance() == null) {
                return;
            }
            runnable.run();
        }).delay(TaskSchedule.tick(delayTicks)).schedule();
        boss.trackAbilityTask(task);
        return task;
    }

    protected List<SkyBlockPlayer> nearbyPlayers(SlayerBossMob boss, double range) {
        Instance instance = boss.getInstance();
        if (instance == null) {
            return List.of();
        }

        return instance.getPlayers().stream()
            .filter(player -> player instanceof SkyBlockPlayer)
            .map(player -> (SkyBlockPlayer) player)
            .filter(player -> player.getPosition().distance(boss.getPosition()) <= range)
            .toList();
    }

    protected SkyBlockPlayer owner(SlayerBossMob boss) {
        Instance instance = boss.getInstance();
        if (instance == null) {
            return null;
        }

        return instance.getPlayers().stream()
            .filter(player -> player.getUuid().equals(boss.getOwnerUuid()))
            .filter(player -> player instanceof SkyBlockPlayer)
            .map(player -> (SkyBlockPlayer) player)
            .findFirst()
            .orElse(null);
    }

    protected void trueDamage(SlayerBossMob boss, SkyBlockPlayer player, double damage, String source) {
        if (damage <= 0 || player.isRemoved()) {
            return;
        }

        player.setHealth((float) Math.max(0, player.getHealth() - damage));
        new DamageIndicator().damage((float) damage).pos(player.getPosition()).critical(false).display(player.getInstance());
        player.sendMessage("<c>{} dealt {} true damage!", source, Math.round(damage));
        player.playSound(Sound.sound(Key.key("entity.player.hurt"), Sound.Source.PLAYER, 0.8f, 0.8f), Sound.Emitter.self());
    }

    protected void damage(SlayerBossMob boss, SkyBlockPlayer player, double damage) {
        if (damage <= 0 || player.isRemoved()) {
            return;
        }
        player.damage(new EntityDamage(boss, (float) damage));
        new DamageIndicator().damage((float) damage).pos(player.getPosition()).critical(false).display(player.getInstance());
    }

    protected void heal(SlayerBossMob boss, double amount) {
        float max = (float) boss.getBaseStatistics().getOverall(net.swofty.commons.skyblock.statistics.ItemStatistic.HEALTH).doubleValue();
        boss.setHealth(Math.min(max, boss.getHealth() + (float) amount));
    }

    protected double healthRatio(SlayerBossMob boss) {
        double max = boss.getBaseStatistics().getOverall(net.swofty.commons.skyblock.statistics.ItemStatistic.HEALTH);
        if (max <= 0) {
            return 0D;
        }
        return boss.getHealth() / max;
    }

    protected void particles(SlayerBossMob boss, Particle particle, float spread, int count) {
        Instance instance = boss.getInstance();
        if (instance == null) {
            return;
        }

        Pos position = boss.getPosition().add(0, 1, 0);
        instance.getPlayers().forEach(player -> player.sendPacket(new ParticlePacket(
            particle,
            true,
            true,
            position.x(),
            position.y(),
            position.z(),
            spread,
            spread,
            spread,
            0.1f,
            count
        )));
    }

    protected void lightning(Pos position, Instance instance) {
        if (instance == null) {
            return;
        }

        LivingEntity lightning = new LivingEntity(net.minestom.server.entity.EntityType.LIGHTNING_BOLT);
        lightning.setInstance(instance, position);
        MinecraftServer.getSchedulerManager().scheduleTask(lightning::remove, TaskSchedule.seconds(1), TaskSchedule.stop());
    }

    protected SlayerMinionMob spawnMinion(SlayerBossMob boss, SlayerMinionMob.SlayerMinionProfile profile, Pos position, int lifespanTicks) {
        Instance instance = boss.getInstance();
        if (instance == null) {
            return null;
        }

        SlayerMinionMob minion = SlayerMinionMob.create(boss.getOwnerUuid(), profile);
        minion.setInstance(instance, position);
        if (lifespanTicks > 0) {
            delayed(boss, lifespanTicks, () -> {
                if (!minion.isRemoved()) {
                    minion.remove();
                }
            });
        }
        return minion;
    }

    @Override public float modifyIncomingDamage(SlayerBossMob boss, net.minestom.server.entity.damage.Damage damage, float amount) { return amount; }
    @Override public void onDamaged(SlayerBossMob boss, net.minestom.server.entity.damage.Damage damage, float appliedDamage) {}
    @Override public void onMeleeHit(SlayerBossMob boss, SkyBlockPlayer target) {}
    @Override public void onDeath(SlayerBossMob boss) {}
}
