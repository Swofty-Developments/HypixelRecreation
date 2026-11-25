package io.github.term4.polyp.mechanics.hunger;

import io.github.term4.polyp.api.event.damage.DamageAppliedEvent;
import io.github.term4.polyp.mechanics.damage.DamageConfigResolver.DamageContext;
import io.github.term4.polyp.mechanics.damage.types.melee.MeleeDamage;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.util.BlockContact;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The action exhaustion sources - detection only, values are preset {@code exhaustionCost} rules (unpriced = inert).
 * Movement categorization mirrors vanilla: eye-in-water 3D, touching-water horizontal, else the packet-flag ground
 * branch. The jump IS vanilla's server-side detection (1.8 {@code PlayerConnection:236}) - ground knockback charges
 * there too, as in vanilla.
 */
final class ExhaustionSources {

    private static final AtomicBoolean INSTALLED = new AtomicBoolean();

    private ExhaustionSources() {}

    static void install(Polyp polyp) {
        if (!INSTALLED.compareAndSet(false, true)) return;
        EventNode<@NotNull Event> node = EventNode.all("polyp:exhaustion-sources");
        node.addListener(PlayerMoveEvent.class, e -> {
            HungerSystem hunger = polyp.module(HungerSystem.class);
            if (hunger != null) onMove(hunger, e);
        });
        node.addListener(PlayerBlockBreakEvent.class, e -> {
            HungerSystem hunger = polyp.module(HungerSystem.class);
            if (hunger != null && e.getPlayer().getGameMode() != GameMode.CREATIVE) // creative never harvests (1.8 PlayerInteractManager)
                hunger.chargePriced(e.getPlayer(), HungerSystem.BLOCK_BREAK_COST, 1f);
        });
        node.addListener(DamageAppliedEvent.class, e -> {
            HungerSystem hunger = polyp.module(HungerSystem.class);
            if (hunger != null) onDamage(hunger, e);
        });
        polyp.install(node);
    }

    private static void onMove(HungerSystem hunger, PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Pos from = p.getPosition(); // pre-move
        Pos to = e.getNewPosition();
        double dx = to.x() - from.x(), dy = to.y() - from.y(), dz = to.z() - from.z();
        if (p.isOnGround() && !e.isOnGround() && dy > 0) // grounded + rising + packet airborne = vanilla's jump
            hunger.chargePriced(p, p.isSprinting() ? HungerSystem.SPRINT_JUMP_COST : HungerSystem.JUMP_COST, 1f);
        double h2 = dx * dx + dz * dz;
        if (h2 == 0 && dy == 0) return;
        if (eyeInWater(p)) {
            hunger.chargePriced(p, HungerSystem.DIVE_COST, (float) Math.sqrt(h2 + dy * dy));
        } else if (BlockContact.touching(p, b -> b.compare(Block.WATER))) {
            hunger.chargePriced(p, HungerSystem.SWIM_COST, (float) Math.sqrt(h2));
        } else if (e.isOnGround() && !BlockContact.climbing(p)) {
            hunger.chargePriced(p, p.isSprinting() ? HungerSystem.SPRINT_COST : HungerSystem.WALK_COST, (float) Math.sqrt(h2));
        }
    }

    private static void onDamage(HungerSystem hunger, DamageAppliedEvent e) {
        // vanilla gates the victim's charge on non-zero post-mitigation damage
        if (e.target() instanceof Player victim && e.dealt() != 0) {
            DamageContext ctx = DamageContext.of(e.snapshot(), e.services());
            hunger.chargePriced(victim, HungerSystem.DAMAGE_TAKEN_COST, ctx.typeConfig().exhaustion(ctx));
        }
        // vanilla charges the attacker on damageEntity == true - i-frame-eaten hits are free
        if (e.type() instanceof MeleeDamage && e.outcome().landed() && e.source() instanceof Player attacker)
            hunger.chargePriced(attacker, HungerSystem.ATTACK_COST, 1f);
    }

    private static boolean eyeInWater(Player p) {
        if (p.getInstance() == null) return false;
        Block eye = MechanicsWorld.viewed(p).getBlock(p.getPosition().add(0, p.getEyeHeight(), 0), Block.Getter.Condition.TYPE);
        return eye != null && eye.compare(Block.WATER);
    }
}
