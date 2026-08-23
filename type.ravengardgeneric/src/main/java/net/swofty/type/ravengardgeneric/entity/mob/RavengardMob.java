package net.swofty.type.ravengardgeneric.entity.mob;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.color.Color;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.ravengardgeneric.entity.animation.RavengardMobClip;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class RavengardMob extends net.minestom.server.entity.EntityCreature {
    private static final List<RavengardMob> MOBS = new CopyOnWriteArrayList<>();
    private static final TextColor BAR_FULL = TextColor.color(0x5FEC7B);
    private static final TextColor BAR_EMPTY = TextColor.color(0x3D3D3D);
    private static final int BAR_SEGMENTS = 10;
    private static final float PLAYER_DAMAGE_PER_HIT = 8f;
    private static final int ATTACK_COOLDOWN_TICKS = 30;
    private static final int FLASH_TICKS = 9;
    private static final int FLASH_COLOR = 0xFF6666;
    private static final int BASE_COLOR = 0xFFFFFF;
    private static final double KNOCKBACK_HORIZONTAL = 0.79 * 20;
    private static final double KNOCKBACK_VERTICAL = 0.50 * 20;

    private final RavengardMobClip clip;
    private final List<Entity> parts = new ArrayList<>();
    private final List<Vec> partOffsets = new ArrayList<>();
    private LivingEntity healthBar;
    private Instance instance;

    private Pos position;
    private double health;
    private final double maxHealth;
    private String currentAnimation = "idle";
    private boolean oneShot;
    private int frame;
    private float[][] lastSentRotation;
    private float[][] localRotation;
    private float[][] localTranslation;
    private int[] partDuration;
    private float lastAppliedYaw = Float.NaN;
    private int attackCooldown;
    private int flashTicksLeft;
    private boolean dying;
    private float rigYaw;
    private static final float MAX_TURN_PER_TICK = 25f;

    /**
     * The captured knight charge: a sprint at roughly three times walk speed for a handful of
     * ticks that always ends in the purple slash. Range and cooldown are not observable.
     */
    private String chargeAnimation() {
        return clip.animation("charge") != null ? "charge" : "attack_purple";
    }
    private static final double CHARGE_MIN_RANGE = 3.0;
    private static final double CHARGE_MAX_RANGE = 7.0;
    private static final double CHARGE_HIT_RANGE = 2.4;
    private static final double CHARGE_SPEED_MULTIPLIER = 3.0;
    private static final int CHARGE_TICKS = 8;
    private static final int CHARGE_COOLDOWN_TICKS = 100;
    private int chargeTicksLeft;
    private int chargeCooldown;

    public RavengardMob(RavengardMobClip clip, Pos position) {
        super(EntityType.ZOMBIE);
        this.clip = clip;
        this.position = position;
        this.rigYaw = position.yaw();
        this.maxHealth = clip.health() <= 0 ? 100 : clip.health();
        this.health = maxHealth;
        setInvisible(true);
        setSilent(true);
        getAttribute(net.minestom.server.entity.attribute.Attribute.MOVEMENT_SPEED)
                .setBaseValue(Math.max(0.15, clip.walkSpeed() * 2));
        getAttribute(net.minestom.server.entity.attribute.Attribute.MAX_HEALTH)
                .setBaseValue((float) maxHealth);
        setHealth((float) maxHealth);
        if (clip.walkSpeed() > 0) {
            addAIGroup(
                    List.of(new net.minestom.server.entity.ai.goal.MeleeAttackGoal(this, 1.8,
                            java.time.Duration.ofMillis(1500))),
                    List.of(new net.minestom.server.entity.ai.target.LastEntityDamagerTarget(this, 16),
                            new net.minestom.server.entity.ai.target.ClosestEntityTarget(this, 14,
                                    entity -> entity instanceof Player)));
        }
    }

    public String displayName() {
        StringBuilder name = new StringBuilder();
        for (String word : clip.name().split("_")) {
            if (word.isBlank()) {
                continue;
            }
            if (!name.isEmpty()) {
                name.append(' ');
            }
            name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return name.toString();
    }

    @Override
    public void attack(Entity target, boolean swingHand) {
        if (dying || attackCooldown > 0 || chargeTicksLeft > 0) return;
        attackCooldown = ATTACK_COOLDOWN_TICKS;
        List<String> options = new ArrayList<>(clip.attackAnimations());
        options.remove(chargeAnimation());
        if (!options.isEmpty()) {
            startOneShot(options.get(ThreadLocalRandom.current().nextInt(options.size())));
        }
        hit(target);
    }

    private static final float SHIELD_BLOCK_KEPT = 0.3f;

    private void hit(Entity target) {
        if (target instanceof LivingEntity living) {
            float damage = PLAYER_DAMAGE_PER_HIT;
            if (living instanceof net.swofty.type.ravengardgeneric.user.RavengardPlayer player
                    && player.isBlockingWithShield()) {
                damage *= SHIELD_BLOCK_KEPT;
                player.playSound(net.kyori.adventure.sound.Sound.sound()
                        .type(net.kyori.adventure.key.Key.key("item.shield.block"))
                        .volume(1.0f)
                        .pitch(1.0f)
                        .build());
            }
            living.damage(net.minestom.server.entity.damage.DamageType.MOB_ATTACK, damage);
            Vec away = living.getPosition().sub(position).asVec().withY(0);
            if (away.lengthSquared() > 1e-6) {
                away = away.normalize();
                living.setVelocity(new Vec(away.x() * KNOCKBACK_HORIZONTAL, KNOCKBACK_VERTICAL,
                        away.z() * KNOCKBACK_HORIZONTAL));
            }
        }
    }

    private void tickCharge() {
        if (dying || clip.animation(chargeAnimation()) == null) return;
        if (chargeCooldown > 0) chargeCooldown--;
        Entity target = getTarget();
        var speed = getAttribute(net.minestom.server.entity.attribute.Attribute.MOVEMENT_SPEED);
        double base = Math.max(0.15, clip.walkSpeed() * 2);
        if (chargeTicksLeft > 0) {
            chargeTicksLeft--;
            boolean arrived = target != null && !target.isRemoved()
                    && position.distance(target.getPosition()) <= CHARGE_HIT_RANGE;
            if (arrived || chargeTicksLeft == 0) {
                chargeTicksLeft = 0;
                speed.setBaseValue(base);
                startOneShot(chargeAnimation());
                attackCooldown = ATTACK_COOLDOWN_TICKS;
                if (arrived) {
                    hit(target);
                }
            }
            return;
        }
        if (target == null || target.isRemoved() || attackCooldown > 0 || chargeCooldown > 0) return;
        double distance = position.distance(target.getPosition());
        if (distance >= CHARGE_MIN_RANGE && distance <= CHARGE_MAX_RANGE) {
            chargeTicksLeft = CHARGE_TICKS;
            chargeCooldown = CHARGE_COOLDOWN_TICKS;
            speed.setBaseValue(base * CHARGE_SPEED_MULTIPLIER);
        }
    }

    @Override
    public boolean damage(net.minestom.server.registry.RegistryKey<net.minestom.server.entity.damage.DamageType> type, float amount) {
        if (dying) return false;
        flash();
        if (getHealth() - amount <= 0) {
            health = 0;
            updateBar();
            beginDeath();
            return true;
        }
        boolean applied = super.damage(type, amount);
        health = getHealth();
        updateBar();
        return applied;
    }

    private void beginDeath() {
        dying = true;
        getAIGroups().clear();
        getNavigator().setPathTo(null);
        if (clip.animation("death") != null) {
            startOneShot("death");
        } else {
            removeRig();
        }
    }

    private void startOneShot(String animation) {
        if (clip.animation(animation) == null) return;
        currentAnimation = animation;
        oneShot = true;
        frame = 0;
    }

    private void flash() {
        flashTicksLeft = FLASH_TICKS;
        retint(FLASH_COLOR);
    }

    private void retint(int rgb) {
        for (int i = 0; i < parts.size() && i < clip.parts().size(); i++) {
            RavengardMobClip.Part part = clip.parts().get(i);
            final int index = i;
            parts.get(index).editEntityMeta(ItemDisplayMeta.class, meta ->
                    meta.setItemStack(partItem(part, rgb)));
        }
    }

    private static ItemStack partItem(RavengardMobClip.Part part, int rgb) {
        return ItemStack.builder(Material.LEATHER_BOOTS)
                .set(DataComponents.ITEM_MODEL, part.model())
                .set(DataComponents.DYED_COLOR, new Color(rgb))
                .build();
    }

    public static List<RavengardMob> mobs() {
        return MOBS;
    }

    public void spawnMob(Instance instance) {
        this.instance = instance;
        int partCount = clip.parts().size();
        this.lastSentRotation = new float[partCount][];
        this.partDuration = new int[partCount];
        this.localRotation = new float[partCount][];
        this.localTranslation = new float[partCount][];
        for (int i = 0; i < partCount; i++) {
            RavengardMobClip.Part part = clip.parts().get(i);
            localRotation[i] = part.leftRotation() != null
                    ? part.leftRotation().clone() : new float[]{0, 0, 0, 1};
            localTranslation[i] = part.translation() != null
                    ? part.translation().clone() : new float[]{0, 0, 0};
        }
        setInstance(instance, position);
        for (RavengardMobClip.Part part : clip.parts()) {
            Entity display = new Entity(EntityType.ITEM_DISPLAY);
            display.setNoGravity(true);
            display.editEntityMeta(ItemDisplayMeta.class, meta -> {
                meta.setItemStack(partItem(part, BASE_COLOR));
                ItemDisplayMeta.DisplayContext[] contexts = ItemDisplayMeta.DisplayContext.values();
                int contextOrdinal = part.context();
                meta.setDisplayContext(contexts[contextOrdinal >= 0 && contextOrdinal < contexts.length
                        ? contextOrdinal : 0]);
                if (part.translation() != null) meta.setTranslation(vec(part.translation()));
                meta.setScale(vec(part.scale()));
                if (part.leftRotation() != null) meta.setLeftRotation(part.leftRotation());
                meta.setRightRotation(part.rightRotation());
                meta.setTransformationInterpolationDuration(1);
                meta.setPosRotInterpolationDuration(2);
                meta.setViewRange(part.viewRange());
            });
            double[] off = part.offset();
            Vec offset = new Vec(off[0], off[1], off[2]);
            display.setInstance(instance, position.add(offset));
            parts.add(display);
            partOffsets.add(offset);
        }
        java.util.Arrays.fill(partDuration, 1);

        healthBar = new LivingEntity(EntityType.TEXT_DISPLAY);
        healthBar.setNoGravity(true);
        healthBar.editEntityMeta(TextDisplayMeta.class, meta -> {
            meta.setText(barText());
            meta.setScale(new Vec(0.3, 0.3, 0.3));
            meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
            meta.setBackgroundColor(0);
            meta.setHasNoGravity(true);
        });
        healthBar.setInstance(instance, position.add(0, 2.25, 0));

        MOBS.add(this);
    }

    private void updateBar() {
        if (healthBar != null) {
            healthBar.editEntityMeta(TextDisplayMeta.class, meta -> meta.setText(barText()));
        }
    }

    private Component barText() {
        int full = (int) Math.ceil(BAR_SEGMENTS * Math.max(0, health) / maxHealth);
        return Component.text("■".repeat(full)).color(BAR_FULL)
                .append(Component.text("■".repeat(BAR_SEGMENTS - full)).color(BAR_EMPTY));
    }

    public void removeRig() {
        parts.forEach(Entity::remove);
        if (healthBar != null) healthBar.remove();
        MOBS.remove(this);
        remove();
    }

    public void tickRig() {
        if (instance == null || isRemoved()) {
            return;
        }
        Pos previous = position;
        position = getPosition();
        boolean moved = !previous.samePoint(position);
        float yaw = updateRigYaw(previous, moved);

        if (attackCooldown > 0) attackCooldown--;
        if (flashTicksLeft > 0 && --flashTicksLeft == 0) {
            retint(BASE_COLOR);
        }
        updateTarget();
        tickCharge();

        RavengardMobClip.Animation animation = clip.animation(currentAnimation);
        if (!oneShot) {
            String desired = moved && clip.animation("walk") != null ? "walk" : "idle";
            if (!desired.equals(currentAnimation)) {
                currentAnimation = desired;
                frame = 0;
                animation = clip.animation(desired);
            }
        }
        if (animation == null || animation.frames().isEmpty()) {
            return;
        }
        if (frame >= animation.frames().size()) {
            if (animation.loop()) {
                frame = 0;
            } else if (dying) {
                removeRig();
                return;
            } else {
                oneShot = false;
                currentAnimation = moved && clip.animation("walk") != null ? "walk" : "idle";
                frame = 0;
                animation = clip.animation(currentAnimation);
                if (animation == null || animation.frames().isEmpty()) return;
            }
        }

        // parts only receive keyframes when the capture animated them, so a facing change
        // re-sends every part's current pose composed with the new yaw or the rig turns piecemeal
        boolean turned = Float.isNaN(lastAppliedYaw)
                || Math.abs(wrapDegrees(yaw - lastAppliedYaw)) > 0.25f;
        lastAppliedYaw = yaw;

        Map<String, RavengardMobClip.Keyframe> keyframes = animation.frames().get(frame);
        for (int i = 0; i < parts.size() && i < clip.parts().size(); i++) {
            Entity part = parts.get(i);
            if ((moved || oneShot) && !dying) {
                part.teleport(position.add(rotateOffset(partOffsets.get(i), yaw)));
            }
            RavengardMobClip.Keyframe keyframe = keyframes.get(String.valueOf(i));
            if (keyframe != null) {
                if (keyframe.translation() != null) localTranslation[i] = keyframe.translation();
                if (keyframe.leftRotation() != null) localRotation[i] = keyframe.leftRotation();
            }
            if (keyframe == null && !turned) continue;
            final int index = i;
            final float finalYaw = yaw;
            final RavengardMobClip.Keyframe finalKeyframe = keyframe;
            part.editEntityMeta(ItemDisplayMeta.class, meta -> {
                Integer duration = finalKeyframe == null ? null : finalKeyframe.duration();
                if (duration != null && duration != partDuration[index]) {
                    partDuration[index] = duration;
                    meta.setTransformationInterpolationDuration(duration);
                }
                meta.setTransformationInterpolationStartDelta(0);
                meta.setTranslation(rotateOffset(vec(localTranslation[index]), finalYaw));
                meta.setLeftRotation(rotateRig(index, localRotation[index], finalYaw));
                if (finalKeyframe != null && finalKeyframe.scale() != null) {
                    meta.setScale(vec(finalKeyframe.scale()));
                }
            });
        }
        if ((moved || oneShot) && !dying) {
            healthBar.teleport(position.add(0, 2.25, 0));
        }

        frame++;
    }

    private static float wrapDegrees(float degrees) {
        while (degrees > 180) degrees -= 360;
        while (degrees < -180) degrees += 360;
        return degrees;
    }

    /**
     * The melee goal keeps its own cached target and never writes the creature's, so the rig
     * would have nothing to face once pathfinding stops beside the player.
     */
    private void updateTarget() {
        if (dying || clip.walkSpeed() <= 0) return;
        Entity target = getTarget();
        if (target != null && (target.isRemoved()
                || target.getPosition().distanceSquared(position) > CHASE_LOSE_RANGE * CHASE_LOSE_RANGE)) {
            setTarget(null);
            target = null;
        }
        if (target == null) {
            Player nearest = null;
            double best = CHASE_ACQUIRE_RANGE * CHASE_ACQUIRE_RANGE;
            for (Player player : instance.getPlayers()) {
                double distance = player.getPosition().distanceSquared(position);
                if (distance < best) {
                    best = distance;
                    nearest = player;
                }
            }
            if (nearest != null) {
                setTarget(nearest);
            }
        }
    }

    private static final double CHASE_ACQUIRE_RANGE = 14;
    private static final double CHASE_LOSE_RANGE = 20;

    private float updateRigYaw(Pos previous, boolean moved) {
        if (dying) return rigYaw;
        Float desired = null;
        Entity target = getTarget();
        if (target != null && !target.isRemoved()) {
            double dx = target.getPosition().x() - position.x();
            double dz = target.getPosition().z() - position.z();
            if (dx * dx + dz * dz > 1e-4) {
                desired = (float) Math.toDegrees(Math.atan2(-dx, dz));
            }
        } else if (moved) {
            double dx = position.x() - previous.x();
            double dz = position.z() - previous.z();
            if (dx * dx + dz * dz > 1e-6) {
                desired = (float) Math.toDegrees(Math.atan2(-dx, dz));
            }
        }
        if (desired != null) {
            float delta = desired - rigYaw;
            while (delta > 180) delta -= 360;
            while (delta < -180) delta += 360;
            rigYaw += Math.clamp(delta, -MAX_TURN_PER_TICK, MAX_TURN_PER_TICK);
            while (rigYaw > 180) rigYaw -= 360;
            while (rigYaw < -180) rigYaw += 360;
        }
        return rigYaw;
    }

    private static Vec vec(float[] values) {
        return new Vec(values[0], values[1], values[2]);
    }

    private static Vec rotateOffset(Vec offset, float yaw) {
        double theta = Math.toRadians(yaw);
        double sin = Math.sin(theta), cos = Math.cos(theta);
        return new Vec(offset.x() * cos + offset.z() * sin, offset.y(),
                -offset.x() * sin + offset.z() * cos);
    }

    private float[] rotateRig(int part, float[] rotation, float yaw) {
        double theta = Math.toRadians(yaw) / 2.0;
        float sin = (float) Math.sin(theta), cos = (float) Math.cos(theta);
        float rx = rotation[0], ry = rotation[1], rz = rotation[2], rw = rotation[3];
        float[] composed = {cos * rx + sin * rz, cos * ry + sin * rw,
                cos * rz - sin * rx, cos * rw - sin * ry};

        float[] last = lastSentRotation[part];
        if (last != null) {
            float dot = last[0] * composed[0] + last[1] * composed[1]
                    + last[2] * composed[2] + last[3] * composed[3];
            if (dot < 0) {
                for (int i = 0; i < 4; i++) composed[i] = -composed[i];
            }
        }
        lastSentRotation[part] = composed;
        return composed;
    }

    public static void startTicking() {
        MinecraftServer.getSchedulerManager()
                .buildTask(() -> MOBS.forEach(mob -> {
                    try {
                        mob.tickRig();
                    } catch (Exception ignored) {
                    }
                }))
                .repeat(net.minestom.server.timer.TaskSchedule.tick(1))
                .schedule();
    }
}
