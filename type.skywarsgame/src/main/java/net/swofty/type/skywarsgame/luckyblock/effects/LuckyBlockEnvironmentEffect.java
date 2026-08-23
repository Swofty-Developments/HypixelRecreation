package net.swofty.type.skywarsgame.luckyblock.effects;

import lombok.Getter;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.other.PrimedTntMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.commons.text.Text;
import net.swofty.type.skywarsgame.TypeSkywarsGameLoader;
import net.swofty.type.skywarsgame.game.SkywarsGame;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

@Getter
public enum LuckyBlockEnvironmentEffect {
    COBWEB_TRAP(
            "Cobweb Trap",
            "<f>You're stuck in cobwebs!",
            false,
            (player, pos) -> {
                Instance instance = player.getInstance();
                if (instance == null) return;

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        for (int dy = 0; dy <= 1; dy++) {
                            Pos webPos = pos.add(dx, dy, dz);
                            Block current = instance.getBlock(webPos);
                            if (current.air()) {
                                instance.setBlock(webPos, Block.COBWEB);
                            }
                        }
                    }
                }

                player.scheduler().buildTask(() -> {
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            for (int dy = 0; dy <= 1; dy++) {
                                Pos webPos = pos.add(dx, dy, dz);
                                Block current = instance.getBlock(webPos);
                                if (current.compare(Block.COBWEB)) {
                                    instance.setBlock(webPos, Block.AIR);
                                }
                            }
                        }
                    }
                }).delay(Duration.ofSeconds(5)).schedule();
            }
    ),

    ANVIL_RAIN(
            "Anvil Rain",
            "<7>Watch out above!",
            false,
            (player, pos) -> {
                Instance instance = player.getInstance();
                if (instance == null) return;

                Random random = new Random();
                for (int i = 0; i < 5; i++) {
                    int delay = i * 200;
                    player.scheduler().buildTask(() -> {
                        double offsetX = random.nextDouble() * 4 - 2;
                        double offsetZ = random.nextDouble() * 4 - 2;
                        Pos anvilPos = player.getPosition().add(offsetX, 10, offsetZ);

                        Entity fallingAnvil = new Entity(EntityType.FALLING_BLOCK);
                        fallingAnvil.setInstance(instance, anvilPos);
                        fallingAnvil.setNoGravity(false);

                        fallingAnvil.scheduler().buildTask(fallingAnvil::remove)
                                .delay(Duration.ofSeconds(5))
                                .schedule();
                    }).delay(Duration.ofMillis(delay)).schedule();
                }
            }
    ),

    LAVA_PIT(
            "Lava Pit",
            "<c>The floor is lava!",
            false,
            (player, pos) -> {
                Instance instance = player.getInstance();
                if (instance == null) return;

                Pos underPos = pos.add(0, -1, 0);
                Block underBlock = instance.getBlock(underPos);

                if (underBlock.solid() && !underBlock.compare(Block.BEDROCK)) {
                    instance.setBlock(underPos, Block.LAVA);

                    player.scheduler().buildTask(() -> {
                        Block current = instance.getBlock(underPos);
                        if (current.compare(Block.LAVA)) {
                            instance.setBlock(underPos, Block.OBSIDIAN);
                        }
                    }).delay(Duration.ofSeconds(10)).schedule();
                }

                player.damage(Damage.fromEntity(null, 4.0f));
                player.setFireTicks(60);
            }
    ),

    LIGHTNING_STRIKE(
            "Lightning Strike",
            "<e>ZAP!",
            false,
            (player, pos) -> {
                Instance instance = player.getInstance();
                if (instance == null) return;

                Entity lightning = new Entity(EntityType.LIGHTNING_BOLT);
                lightning.setInstance(instance, player.getPosition());

                lightning.scheduler().buildTask(lightning::remove)
                        .delay(Duration.ofMillis(300))
                        .schedule();

                player.damage(Damage.fromEntity(null, 5.0f));
                player.setFireTicks(20);

                player.addEffect(new Potion(PotionEffect.SLOWNESS, (byte) 1, 60));
            }
    ),

    TNT_SURROUND(
            "TNT Trap",
            "<4>Run! TNT everywhere!",
            false,
            (player, pos) -> {
                Instance instance = player.getInstance();
                if (instance == null) return;

                Random random = new Random();

                for (int i = 0; i < 4; i++) {
                    double angle = (Math.PI * 2 / 4) * i;
                    double offsetX = Math.cos(angle) * 3;
                    double offsetZ = Math.sin(angle) * 3;
                    Pos tntPos = pos.add(offsetX, 0, offsetZ);

                    Entity tnt = new Entity(EntityType.TNT);
                    tnt.setInstance(instance, tntPos);

                    if (tnt.getEntityMeta() instanceof PrimedTntMeta tntMeta) {
                        tntMeta.setFuseTime(60 + random.nextInt(20));
                    }
                }
            }
    ),

    RANDOM_TELEPORT(
            "Random Teleport",
            "<d>Whoooosh!",
            false,
            (player, pos) -> {
                Random random = new Random();

                double offsetX = random.nextDouble() * 20 - 10;
                double offsetZ = random.nextDouble() * 20 - 10;
                double offsetY = random.nextDouble() * 5;

                Pos newPos = player.getPosition().add(offsetX, offsetY, offsetZ);
                player.teleport(newPos);

                player.addEffect(new Potion(PotionEffect.RESISTANCE, (byte) 4, 40));
            }
    ),

    BRIDGE_BUILD(
            "Instant Bridge",
            "<6>A bridge appears before you!",
            true,
            (player, pos) -> {
                Instance instance = player.getInstance();
                if (instance == null) return;

                float yaw = player.getPosition().yaw();
                double radians = Math.toRadians(yaw);
                int dx = (int) Math.round(-Math.sin(radians));
                int dz = (int) Math.round(Math.cos(radians));

                Pos startPos = pos.add(0, -1, 0);
                for (int i = 1; i <= 10; i++) {
                    Pos bridgePos = startPos.add(dx * i, 0, dz * i);
                    Block current = instance.getBlock(bridgePos);
                    if (current.air()) {
                        instance.setBlock(bridgePos, Block.OAK_PLANKS);
                    }
                }
            }
    ),

    PROTECTIVE_WALL(
            "Protective Wall",
            "<7>A wall rises to protect you!",
            true,
            (player, pos) -> {
                Instance instance = player.getInstance();
                if (instance == null) return;

                int radius = 3;
                for (int angle = 0; angle < 360; angle += 15) {
                    double radians = Math.toRadians(angle);
                    int dx = (int) Math.round(Math.cos(radians) * radius);
                    int dz = (int) Math.round(Math.sin(radians) * radius);

                    for (int dy = 0; dy < 3; dy++) {
                        Pos wallPos = pos.add(dx, dy, dz);
                        Block current = instance.getBlock(wallPos);
                        if (current.air()) {
                            instance.setBlock(wallPos, Block.COBBLESTONE);
                        }
                    }
                }
            }
    ),

    LAUNCH_PAD(
            "Launch Pad",
            "<a>WEEEEE!",
            true,
            (player, pos) -> {
                player.setVelocity(player.getVelocity().add(0, 35, 0));

                player.addEffect(new Potion(PotionEffect.SLOW_FALLING, (byte) 0, 200));
            }
    ),

    ARROW_RAIN(
            "Arrow Rain",
            "<8>Arrows from the sky!",
            false,
            (player, pos) -> {
                Instance instance = player.getInstance();
                if (instance == null) return;

                Random random = new Random();

                for (int i = 0; i < 20; i++) {
                    int delay = i * 100;
                    player.scheduler().buildTask(() -> {
                        double offsetX = random.nextDouble() * 6 - 3;
                        double offsetZ = random.nextDouble() * 6 - 3;
                        Pos arrowPos = player.getPosition().add(offsetX, 15, offsetZ);

                        EntityProjectile arrow = new EntityProjectile(null, EntityType.ARROW);
                        arrow.setInstance(instance, arrowPos);
                        arrow.setVelocity(arrow.getVelocity().add(0, -30, 0));

                        arrow.scheduler().buildTask(arrow::remove)
                                .delay(Duration.ofSeconds(5))
                                .schedule();
                    }).delay(Duration.ofMillis(delay)).schedule();
                }
            }
    ),

    BLESSING(
            "Lucky Blessing",
            "<d>You feel incredibly lucky!",
            true,
            (player, pos) -> {
                player.addEffect(new Potion(PotionEffect.SPEED, (byte) 1, 600));
                player.addEffect(new Potion(PotionEffect.STRENGTH, (byte) 0, 600));
                player.addEffect(new Potion(PotionEffect.JUMP_BOOST, (byte) 1, 600));
                player.addEffect(new Potion(PotionEffect.REGENERATION, (byte) 0, 200));
            }
    ),

    CURSE(
            "Cursed",
            "<5>You feel incredibly unlucky!",
            false,
            (player, pos) -> {
                player.addEffect(new Potion(PotionEffect.SLOWNESS, (byte) 1, 200));
                player.addEffect(new Potion(PotionEffect.WEAKNESS, (byte) 0, 200));
                player.addEffect(new Potion(PotionEffect.MINING_FATIGUE, (byte) 1, 200));
                player.addEffect(new Potion(PotionEffect.HUNGER, (byte) 1, 200));
            }
    ),

    FIREWORKS(
            "Fireworks",
            "<b>Celebration time!",
            true,
            (player, pos) -> {
                Instance instance = player.getInstance();
                if (instance == null) return;

                for (int i = 0; i < 5; i++) {
                    Entity firework = new Entity(EntityType.FIREWORK_ROCKET);
                    firework.setInstance(instance, player.getPosition());
                    firework.setVelocity(new Vec(0, 20, 0));

                    firework.scheduler().buildTask(firework::remove)
                            .delay(Duration.ofSeconds(3))
                            .schedule();
                }
            }
    ),

    INSTA_HOLE(
            "Insta Hole",
            "<8>The ground disappears beneath you!",
            false,
            (player, pos) -> {
                Instance instance = player.getInstance();
                if (instance == null) return;

                SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(player);
                int startY = (int) pos.y();
                for (int y = startY; y >= 0; y--) {
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            Pos blockPos = new Pos(pos.x() + dx, y, pos.z() + dz);
                            Block current = instance.getBlock(blockPos);
                            boolean isChest = game != null && game.getChestManager().isChestPosition(blockPos);
                            if (!current.compare(Block.BEDROCK) && !current.air() && !isChest) {
                                instance.setBlock(blockPos, Block.AIR);
                            }
                        }
                    }
                }
            }
    ),

    IRON_POLE(
            "Iron Pole",
            "<f>A tower of iron rises!",
            true,
            (player, pos) -> {
                Instance instance = player.getInstance();
                if (instance == null) return;

                for (int i = 1; i <= 10; i++) {
                    Pos polePos = pos.add(0, i, 0);
                    instance.setBlock(polePos, Block.IRON_BLOCK);
                }
            }
    ),

    NINJA_MODE(
            "Ninja Mode",
            "<8>You vanish into the shadows!",
            true,
            (player, pos) -> {
                player.addEffect(new Potion(PotionEffect.INVISIBILITY, (byte) 127, 600));
            }
    ),

    SWAPPING_PLACE(
            "Swapping Places",
            "<d>You're about to swap places with someone!",
            false,
            (player, pos) -> {
                SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(player);
                if (game == null) return;

                List<SkywarsPlayer> alivePlayers = game.getAlivePlayers().stream()
                        .filter(p -> !p.equals(player))
                        .toList();

                if (alivePlayers.isEmpty()) return;

                Random random = new Random();
                SkywarsPlayer target = alivePlayers.get(random.nextInt(alivePlayers.size()));

                player.sendMessage("<e>Swapping in 3 seconds...");
                target.sendMessage("<e>You're being swapped in 3 seconds!");

                Pos playerPos = player.getPosition();
                Pos targetPos = target.getPosition();

                player.scheduler().buildTask(() -> {
                    player.teleport(targetPos);
                    target.teleport(playerPos);
                }).delay(Duration.ofSeconds(3)).schedule();
            }
    ),

    SPAWN_LARGE_TREE(
            "Spawn Large Tree",
            "<2>A mighty oak grows!",
            true,
            (player, pos) -> {
                Instance instance = player.getInstance();
                if (instance == null) return;

                for (int i = 1; i <= 5; i++) {
                    Pos trunkPos = pos.add(0, i, 0);
                    instance.setBlock(trunkPos, Block.OAK_LOG);
                }

                Pos topPos = pos.add(0, 5, 0);
                int leafRadius = 2;
                for (int dx = -leafRadius; dx <= leafRadius; dx++) {
                    for (int dy = 0; dy <= 3; dy++) {
                        for (int dz = -leafRadius; dz <= leafRadius; dz++) {
                            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                            if (distance <= leafRadius + 0.5 && distance > 0) {
                                Pos leafPos = topPos.add(dx, dy, dz);
                                Block current = instance.getBlock(leafPos);
                                if (current.air()) {
                                    instance.setBlock(leafPos, Block.OAK_LEAVES);
                                }
                            }
                        }
                    }
                }
            }
    ),

    KABOOM(
            "KABOOM",
            "<c>Everyone goes flying!",
            false,
            (player, pos) -> {
                SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(player);
                if (game == null) return;

                for (SkywarsPlayer alivePlayer : game.getAlivePlayers()) {
                    alivePlayer.setVelocity(alivePlayer.getVelocity().add(0, 40, 0));
                    alivePlayer.addEffect(new Potion(PotionEffect.SLOW_FALLING, (byte) 0, 200));
                }
            }
    ),

    INSTANT_WALL(
            "Instant Wall",
            "<c>A brick wall rises before you!",
            true,
            (player, pos) -> {
                Instance instance = player.getInstance();
                if (instance == null) return;

                float yaw = player.getPosition().yaw();
                double radians = Math.toRadians(yaw);
                int forwardX = (int) Math.round(-Math.sin(radians));
                int forwardZ = (int) Math.round(Math.cos(radians));

                int perpX = -forwardZ;
                int perpZ = forwardX;

                Pos wallCenter = pos.add(forwardX * 2, 0, forwardZ * 2);

                for (int w = -2; w <= 2; w++) {
                    for (int h = 0; h < 4; h++) {
                        Pos wallPos = wallCenter.add(perpX * w, h, perpZ * w);
                        Block current = instance.getBlock(wallPos);
                        if (current.air()) {
                            instance.setBlock(wallPos, Block.BRICKS);
                        }
                    }
                }
            }
    );

    private static final Random RANDOM = new Random();

    private final String displayName;
    private final String message;
    private final boolean isGood;
    private final BiConsumer<SkywarsPlayer, Pos> effectAction;

    LuckyBlockEnvironmentEffect(String displayName, String message,
                                boolean isGood, BiConsumer<SkywarsPlayer, Pos> effectAction) {
        this.displayName = displayName;
        this.message = message;
        this.isGood = isGood;
        this.effectAction = effectAction;
    }

    public void apply(SkywarsPlayer player, Pos position) {
        player.sendMessage(message);
        effectAction.accept(player, position);
    }

    public Text getMessageText() {
        return Text.of(message);
    }

    public static LuckyBlockEnvironmentEffect randomGood() {
        LuckyBlockEnvironmentEffect[] goodEffects = java.util.Arrays.stream(values())
                .filter(e -> e.isGood)
                .toArray(LuckyBlockEnvironmentEffect[]::new);
        return goodEffects[RANDOM.nextInt(goodEffects.length)];
    }

    public static LuckyBlockEnvironmentEffect randomBad() {
        LuckyBlockEnvironmentEffect[] badEffects = java.util.Arrays.stream(values())
                .filter(e -> !e.isGood)
                .toArray(LuckyBlockEnvironmentEffect[]::new);
        return badEffects[RANDOM.nextInt(badEffects.length)];
    }

    public static LuckyBlockEnvironmentEffect random() {
        LuckyBlockEnvironmentEffect[] effects = values();
        return effects[RANDOM.nextInt(effects.length)];
    }
}
