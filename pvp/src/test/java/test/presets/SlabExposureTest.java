package test.presets;

import io.github.term4.polyp.presets.mmc18.Explosion;
import io.github.term4.polyp.mechanics.explosion.ExplosionExposure;
import io.github.term4.polyp.mechanics.explosion.ExplosionSystem;
import io.github.term4.polyp.world.MechanicsWorld;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;
import io.github.term4.polyp.entity.PrimedTnt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The off-flat (slab) exposure split. Faithful 1.8 ({@code seenPercent18}) and modern ({@code seenPercent}) both use the
 * block's REAL shape - a slab is a half-box, rays pass over it, so both PUSH off-flat (vanilla singleplayer 1.8 + 26.1).
 * The Hypixel/MineMen servers instead treat every solid as a FULL cube ({@code seenPercent18FullCube}: {@code isSolid()}
 * + a unit-cube intercept), so an off-flat blast is fully occluded (exposure 0, no push) - user-confirmed live on both.
 * The server presets ride the full-cube variant; {@code vanilla18} rides the shape-aware one. Flat ground: all read full.
 */
class SlabExposureTest extends HeadlessServerTest {

    private static final PrimedTnt.Config CFG = new PrimedTnt.Config(400, 4.0f, true, PrimedTnt.Wire.MINEMEN, true, null);

    /** Settles a source + victim TNT (on slabs if requested); returns {@code {seenPercent18 (vanilla-1.8), seenPercent18FullCube (server), seenPercent (modern)}}. */
    private float[] exposures(ExplosionSystem ex, boolean onSlabs) {
        if (onSlabs) for (int x = 4; x <= 9; x++) instance.setBlock(x, 64, 8, Block.SMOOTH_STONE_SLAB.withProperty("type", "bottom"));
        PrimedTnt src = PrimedTnt.spawn(ex, instance, new BlockVec(5, 64, 8), CFG);
        PrimedTnt vic = PrimedTnt.spawn(ex, instance, new BlockVec(8, 64, 8), CFG);
        for (int i = 0; i < 40; i++) { src.tick(0); vic.tick(0); }
        float[] e = {ExplosionExposure.seenPercent18(MechanicsWorld.of(instance), src.getPosition(), vic),
                ExplosionExposure.seenPercent18FullCube(MechanicsWorld.of(instance), src.getPosition(), vic),
                ExplosionExposure.seenPercent(MechanicsWorld.of(instance), src.getPosition(), vic)};
        src.remove(); vic.remove();
        for (int x = 4; x <= 9; x++) instance.setBlock(x, 64, 8, Block.AIR);
        return e;
    }

    @Test
    void minestomStoresSlabAsHalfBox() {
        assertEquals(0.5, Block.SMOOTH_STONE_SLAB.withProperty("type", "bottom").registry().collisionShape().relativeEnd().y(), 1e-9,
                "Minestom's collision uses the real half-slab shape - it does NOT treat a slab as a full block");
    }

    @Test
    void flatGroundReadsFullEverywhere() {
        float[] e = exposures(new ExplosionSystem(polyp, Explosion.config().toBuilder().blockBreaking((io.github.term4.polyp.mechanics.explosion.BlockBreaking) null).build()), false);
        assertEquals(1.0, e[0], 1e-6, "vanilla-1.8 (shape-aware) full on flat");
        assertEquals(1.0, e[1], 1e-6, "server full-cube full on flat");
        assertEquals(1.0, e[2], 1e-6, "modern full on flat");
    }

    @Test
    void slabPushesUnderRealShapeButServerGatesFullCube() {
        float[] e = exposures(new ExplosionSystem(polyp, Explosion.config().toBuilder().blockBreaking((io.github.term4.polyp.mechanics.explosion.BlockBreaking) null).build()), true);
        assertTrue(e[0] > 0.0, "vanilla-1.8 real half-slab shape lets rays over the slab -> pushes (matches singleplayer 1.8): " + e[0]);
        assertEquals(0.0, e[1], 1e-6, "server full-cube fully occludes a slab -> gates the push (matches Hypixel/MineMen live)");
        assertTrue(e[2] > 0.0, "modern real shape -> pushes, like 26.1: " + e[2]);
    }
}
