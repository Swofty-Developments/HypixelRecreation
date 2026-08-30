package net.swofty.type.skywarsgame.luckyblock.oprule;

import lombok.Getter;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.text.Text;
import net.swofty.type.skywarsgame.game.SkywarsGame;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.time.Duration;
import java.util.List;
import java.util.Random;

public class OPRuleManager {

    private static final Random RANDOM = new Random();
    private static final int TNT_RAIN_INTERVAL_TICKS = 100;
    private static final int CREEPER_SPAWN_INTERVAL_TICKS = 200;

    private final SkywarsGame game;
    private OPRule activeRule = null;
    @Getter
    private boolean opRuleUsed = false;
    private Task continuousEffectTask = null;

    public OPRuleManager(SkywarsGame game) {
        this.game = game;
    }

    public boolean activateRandomRule(SkywarsPlayer activator) {
        if (opRuleUsed) {
            activator.sendMessage("<c>An OP Rule has already been used this game!");
            return false;
        }

        OPRule rule = OPRule.random();
        return activateRule(rule, activator);
    }

    public boolean activateRule(OPRule rule, SkywarsPlayer activator) {
        if (opRuleUsed) {
            return false;
        }

        activeRule = rule;
        opRuleUsed = true;

        game.broadcastMessage(rule.getAnnouncement());
        game.broadcastMessage(Text.of("<b>{} activated the OP Rule!", activator.getUsername()));

        rule.activate(game, activator);

        startContinuousEffects();

        Logger.info("OP Rule {} activated by {} in game {}",
                rule.name(), activator.getUsername(), game.getGameId());

        return true;
    }

    private void startContinuousEffects() {
        if (activeRule == null) return;

        switch (activeRule) {
            case TNT_RAIN -> startTNTRain();
            case CREEPER_INFESTATION -> startCreeperSpawning();
            default -> {
            }
        }
    }

    private void startTNTRain() {
        continuousEffectTask = game.getInstance().scheduler()
                .buildTask(() -> {
                    if (!game.isInProgress()) {
                        stopContinuousEffects();
                        return;
                    }
                    spawnTNT();
                })
                .repeat(TaskSchedule.tick(TNT_RAIN_INTERVAL_TICKS))
                .schedule();
    }

    private void spawnTNT() {
        Instance instance = game.getInstance();
        if (instance == null) return;

        for (SkywarsPlayer player : game.getAlivePlayers()) {
            if (RANDOM.nextDouble() < 0.5) {
                Pos playerPos = player.getPosition();
                Pos tntPos = new Pos(
                        playerPos.x() + RANDOM.nextDouble() * 10 - 5,
                        playerPos.y() + 30,
                        playerPos.z() + RANDOM.nextDouble() * 10 - 5
                );

                var tnt = new net.minestom.server.entity.Entity(EntityType.TNT);
                tnt.setInstance(instance, tntPos);

                tnt.scheduler().buildTask(() -> {
                    Pos explosionPos = tnt.getPosition();
                    tnt.remove();
                    if (!game.isInProgress()) return;
                    createExplosion(explosionPos);
                }).delay(Duration.ofSeconds(4)).schedule();
            }
        }
    }

    private void createExplosion(Pos pos) {
        if (!game.isInProgress()) return;

        Instance instance = game.getInstance();
        if (instance == null) return;

        for (SkywarsPlayer player : game.getAlivePlayers()) {
            double distance = player.getPosition().distance(pos);
            if (distance < 5) {
                float damage = (float) (10 * (1 - distance / 5));
                player.damage(Damage.fromEntity(null, damage));
            }
        }

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (RANDOM.nextDouble() < 0.4) {
                        Pos blockPos = pos.add(dx, dy, dz);
                        var block = instance.getBlock(blockPos);
                        if (!block.air()
                                && !block.compare(Block.BEDROCK)
                                && !game.getChestManager().isChestPosition(blockPos)) {
                            instance.setBlock(blockPos, Block.AIR);
                        }
                    }
                }
            }
        }
    }

    private void startCreeperSpawning() {
        continuousEffectTask = game.getInstance().scheduler()
                .buildTask(() -> {
                    if (!game.isInProgress()) {
                        stopContinuousEffects();
                        return;
                    }
                    spawnCreepers();
                })
                .repeat(TaskSchedule.tick(CREEPER_SPAWN_INTERVAL_TICKS))
                .schedule();
    }

    private void spawnCreepers() {
        Instance instance = game.getInstance();
        if (instance == null) return;

        for (SkywarsPlayer player : game.getAlivePlayers()) {
            if (RANDOM.nextDouble() < 0.3) {
                Pos playerPos = player.getPosition();
                Pos creeperPos = new Pos(
                        playerPos.x() + RANDOM.nextDouble() * 16 - 8,
                        playerPos.y(),
                        playerPos.z() + RANDOM.nextDouble() * 16 - 8
                );

                EntityCreature creeper = new EntityCreature(EntityType.CREEPER);
                creeper.setInstance(instance, creeperPos);

                creeper.addAIGroup(
                        List.of(new MeleeAttackGoal(creeper, 1.0, 20,
                                net.minestom.server.utils.time.TimeUnit.SERVER_TICK)),
                        List.of(new ClosestEntityTarget(creeper, 32,
                                entity -> entity instanceof SkywarsPlayer))
                );

                creeper.scheduler().buildTask(creeper::remove)
                        .delay(Duration.ofSeconds(30))
                        .schedule();
            }
        }
    }

    public void stopContinuousEffects() {
        if (continuousEffectTask != null) {
            continuousEffectTask.cancel();
            continuousEffectTask = null;
        }
    }

    @Nullable
    public OPRule getActiveRule() {
        return activeRule;
    }

    public boolean isRuleActive(OPRule rule) {
        return activeRule == rule;
    }

    public boolean isDoubleJumpEnabled() {
        return isRuleActive(OPRule.DOUBLE_JUMPS);
    }

    public double getProjectileVelocityMultiplier() {
        return isRuleActive(OPRule.THROW_FURTHER) ? 2.0 : 1.0;
    }

    public void reset() {
        stopContinuousEffects();
        activeRule = null;
        opRuleUsed = false;
    }
}
