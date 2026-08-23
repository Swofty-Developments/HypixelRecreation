package net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.ability;

import java.util.List;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.particle.Particle;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerBossMob;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerMinionMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

final class InfernoDemonlordAbility extends SlayerAbilitySupport {
    private final String[] attunements = {"ASHEN", "SPIRIT", "AURIC", "CRYSTAL"};
    private int attunementIndex;
    private int shieldHits;
    private boolean split66;
    private boolean split50;
    private boolean split33;
    private boolean pillars;
    private boolean apocalypse;

    @Override
    public void onSpawn(SlayerBossMob boss) {
        repeating(boss, 20, 20, () -> {
            nearbyPlayers(boss, 6).forEach(player -> trueDamage(boss, player, immolateDamage(boss, player), "Immolate"));
            particles(boss, Particle.FLAME, 1.3f, 35);

            if (boss.getProfile().tier().tier().number() >= 3 && !split66 && healthRatio(boss) <= 0.66D) {
                split66 = true;
                demonSplit(boss);
            }
            if (!split50 && healthRatio(boss) <= 0.50D) {
                split50 = true;
                demonSplit(boss);
            }
            if (boss.getProfile().tier().tier().number() >= 3 && !split33 && healthRatio(boss) <= 0.33D) {
                split33 = true;
                demonSplit(boss);
            }
            if (!pillars && healthRatio(boss) <= (boss.getProfile().tier().tier().number() >= 4 ? 0.66D : 0.50D)) {
                pillars = true;
                firePillars(boss);
            }
            if (boss.getProfile().tier().tier().number() >= 3 && !apocalypse && healthRatio(boss) <= 0.33D) {
                apocalypse = true;
                ddrApocalypse(boss);
            }
        });
    }

    @Override
    public float modifyIncomingDamage(SlayerBossMob boss, Damage damage, float amount) {
        shieldHits++;
        if (shieldHits >= 8) {
            shieldHits = 0;
            attunementIndex = (attunementIndex + 1) % attunements.length;
            nearbyPlayers(boss, 16).forEach(player -> player.sendMessage("<6>Hellion Shield attuned to <e>{}<6>!", attunements[attunementIndex]));
        }

        if (boss.getProfile().tier().tier().number() >= 2) {
            return amount * 0.35F;
        }
        return amount;
    }

    private double immolateDamage(SlayerBossMob boss, SkyBlockPlayer player) {
        double percent = switch (boss.getProfile().tier().tier()) {
            case I -> healthRatio(boss) <= 0.33D ? 0.12D : healthRatio(boss) <= 0.66D ? 0.08D : 0.05D;
            case II -> healthRatio(boss) <= 0.33D ? 0.15D : healthRatio(boss) <= 0.66D ? 0.10D : 0.05D;
            case III -> healthRatio(boss) <= 0.33D ? 0.20D : healthRatio(boss) <= 0.66D ? 0.10D : 0.05D;
            case IV, V -> healthRatio(boss) <= 0.33D ? 0.50D : healthRatio(boss) <= 0.66D ? 0.30D : 0.20D;
        };
        return 100D + player.getMaxHealth() * percent;
    }

    private void demonSplit(SlayerBossMob boss) {
        nearbyPlayers(boss, 18).forEach(player -> player.sendMessage("<6>Demonsplit! <e>Quazii and Typhoeus join the fight."));
        spawnMinion(boss, new SlayerMinionMob.SlayerMinionProfile(
            "Quazii",
            boss.getLevel(),
            EntityType.BLAZE,
            boss.getProfile().tier().bossHealth() * 0.08D,
            boss.getProfile().tier().bossDamage() * 0.5D,
            120D,
            List.of(MobType.INFERNAL, MobType.MAGMATIC)
        ), boss.getPosition().add(2, 0, 0), 260);
        spawnMinion(boss, new SlayerMinionMob.SlayerMinionProfile(
            "Typhoeus",
            boss.getLevel(),
            EntityType.BLAZE,
            boss.getProfile().tier().bossHealth() * 0.08D,
            boss.getProfile().tier().bossDamage() * 0.5D,
            120D,
            List.of(MobType.INFERNAL, MobType.MAGMATIC)
        ), boss.getPosition().add(-2, 0, 0), 260);
        repeating(boss, 20, 20, () -> nearbyPlayers(boss, 7).forEach(player ->
            trueDamage(boss, player, scorchedAngerDamage(boss), "Scorched Anger")));
    }

    private void firePillars(SlayerBossMob boss) {
        repeating(boss, 140, 160, () -> {
            SkyBlockPlayer owner = owner(boss);
            if (owner == null) {
                return;
            }

            Pos pillar = owner.getPosition().add((Math.random() - 0.5D) * 6D, 0, (Math.random() - 0.5D) * 6D);
            nearbyPlayers(boss, 18).forEach(player -> player.sendMessage("<6>Fire Pillar! <e>Clear it before it explodes."));
            delayed(boss, 140, () -> {
                if (owner.getPosition().distance(pillar) <= 4D) {
                    trueDamage(boss, owner, owner.getMaxHealth() * 10D, "Fire Pillar");
                }
            });
        });
    }

    private void ddrApocalypse(SlayerBossMob boss) {
        nearbyPlayers(boss, 18).forEach(player -> player.sendMessage("<c>DDR Apocalypse!"));
        repeating(boss, 20, 20, () -> nearbyPlayers(boss, 8).forEach(player ->
            trueDamage(boss, player, player.getMaxHealth() * 2D, "DDR Apocalypse")));
    }

    private double scorchedAngerDamage(SlayerBossMob boss) {
        return switch (boss.getProfile().tier().tier()) {
            case I -> 1250D;
            case II -> 4000D;
            case III -> 7500D;
            case IV, V -> 15000D;
        };
    }
}
