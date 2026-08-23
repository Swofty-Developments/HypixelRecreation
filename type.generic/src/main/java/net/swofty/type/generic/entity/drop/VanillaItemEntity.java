package net.swofty.type.generic.entity.drop;

import net.minestom.server.entity.ItemEntity;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityItemMergeEvent;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.time.TimeUnit;

import java.time.Duration;

public class VanillaItemEntity extends ItemEntity {
    private final long createdNanos = System.nanoTime();
    private int despawnTicks = ItemDropPhysics.DESPAWN_TICKS;
    private int mergeCheckIntervalTicks = ItemDropPhysics.MERGE_CHECK_INTERVAL_TICKS;
    private int ticksSinceMergeCheck;

    public VanillaItemEntity(ItemStack itemStack) {
        super(itemStack);
        setMergeRange(ItemDropPhysics.MERGE_RANGE);
    }

    @Override
    public long getTimeSinceSpawn() {
        return java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - createdNanos);
    }

    public int getDespawnTicks() {
        return despawnTicks;
    }

    public void setDespawnTicks(int despawnTicks) {
        this.despawnTicks = despawnTicks;
    }

    public int getMergeCheckIntervalTicks() {
        return mergeCheckIntervalTicks;
    }

    public void setMergeCheckIntervalTicks(int mergeCheckIntervalTicks) {
        this.mergeCheckIntervalTicks = Math.max(1, mergeCheckIntervalTicks);
    }

    public void setPickupDelayTicks(int ticks) {
        setPickupDelay(Duration.of(ticks, TimeUnit.SERVER_TICK));
    }

    @Override
    public void spawn() {
        super.spawn();
        if (despawnTicks > 0) scheduleRemove(despawnTicks, TimeUnit.SERVER_TICK);
    }

    @Override
    public void update(long time) {
        if (++ticksSinceMergeCheck < mergeCheckIntervalTicks) return;
        ticksSinceMergeCheck = 0;
        mergeNearby();
    }

    protected boolean canMergeWith(VanillaItemEntity other) {
        return true;
    }

    private void mergeNearby() {
        final Instance instance = getInstance();
        if (instance == null || isRemoved()) return;
        if (!isMergeable() || !isPickable()) return;

        instance.getEntityTracker().nearbyEntities(getPosition(), getMergeRange(),
                EntityTracker.Target.ITEMS, nearby -> {
                    if (nearby == this || isRemoved() || nearby.isRemoved()) return;
                    if (!(nearby instanceof VanillaItemEntity other)) return;
                    if (!other.isMergeable() || !other.isPickable()) return;
                    if (!canMergeWith(other) || !other.canMergeWith(this)) return;

                    final ItemStack held = getItemStack();
                    final ItemStack absorbed = other.getItemStack();
                    if (!ItemStackMerging.canMerge(held, absorbed)) return;

                    final EntityItemMergeEvent event = new EntityItemMergeEvent(this, other,
                            ItemStackMerging.merge(held, absorbed));
                    EventDispatcher.callCancellable(event, () -> {
                        setItemStack(event.getResult());
                        other.remove();
                    });
                });
    }
}
