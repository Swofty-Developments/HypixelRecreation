package net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.ability;

import java.util.List;
import net.minestom.server.entity.EntityType;
import net.minestom.server.particle.Particle;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerBossMob;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerMinionMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

final class SvenPackmasterAbility extends SlayerAbilitySupport {
    private boolean pupsCalled;

    @Override
    public void onSpawn(SlayerBossMob boss) {
        repeating(boss, 20, 20, () -> {
            if (boss.getProfile().tier().tier().number() >= 3 && !pupsCalled && healthRatio(boss) <= 0.50D) {
                pupsCalled = true;
                callPups(boss);
            }
            particles(boss, Particle.CRIT, 0.5f, 4);
        });
    }

    @Override
    public void onMeleeHit(SlayerBossMob boss, SkyBlockPlayer target) {
        double trueDamage = switch (boss.getProfile().tier().tier()) {
            case I -> 0D;
            case II -> 10D;
            case III -> 50D;
            case IV, V -> 200D;
        };
        if (trueDamage > 0) {
            trueDamage(boss, target, trueDamage, "True Damage");
        }
    }

    private void callPups(SlayerBossMob boss) {
        int pups = boss.getProfile().tier().tier().number() >= 4 ? 6 : 5;
        double damage = boss.getProfile().tier().tier().number() >= 4 ? 220D : 90D;
        nearbyPlayers(boss, 16).forEach(player -> player.sendMessage("<f>Call the pups!"));
        for (int i = 0; i < pups; i++) {
            int index = i;
            delayed(boss, i * 10, () -> spawnMinion(boss, new SlayerMinionMob.SlayerMinionProfile(
                "Pack Pup",
                boss.getLevel(),
                EntityType.WOLF,
                boss.getProfile().tier().bossHealth() * 0.02D,
                damage,
                160D,
                List.of(MobType.WOODLAND)
            ), boss.getPosition().add(index - pups / 2D, 0, 1.5), 360));
        }
    }
}
