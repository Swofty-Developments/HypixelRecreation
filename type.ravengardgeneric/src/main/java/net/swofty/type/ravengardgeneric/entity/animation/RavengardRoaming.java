package net.swofty.type.ravengardgeneric.entity.animation;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;

import java.util.concurrent.ThreadLocalRandom;

public class RavengardRoaming {
    public static final int MIN_PAUSE_TICKS = 40;
    public static final int MAX_PAUSE_TICKS = 160;

    private static final double ARRIVAL_DISTANCE = 0.15;
    private static final double YAW_SMOOTHING = 0.25;
    private static final float MAX_STRAFE_DEGREES = 35f;
    private static final int IDLE_LOOK_MIN_TICKS = 60;
    private static final int IDLE_LOOK_MAX_TICKS = 140;
    private static final float IDLE_LOOK_SPREAD = 70f;

    private final Pos home;
    private final double radius;
    private final double speed;
    private final int minPauseTicks;
    private final int maxPauseTicks;

    private Vec target;
    private int pauseRemaining;
    private float yaw;
    private float lookYaw;
    private int lookRemaining;

    public RavengardRoaming(Pos home, double radius, double speed, int minPauseTicks, int maxPauseTicks) {
        this.home = home;
        this.radius = radius;
        this.speed = speed;
        this.minPauseTicks = minPauseTicks;
        this.maxPauseTicks = maxPauseTicks;
        this.yaw = home.yaw();
        this.pauseRemaining = randomPause();
    }

    public boolean isMoving() {
        return target != null && pauseRemaining <= 0;
    }

    public float yaw() {
        return yaw;
    }

    private net.minestom.server.instance.Instance instance;

    public void setInstance(net.minestom.server.instance.Instance instance) {
        this.instance = instance;
    }

    private boolean isWalkable(double x, double y, double z) {
        if (instance == null) {
            return true;
        }
        try {
            int bx = (int) Math.floor(x);
            int by = (int) Math.floor(y);
            int bz = (int) Math.floor(z);
            return !instance.getBlock(bx, by, bz).solid()
                    && !instance.getBlock(bx, by + 1, bz).solid();
        } catch (Exception exception) {
            return false;
        }
    }

    public Vec advance(Vec current) {
        if (pauseRemaining > 0) {
            pauseRemaining--;
            idleLook();
            return current;
        }

        if (target == null) {
            target = pickTarget();
        }

        Vec delta = target.sub(current);
        double distance = Math.sqrt(delta.x() * delta.x() + delta.z() * delta.z());
        if (distance <= ARRIVAL_DISTANCE) {
            target = null;
            pauseRemaining = randomPause();
            return current;
        }

        Vec direction = new Vec(delta.x() / distance, 0, delta.z() / distance);
        turnTowards(direction);

        if (Math.abs(headingError(direction)) > MAX_STRAFE_DEGREES) {
            return current;
        }

        double step = Math.min(speed, distance);
        Vec next = current.add(direction.mul(step));

        if (!isWalkable(home.x() + next.x(), home.y(), home.z() + next.z())) {
            target = null;
            pauseRemaining = randomPause();
            return current;
        }

        return next;
    }

    private float headingError(Vec direction) {
        float desired = (float) Math.toDegrees(Math.atan2(-direction.x(), direction.z()));
        return ((desired - yaw + 540f) % 360f) - 180f;
    }

    private void turnTowards(Vec direction) {
        float desired = (float) Math.toDegrees(Math.atan2(-direction.x(), direction.z()));
        float difference = ((desired - yaw + 540f) % 360f) - 180f;
        yaw = (yaw + difference * (float) YAW_SMOOTHING + 360f) % 360f;
    }

    private void idleLook() {
        if (--lookRemaining > 0) {
            float difference = ((lookYaw - yaw + 540f) % 360f) - 180f;
            yaw = (yaw + difference * (float) YAW_SMOOTHING + 360f) % 360f;
            return;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        lookRemaining = random.nextInt(IDLE_LOOK_MIN_TICKS, IDLE_LOOK_MAX_TICKS + 1);
        lookYaw = (yaw + random.nextFloat(-IDLE_LOOK_SPREAD, IDLE_LOOK_SPREAD) + 360f) % 360f;
    }

    private Vec pickTarget() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int attempt = 0; attempt < 12; attempt++) {
            double angle = random.nextDouble(Math.PI * 2);
            double distance = random.nextDouble(radius * 0.35, radius);
            double dx = Math.cos(angle) * distance;
            double dz = Math.sin(angle) * distance;
            if (isWalkable(home.x() + dx, home.y(), home.z() + dz)) {
                return new Vec(dx, 0, dz);
            }
        }
        return Vec.ZERO;
    }

    private int randomPause() {
        return ThreadLocalRandom.current().nextInt(minPauseTicks, maxPauseTicks + 1);
    }
}
