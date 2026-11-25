package test.presets;

import io.github.term4.polyp.mechanics.explosion.BlockBreaking;
import io.github.term4.polyp.mechanics.explosion.ExplosionSystem;
import io.github.term4.polyp.presets.mmc18.Explosion;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A MineMen blast destroys ground loot or leaves it put - it never shoves it, the way vanilla's radial push does. */
class Mmc18ItemBlastTest extends HeadlessServerTest {

    /** Blast next to a dropped stack and return the velocity it came away with. */
    private static Vec blastVelocity(io.github.term4.polyp.mechanics.explosion.ExplosionConfig config) {
        // block breaking off: this measures the push, not the crater
        ExplosionSystem explosions = new ExplosionSystem(polyp,
                config.toBuilder().blockBreaking((BlockBreaking) null).build());
        ItemEntity item = new ItemEntity(ItemStack.of(Material.DIAMOND, 3));
        item.setInstance(instance, new Pos(600.5, 65, 600.5)).join();
        try {
            item.setVelocity(Vec.ZERO);
            explosions.explode(instance, new Pos(602.5, 65, 600.5), 4.0f);
            return item.getVelocity();
        } finally {
            item.remove();
        }
    }

    @Test
    void droppedItemsAreNeverPushedByAMineMenBlast() {
        assertEquals(Vec.ZERO, blastVelocity(Explosion.config()),
                "mmc18: a dropped stack does not budge");
        assertTrue(blastVelocity(io.github.term4.polyp.presets.vanilla18.Explosion.config()).length() > 0.0,
                "vanilla18: the same blast rides it out on the radial push (the contrast)");
    }
}
