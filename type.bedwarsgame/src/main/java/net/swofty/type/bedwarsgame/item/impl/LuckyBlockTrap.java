package net.swofty.type.bedwarsgame.item.impl;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.tag.Tag;
import net.swofty.type.bedwarsgame.TypeBedWarsGameLoader;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LuckyBlockTrap {
    public static final Tag<String> TRAP_TAG = Tag.String("lucky_trap");
    private static final Tag<String> OWNER_TEAM_TAG = Tag.String("lucky_trap_owner_team");
    private static final Map<Instance, Map<BlockPoint, Entity>> TRAP_DISPLAYS = new ConcurrentHashMap<>();

    private LuckyBlockTrap() {
    }

    public static void place(BedWarsPlayer owner, Point point, String trap) {
        Point blockPoint = new Pos(point.blockX(), point.blockY(), point.blockZ());
        owner.getInstance().setBlock(blockPoint, Block.WHITE_CARPET
            .withTag(TypeBedWarsGameLoader.PLAYER_PLACED_TAG, true)
            .withTag(TRAP_TAG, trap)
            .withTag(OWNER_TEAM_TAG, owner.getTeamKey().name()));
        Entity display = spawnSmallCarpet(owner.getInstance(), blockPoint);
        TRAP_DISPLAYS.computeIfAbsent(owner.getInstance(), _ -> new ConcurrentHashMap<>())
            .put(BlockPoint.of(blockPoint), display);
        owner.sendMessage("<a>Placed {} Trap!", trap);
    }

    public static boolean trigger(BedWarsPlayer player, Point point, Block block) {
        String trap = block.getTag(TRAP_TAG);
        if (trap == null) {
            return false;
        }
        String ownerTeam = block.getTag(OWNER_TEAM_TAG);
        if (ownerTeam != null && player.getTeamKey() != null && ownerTeam.equals(player.getTeamKey().name())) {
            return false;
        }

        switch (trap) {
            case "Arrow Rain" -> {
                player.damage(net.minestom.server.entity.damage.Damage.fromPlayer(player, 4f));
                player.setVelocity(player.getVelocity().add(0, 8, 0));
            }
            case "Damage" -> player.damage(net.minestom.server.entity.damage.Damage.fromPlayer(player, 6f));
            case "Slow" -> player.addEffect(new Potion(PotionEffect.SLOWNESS, (byte) 2, 120));
            case "Freeze" -> {
                player.addEffect(new Potion(PotionEffect.SLOWNESS, (byte) 10, 80));
                player.addEffect(new Potion(PotionEffect.MINING_FATIGUE, (byte) 2, 80));
            }
            case "Poison" -> player.addEffect(new Potion(PotionEffect.POISON, (byte) 1, 100));
            default -> {
                return false;
            }
        }
        Map<BlockPoint, Entity> displays = TRAP_DISPLAYS.get(player.getInstance());
        Entity display = displays == null ? null : displays.remove(BlockPoint.of(point));
        if (display != null) {
            display.remove();
        }
        player.getInstance().setBlock(point, Block.AIR);
        player.sendMessage("<c>You triggered a {} Trap!", trap);
        return true;
    }

    private static Entity spawnSmallCarpet(Instance instance, Point point) {
        LivingEntity displayEntity = new LivingEntity(EntityType.BLOCK_DISPLAY);
        displayEntity.editEntityMeta(BlockDisplayMeta.class, meta -> {
            meta.setBlockState(Block.WHITE_CARPET);
            meta.setScale(new Vec(0.55, 0.55, 0.55));
            meta.setHasNoGravity(true);
        });
        displayEntity.setInstance(instance, new Pos(point.blockX() + 0.22, point.blockY() + 0.04, point.blockZ() + 0.22));
        return displayEntity;
    }

    private record BlockPoint(int x, int y, int z) {
        static BlockPoint of(Point point) {
            return new BlockPoint(point.blockX(), point.blockY(), point.blockZ());
        }
    }
}
