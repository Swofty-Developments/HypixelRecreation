package net.swofty.type.skywarsgame.luckyblock.spawns;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.utility.EntityUtility;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

import java.time.Duration;

public abstract class RideableMob {

    protected final SkywarsPlayer rider;
    protected final Instance instance;
    protected EntityCreature mount;
    protected boolean active = false;

    public RideableMob(SkywarsPlayer rider, Instance instance) {
        this.rider = rider;
        this.instance = instance;
    }

    public abstract EntityType getEntityType();

    public abstract String getDisplayName();

    public abstract int getDurationSeconds();

    protected void onMountCreated() {
    }

    protected void onMountTick() {
    }

    protected void onMountRemoved() {
    }

    public void spawn(Pos position) {
        if (active) return;

        mount = new EntityCreature(getEntityType());
        EntityUtility.nameEntityVisible(mount, "<6>{}'s {}", rider.getUsername(), getDisplayName());

        onMountCreated();

        mount.setInstance(instance, position);
        active = true;

        rider.getVehicle();
        mount.addPassenger(rider);

        rider.sendMessage("<6>You are now riding a {}!", getDisplayName());
        rider.sendMessage("<7>Sneak to dismount. Duration: {} seconds", getDurationSeconds());

        mount.scheduler().buildTask(() -> {
            if (!active || mount.isRemoved()) return;
            onMountTick();
        }).repeat(TaskSchedule.tick(1)).schedule();

        mount.scheduler().buildTask(this::remove)
                .delay(Duration.ofSeconds(getDurationSeconds()))
                .schedule();
    }

    public void remove() {
        if (!active) return;
        active = false;

        onMountRemoved();

        if (mount != null && !mount.isRemoved()) {
            mount.getPassengers().forEach(Entity::remove);
            mount.remove();
        }

        rider.sendMessage("<7>Your {} has expired!", getDisplayName());
    }

    public void handleSneak() {
        if (active && mount != null) {
            remove();
        }
    }

    public boolean isActive() {
        return active;
    }

    public EntityCreature getMount() {
        return mount;
    }
}
