package io.github.term4.polyp.mechanics.damage.types.burning;

import io.github.term4.polyp.mechanics.attribute.catalog.enchant.ProtectionEnchant;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Every vanilla ignition routes through setOnFire, so the fire-block 8s obeys Fire Protection like the enchants. */
class BurningIgniteTest extends HeadlessServerTest {

    private int igniteStandingInFire(Pos pos, ItemStack boots) {
        instance.setBlock(pos.blockX(), pos.blockY(), pos.blockZ(), Block.FIRE);
        LivingEntity mob = zombie(pos);
        mob.setHealth(20f);
        if (boots != null) mob.setBoots(boots);
        try {
            for (int t = 0; t < 100 && mob.getFireTicks() == 0; t++) {
                BurningTicker.INSTANCE.tick(mob, services.damage());
            }
            assertTrue(mob.getFireTicks() > 0, "standing in fire must ignite within the warmup");
            return mob.getFireTicks();
        } finally {
            mob.remove();
            instance.setBlock(pos.blockX(), pos.blockY(), pos.blockZ(), Block.AIR);
        }
    }

    @Test
    void fireProtectionCutsTheFireBlockIgnition() {
        assertEquals(160, igniteStandingInFire(new Pos(30.5, 65, 700.5), null), "vanilla 8s");
        ItemStack boots = enchanted(Material.DIAMOND_BOOTS, ProtectionEnchant.FIRE_PROTECTION.key(), 4);
        assertEquals(160 - (int) Math.floor(160 * 4 * 0.15f), igniteStandingInFire(new Pos(30.5, 65, 702.5), boots),
                "-15% per level, highest piece");
    }
}
