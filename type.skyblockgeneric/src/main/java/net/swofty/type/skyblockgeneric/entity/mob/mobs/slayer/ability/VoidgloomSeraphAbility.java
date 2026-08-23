package net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.ability;

import java.util.HashSet;
import java.util.Set;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.particle.Particle;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerBossMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

final class VoidgloomSeraphAbility extends SlayerAbilitySupport {
    private final Set<Integer> triggeredShields = new HashSet<>();
    private int shieldHits;
    private boolean glyphStarted;
    private int fixationHeads;

    @Override
    public void onSpawn(SlayerBossMob boss) {
        triggerShield(boss, 100);

        repeating(boss, 20, 20, () -> {
            SkyBlockPlayer owner = owner(boss);
            if (owner != null) {
                Pos side = owner.getPosition().add((Math.random() - 0.5D) * 3D, 0, (Math.random() - 0.5D) * 3D);
                boss.teleport(side);
            }
            nearbyPlayers(boss, 6.5).forEach(player -> trueDamage(boss, player, dissonanceDamage(boss), "Dissonance"));
            particles(boss, Particle.PORTAL, 1.2f, 25);

            if (healthRatio(boss) <= 0.66D) {
                triggerShield(boss, 66);
            }
            if (healthRatio(boss) <= 0.33D) {
                triggerShield(boss, 33);
            }
            if (boss.getProfile().tier().tier().number() >= 2 && !glyphStarted && healthRatio(boss) <= 0.50D) {
                glyphStarted = true;
                startGlyphs(boss);
            }
        });

        if (boss.getProfile().tier().tier().number() >= 3) {
            repeating(boss, 120, 120, () -> {
                if (healthRatio(boss) <= 0.33D && fixationHeads < 6) {
                    fixationHeads++;
                    nearbyPlayers(boss, 12).forEach(player -> player.sendMessage("<5>Nukekubi Fixation spawned!"));
                }
            });
            repeating(boss, 20, 20, () -> {
                if (fixationHeads > 0) {
                    double damage = Math.min(dissonanceDamage(boss), 800D * (fixationHeads * Math.pow(2, fixationHeads - 1)));
                    nearbyPlayers(boss, 8).forEach(player -> trueDamage(boss, player, damage, "Nukekubi Fixation"));
                }
            });
        }
    }

    @Override
    public float modifyIncomingDamage(SlayerBossMob boss, Damage damage, float amount) {
        if (shieldHits <= 0) {
            return amount;
        }

        shieldHits--;
        if (shieldHits == 0) {
            nearbyPlayers(boss, 16).forEach(player -> player.sendMessage("<d>Malevolent Hitshield broken!"));
        } else {
            nearbyPlayers(boss, 12).forEach(player -> player.sendMessage("<5>Hitshield: <d>{} hits left", shieldHits));
        }
        boss.teleport(boss.getPosition().add((Math.random() - 0.5D) * 1.5D, 0, (Math.random() - 0.5D) * 1.5D));
        return 0F;
    }

    private void triggerShield(SlayerBossMob boss, int threshold) {
        if (!triggeredShields.add(threshold)) {
            return;
        }

        shieldHits = switch (boss.getProfile().tier().tier()) {
            case I -> 15;
            case II -> 30;
            case III -> 60;
            case IV, V -> 100;
        };
        nearbyPlayers(boss, 16).forEach(player -> player.sendMessage("<5>Malevolent Hitshield! <d>{} hits required.", shieldHits));
    }

    private void startGlyphs(SlayerBossMob boss) {
        repeating(boss, 160, 160, () -> {
            SkyBlockPlayer owner = owner(boss);
            if (owner == null) {
                return;
            }

            Pos glyph = owner.getPosition().add((Math.random() - 0.5D) * 7D, 0, (Math.random() - 0.5D) * 7D);
            nearbyPlayers(boss, 16).forEach(player -> player.sendMessage("<e>Yang Glyph! <7>Stand on it before it detonates."));
            delayed(boss, 100, () -> {
                if (owner.getPosition().distance(glyph) > 1.5D) {
                    trueDamage(boss, owner, owner.getMaxHealth() * 1000D, "Yang Glyph");
                } else {
                    owner.sendMessage("<a>Yang Glyph shattered!");
                }
            });
        });
    }

    private double dissonanceDamage(SlayerBossMob boss) {
        return switch (boss.getProfile().tier().tier()) {
            case I -> 720D;
            case II -> 3000D;
            case III -> 7200D;
            case IV, V -> 12600D;
        };
    }
}
