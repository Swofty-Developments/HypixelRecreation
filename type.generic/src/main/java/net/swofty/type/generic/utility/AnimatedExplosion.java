package net.swofty.type.generic.utility;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.swofty.type.generic.entity.ExplosionBlockEntity;

import java.util.concurrent.ThreadLocalRandom;

public class AnimatedExplosion {

    public static int create(Instance instance, Pos center, int radius, double knockbackStrength) {
        return create(instance, center, radius, knockbackStrength, 0, null);
    }

    public static int create(Instance instance, Pos center, int radius, double knockbackStrength, Player excludeFromKnockback) {
        return create(instance, center, radius, knockbackStrength, 0, excludeFromKnockback);
    }

    public static int create(Instance instance, Pos center, int radius, double knockbackStrength, float maxDamage, Player exclude) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int blockCount = 0;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z > radius * radius) continue;

                    Pos blockPos = new Pos(
                            center.blockX() + x,
                            center.blockY() + y,
                            center.blockZ() + z
                    );

                    Block block = instance.getBlock(blockPos);

                    if (block.air()) continue;
                    if (block.compare(Block.BEDROCK)) continue;
                    if (!block.solid()) continue;

                    instance.setBlock(blockPos, Block.AIR);

                    ExplosionBlockEntity entity = new ExplosionBlockEntity(block);
                    entity.setInstance(instance, blockPos.add(0.5, 0, 0.5));
                    instance.getPlayers().forEach(entity::addViewer);

                    double strength = 8.0 + random.nextDouble() * 6.0;
                    entity.launchFrom(center, strength);

                    blockCount++;
                }
            }
        }

        if (knockbackStrength > 0 || maxDamage > 0) {
            applyEffects(instance, center, radius, knockbackStrength, maxDamage, exclude);
        }

        return blockCount;
    }

    public static void applyEffects(Instance instance, Pos center, int radius, double knockbackStrength, float maxDamage, Player exclude) {
        for (Player p : instance.getPlayers()) {
            if (p.equals(exclude)) continue;

            double dist = p.getPosition().distance(center);
            if (dist <= radius && dist > 0.1) {
                double falloff = 1 - (dist / radius);

                if (knockbackStrength > 0) {
                    double kbMultiplier = falloff * knockbackStrength;
                    Vec direction = new Vec(
                            p.getPosition().x() - center.x(),
                            0.5,
                            p.getPosition().z() - center.z()
                    ).normalize().mul(kbMultiplier * 20);

                    p.setVelocity(p.getVelocity().add(direction));
                }

                if (maxDamage > 0) {
                    float damage = (float) (falloff * maxDamage);
                    p.damage(DamageType.EXPLOSION, damage);
                }
            }
        }
    }
}
