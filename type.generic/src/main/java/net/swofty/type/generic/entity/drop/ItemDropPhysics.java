package net.swofty.type.generic.entity.drop;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;

import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

public final class ItemDropPhysics {
    public static final double TICKS_PER_SECOND = 20.0;

    public static final double ITEM_WIDTH = 0.25;
    public static final double ITEM_HEIGHT = 0.25;

    public static final double THROW_EYE_OFFSET = 0.3;
    public static final double THROW_SPEED = 0.3;
    public static final double THROW_UPWARDS = 0.1;
    public static final double THROW_SPREAD = 0.02;
    public static final double THROW_VERTICAL_SPREAD = 0.1;

    public static final double BLOCK_DROP_SPREAD = 0.2;
    public static final double BLOCK_DROP_UPWARDS = 0.2;

    public static final int THROW_PICKUP_DELAY_TICKS = 40;
    public static final int BLOCK_DROP_PICKUP_DELAY_TICKS = 10;
    public static final int DESPAWN_TICKS = 6000;
    public static final int MERGE_CHECK_INTERVAL_TICKS = 10;
    public static final float MERGE_RANGE = 0.5f;

    public static final double PICKUP_REACH_HORIZONTAL = 1.0;
    public static final double PICKUP_REACH_VERTICAL = 0.5;
    public static final double PICKUP_SCAN_RANGE = 2.0;

    public static final float PICKUP_SOUND_VOLUME = 0.2f;
    public static final double PICKUP_SOUND_PITCH_SPREAD = 1.4;
    public static final double PICKUP_SOUND_PITCH_BASE = 2.0;

    private ItemDropPhysics() {
    }

    public static Pos throwPosition(Pos playerPosition, double eyeHeight) {
        return playerPosition.withY(playerPosition.y() + eyeHeight - THROW_EYE_OFFSET);
    }

    public static Vec throwVelocity(float yaw, float pitch) {
        return throwVelocity(yaw, pitch, ThreadLocalRandom.current());
    }

    public static Vec throwVelocity(float yaw, float pitch, RandomGenerator random) {
        final double yawRadians = Math.toRadians(yaw);
        final double pitchRadians = Math.toRadians(pitch);
        final double sinYaw = Math.sin(yawRadians);
        final double cosYaw = Math.cos(yawRadians);
        final double sinPitch = Math.sin(pitchRadians);
        final double cosPitch = Math.cos(pitchRadians);

        final double spreadAngle = random.nextDouble() * Math.PI * 2.0;
        final double spreadMagnitude = THROW_SPREAD * random.nextDouble();
        final double verticalSpread = (random.nextDouble() - random.nextDouble()) * THROW_VERTICAL_SPREAD;

        return perSecond(new Vec(
                -sinYaw * cosPitch * THROW_SPEED + Math.cos(spreadAngle) * spreadMagnitude,
                -sinPitch * THROW_SPEED + THROW_UPWARDS + verticalSpread,
                cosYaw * cosPitch * THROW_SPEED + Math.sin(spreadAngle) * spreadMagnitude));
    }

    public static Vec blockDropVelocity() {
        return blockDropVelocity(ThreadLocalRandom.current());
    }

    public static Vec blockDropVelocity(RandomGenerator random) {
        return perSecond(new Vec(
                random.nextDouble() * BLOCK_DROP_SPREAD - BLOCK_DROP_SPREAD / 2.0,
                BLOCK_DROP_UPWARDS,
                random.nextDouble() * BLOCK_DROP_SPREAD - BLOCK_DROP_SPREAD / 2.0));
    }

    public static Pos blockDropPosition(Point blockPosition) {
        return blockDropPosition(blockPosition, ThreadLocalRandom.current());
    }

    public static Pos blockDropPosition(Point blockPosition, RandomGenerator random) {
        final double span = 1.0 - ITEM_WIDTH;
        final double inset = ITEM_WIDTH / 2.0;
        return new Pos(
                Math.floor(blockPosition.x()) + random.nextDouble() * span + inset,
                Math.floor(blockPosition.y()) + random.nextDouble() * span + inset,
                Math.floor(blockPosition.z()) + random.nextDouble() * span + inset);
    }

    public static Vec perSecond(Vec perTick) {
        return perTick.mul(TICKS_PER_SECOND);
    }

    public static Vec perTick(Vec perSecond) {
        return perSecond.div(TICKS_PER_SECOND);
    }

    public static float pickupSoundPitch() {
        return pickupSoundPitch(ThreadLocalRandom.current());
    }

    public static float pickupSoundPitch(RandomGenerator random) {
        return (float) ((random.nextDouble() - random.nextDouble()) * PICKUP_SOUND_PITCH_SPREAD
                + PICKUP_SOUND_PITCH_BASE);
    }

    public static boolean withinPickupReach(Point playerPosition, double playerWidth, double playerHeight,
                                            Point itemPosition, double itemWidth, double itemHeight) {
        final double horizontalReach = playerWidth / 2.0 + itemWidth / 2.0 + PICKUP_REACH_HORIZONTAL;
        if (Math.abs(itemPosition.x() - playerPosition.x()) > horizontalReach) return false;
        if (Math.abs(itemPosition.z() - playerPosition.z()) > horizontalReach) return false;

        final double itemBottom = itemPosition.y() - PICKUP_REACH_VERTICAL;
        final double itemTop = itemPosition.y() + itemHeight + PICKUP_REACH_VERTICAL;
        return itemTop >= playerPosition.y() && itemBottom <= playerPosition.y() + playerHeight;
    }
}
