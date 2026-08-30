package io.github.term4.polyp.mechanics.projectile.shootables;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.fx.FxContext;
import io.github.term4.polyp.fx.Fx;
import io.github.term4.polyp.mechanics.durability.DurabilitySystem;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.ProjectileSystem;
import io.github.term4.polyp.mechanics.projectile.entities.FishingBobberEntity;
import io.github.term4.polyp.mechanics.projectile.types.FishingBobber;
import io.github.term4.polyp.mechanics.projectile.types.ProjectileType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

/**
 * Fishing rod launcher ({@link Shootable}): a rod use casts a {@link FishingBobber}, a second use retrieves it (reel
 * pull + rod durability). PvP rod only - no fishing loot. Pass {@code new FishingRod()} to {@link ProjectileSystem#install}.
 */
public final class FishingRod implements Shootable {

    private final ProjectileType bobberType;

    /** A rod that casts the built-in {@link FishingBobber}. */
    public FishingRod() { this(FishingBobber.INSTANCE); }

    /** A rod casting a custom bobber type (its entity must be a {@link FishingBobberEntity} for hook/retrieve wiring). */
    public FishingRod(ProjectileType bobberType) { this.bobberType = bobberType; }

    @Override
    public void install(@NotNull EventNode<@NotNull Event> node, @NotNull ProjectileSystem system) {
        // use_item ONLY: a click at a block in reach sends use_item_on FOLLOWED by use_item (the rod has no block
        // action), and vanilla acts on the latter alone - handling both toggles cast+retrieve twice per click
        node.addListener(PlayerUseItemEvent.class, e -> onUse(e.getPlayer(), e.getHand(), e.getItemStack(), system));
    }

    /** Bobber aliveTicks at end-of-cast; the cast tick includes the first step, so aliveTicks alone can't spot a same-tick retract. */
    private static final Tag<Long> CAST_TICKS = Tag.Long("polyp:cast_ticks");

    private void onUse(Player p, PlayerHand hand, ItemStack item, ProjectileSystem system) {
        if (item.material() != Material.FISHING_ROD) return;
        FishingBobberEntity active = p.getTag(FishingBobberEntity.ACTIVE_BOBBER);
        if (active != null && !active.isRemoved()) {
            // same-tick spawn+destroy ghosts on the ViaRewind held-spawn path; ignore a retract in the cast's own tick
            Long cast = active.getTag(CAST_TICKS);
            if (cast != null && active.getAliveTicks() <= cast) return;
            Fx.play(system.services(), Fx.ROD_RETRIEVE, FxContext.of(p));
            damageRod(p, hand, active.retrieve());
        } else {
            var proj = system.launch(ProjectileSnapshot.of(p, bobberType).withItem(item));
            if (proj instanceof FishingBobberEntity bobber) p.setTag(FishingBobberEntity.ACTIVE_BOBBER, bobber);
            Fx.play(system.services(), Fx.ROD_CAST, FxContext.of(p));
            system.firstStep(proj);
            if (proj instanceof FishingBobberEntity bobber) bobber.setTag(CAST_TICKS, bobber.getAliveTicks());
        }
    }

    private static void damageRod(Player p, PlayerHand hand, int amount) {
        if (amount <= 0) return;
        var polyp = Polyp.getInstance();
        DurabilitySystem durability = polyp.isInitialized() ? polyp.services().durability() : null;
        if (durability != null) {
            durability.damage(p, hand == PlayerHand.MAIN ? EquipmentSlot.MAIN_HAND : EquipmentSlot.OFF_HAND, amount);
        }
    }
}
