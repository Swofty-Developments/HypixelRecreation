package net.swofty.type.skywarsgame.manager;

import net.swofty.commons.text.Text;
import net.swofty.type.generic.utility.EntityUtility;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.entity.DragonEntity;
import net.swofty.type.generic.utility.AnimatedExplosion;
import net.swofty.type.skywarsgame.game.SkywarsGame;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class DragonManager {
    private static final double IDLE_DISTANCE = 35;
    private static final double IDLE_HEIGHT = 25;
    private static final double IDLE_SPEED = 0.6;
    private static final long IDLE_DURATION_MS = 30000;
    private static final double DIVE_SPEED = 1.2;
    private static final double RETURN_SPEED = 0.6;
    private static final double ATTACK_RANGE = 6.0;
    private static final float ATTACK_DAMAGE = 10f;
    private static final double DIVE_THROUGH_DISTANCE = 40;
    private static final int EXPLOSION_RADIUS = 8;
    private static final double EXPLOSION_KNOCKBACK = 6.0;
    private static final float EXPLOSION_DAMAGE = 6f;
    private static final long EXPLOSION_INTERVAL_MS = 250;

    private final SkywarsGame game;
    private final Instance instance;
    private final Pos centerPos;
    private DragonEntity dragon;
    private Task behaviorTask;
    private Task warningTask;
    private Task spawnTask;
    private boolean dragonSpawned = false;

    private enum DragonState { IDLE, DIVING, RETURNING }
    private DragonState state = DragonState.IDLE;

    private SkywarsPlayer diveTarget = null;
    private Pos diveThroughPoint = null;
    private boolean hasHitPlayer = false;
    private long idleStartTime = 0;
    private long diveStartTime = 0;
    private long lastExplosionTime = 0;
    private Consumer<Text> broadcaster;

    public DragonManager(SkywarsGame game, Instance instance, Pos centerPos) {
        this.game = game;
        this.instance = instance;
        this.centerPos = centerPos;
    }

    public void spawnDragonNow(Consumer<Text> broadcaster) {
        spawnDragonNow(broadcaster, () -> {
        });
    }

    public void spawnDragonNow(Consumer<Text> broadcaster, Runnable onSpawn) {
        cancelScheduledSpawnTasks();
        spawnDragon(broadcaster, onSpawn);
    }

    public void scheduleDragonSpawn(Consumer<Text> broadcaster) {
        scheduleDragonSpawn(broadcaster, () -> {
        });
    }

    public void scheduleDragonSpawn(Consumer<Text> broadcaster, Runnable onSpawn) {
        if (warningTask != null || spawnTask != null || dragonSpawned) return;

        warningTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
            warningTask = null;
            if (!game.isInProgress()) return;
            broadcaster.accept(Text.of("<c>The Ender Dragon will spawn in 1 minute!"));
        }).delay(TaskSchedule.seconds(SkywarsGame.DRAGON_SPAWN_SECONDS - 60)).schedule();

        spawnTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
            spawnTask = null;
            if (!game.isInProgress()) return;
            spawnDragon(broadcaster, onSpawn);
        }).delay(TaskSchedule.seconds(SkywarsGame.DRAGON_SPAWN_SECONDS)).schedule();
    }

    private void cancelScheduledSpawnTasks() {
        if (warningTask != null) {
            warningTask.cancel();
            warningTask = null;
        }
        if (spawnTask != null) {
            spawnTask.cancel();
            spawnTask = null;
        }
    }

    private void spawnDragon(Consumer<Text> broadcaster, Runnable onSpawn) {
        if (dragonSpawned) return;
        dragonSpawned = true;
        this.broadcaster = broadcaster;

        onSpawn.run();
        broadcaster.accept(Text.of("<5>The Ender Dragon has spawned!"));

        dragon = new DragonEntity();
        dragon.getAttribute(Attribute.MAX_HEALTH).setBaseValue(200);
        dragon.setHealth(200);
        EntityUtility.nameEntity(dragon, "<5>Ender Dragon");
        dragon.setCustomNameVisible(true);

        Pos idleCenter = centerPos.add(0, IDLE_HEIGHT, 0);
        dragon.setInstance(instance, idleCenter.add(IDLE_DISTANCE, 0, 0));
        dragon.setIdle(idleCenter, IDLE_DISTANCE, IDLE_SPEED);

        state = DragonState.IDLE;
        idleStartTime = System.currentTimeMillis();

        behaviorTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (!game.isInProgress()) {
                cleanup();
                return;
            }
            if (dragon == null || dragon.isDead() || !dragonSpawned) {
                if (behaviorTask != null) {
                    behaviorTask.cancel();
                }
                return;
            }
            dragonBehaviorTick();
        }).repeat(TaskSchedule.tick(1)).schedule();
    }

    private void dragonBehaviorTick() {
        long now = System.currentTimeMillis();
        if (now - lastExplosionTime >= EXPLOSION_INTERVAL_MS) {
            lastExplosionTime = now;
            AnimatedExplosion.create(instance, dragon.getPosition(), EXPLOSION_RADIUS, EXPLOSION_KNOCKBACK, EXPLOSION_DAMAGE, null);
        }

        switch (state) {
            case IDLE -> handleIdle();
            case DIVING -> handleDiving();
            case RETURNING -> handleReturning();
        }
    }

    private void handleIdle() {
        long now = System.currentTimeMillis();
        if (now - idleStartTime > IDLE_DURATION_MS) {
            SkywarsPlayer target = findRandomPlayer();
            if (target != null) {
                diveTarget = target;
                state = DragonState.DIVING;
                diveStartTime = now;
                hasHitPlayer = false;
                dragon.clearTarget();

                Pos dragonPos = dragon.getPosition();
                Pos playerPos = target.getPosition();
                double dx = playerPos.x() - dragonPos.x();
                double dy = playerPos.y() - dragonPos.y();
                double dz = playerPos.z() - dragonPos.z();
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

                if (dist > 0.1) {
                    double nx = dx / dist;
                    double ny = dy / dist;
                    double nz = dz / dist;
                    diveThroughPoint = playerPos.add(nx * DIVE_THROUGH_DISTANCE, ny * DIVE_THROUGH_DISTANCE * 0.3, nz * DIVE_THROUGH_DISTANCE);
                } else {
                    diveThroughPoint = playerPos.add(DIVE_THROUGH_DISTANCE, 0, 0);
                }

                broadcaster.accept(Text.of("<c>The Ender Dragon is diving at {}!", target.getUsername()));
            }
        }
    }

    private void handleDiving() {
        if (diveThroughPoint == null) {
            state = DragonState.RETURNING;
            diveTarget = null;
            return;
        }

        if (System.currentTimeMillis() - diveStartTime > 8000) {
            state = DragonState.RETURNING;
            diveTarget = null;
            return;
        }

        dragon.setTarget(diveThroughPoint, DIVE_SPEED);

        if (!hasHitPlayer && diveTarget != null && !diveTarget.isEliminated() && diveTarget.isOnline()) {
            double dist = dragon.getPosition().distance(diveTarget.getPosition());
            if (dist < ATTACK_RANGE) {
                diveTarget.damage(DamageType.MOB_ATTACK, ATTACK_DAMAGE);
                broadcaster.accept(Text.of("<c>{} was struck by the Ender Dragon!", diveTarget.getUsername()));
                hasHitPlayer = true;
            }
        }

        double distToEnd = dragon.getPosition().distance(diveThroughPoint);
        if (distToEnd < 5) {
            state = DragonState.RETURNING;
            diveTarget = null;
            diveThroughPoint = null;
        }
    }

    private void handleReturning() {
        Pos idleCenter = centerPos.add(0, IDLE_HEIGHT, 0);
        dragon.setTarget(idleCenter, RETURN_SPEED);

        if (dragon.getPosition().distance(idleCenter) < 10) {
            state = DragonState.IDLE;
            diveTarget = null;
            idleStartTime = System.currentTimeMillis();
            dragon.setIdle(idleCenter, IDLE_DISTANCE, IDLE_SPEED);
        }
    }

    private SkywarsPlayer findRandomPlayer() {
        List<SkywarsPlayer> alivePlayers = game.getPlayers().stream()
                .filter(p -> !p.isEliminated() && p.isOnline())
                .toList();

        if (alivePlayers.isEmpty()) return null;
        return alivePlayers.get(ThreadLocalRandom.current().nextInt(alivePlayers.size()));
    }

    public void onDragonDamaged(UUID damagerUuid, float damage) {
        if (!game.isInProgress() || dragon == null || damage <= 0 || !Float.isFinite(damage)) return;

        float newHealth = dragon.getHealth() - damage;
        if (newHealth <= 0) {
            onDragonKilled(damagerUuid);
        } else {
            dragon.setHealth(newHealth);
        }
    }

    private void onDragonKilled(UUID killerUuid) {
        if (behaviorTask != null) {
            behaviorTask.cancel();
        }

        dragon.kill();
        game.onDragonKilled(killerUuid);
    }

    public boolean isDragonSpawned() {
        return dragonSpawned;
    }

    public void cleanup() {
        cancelScheduledSpawnTasks();
        if (behaviorTask != null) {
            behaviorTask.cancel();
            behaviorTask = null;
        }
        if (dragon != null && !dragon.isDead()) {
            dragon.remove();
        }
        dragon = null;
        dragonSpawned = false;
        state = DragonState.IDLE;
        diveTarget = null;
        diveThroughPoint = null;
        broadcaster = null;
    }
}
