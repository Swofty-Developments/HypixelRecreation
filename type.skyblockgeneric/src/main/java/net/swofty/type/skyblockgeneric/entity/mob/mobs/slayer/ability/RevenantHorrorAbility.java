package net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.ability;

import net.minestom.server.particle.Particle;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerBossMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

final class RevenantHorrorAbility extends SlayerAbilitySupport {
    private boolean enrage;
    private boolean thermonuclear;
    private long spawnedAt;

    @Override
    public void onSpawn(SlayerBossMob boss) {
        spawnedAt = System.currentTimeMillis();

        repeating(boss, 50, 50, () -> {
            double damage = lifeDrainDamage(boss);
            nearbyPlayers(boss, 5.5).forEach(player -> trueDamage(boss, player, damage, "Life Drain"));
            particles(boss, Particle.SOUL, 0.7f, 12);
        });

        if (boss.getProfile().tier().tier().number() >= 3) {
            repeating(boss, 20, 20, () -> {
                double damage = pestilenceDamage(boss) * (enrage ? 6D : 1D);
                nearbyPlayers(boss, 8.5).forEach(player -> trueDamage(boss, player, damage, "Pestilence"));
                particles(boss, Particle.ASH, 1.2f, 20);
            });
        }

        if (boss.getProfile().tier().tier().number() >= 4) {
            repeating(boss, 800, 800, () -> {
                enrage = true;
                nearbyPlayers(boss, 16).forEach(player -> player.sendMessage("<c>{} is enraged!", boss.getDisplayName()));
                delayed(boss, 240, () -> enrage = false);
            });
        }

        if (boss.getProfile().tier().tier().number() >= 5) {
            repeating(boss, 40, 40, () -> heal(boss, boss.getProfile().tier().bossHealth() * 0.015D));
            repeating(boss, 160, 160, () -> nearbyPlayers(boss, 6).forEach(player -> {
                player.sendMessage("<c>Explosive Assault!");
                trueDamage(boss, player, 4800, "Explosive Assault");
            }));
        }
    }

    @Override
    public void onDamaged(SlayerBossMob boss, net.minestom.server.entity.damage.Damage damage, float appliedDamage) {
        if (boss.getProfile().tier().tier().number() < 5 || thermonuclear || healthRatio(boss) > 0.33D) {
            return;
        }

        thermonuclear = true;
        nearbyPlayers(boss, 16).forEach(player -> player.sendMessage("<4><l>THERMONUCLEAR! </l><c>Move away!"));
        delayed(boss, 45, () -> {
            nearbyPlayers(boss, 7).forEach(player -> {
                lightning(player.getPosition(), player.getInstance());
                trueDamage(boss, player, 24000, "Thermonuclear");
            });
        });
    }

    @Override
    public void onMeleeHit(SlayerBossMob boss, SkyBlockPlayer target) {
        if (enrage) {
            trueDamage(boss, target, boss.getProfile().tier().bossDamage() * 0.5D, "Enrage");
        }
    }

    private double lifeDrainDamage(SlayerBossMob boss) {
        return switch (boss.getProfile().tier().tier()) {
            case I -> 15D;
            case II -> 40D;
            case III -> 100D;
            case IV -> 260D;
            case V -> 900D;
        };
    }

    private double pestilenceDamage(SlayerBossMob boss) {
        return switch (boss.getProfile().tier().tier()) {
            case I, II -> 0D;
            case III -> 35D;
            case IV -> 120D;
            case V -> 400D;
        };
    }
}
