package io.github.term4.polyp.vri;

import io.github.term4.polyp.util.HeldItems;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.api.event.explosion.TntPrimeEvent;
import io.github.term4.polyp.entity.PrimedTnt;
import io.github.term4.polyp.mechanics.durability.DurabilitySystem;
import io.github.term4.polyp.mechanics.explosion.ExplosionSystem;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

/**
 * Vanilla TNT ignition by hand: flint and steel (1 durability) or a fire charge (consumed outside creative).
 * Redstone, fire spread, dispensers, and flaming projectiles (1.9+, never 1.8) are not implemented.
 */
final class TntIgnite {

    private TntIgnite() {}

    static void install(EventNode<? super PlayerEvent> node, Vri system) {
        node.addListener(PlayerBlockInteractEvent.class, e -> onInteract(e, system));
    }

    private static void onInteract(PlayerBlockInteractEvent e, Vri system) {
        if (!e.getBlock().compare(Block.TNT)) return;
        Player p = e.getPlayer();
        if (!system.configFor(p).tntIgnite) return;
        ItemStack held = p.getItemInHand(e.getHand());
        boolean flintAndSteel = held.material() == Material.FLINT_AND_STEEL;
        if (!flintAndSteel && held.material() != Material.FIRE_CHARGE) return;

        Polyp polyp = system.polyp();
        ExplosionSystem explosions = polyp.module(ExplosionSystem.class);
        if (explosions == null) return;
        MechanicsWorld world = MechanicsWorld.of(p);
        TntPrimeEvent.Cause cause = flintAndSteel ? TntPrimeEvent.Cause.FLINT_AND_STEEL : TntPrimeEvent.Cause.FIRE_CHARGE;
        PrimedTnt ignited = PrimedTnt.ignite(explosions, world, e.getBlockPosition(),
                explosions.resolveTnt(p, world, cause), p, cause);
        if (ignited == null) return;

        e.setBlockingItemUse(true);
        if (flintAndSteel) {
            DurabilitySystem durability = polyp.services().durability();
            if (durability != null) durability.damage(p,
                    e.getHand() == PlayerHand.OFF ? EquipmentSlot.OFF_HAND : EquipmentSlot.MAIN_HAND, 1);
        } else {
            HeldItems.consumeOne(p, e.getHand());
        }
    }
}
