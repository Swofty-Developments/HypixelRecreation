package io.github.term4.polyp.vri;

import io.github.term4.polyp.api.event.item.ItemSpawnEvent;
import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.ScopedSystem;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.world.WorldPolicy;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityItemMergeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * VRI (Vanilla Re-Implemented): world behaviors Minestom omits - crack overlay, block drops, item pickup/drop,
 * fire-break parity (chests, deaths later). Each behavior reads {@link #configFor} per event, so a scope can enable one the install
 * config left off. Drop spawns fire {@link ItemSpawnEvent}. Break FX (world event 2001) is native in
 * {@code breakBlock} - don't re-add it.
 */
public final class Vri extends ScopedSystem<VriConfig> {

    private final EventNode<@NotNull Event> node;

    private Vri(Polyp polyp, VriConfig config) {
        super(polyp, MechanicsKeys.VRI, config);
        this.node = EventNode.all("polyp:vri");
    }

    @Override public EventNode<@NotNull Event> node() { return node; }

    public static Vri install(@NotNull Polyp polyp, @NotNull VriConfig config) {
        Vri system = new Vri(polyp, config);
        // not a toggle: Minestom's item-merge scan is instance-wide (like the pickup scan ItemPickup gates) -
        // co-located items from different worlds would absorb each other
        system.node.addListener(EntityItemMergeEvent.class, e -> {
            if (!WorldPolicy.canAffect(e.getEntity(), e.getMerged())) e.setCancelled(true);
        });
        BlockBreakProgress.install(system.node, system);
        BlockDrops.install(system.node, system);
        ItemPickup.install(system.node, system);
        ItemDrop.install(system.node, system);
        FireBreaks.install(system.node, system);
        TntIgnite.install(system.node, system);
        return polyp.installModule(system);
    }
}
