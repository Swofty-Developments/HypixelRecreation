package io.github.term4.polyp.mechanics.projectile;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.attribute.catalog.enchant.Flame;
import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import io.github.term4.polyp.mechanics.projectile.types.Arrow;
import io.github.term4.polyp.presets.vanilla18.Vanilla18;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Flame keys off the ARROW burning, not the enchant (1.8 EntityArrow: {@code isBurning() -> setOnFire(5)}):
 * the arrow itself burns on the wire, water douses it, and the ignite lands before the damage roll.
 */
class FlameArrowTest extends HeadlessServerTest {

    private static ItemStack flameBow() {
        return enchanted(Material.BOW, Flame.KEY, 1);
    }

    /** Shoots an arrow from a zombie at {@code from} (yaw 0 = +z), flying it up to 40 ticks. */
    private ProjectileEntity shoot(Pos from, ItemStack bow, int flyTicks) {
        LivingEntity shooter = looseZombie();
        shooter.setInstance(instance, from).join();
        try {
            var snap = ProjectileSnapshot.of(shooter, Arrow.INSTANCE)
                    .withConfig(Vanilla18.projectiles()).withItem(bow);
            ProjectileEntity arrow = new ProjectileSystem(Polyp.getInstance(), Vanilla18.projectiles()).launch(snap);
            assertNotNull(arrow);
            awaitSpawn(arrow);
            for (int t = 1; t <= flyTicks && !arrow.isRemoved(); t++) arrow.tick(t * 50L);
            return arrow;
        } finally {
            shooter.remove();
        }
    }

    @Test
    void flameArrowBurnsOnTheWireAndIgnitesTheVictim() {
        LivingEntity victim = zombie(new Pos(100.5, 65, 105.5));
        victim.setHealth(20f); // harness zombies spawn at 1 hp - a kill's death cleanup clears the fire
        ProjectileEntity arrow = shoot(new Pos(100.5, 65, 100.5, 0f, 0f), flameBow(), 40);
        assertTrue(arrow.isRemoved(), "the arrow must reach the victim");
        assertEquals(Flame.FIRE_TICKS, victim.getFireTicks(), "vanilla fixed 5s");
        victim.remove();
    }

    @Test
    void plainArrowIgnitesNothing() {
        LivingEntity victim = zombie(new Pos(120.5, 65, 125.5));
        victim.setHealth(20f);
        shoot(new Pos(120.5, 65, 120.5, 0f, 0f), ItemStack.of(Material.BOW), 40);
        assertEquals(0, victim.getFireTicks());
        victim.remove();
    }

    @Test
    void waterDousesTheArrow() {
        // a wall of water across the flight path; 3 thick so a 3 b/t arrow cannot skip it between ticks.
        // Shooter height puts the crossing at y-fraction ~0.4: the 1.8 water scan inverts on the arrow's short
        // box and goes blind for fractions in [0.6, 0.9) - vanilla's own flicker, replicated by FluidFlow
        for (int z = 143; z <= 145; z++)
            for (int y = 64; y <= 70; y++) instance.setBlock(140, y, z, Block.WATER);
        ProjectileEntity arrow = shoot(new Pos(140.5, 64.8, 140.5, 0f, 0f), flameBow(), 10);
        assertFalse(arrow.getEntityMeta().isOnFire(), "water puts the flame out");
        if (!arrow.isRemoved()) arrow.remove();
    }

    /** Vanilla ignites BEFORE the damage roll, so an i-frame deflect still burns. */
    @Test
    void deflectedArrowStillIgnites() {
        LivingEntity victim = zombie(new Pos(160.5, 65, 165.5));
        victim.setHealth(20f);
        // open the invul window with a melee hit; the attacker stands BESIDE the lane or the arrow hits them first
        LivingEntity attacker = zombie(new Pos(162.0, 65, 165.5));
        services.damage().apply(io.github.term4.polyp.mechanics.damage.types.melee.MeleeDamage.INSTANCE
                .snapshot(attacker, victim, false, ItemStack.of(Material.DIAMOND_SWORD), services));
        victim.setFireTicks(0); // isolate the arrow's ignite

        ProjectileEntity arrow = shoot(new Pos(160.5, 65, 160.5, 0f, 0f), flameBow(), 4);
        assertFalse(arrow.isRemoved(), "the arrow must DEFLECT off the invul window, not land");
        assertEquals(Flame.FIRE_TICKS, victim.getFireTicks(), "the ignite precedes the damage roll");
        arrow.remove();
        attacker.remove();
        victim.remove();
    }
}
