package net.swofty.type.skyblockgeneric.entity;

import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public final class ExperienceBottleEntity extends Entity {
    private final SkyBlockPlayer thrower;
    private final long experience;
    private Vec velocity;
    private boolean impacted;

    public ExperienceBottleEntity(SkyBlockPlayer thrower, long experience) {
        super(EntityType.EXPERIENCE_BOTTLE);
        this.thrower = thrower;
        this.experience = experience;
        Pos position = thrower.getPosition();
        double yaw = Math.toRadians(position.yaw());
        double pitch = Math.toRadians(position.pitch());
        velocity = new Vec(-Math.sin(yaw) * Math.cos(pitch) * .7,
                -Math.sin(pitch) * .7 + .15, Math.cos(yaw) * Math.cos(pitch) * .7);
    }

    @Override
    public void spawn() {
        super.spawn();
        MinecraftServer.getSchedulerManager().scheduleTask(this::impact,
                TaskSchedule.tick(100), TaskSchedule.stop());
    }

    @Override
    public void tick(long time) {
        if (impacted || isRemoved()) return;
        Pos current = getPosition();
        velocity = velocity.add(0, -.05, 0).mul(.99);
        var result = CollisionUtils.handlePhysics(instance, getChunk(), getBoundingBox(),
                current, velocity, null, true);
        if (result.hasCollision()) {
            teleport(result.newPosition());
            impact();
            return;
        }
        teleport(current.add(velocity));
    }

    private void impact() {
        if (impacted || isRemoved()) return;
        impacted = true;
        Pos position = getPosition();
        instance.sendGroupedPacket(new ParticlePacket(Particle.HAPPY_VILLAGER,
                position.x(), position.y(), position.z(), .35f, .35f, .35f, .1f, 30));
        thrower.addExperience(experience);
        thrower.sendMessage("<a>+" + String.format("%,d", experience) + " Experience");
        remove();
    }
}
