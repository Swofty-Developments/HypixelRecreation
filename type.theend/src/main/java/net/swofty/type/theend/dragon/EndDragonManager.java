package net.swofty.type.theend.dragon;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.entity.metadata.item.FireballMeta;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class EndDragonManager {
    private static final List<EndDragonVariant> VARIANTS = List.of(EndDragonVariant.values());
    private static final Pos VAULT_DOOR = new Pos(-597, 12, -276);
    private static EndDragonManager instance;

    private final Instance endInstance;
    private final Pos altar;
    private final Pos pathOffset;
    private final Map<UUID, Float> damageByPlayer = new ConcurrentHashMap<>();
    private final Set<Point> occupiedFrames = ConcurrentHashMap.newKeySet();
    private final Set<UUID> bossBarViewers = ConcurrentHashMap.newKeySet();
    private final Map<Point, UUID> eyePlacers = new ConcurrentHashMap<>();
    private final Map<Point, Block> changedBlocks = new LinkedHashMap<>();
    private final Map<Point, Block> temporaryBlocks = new LinkedHashMap<>();
    private final List<Entity> animationEntities = new ArrayList<>();

    private EnderDragonEntity dragon;
    private BossBar bossBar;
    private Task behaviorTask;
    private boolean summoning;
    private long nextAbilityAt;
    private DragonState state = DragonState.PATH;
    private long stateStarted;
    private SkyBlockPlayer target;
    private Pos rushStart;
    private UUID lastDamager;
    private int fireballs;

    private EndDragonManager(Instance endInstance, Pos altar) {
        this.endInstance = endInstance;
        this.altar = altar;
        this.pathOffset = altar.add(0, 45, 0);
    }

    public static void initialize(Instance endInstance, Pos altar) {
        if (instance != null) instance.cleanup();
        instance = new EndDragonManager(endInstance, altar);
    }

    public static boolean placeEye(SkyBlockPlayer player, Point frame) {
        return instance != null && instance.addEye(player, frame);
    }

    public static boolean isDragonAlive() {
        return instance != null && instance.hasActiveDragon();
    }

    public static boolean isCurrent(EnderDragonEntity dragon) {
        return instance != null && instance.dragon == dragon;
    }

    public static void recordDamage(SkyBlockPlayer player, float damage) {
        if (instance == null || !instance.hasActiveDragon()) return;
        instance.damageByPlayer.merge(player.getUuid(), damage, Float::sum);
        instance.lastDamager = player.getUuid();
    }

    private boolean addEye(SkyBlockPlayer player, Point frame) {
        if (hasActiveDragon() || summoning) {
            player.sendMessage("§cThere is already a dragon in place!");
            return false;
        }
        if (!occupiedFrames.add(frame)) {
            player.sendMessage("§cSorry, there is already an eye here!");
            return false;
        }

        endInstance.setBlock(frame, endInstance.getBlock(frame).withProperty("eye", "true"));
        eyePlacers.put(frame, player.getUuid());
        int eyes = occupiedFrames.size();
        String countColor = eyes == 8 ? "§a" : "§e";
        for (SkyBlockPlayer viewer : players()) {
            String name = viewer.getUuid().equals(player.getUuid()) ? "You" : player.getUsername();
            viewer.sendMessage("§5⇒ " + name + " §5placed a Summoning Eye! §7(" + countColor + eyes + "§7/§a8§7)");
        }
        if (eyes == 8) beginSummoning();
        return true;
    }

    private void beginSummoning() {
        summoning = true;
        eyePlacers.values().forEach(uuid -> {
            SkyBlockPlayer placer = SkyBlockGenericLoader.getFromUUID(uuid);
            if (placer != null) placer.addAndUpdateItem(ItemType.REMNANT_OF_THE_EYE);
        });
        animateEyes();
        closeVault();
        throwPlayersFromAltar();
        MinecraftServer.getSchedulerManager().buildTask(this::animateHeart)
                .delay(TaskSchedule.seconds(4)).schedule();
        MinecraftServer.getSchedulerManager().buildTask(this::spawnDragon)
                .delay(TaskSchedule.seconds(10)).schedule();
    }

    private void animateEyes() {
        for (Point frame : occupiedFrames) {
            endInstance.setBlock(frame, endInstance.getBlock(frame).withProperty("eye", "false"));
            Entity eye = new Entity(EntityType.ITEM_DISPLAY);
            eye.editEntityMeta(ItemDisplayMeta.class, meta -> {
                meta.setItemStack(new SkyBlockItem(ItemType.SUMMONING_EYE).getItemStack());
                meta.setScale(new Vec(0.65, 0.65, 0.65));
                meta.setHasNoGravity(true);
            });
            eye.setAutoViewable(true);
            eye.setInstance(endInstance, new Pos(frame.x() + 0.5, frame.y() + 0.7, frame.z() + 0.5));
            animationEntities.add(eye);
        }
        int[] ticks = {0};
        Task[] task = {null};
        task[0] = MinecraftServer.getSchedulerManager().buildTask(() -> {
            ticks[0]++;
            for (Entity eye : List.copyOf(animationEntities)) {
                if (!eye.isRemoved()) eye.teleport(eye.getPosition().add(0, 0.05, 0));
            }
            if (ticks[0] < 80) return;
            animationEntities.forEach(Entity::remove);
            animationEntities.clear();
            task[0].cancel();
        }).repeat(TaskSchedule.tick(1)).schedule();
    }

    private void closeVault() {
        for (int y = 0; y <= 9; y++) {
            for (int z = -5; z <= 5; z++) {
                Point point = VAULT_DOOR.add(0, y, z);
                Block block = endInstance.getBlock(point);
                if (!block.isAir()) continue;
                changedBlocks.put(point, block);
                endInstance.setBlock(point, Block.BARRIER);
            }
        }
    }

    private void throwPlayersFromAltar() {
        for (SkyBlockPlayer player : players()) {
            Vec away = player.getPosition().sub(altar).asVec();
            away = new Vec(away.x(), 0, away.z());
            if (away.lengthSquared() > 35 * 35) continue;
            if (away.lengthSquared() < 0.01) away = new Vec(1, 0, 0);
            away = away.normalize();
            player.takeKnockback(4.5f, away.x(), away.z());
        }
    }

    private void animateHeart() {
        endInstance.playSound(Sound.sound(SoundEvent.ENTITY_ENDER_DRAGON_DEATH, Sound.Source.HOSTILE, 8, 1), altar);
        int minY = altar.blockY() + 20;
        int heartY = altar.blockY() + 38;
        int[] layer = {minY};
        Task[] task = {null};
        task[0] = MinecraftServer.getSchedulerManager().buildTask(() -> {
            int y = layer[0]++;
            Point point = new Pos(altar.blockX(), y, altar.blockZ());
            Block block = endInstance.getBlock(point);
            if (!block.isAir()) {
                changedBlocks.putIfAbsent(point, block);
                endInstance.setBlock(point, Block.AIR);
                sendParticles(new Pos(point.x() + 0.5, point.y() + 0.5, point.z() + 0.5), Particle.PORTAL, 3);
            }
            if (layer[0] < heartY) return;
            explodeHeart(heartY, altar.blockY() + 71);
            task[0].cancel();
        }).repeat(TaskSchedule.tick(4)).schedule();
    }

    private void explodeHeart(int minY, int maxY) {
        Pos center = new Pos(altar.x() + 0.5, (minY + maxY) / 2d, altar.z() + 0.5);
        for (int y = minY; y <= maxY; y++) {
            for (int x = -10; x <= 10; x++) {
                for (int z = -10; z <= 10; z++) {
                    Point point = new Pos(altar.blockX() + x, y, altar.blockZ() + z);
                    Block block = endInstance.getBlock(point);
                    if (block.isAir() || block == Block.BEDROCK || block == Block.BARRIER) continue;
                    changedBlocks.putIfAbsent(point, block);
                    endInstance.setBlock(point, Block.AIR);
                    launchBlock(point, block, center);
                }
            }
        }
        sendParticles(center, Particle.EXPLOSION, 60);
    }

    private void launchBlock(Point point, Block block, Pos center) {
        Entity falling = new Entity(EntityType.FALLING_BLOCK);
        falling.editEntityMeta(FallingBlockMeta.class, meta -> meta.setBlock(block));
        Pos position = new Pos(point.x() + 0.5, point.y() + 0.5, point.z() + 0.5);
        falling.setInstance(endInstance, position);
        Vec direction = position.sub(center).asVec();
        if (direction.lengthSquared() < 0.01) direction = new Vec(
                ThreadLocalRandom.current().nextDouble(-1, 1), 0,
                ThreadLocalRandom.current().nextDouble(-1, 1));
        direction = direction.normalize().mul(ThreadLocalRandom.current().nextDouble(8, 18));
        falling.setVelocity(direction.add(0, ThreadLocalRandom.current().nextDouble(5, 13), 0));
        MinecraftServer.getSchedulerManager().buildTask(falling::remove)
                .delay(TaskSchedule.seconds(6)).schedule();
    }

    private void spawnDragon() {
        if (hasActiveDragon()) return;
        summoning = false;
        damageByPlayer.clear();
        lastDamager = null;

        EndDragonVariant variant = chooseVariant();
        dragon = new EnderDragonEntity(variant);
        dragon.getAttribute(Attribute.MAX_HEALTH).setBaseValue(variant.health());
        dragon.setHealth((float) variant.health());
        dragon.setCustomNameVisible(false);
        dragon.setInstance(endInstance, pathOffset);
        dragon.setPath(dragonPath(), 0.01 * variant.movementSpeed() / 0.86);
        state = DragonState.PATH;
        nextAbilityAt = System.currentTimeMillis() + 20_000;
        bossBar = BossBar.bossBar(Component.text("§5" + displayName(variant) + " Dragon"), 1,
                BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS);
        showBossBar();
        broadcast("§5⇒ §5§l" + variant.name() + " DRAGON §5Dragon Spawned!");
        behaviorTask = MinecraftServer.getSchedulerManager().buildTask(this::behaviorTick)
                .repeat(TaskSchedule.tick(1)).schedule();
    }

    private List<Pos[]> dragonPath() {
        return List.of(
                path(0, 0, 0, -116, 5, 97, -57, 15, -62, 50, -15, -60),
                path(50, -15, -60, 63.5, -15, -68.25, 59.25, -15, 53.25, -32.75, -15, 24.75),
                path(-32.75, -15, 24.75, -45, -5, 40, 10, 10, 50, 40, 5, 20),
                path(40, 5, 20, 30, 0, 0, -10, 5, -20, -40, -5, -40),
                path(-40, -5, -40, 0, -10, -60, 30, -5, -20, 10, 5, 30),
                path(10, 5, 30, -20, 15, 40, -60, 0, 10, 0, 0, 0)
        );
    }

    private Pos[] path(double... coordinates) {
        Pos[] points = new Pos[4];
        for (int i = 0; i < 4; i++) {
            points[i] = pathOffset.add(coordinates[i * 3], coordinates[i * 3 + 1], coordinates[i * 3 + 2]);
        }
        return points;
    }

    private void behaviorTick() {
        if (!hasActiveDragon()) {
            if (dragon != null) finishDragon();
            return;
        }
        updateBossBar();
        showBossBar();
        switch (state) {
            case PATH -> pathTick();
            case RUSH -> rushTick();
            case FIREBALL -> fireballTick();
        }
    }

    private void pathTick() {
        if (System.currentTimeMillis() < nextAbilityAt) return;
        target = closestPlayer();
        if (target == null) {
            nextAbilityAt = System.currentTimeMillis() + 5_000;
            return;
        }
        stateStarted = System.currentTimeMillis();
        if (ThreadLocalRandom.current().nextInt(3) == 0) {
            state = DragonState.RUSH;
            rushStart = dragon.getPosition();
        } else {
            state = DragonState.FIREBALL;
            fireballs = 0;
            dragon.clearTarget();
        }
    }

    private void rushTick() {
        if (!validTarget() || System.currentTimeMillis() - stateStarted > 5_000) {
            resumePath();
            return;
        }
        double progress = Math.min(1, (System.currentTimeMillis() - stateStarted) / 2_000d);
        Pos destination = target.getPosition();
        Pos next = interpolate(rushStart, destination, progress);
        dragon.teleport(next);
        dragon.lookAt(destination);
        if (progress < 1 && dragon.getPosition().distanceSquared(destination) > 9) return;
        target.damage(DamageType.MOB_ATTACK, scaledDamage(11_000));
        Vec direction = target.getPosition().sub(dragon.getPosition()).asVec();
        double length = Math.max(0.01, Math.sqrt(direction.x() * direction.x() + direction.z() * direction.z()));
        target.takeKnockback(3, direction.x() / length, direction.z() / length);
        target.sendMessage("§5☬ §c" + dragon.getVariant().name() + " Dragon §dused §eRush §don you for §c"
                + Math.round(scaledDamage(11_000)) + " damage!");
        resumePath();
    }

    private void fireballTick() {
        long elapsed = System.currentTimeMillis() - stateStarted;
        sendParticles(dragon.getPosition(), Particle.FLAME, 4);
        if (elapsed < 2_000) return;
        int expected = (int) ((elapsed - 2_000) / 1_000) + 1;
        while (fireballs < Math.min(15, expected)) {
            launchFireball();
            fireballs++;
        }
        if (fireballs >= 15 && elapsed >= 17_000) resumePath();
    }

    private void launchFireball() {
        SkyBlockPlayer fireballTarget = closestPlayer();
        if (fireballTarget == null) return;
        Pos origin = dragon.getPosition().add(0, 2, 0);
        Vec direction = fireballTarget.getPosition().add(0, 1, 0).sub(origin).asVec().normalize();
        Entity fireball = new Entity(EntityType.FIREBALL);
        fireball.editEntityMeta(FireballMeta.class, meta -> meta.setHasNoGravity(true));
        fireball.setInstance(endInstance, origin);
        fireball.setVelocity(direction.mul(22));
        int[] age = {0};
        Task[] task = {null};
        task[0] = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (fireball.isRemoved()) {
                task[0].cancel();
                return;
            }
            age[0]++;
            SkyBlockPlayer nearest = players().stream().min(Comparator.comparingDouble(player ->
                    player.getPosition().distanceSquared(fireball.getPosition()))).orElse(null);
            if (nearest != null) {
                Vec aim = nearest.getPosition().add(0, 1, 0).sub(fireball.getPosition()).asVec().normalize();
                fireball.setVelocity(aim.mul(22));
                if (nearest.getPosition().distanceSquared(fireball.getPosition()) <= 9) {
                    explodeFireball(fireball);
                    task[0].cancel();
                    return;
                }
            }
            if (age[0] < 120) return;
            explodeFireball(fireball);
            task[0].cancel();
        }).repeat(TaskSchedule.tick(1)).schedule();
    }

    private void explodeFireball(Entity fireball) {
        if (fireball.isRemoved()) return;
        Pos position = fireball.getPosition();
        damageNearby(position, 6, scaledDamage(4_000), 1.8f);
        sendParticles(position, Particle.EXPLOSION, 18);
        fireball.remove();
    }

    private void resumePath() {
        state = DragonState.PATH;
        target = null;
        dragon.returnToPath(0.02 * dragon.getVariant().movementSpeed() / 0.86);
        nextAbilityAt = System.currentTimeMillis() + ThreadLocalRandom.current().nextLong(20_000, 40_001);
    }

    private EndDragonVariant chooseVariant() {
        int totalWeight = VARIANTS.stream().mapToInt(EndDragonVariant::weight).sum();
        int selected = ThreadLocalRandom.current().nextInt(totalWeight);
        for (EndDragonVariant variant : VARIANTS) {
            selected -= variant.weight();
            if (selected < 0) return variant;
        }
        return EndDragonVariant.PROTECTOR;
    }

    private void updateBossBar() {
        if (dragon == null || bossBar == null) return;
        double maximum = dragon.getAttributeValue(Attribute.MAX_HEALTH);
        bossBar.progress((float) Math.max(0, Math.min(1, dragon.getHealth() / maximum)));
    }

    private void damageNearby(Pos position, double radius, float damage, float knockback) {
        for (SkyBlockPlayer player : players()) {
            if (player.getPosition().distanceSquared(position) > radius * radius) continue;
            player.damage(DamageType.EXPLOSION, damage);
            Vec direction = player.getPosition().sub(position).asVec();
            double length = Math.max(0.01, Math.sqrt(direction.x() * direction.x() + direction.z() * direction.z()));
            player.takeKnockback(knockback, direction.x() / length, direction.z() / length);
        }
    }

    private float scaledDamage(double baseDamage) {
        double multiplier = dragon.getHealth() <= dragon.getAttributeValue(Attribute.MAX_HEALTH) * 0.5 ? 1.5 : 1;
        return (float) (baseDamage * multiplier);
    }

    private boolean validTarget() {
        return target != null && target.getInstance() == endInstance;
    }

    private SkyBlockPlayer closestPlayer() {
        return players().stream().min(Comparator.comparingDouble(player ->
                player.getPosition().distanceSquared(dragon.getPosition()))).orElse(null);
    }

    private List<SkyBlockPlayer> players() {
        return SkyBlockGenericLoader.getLoadedPlayers().stream()
                .filter(player -> player.getInstance() == endInstance).toList();
    }

    private Pos interpolate(Pos start, Pos end, double progress) {
        return new Pos(start.x() + (end.x() - start.x()) * progress,
                start.y() + (end.y() - start.y()) * progress,
                start.z() + (end.z() - start.z()) * progress);
    }

    private void sendParticles(Pos position, Particle particle, int count) {
        ParticlePacket packet = new ParticlePacket(particle, true, true,
                position.x(), position.y(), position.z(), 2, 2, 2, 0.1f, count);
        endInstance.getPlayers().forEach(player -> player.sendPacket(packet));
    }

    private void broadcast(String message) {
        endInstance.getPlayers().forEach(player -> player.sendMessage(message));
    }

    private boolean hasActiveDragon() {
        return dragon != null && !dragon.isRemoved() && !dragon.isDead();
    }

    private void finishDragon() {
        if (behaviorTask != null) behaviorTask.cancel();
        behaviorTask = null;
        SkyBlockPlayer killer = lastDamager == null ? null : SkyBlockGenericLoader.getFromUUID(lastDamager);
        if (killer != null) {
            killer.addAndUpdateItem(dragon.getVariant().fragment(), 2);
            killer.addAndUpdateItem(ItemType.DRAGON_SCALE);
            if (ThreadLocalRandom.current().nextDouble() < 0.1) killer.addAndUpdateItem(ItemType.DRAGON_CLAW);
        }
        Pos deathPosition = dragon.getPosition();
        sendVictoryMessage(killer);
        hideBossBar();
        restoreArena();
        createLootCircle(deathPosition);
        if (dragon != null && !dragon.isRemoved()) dragon.remove();
        dragon = null;
        damageByPlayer.clear();
        occupiedFrames.clear();
        eyePlacers.clear();
        lastDamager = null;
        state = DragonState.PATH;
    }

    private void createLootCircle(Pos deathPosition) {
        int groundY = deathPosition.blockY();
        while (groundY > altar.blockY() && endInstance.getBlock(deathPosition.blockX(), groundY, deathPosition.blockZ()).isAir()) {
            groundY--;
        }
        for (int degrees = 0; degrees < 360; degrees += 6) {
            double radians = Math.toRadians(degrees);
            int x = (int) Math.round(deathPosition.x() + Math.cos(radians) * 6);
            int z = (int) Math.round(deathPosition.z() + Math.sin(radians) * 6);
            int y = groundY;
            while (y > altar.blockY() && endInstance.getBlock(x, y, z).isAir()) y--;
            Point point = new Pos(x, y, z);
            temporaryBlocks.putIfAbsent(point, endInstance.getBlock(point));
            endInstance.setBlock(point, Block.TERRACOTTA);
        }
        MinecraftServer.getSchedulerManager().buildTask(this::restoreTemporaryBlocks)
                .delay(TaskSchedule.seconds(30)).schedule();
    }

    private void restoreTemporaryBlocks() {
        temporaryBlocks.forEach(endInstance::setBlock);
        temporaryBlocks.clear();
    }

    private String displayName(EndDragonVariant variant) {
        String name = variant.name().toLowerCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private void sendVictoryMessage(SkyBlockPlayer killer) {
        List<Map.Entry<UUID, Float>> damagers = damageByPlayer.entrySet().stream()
                .sorted(Map.Entry.<UUID, Float>comparingByValue().reversed()).toList();
        for (SkyBlockPlayer player : players()) {
            int rank = -1;
            for (int i = 0; i < damagers.size(); i++)
                if (damagers.get(i).getKey().equals(player.getUuid())) rank = i + 1;
            player.sendMessage("§a----------------------------------------------------");
            player.sendMessage("§6                   §l" + dragon.getVariant().name() + " DRAGON DOWN!");
            player.sendMessage("");
            player.sendMessage("§a                " + (killer == null ? "Unknown" : killer.getUsername()) + "§7 dealt the final blow.");
            player.sendMessage("");
            sendDamager(player, damagers, 1, "§e");
            sendDamager(player, damagers, 2, "§6");
            sendDamager(player, damagers, 3, "§c");
            player.sendMessage("");
            float ownDamage = damageByPlayer.getOrDefault(player.getUuid(), 0f);
            player.sendMessage("§e          Your Damage: " + rankColor(rank) + String.format("%,.0f", ownDamage)
                    + " §7(Position #" + (rank < 0 ? "N/A" : rank) + ")");
            player.sendMessage("§e               Runecrafting Experience: §d0§c (WIP)");
            player.sendMessage("");
            player.sendMessage("§a----------------------------------------------------");
        }
    }

    private void sendDamager(SkyBlockPlayer viewer, List<Map.Entry<UUID, Float>> damagers, int rank, String color) {
        String suffix = rank == 1 ? "st" : rank == 2 ? "nd" : "rd";
        if (damagers.size() < rank) {
            viewer.sendMessage(color + "          §l" + rank + suffix + " Damager§7 - N/A");
            return;
        }
        Map.Entry<UUID, Float> entry = damagers.get(rank - 1);
        SkyBlockPlayer damager = SkyBlockGenericLoader.getFromUUID(entry.getKey());
        String name = damager == null ? entry.getKey().toString().substring(0, 8) : damager.getUsername();
        viewer.sendMessage(color + "          §l" + rank + suffix + " Damager§7 - " + name + "§7 - §e"
                + String.format("%,.0f", entry.getValue()));
    }

    private String rankColor(int rank) {
        return switch (rank) {
            case 1 -> "§6";
            case 2 -> "§e";
            case 3 -> "§c";
            default -> "§7";
        };
    }

    private void hideBossBar() {
        if (bossBar == null) return;
        players().forEach(player -> player.hideBossBar(bossBar));
        bossBarViewers.clear();
        bossBar = null;
    }

    private void showBossBar() {
        if (bossBar == null) return;
        List<SkyBlockPlayer> players = players();
        bossBarViewers.retainAll(players.stream().map(SkyBlockPlayer::getUuid).toList());
        for (SkyBlockPlayer player : players) {
            if (bossBarViewers.add(player.getUuid())) player.showBossBar(bossBar);
        }
    }

    private void restoreArena() {
        changedBlocks.forEach(endInstance::setBlock);
        changedBlocks.clear();
        occupiedFrames.forEach(point -> endInstance.setBlock(point,
                endInstance.getBlock(point).withProperty("eye", "false")));
    }

    public void cleanup() {
        if (behaviorTask != null) behaviorTask.cancel();
        hideBossBar();
        animationEntities.forEach(Entity::remove);
        animationEntities.clear();
        restoreArena();
        restoreTemporaryBlocks();
        if (dragon != null && !dragon.isRemoved()) dragon.remove();
        dragon = null;
        summoning = false;
        occupiedFrames.clear();
        eyePlacers.clear();
        damageByPlayer.clear();
    }

    private enum DragonState {
        PATH,
        RUSH,
        FIREBALL
    }
}
