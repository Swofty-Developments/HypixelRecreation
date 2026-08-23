package net.swofty.type.skywarsgame.luckyblock;

import lombok.Setter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skywarsgame.game.SkywarsGame;
import net.swofty.type.skywarsgame.luckyblock.oprule.OPRuleManager;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LuckyBlock {
    private static final float EXPLOSION_DAMAGE = 6.0f;
    private static final double KNOCKBACK_STRENGTH_XZ = 20.0;
    private static final double KNOCKBACK_STRENGTH_Y = 10.0;
    private static final int EXPLOSION_RADIUS = 1;
    private static final double BLOCK_DESTROY_CHANCE = 0.5;

    private static final Duration MOB_DESPAWN_DURATION = Duration.ofSeconds(30);
    private static final double MOB_SPAWN_OFFSET = 1.0;
    private static final int MOB_AI_TARGET_RANGE = 32;

    private static final Duration ITEM_PICKUP_DELAY = Duration.ofMillis(500);

    private static final Random RANDOM = new Random();

    private final Instance instance;
    private final Map<Pos, LuckyBlockType> luckyBlockPositions = new HashMap<>();
    private final Map<Pos, Entity> blockDisplayEntities = new HashMap<>();
    @Setter
    @Nullable
    private SkywarsGame game;

    public LuckyBlock(Instance instance) {
        this.instance = instance;
    }

    @Nullable
    public SkywarsGame getGame() {
        return game;
    }

    public void placeLuckyBlock(Pos pos, LuckyBlockType type) {
        Pos blockPos = new Pos(pos.blockX(), pos.blockY(), pos.blockZ());

        instance.setBlock(blockPos, type.getGlassBlock());

        ItemStack headItem = ItemStacks.head(type.getSkullTexture(), Text.empty(), List.of()).build();
        LivingEntity displayEntity = new LivingEntity(EntityType.ITEM_DISPLAY);
        displayEntity.editEntityMeta(ItemDisplayMeta.class, meta -> {
            meta.setItemStack(headItem);
            meta.setScale(new Vec(1.2, 1.2, 1.2));
            meta.setHasNoGravity(true);
        });
        displayEntity.setInstance(instance, blockPos.add(0.5, 0.8, 0.5));

        luckyBlockPositions.put(blockPos, type);
        blockDisplayEntities.put(blockPos, displayEntity);
    }

    public boolean isLuckyBlock(Pos pos) {
        Pos blockPos = new Pos(pos.blockX(), pos.blockY(), pos.blockZ());
        return luckyBlockPositions.containsKey(blockPos);
    }

    public LuckyBlockType getLuckyBlockType(Pos pos) {
        Pos blockPos = new Pos(pos.blockX(), pos.blockY(), pos.blockZ());
        return luckyBlockPositions.get(blockPos);
    }

    public boolean isLuckyBlockMaterial(Block block) {
        return LuckyBlockType.isLuckyBlock(block);
    }

    public boolean breakLuckyBlock(SkywarsPlayer player, Pos blockPos) {
        Pos normalizedPos = new Pos(blockPos.blockX(), blockPos.blockY(), blockPos.blockZ());

        LuckyBlockType type = luckyBlockPositions.get(normalizedPos);
        if (type == null) {
            Block block = instance.getBlock(normalizedPos);
            type = LuckyBlockType.fromBlock(block);
            if (type == null) {
                return false;
            }
        }

        luckyBlockPositions.remove(normalizedPos);
        instance.setBlock(normalizedPos, Block.AIR);

        Entity displayEntity = blockDisplayEntities.remove(normalizedPos);
        if (displayEntity != null) {
            displayEntity.remove();
        }

        player.sendMessage("<7>You broke a <color:{}>{}<7> Lucky Block!",
                NamedTextColor.nearestTo(type.getColor()), type.getDisplayName());

        LuckyBlockLootTable.LuckyBlockReward reward = LuckyBlockLootTable.generateReward(type);
        applyReward(player, normalizedPos, reward);

        return true;
    }

    private void applyReward(SkywarsPlayer player, Pos blockPos, LuckyBlockLootTable.LuckyBlockReward reward) {
        player.sendMessage(reward.message());

        switch (reward.type()) {
            case ITEMS -> {
                if (reward.items() != null) {
                    for (ItemStack item : reward.items()) {
                        if (!player.getInventory().addItemStack(item)) {
                            var itemEntity = new net.minestom.server.entity.ItemEntity(item);
                            itemEntity.setInstance(instance, blockPos.add(0.5, 0.5, 0.5));
                            itemEntity.setPickupDelay(ITEM_PICKUP_DELAY);
                        }
                    }
                }
            }
            case POTION -> {
                if (reward.potion() != null) {
                    player.addEffect(reward.potion());
                }
            }
            case BAD_EFFECT -> {
                if (reward.potion() != null) {
                    player.addEffect(reward.potion());
                }
            }
            case SPAWN_MOB -> {
                spawnHostileMob(player, blockPos, reward.mobType(), reward.mobCount());
            }
            case EXPLOSION -> {
                createExplosion(player, blockPos);
            }
            case OP_RULE -> {
                activateOPRule(player);
            }
            case ENVIRONMENT_EFFECT -> {
                if (reward.environmentEffect() != null) {
                    reward.environmentEffect().apply(player, blockPos);
                }
            }
            case NOTHING -> {
            }
        }
    }

    private void activateOPRule(SkywarsPlayer player) {
        if (game == null) {
            player.sendMessage("<c>OP Rule could not be activated!");
            return;
        }

        OPRuleManager opRuleManager = game.getOpRuleManager();
        if (opRuleManager == null) {
            player.sendMessage("<c>OP Rule could not be activated!");
            return;
        }

        opRuleManager.activateRandomRule(player);
    }

    private void spawnHostileMob(SkywarsPlayer player, Pos blockPos, EntityType mobType, int count) {
        for (int i = 0; i < count; i++) {
            double offsetX = RANDOM.nextDouble() * 2 - 1;
            double offsetZ = RANDOM.nextDouble() * 2 - 1;
            Pos spawnPos = blockPos.add(offsetX, 0, offsetZ);

            EntityCreature mob = new EntityCreature(mobType);

            double vanillaSpeed = getVanillaMobSpeed(mobType);
            mob.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(vanillaSpeed);

            mob.setInstance(instance, spawnPos);

            mob.addAIGroup(
                    List.of(new MeleeAttackGoal(mob, 1.0, 20, net.minestom.server.utils.time.TimeUnit.SERVER_TICK)),
                    List.of(new ClosestEntityTarget(mob, MOB_AI_TARGET_RANGE, entity -> entity instanceof SkywarsPlayer))
            );

            mob.scheduler().buildTask(mob::remove)
                    .delay(MOB_DESPAWN_DURATION)
                    .schedule();
        }
    }

    private double getVanillaMobSpeed(EntityType type) {
        if (type == EntityType.ZOMBIE) return 0.23;
        if (type == EntityType.SKELETON) return 0.25;
        if (type == EntityType.SPIDER) return 0.3;
        if (type == EntityType.CAVE_SPIDER) return 0.3;
        if (type == EntityType.CREEPER) return 0.25;
        if (type == EntityType.SLIME) return 0.2;
        if (type == EntityType.SILVERFISH) return 0.25;
        if (type == EntityType.ENDERMAN) return 0.3;
        if (type == EntityType.WOLF) return 0.3;
        if (type == EntityType.BLAZE) return 0.23;
        if (type == EntityType.WITHER_SKELETON) return 0.35;
        if (type == EntityType.WITCH) return 0.25;
        if (type == EntityType.VINDICATOR) return 0.35;
        if (type == EntityType.PIGLIN) return 0.35;
        if (type == EntityType.PIGLIN_BRUTE) return 0.35;
        if (type == EntityType.ZOMBIFIED_PIGLIN) return 0.23;
        if (type == EntityType.IRON_GOLEM) return 0.25;
        if (type == EntityType.RAVAGER) return 0.3;
        return 0.25;
    }

    private void createExplosion(SkywarsPlayer player, Pos blockPos) {
        player.damage(Damage.fromEntity(null, EXPLOSION_DAMAGE));

        double knockbackX = RANDOM.nextDouble() * 2 - 1;
        double knockbackZ = RANDOM.nextDouble() * 2 - 1;
        player.setVelocity(player.getVelocity().add(
                knockbackX * KNOCKBACK_STRENGTH_XZ,
                KNOCKBACK_STRENGTH_Y,
                knockbackZ * KNOCKBACK_STRENGTH_XZ));

        for (int dx = -EXPLOSION_RADIUS; dx <= EXPLOSION_RADIUS; dx++) {
            for (int dy = -EXPLOSION_RADIUS; dy <= EXPLOSION_RADIUS; dy++) {
                for (int dz = -EXPLOSION_RADIUS; dz <= EXPLOSION_RADIUS; dz++) {
                    if (RANDOM.nextDouble() < BLOCK_DESTROY_CHANCE) {
                        Pos destroyPos = blockPos.add(dx, dy, dz);
                        Block block = instance.getBlock(destroyPos);
                        boolean isChest = game != null && game.getChestManager().isChestPosition(destroyPos);
                        if (!block.air() && block.solid() && !block.compare(Block.BEDROCK) && !isChest) {
                            instance.setBlock(destroyPos, Block.AIR);
                        }
                    }
                }
            }
        }

        player.sendMessage("<c>The Lucky Block exploded!");
    }

    public int getRemainingCount() {
        return luckyBlockPositions.size();
    }

    public int getRemainingCount(LuckyBlockType type) {
        return (int) luckyBlockPositions.values().stream()
                .filter(t -> t == type)
                .count();
    }

    public Map<Pos, LuckyBlockType> getRemainingBlocks() {
        return new HashMap<>(luckyBlockPositions);
    }

    public void reset() {
        for (Entity entity : blockDisplayEntities.values()) {
            entity.remove();
        }
        blockDisplayEntities.clear();
        luckyBlockPositions.clear();
    }
}
