package net.swofty.pvp.feature.explosion;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.property.ServerProperties;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Explosion;
import net.minestom.server.instance.ExplosionSupplier;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ExplosionPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.WeightedList;
import net.swofty.pvp.events.ExplosionEvent;
import net.swofty.pvp.feature.enchantment.EnchantmentFeature;
import net.swofty.pvp.player.CombatPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class VanillaExplosionSupplier implements ExplosionSupplier {
    private final ExplosionFeature feature;
    private final EnchantmentFeature enchantmentFeature;

    VanillaExplosionSupplier(ExplosionFeature feature, EnchantmentFeature enchantmentFeature) {
        this.feature = feature;
        this.enchantmentFeature = enchantmentFeature;
    }

    public static double getExposure(Point center, Entity entity) {
        BoundingBox box = entity.getBoundingBox();
        double xStep = 1 / (box.width() * 2 + 1);
        double yStep = 1 / (box.height() * 2 + 1);
        double zStep = 1 / (box.depth() * 2 + 1);
        double g = (1 - Math.floor(1 / xStep) * xStep) / 2;
        double h = (1 - Math.floor(1 / zStep) * zStep) / 2;
        if (xStep < 0 || yStep < 0 || zStep < 0) return 0;

        int exposedCount = 0;
        int rayCount = 0;
        double dx = 0;
        while (dx <= 1) {
            double dy = 0;
            while (dy <= 1) {
                double dz = 0;
                while (dz <= 1) {
                    double rayX = box.minX() + dx * box.width();
                    double rayY = box.minY() + dy * box.height();
                    double rayZ = box.minZ() + dz * box.depth();
                    Point point = new Vec(rayX + g, rayY, rayZ + h).add(entity.getPosition());
                    if (noBlocking(entity.getInstance(), point, center)) exposedCount++;
                    rayCount++;
                    dz += zStep;
                }
                dy += yStep;
            }
            dx += xStep;
        }

        return exposedCount / (double) rayCount;
    }

    public static boolean noBlocking(Instance instance, Point start, Point end) {
        return CollisionUtils.isLineOfSightReachingShape(instance, null, start, end, new BoundingBox(1, 1, 1), new Pos(0, 0, 0));
    }

    @Override
    public @NonNull Explosion createExplosion(float centerX, float centerY, float centerZ,
                                              float strength, @Nullable CompoundBinaryTag additionalData) {
        return new Explosion(centerX, centerY, centerZ, strength) {
            private final Map<Player, Vec> playerKnockback = new HashMap<>();

            @Override
            protected List<Point> prepare(@NonNull Instance instance) {
                List<Point> blocks = new ArrayList<>();
                ThreadLocalRandom random = ThreadLocalRandom.current();

                boolean breakBlocks = true;
                if (additionalData != null && additionalData.keySet().contains("breakBlocks"))
                    breakBlocks = additionalData.getBoolean("breakBlocks");

                // UNOFFICIAL -- START
                String requiredTag = null;
                if (additionalData != null && additionalData.keySet().contains("requiredTag")) {
                    requiredTag = additionalData.getString("requiredTag");
                }

                UUID shooterUuid = null;
                if (additionalData != null && additionalData.keySet().contains("shooter")) {
                    int[] shooterUuidParts = additionalData.getIntArray("shooter");
                    shooterUuid = new UUID(
                        ((long) shooterUuidParts[0] << 32) | (shooterUuidParts[1] & 0xFFFFFFFFL),
                        ((long) shooterUuidParts[2] << 32) | (shooterUuidParts[3] & 0xFFFFFFFFL)
                    );
                }

                String ignorePlayerTag = null;
                if (additionalData != null && additionalData.keySet().contains("ignorePlayerTag")) {
                    ignorePlayerTag = additionalData.getString("ignorePlayerTag");
                }

                int[] blacklist = null;
                if (additionalData != null && additionalData.keySet().contains("blacklist")) {
                    blacklist = additionalData.getIntArray("blacklist");
                }
                // UNOFFICIAL -- END

                if (breakBlocks) {
                    for (int x = 0; x < 16; ++x) {
                        for (int y = 0; y < 16; ++y) {
                            for (int z = 0; z < 16; ++z) {
                                if (x == 0 || x == 15 || y == 0 || y == 15 || z == 0 || z == 15) {
                                    double xLength = (float) x / 15.0F * 2.0F - 1.0F;
                                    double yLength = (float) y / 15.0F * 2.0F - 1.0F;
                                    double zLength = (float) z / 15.0F * 2.0F - 1.0F;
                                    double length = Math.sqrt(xLength * xLength + yLength * yLength + zLength * zLength);
                                    xLength /= length;
                                    yLength /= length;
                                    zLength /= length;
                                    double centerX = this.getCenterX();
                                    double centerY = this.getCenterY();
                                    double centerZ = this.getCenterZ();

                                    float strengthLeft = this.getStrength() * (0.7F + random.nextFloat() * 0.6F);
                                    for (; strengthLeft > 0.0F; strengthLeft -= 0.225F) {
                                        Vec position = new Vec(centerX, centerY, centerZ);
                                        Block block = instance.getBlock(position);

                                        if (!block.air()) {
                                            float explosionResistance = block.explosionResistance();
                                            strengthLeft -= (explosionResistance + 0.3F) * 0.3F;

                                            if (strengthLeft > 0.0F) {
                                                Vec blockPosition = position.apply(Vec.Operator.FLOOR);
                                                if (!blocks.contains(blockPosition)) {
                                                    Block blockAtPosition = instance.getBlock(blockPosition);
                                                    boolean blacklisted = false;
                                                    if (blacklist != null) {
                                                        for (int id : blacklist) {
                                                            if (id == blockAtPosition.id()) {
                                                                blacklisted = true;
                                                                break;
                                                            }
                                                        }
                                                    }

                                                    if (!blacklisted) {
                                                        if (requiredTag != null) {
                                                            if (blockAtPosition.hasTag(Tag.Boolean(requiredTag))) {
                                                                blocks.add(blockPosition);
                                                            }
                                                        } else {
                                                            blocks.add(blockPosition);
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        centerX += xLength * 0.30000001192092896D;
                                        centerY += yLength * 0.30000001192092896D;
                                        centerZ += zLength * 0.30000001192092896D;
                                    }
                                }
                            }
                        }
                    }
                }

                double strength = this.getStrength() * 2.0F;
                int minX_ = (int) Math.floor(this.getCenterX() - strength - 1.0D);
                int maxX_ = (int) Math.floor(this.getCenterX() + strength + 1.0D);
                int minY_ = (int) Math.floor(this.getCenterY() - strength - 1.0D);
                int maxY_ = (int) Math.floor(this.getCenterY() + strength + 1.0D);
                int minZ_ = (int) Math.floor(this.getCenterZ() - strength - 1.0D);
                int maxZ_ = (int) Math.floor(this.getCenterZ() + strength + 1.0D);

                int minX = Math.min(minX_, maxX_);
                int maxX = Math.max(minX_, maxX_);
                int minY = Math.min(minY_, maxY_);
                int maxY = Math.max(minY_, maxY_);
                int minZ = Math.min(minZ_, maxZ_);
                int maxZ = Math.max(minZ_, maxZ_);

                BoundingBox explosionBox = new BoundingBox(
                    maxX - minX,
                    maxY - minY,
                    maxZ - minZ
                );

                Vec centerPoint = new Vec(getCenterX(), getCenterY(), getCenterZ());

                Vec src = centerPoint.sub(0, explosionBox.height() / 2, 0);
                List<Entity> entities = new ArrayList<>(instance.getEntities().stream()
                    .filter(entity -> explosionBox.intersectEntity(src, entity))
                    .toList());

                boolean anchor = false;
                if (additionalData != null && additionalData.keySet().contains("anchor")) {
                    anchor = additionalData.getBoolean("anchor");
                }

                Damage damageObj;
                if (anchor) {
                    damageObj = new Damage(DamageType.BAD_RESPAWN_POINT, null, null, null, 0);
                } else {
                    Entity causingEntity = getCausingEntity(instance);
                    damageObj = new Damage(DamageType.PLAYER_EXPLOSION, causingEntity, causingEntity, null, 0);
                }

                // Blocks and entities list may be modified during the event call
                ExplosionEvent explosionEvent = new ExplosionEvent(instance, blocks, entities, damageObj);
                EventDispatcher.call(explosionEvent);
                if (explosionEvent.isCancelled()) return null;
                damageObj = explosionEvent.getDamageObject();

                for (Entity entity : entities) {
                    double currentStrength = entity.getPosition().distance(centerPoint) / strength;
                    if (currentStrength <= 1.0D) {
                        double dx = entity.getPosition().x() - this.getCenterX();
                        double dy = (entity.getEntityType() == EntityType.TNT ? entity.getPosition().y() :
                            entity.getPosition().y() + entity.getEyeHeight()) - this.getCenterY();
                        double dz = entity.getPosition().z() - this.getCenterZ();
                        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                        if (distance != 0.0D) {
                            dx /= distance;
                            dy /= distance;
                            dz /= distance;
                            double exposure = getExposure(centerPoint, entity);
                            currentStrength = (1.0D - currentStrength) * exposure;
                            // UNOFFICIAL - START
                            float damage = (float) ((currentStrength * currentStrength + currentStrength)
                                / 2.0D * 7.0D * strength + 1.0D);
                            damageObj.setAmount(damage);
                            // UNOFFICIAL - END
                            double knockback = currentStrength;
                            if (entity instanceof LivingEntity living) {
                                // UNOFFICIAL - START
                                if (ignorePlayerTag != null && living.hasTag(Tag.String(ignorePlayerTag))) {
                                    continue;
                                }
                                if (living.getUuid().equals(shooterUuid)) {
                                    damage *= 0.15f;
                                    damageObj.setAmount(damage);
                                } else {
                                    damage *= 0.4f;
                                    damageObj.setAmount(damage);
                                }
                                // UNOFFICIAL - END
                                if (!living.damage(damageObj)) continue;
                                knockback = enchantmentFeature.getExplosionKnockback(living, currentStrength);
                            }

                            Vec knockbackVec = new Vec(
                                dx * knockback,
                                dy * knockback,
                                dz * knockback
                            );

                            int tps = ServerProperties.SERVER_TICKS_PER_SECOND.get();
                            if (entity instanceof Player player) {
                                if (!player.getGameMode().invulnerable() && !player.isFlying()) {
                                    playerKnockback.put(player, knockbackVec);

                                    if (player instanceof CombatPlayer custom)
                                        custom.setVelocityNoUpdate(velocity -> velocity.add(knockbackVec.mul(tps)));
                                }
                            } else {
                                entity.setVelocity(entity.getVelocity().add(knockbackVec.mul(tps)));
                            }
                        }
                    }
                }

                return blocks;
            }

            @Override
            public void apply(@NotNull Instance instance) {
                List<Point> blocks = prepare(instance);
                if (blocks == null) return; // Event was cancelled
                byte[] records = new byte[3 * blocks.size()];
                for (int i = 0; i < blocks.size(); i++) {
                    final var pos = blocks.get(i);
                    // UNOFFICIAL - START - DISABLED FOR NOW
					/*if (instance.getBlock(pos).compare(Block.TNT)) {
						Entity causingEntity = getCausingEntity(instance);
						feature.primeExplosive(instance, pos, new ExplosionFeature.IgnitionCause.Explosion(causingEntity),
								ThreadLocalRandom.current().nextInt(20) + 10);
					}*/
                    // UNOFFICIAL - END
                    instance.setBlock(pos, Block.AIR);
                    final byte x = (byte) (pos.x() - Math.floor(getCenterX()));
                    final byte y = (byte) (pos.y() - Math.floor(getCenterY()));
                    final byte z = (byte) (pos.z() - Math.floor(getCenterZ()));
                    records[i * 3] = x;
                    records[i * 3 + 1] = y;
                    records[i * 3 + 2] = z;
                }

                Chunk chunk = instance.getChunkAt(getCenterX(), getCenterZ());
                if (chunk != null) {
                    for (Player player : chunk.getViewers()) {
                        Vec knockbackVec = playerKnockback.getOrDefault(player, Vec.ZERO);
                        player.sendPacket(
                            new ExplosionPacket(
                                new BlockVec(centerX, centerY, centerZ),
                                getStrength(),
                                blocks.size(),
                                knockbackVec,
                                Particle.EXPLOSION,
                                SoundEvent.ENTITY_GENERIC_EXPLODE,
                                WeightedList.of(
                                    new WeightedList.Entry<>(new ExplosionPacket.BlockParticleInfo(Particle.POOF, 0.5f, 1.0f), 1),
                                    new WeightedList.Entry<>(new ExplosionPacket.BlockParticleInfo(Particle.SMOKE, 1.0f, 1.0f), 1)
                                ),
                                true
                            )
                        );
                    }
                }
                playerKnockback.clear();

                if (additionalData != null && additionalData.keySet().contains("fire")) {
                    if (additionalData.getBoolean("fire")) {
                        ThreadLocalRandom random = ThreadLocalRandom.current();
                        for (Point point : blocks) {
                            if (random.nextInt(3) != 0
                                || !instance.getBlock(point).air()
                                || !instance.getBlock(point.sub(0, 1, 0)).solid())
                                continue;

                            instance.setBlock(point, Block.FIRE);
                        }
                    }
                }

                postSend(instance, blocks);
            }

            private @Nullable Entity getCausingEntity(Instance instance) {
                Entity causingEntity = null;
                if (additionalData != null && additionalData.keySet().contains("causingEntity")) {
                    UUID causingUuid = UUID.fromString(additionalData.getString("causingEntity"));
                    causingEntity = instance.getEntities().stream()
                        .filter(entity -> entity.getUuid().equals(causingUuid))
                        .findAny().orElse(null);
                }

                return causingEntity;
            }
        };
    }
}
