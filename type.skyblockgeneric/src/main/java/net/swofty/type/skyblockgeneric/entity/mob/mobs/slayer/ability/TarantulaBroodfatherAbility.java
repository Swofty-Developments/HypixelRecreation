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

final class TarantulaBroodfatherAbility extends SlayerAbilitySupport {
    private boolean web66;
    private boolean web33;
    private boolean barrage;
    private boolean conjoined;

    @Override
    public void onSpawn(SlayerBossMob boss) {
        repeating(boss, 80, 120, () -> {
            SkyBlockPlayer owner = owner(boss);
            if (owner == null) {
                return;
            }
            Pos behind = owner.getPosition().sub(owner.getPosition().direction().mul(2)).withY(owner.getPosition().y());
            boss.teleport(behind);
            owner.sendMessage("<5>Backstab!");
        });

        repeating(boss, 20, 20, () -> {
            int tier = boss.getProfile().tier().tier().number();
            if (tier >= 3 && !web66 && healthRatio(boss) <= 0.66D) {
                web66 = true;
                webOfLies(boss);
            }
            if (tier >= 3 && !web33 && healthRatio(boss) <= 0.33D) {
                web33 = true;
                webOfLies(boss);
            }
            if (tier >= 4 && !barrage && healthRatio(boss) <= (tier == 4 ? 0.50D : 0.66D)) {
                barrage = true;
                batBarrage(boss);
            }
        });
    }

    @Override
    public float modifyIncomingDamage(SlayerBossMob boss, Damage damage, float amount) {
        if (boss.getProfile().tier().tier().number() >= 5 && !conjoined && amount >= boss.getHealth()) {
            conjoined = true;
            boss.setHealth((float) boss.getProfile().tier().bossHealth());
            boss.getAttribute(net.minestom.server.entity.attribute.Attribute.MAX_HEALTH).setBaseValue(boss.getProfile().tier().bossHealth() * 2D);
            nearbyPlayers(boss, 16).forEach(player -> player.sendMessage("<5>Til Death Do Us Part! <d>The Conjoined Brood awakens!"));
            return 0F;
        }
        return amount;
    }

    @Override
    public void onMeleeHit(SlayerBossMob boss, SkyBlockPlayer target) {
        int tier = boss.getProfile().tier().tier().number();
        if (tier >= 2) {
            trueDamage(boss, target, tier * 25D, "Noxious Paralysis");
        }
    }

    private void webOfLies(SlayerBossMob boss) {
        nearbyPlayers(boss, 16).forEach(player -> player.sendMessage("<5>Web of Lies! <d>Destroy the egg sacs!"));
        heal(boss, boss.getProfile().tier().bossHealth() * 0.05D);
        int sacs = boss.getProfile().tier().tier().number() >= 5 ? 3 : 2;
        for (int i = 0; i < sacs; i++) {
            Pos pos = boss.getPosition().add(i - 1, 2, i % 2 == 0 ? 1 : -1);
            spawnMinion(boss, new SlayerMinionMob.SlayerMinionProfile(
                "Tarantula Egg Sac",
                boss.getLevel(),
                EntityType.SPIDER,
                boss.getProfile().tier().bossHealth() * 0.04D,
                boss.getProfile().tier().bossDamage() * 0.25D,
                60D,
                List.of(MobType.ARTHROPOD)
            ), pos, 220);
        }
        particles(boss, Particle.ITEM_COBWEB, 1.5f, 40);
    }

    private void batBarrage(SlayerBossMob boss) {
        nearbyPlayers(boss, 16).forEach(player -> player.sendMessage("<5>Bat Barrage!"));
        int bats = boss.getProfile().tier().tier().number() >= 5 ? 6 : 4;
        for (int i = 0; i < bats; i++) {
            spawnMinion(boss, new SlayerMinionMob.SlayerMinionProfile(
                "Brood Bat",
                boss.getLevel(),
                EntityType.BAT,
                boss.getProfile().tier().bossHealth() * 0.015D,
                boss.getProfile().tier().bossDamage() * 0.2D,
                160D,
                List.of(MobType.ARTHROPOD)
            ), boss.getPosition().add(i - 2, 1, i % 2 == 0 ? 2 : -2), 300);
        }

        repeating(boss, 20, 20, () -> nearbyPlayers(boss, 7).forEach(player ->
            trueDamage(boss, player, player.getMaxHealth() * (boss.getProfile().tier().tier().number() >= 5 ? 0.04D : 0.02D), "Bat Barrage")));
    }
}
