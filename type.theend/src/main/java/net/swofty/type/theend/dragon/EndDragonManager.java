package net.swofty.type.theend.dragon;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class EndDragonManager {
    private static final List<EndDragonVariant> VARIANTS = List.of(EndDragonVariant.values());
    private static EndDragonManager instance;

    private final Instance endInstance;
    private final Pos idleCenter;
    private final Map<UUID, Float> damageByPlayer = new ConcurrentHashMap<>();

    private EnderDragonEntity dragon;
    private Task behaviorTask;
    private int eyesPlaced;
    private boolean summoning;
    private DragonState state = DragonState.IDLE;
    private long stateStarted;
    private SkyBlockPlayer target;
    private Pos attackPosition;
    private boolean attackApplied;

    private EndDragonManager(Instance endInstance, Pos altar) {
        this.endInstance = endInstance;
        this.idleCenter = altar.add(0, 40, 0);
    }

    public static void initialize(Instance endInstance, Pos altar) {
        if (instance != null) instance.cleanup();
        instance = new EndDragonManager(endInstance, altar);
    }

    public static boolean placeEye(SkyBlockPlayer player) {
        return instance != null && instance.addEye(player);
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
    }

    private boolean addEye(SkyBlockPlayer player) {
        if (hasActiveDragon() || summoning || eyesPlaced >= 8) {
            player.sendMessage("§cThere is already an active dragon summoning ritual.");
            return false;
        }

        eyesPlaced++;
        broadcast("§d§lDRAGON ALTAR §f" + eyesPlaced + "/8 Summoning Eyes placed.");
        if (eyesPlaced == 8) {
            summoning = true;
            broadcast("§5The altar begins to glow. An Ender Dragon will arrive soon!");
            MinecraftServer.getSchedulerManager().buildTask(this::spawnDragon)
                    .delay(TaskSchedule.seconds(3))
                    .schedule();
        }
        return true;
    }

    private void spawnDragon() {
        if (hasActiveDragon()) return;
        summoning = false;
        eyesPlaced = 0;
        damageByPlayer.clear();

        EndDragonVariant variant = chooseVariant();
        dragon = new EnderDragonEntity(variant);
        dragon.getAttribute(Attribute.MAX_HEALTH).setBaseValue(variant.health());
        dragon.setHealth((float) variant.health());
        dragon.setCustomNameVisible(true);
        dragon.setInstance(endInstance, idleCenter.add(45, 0, 0));
        dragon.setIdle(idleCenter, 45, variant.movementSpeed());
        state = DragonState.IDLE;
        stateStarted = System.currentTimeMillis();
        target = null;
        attackPosition = null;
        attackApplied = false;

        broadcast("§5§lTHE END §fA §d§l" + variant.name() + " DRAGON §fhas spawned!");
        behaviorTask = MinecraftServer.getSchedulerManager().buildTask(this::behaviorTick)
                .repeat(TaskSchedule.tick(1))
                .schedule();
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

    private void behaviorTick() {
        if (!hasActiveDragon()) {
            if (dragon != null) finishDragon(null);
            return;
        }

        updateName();
        switch (state) {
            case IDLE -> idleTick();
            case RUSH -> rushTick();
            case FIREBALL -> fireballTick();
            case LIGHTNING -> lightningTick();
            case RETURNING -> returningTick();
        }
    }

    private void idleTick() {
        long now = System.currentTimeMillis();
        long delay = dragon.getVariant() == EndDragonVariant.WISE ? 7_000 : 11_000;
        if (now - stateStarted < delay) return;

        target = randomTarget();
        if (target == null) {
            stateStarted = now;
            return;
        }

        attackApplied = false;
        attackPosition = target.getPosition();
        int attack = ThreadLocalRandom.current().nextInt(3);
        state = switch (attack) {
            case 0 -> DragonState.RUSH;
            case 1 -> DragonState.FIREBALL;
            default -> DragonState.LIGHTNING;
        };
        stateStarted = now;
        dragon.clearTarget();
        broadcast("§cThe dragon prepares a " + state.name().toLowerCase() + " attack!");
    }

    private void rushTick() {
        if (target == null || target.getInstance() != endInstance || System.currentTimeMillis() - stateStarted > 7_000) {
            beginReturning();
            return;
        }

        Pos currentTarget = target.getPosition();
        double dx = currentTarget.x() - dragon.getPosition().x();
        double dz = currentTarget.z() - dragon.getPosition().z();
        double length = Math.sqrt(dx * dx + dz * dz);
        Pos through = length < 0.1 ? currentTarget : currentTarget.add(dx / length * 25, 0, dz / length * 25);
        dragon.setTarget(through, dragon.getVariant().movementSpeed() * 1.8);

        if (!attackApplied && dragon.getPosition().distanceSquared(currentTarget) <= 9 * 9) {
            attackApplied = true;
            target.damage(DamageType.MOB_ATTACK, scaledDamage(2_000));
            target.takeKnockback(2.5f, length < 0.1 ? 0 : dx / length, length < 0.1 ? 0 : dz / length);
            sendParticles(currentTarget, Particle.EXPLOSION, 16);
        }

        if (attackApplied || System.currentTimeMillis() - stateStarted > 5_000) beginReturning();
    }

    private void fireballTick() {
        if (target == null || target.getInstance() != endInstance || System.currentTimeMillis() - stateStarted > 6_000) {
            beginReturning();
            return;
        }

        attackPosition = target.getPosition();
        dragon.setTarget(attackPosition.add(0, 8, 0), dragon.getVariant().movementSpeed());
        if (!attackApplied && (dragon.getPosition().distanceSquared(attackPosition) <= 12 * 12
                || System.currentTimeMillis() - stateStarted > 3_000)) {
            attackApplied = true;
            damageNearby(attackPosition, 10, scaledDamage(1_700), 1.5f);
            sendParticles(attackPosition, Particle.EXPLOSION, 24);
            beginReturning();
        }
    }

    private void lightningTick() {
        if (target == null || target.getInstance() != endInstance || System.currentTimeMillis() - stateStarted > 5_000) {
            beginReturning();
            return;
        }

        attackPosition = target.getPosition();
        dragon.setTarget(attackPosition.add(0, 12, 0), dragon.getVariant().movementSpeed() * 1.2);
        if (!attackApplied && (dragon.getPosition().distanceSquared(attackPosition) <= 15 * 15
                || System.currentTimeMillis() - stateStarted > 2_000)) {
            attackApplied = true;
            LivingEntity lightning = new LivingEntity(EntityType.LIGHTNING_BOLT);
            lightning.setInstance(endInstance, attackPosition);
            MinecraftServer.getSchedulerManager().scheduleTask(lightning::remove, TaskSchedule.seconds(1), TaskSchedule.stop());
            damageNearby(attackPosition, 5, scaledDamage(200), 0.5f);
            beginReturning();
        }
    }

    private void beginReturning() {
        state = DragonState.RETURNING;
        stateStarted = System.currentTimeMillis();
        target = null;
        attackPosition = null;
        dragon.clearTarget();
    }

    private void returningTick() {
        dragon.setTarget(idleCenter, dragon.getVariant().movementSpeed());
        if (dragon.getPosition().distanceSquared(idleCenter) <= 12 * 12) {
            dragon.setIdle(idleCenter, 45, dragon.getVariant().movementSpeed());
            state = DragonState.IDLE;
            stateStarted = System.currentTimeMillis();
        }
    }

    private float scaledDamage(double baseDamage) {
        double multiplier = dragon.getHealth() <= dragon.getAttributeValue(Attribute.MAX_HEALTH) * 0.5 ? 1.5 : 1;
        return (float) (baseDamage * multiplier);
    }

    private void damageNearby(Pos position, double radius, float damage, float knockback) {
        for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
            if (player.getInstance() != endInstance || player.getPosition().distanceSquared(position) > radius * radius) continue;
            player.damage(DamageType.EXPLOSION, damage);
            double dx = player.getPosition().x() - position.x();
            double dz = player.getPosition().z() - position.z();
            double length = Math.sqrt(dx * dx + dz * dz);
            player.takeKnockback(knockback, length < 0.1 ? 0 : dx / length, length < 0.1 ? 0 : dz / length);
        }
    }

    private SkyBlockPlayer randomTarget() {
        List<SkyBlockPlayer> players = SkyBlockGenericLoader.getLoadedPlayers().stream()
                .filter(player -> player.getInstance() == endInstance)
                .toList();
        if (players.isEmpty()) return null;
        return players.get(ThreadLocalRandom.current().nextInt(players.size()));
    }

    private void sendParticles(Pos position, Particle particle, int count) {
        ParticlePacket packet = new ParticlePacket(particle, true, true,
                position.x(), position.y(), position.z(), 2, 2, 2, 0.1f, count);
        endInstance.getPlayers().forEach(player -> player.sendPacket(packet));
    }

    private void broadcast(String message) {
        endInstance.getPlayers().forEach(player -> player.sendMessage(message));
    }

    private void updateName() {
        if (dragon == null) return;
        dragon.setCustomName(Component.text("§5§l" + dragon.getVariant().name() + " DRAGON §a"
                + Math.max(0, Math.round(dragon.getHealth())) + "§f/§a" + Math.round(dragon.getAttributeValue(Attribute.MAX_HEALTH))));
    }

    private boolean hasActiveDragon() {
        return dragon != null && !dragon.isRemoved() && !dragon.isDead();
    }

    private void finishDragon(SkyBlockPlayer fallbackKiller) {
        if (behaviorTask != null) {
            behaviorTask.cancel();
            behaviorTask = null;
        }

        UUID killerUuid = fallbackKiller == null ? damageByPlayer.entrySet().stream()
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey).orElse(null) : fallbackKiller.getUuid();
        SkyBlockPlayer killer = killerUuid == null ? null : SkyBlockGenericLoader.getFromUUID(killerUuid);
        if (killer != null) {
            killer.addAndUpdateItem(dragon.getVariant().fragment(), 2);
            killer.addAndUpdateItem(ItemType.DRAGON_SCALE);
            if (ThreadLocalRandom.current().nextDouble() < 0.1) killer.addAndUpdateItem(ItemType.DRAGON_CLAW);
            killer.sendMessage("§5§lDRAGON DEFEATED §fYou received loot from the " + dragon.getVariant().name() + " Dragon!");
        }

        if (dragon != null && !dragon.isRemoved()) dragon.remove();
        dragon = null;
        damageByPlayer.clear();
        state = DragonState.IDLE;
        target = null;
        attackPosition = null;
    }

    public void cleanup() {
        if (behaviorTask != null) behaviorTask.cancel();
        if (dragon != null && !dragon.isRemoved()) dragon.remove();
        dragon = null;
        summoning = false;
        eyesPlaced = 0;
        damageByPlayer.clear();
    }

    private enum DragonState {
        IDLE,
        RUSH,
        FIREBALL,
        LIGHTNING,
        RETURNING
    }
}
