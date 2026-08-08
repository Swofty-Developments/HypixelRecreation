package io.github.term4.polyp.mechanics.projectile.shootables;

import io.github.term4.polyp.fx.FxContext;
import io.github.term4.polyp.fx.Fx;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.ProjectileSystem;
import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import io.github.term4.polyp.mechanics.projectile.entities.arrow.ArrowEntity;
import io.github.term4.polyp.mechanics.projectile.entities.arrow.TippedArrows;
import io.github.term4.polyp.mechanics.attribute.catalog.enchant.Infinity;
import io.github.term4.polyp.mechanics.projectile.types.Arrow;
import io.github.term4.polyp.mechanics.projectile.types.ProjectileType;
import io.github.term4.polyp.item.Enchants;
import net.minestom.server.ServerFlag;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.PlayerCancelItemUseEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Bow launcher ({@link Shootable}): a drawn-bow release fires an {@link Arrow}. Pass {@code new Bow()} to
 * {@link ProjectileSystem#install}.
 *
 * <p>Vanilla 1.8: draw power {@code (s^2 + 2s)/3} capped at 1, {@code < 0.1} fires nothing, full draw is critical
 * (chance = the arrow type's {@code critChance} knob); consumes one arrow (unless creative); the arrow launches at
 * speed {@code power * speed}.
 * TODO: offhand arrow selection; gate drawing (not just release) on ammo.
 */
public final class Bow implements Shootable {

    /** Minimum draw power that fires (vanilla: {@code < 0.1} releases nothing). */
    private static final float MIN_POWER = 0.1f;

    private final ProjectileType arrowType;

    /** A bow that fires the built-in {@link Arrow}. */
    public Bow() { this(Arrow.INSTANCE); }

    /** A bow that fires a custom arrow type (its entity must be an {@link ArrowEntity} for crit/pickup wiring). */
    public Bow(ProjectileType arrowType) { this.arrowType = arrowType; }

    @Override
    public void install(@NotNull EventNode<@NotNull Event> node, @NotNull ProjectileSystem system) {
        node.addListener(PlayerCancelItemUseEvent.class, e -> onRelease(e, system));
    }

    private void onRelease(PlayerCancelItemUseEvent e, ProjectileSystem system) {
        if (e.getItemStack().material() != Material.BOW) return;
        Player p = e.getPlayer();
        float power = drawPower(e.getUseDuration());
        if (power < MIN_POWER) return;
        boolean creative = p.getGameMode() == GameMode.CREATIVE;
        int slot = firstArrowSlot(p);
        if (slot < 0 && !creative) return; // survival/Infinity still need an arrow
        ItemStack arrowItem = slot >= 0 ? p.getInventory().getItemStack(slot) : ItemStack.AIR;
        // Infinity keeps only PLAIN arrows (vanilla); tipped/spectral are always consumed.
        boolean keepArrow = creative || (Enchants.level(e.getItemStack(), Infinity.KEY) > 0 && arrowItem.material() == Material.ARROW);
        if (!keepArrow && slot >= 0) p.getInventory().setItemStack(slot, arrowItem.withAmount(arrowItem.amount() - 1));
        ProjectileSnapshot snap = ProjectileSnapshot.of(p, arrowType).withPower(power).withItem(e.getItemStack());
        ProjectileEntity proj = system.launch(snap);
        Fx.play(system.services(), Fx.BOW_SHOOT, FxContext.of(p));
        if (proj instanceof ArrowEntity arrow) {
            arrow.setCritical(power >= 1f && rollCrit(system.resolveFlight(snap).critChance()));
            // a kept shot (creative / Infinity) must not hand the collector a free arrow
            arrow.setPickup(keepArrow ? ArrowEntity.Pickup.CREATIVE_ONLY : ArrowEntity.Pickup.ALLOWED);
            TippedArrows.apply(arrow, arrowItem);
        }
        system.firstStep(proj);
    }

    private static boolean rollCrit(double chance) {
        if (chance >= 1.0) return true;
        return chance > 0.0 && ThreadLocalRandom.current().nextDouble() < chance;
    }

    /** Vanilla bow power curve over the draw in real SECONDS (ticks / server TPS, not a hardcoded 20), so charge is TPS-invariant. */
    private static float drawPower(long useDurationTicks) {
        float f = useDurationTicks / (float) ServerFlag.SERVER_TICKS_PER_SECOND;
        float power = (f * f + 2 * f) / 3.0f;
        return power > 1f ? 1f : power;
    }

    /** Inventory slot of the first arrow the bow can fire (plain / tipped / spectral), or {@code -1} if none. */
    private static int firstArrowSlot(Player p) {
        var inv = p.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            if (isArrow(inv.getItemStack(i))) return i;
        }
        return -1;
    }

    /** Whether {@code stack} is an arrow the bow fires (plain, tipped → potion payload, or spectral). */
    private static boolean isArrow(ItemStack stack) {
        Material m = stack.material();
        return m == Material.ARROW || m == Material.TIPPED_ARROW || m == Material.SPECTRAL_ARROW;
    }
}
