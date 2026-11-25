package io.github.term4.polyp.vri;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.api.event.explosion.TntPrimeEvent;
import io.github.term4.polyp.entity.PrimedTnt;
import io.github.term4.polyp.mechanics.explosion.ExplosionSystem;
import io.github.term4.polyp.presets.vanilla18.Explosion;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** VRI hand ignition: flint and steel converts the block per the clicker's scope; off-scope clicks and cancelled primes leave it. */
class TntIgniteTest extends HeadlessServerTest {

    @BeforeAll
    static void mechanics() {
        ExplosionSystem.install(polyp, Explosion.config());
        Vri.install(polyp, VriConfig.builder().tntIgnite(true).build());
    }

    private static void click(FakePlayer p, BlockVec pos) {
        EventDispatcher.call(new PlayerBlockInteractEvent(p.player, PlayerHand.MAIN, instance,
                instance.getBlock(pos), pos, new Vec(0.5, 1, 0.5), BlockFace.TOP));
    }

    @Test
    void flintAndSteelIgnitesTheBlock() {
        FakePlayer igniter = FakePlayer.connect(instance, new Pos(20.5, 65, 20.5), "TntIgniter");
        igniter.player.setItemInMainHand(ItemStack.of(Material.FLINT_AND_STEEL));
        BlockVec pos = new BlockVec(22, 65, 20);
        instance.setBlock(pos, Block.TNT);

        click(igniter, pos);

        assertTrue(instance.getBlock(pos).air(), "the block became the entity");
        assertTrue(instance.getEntities().stream().anyMatch(e -> e instanceof PrimedTnt), "primed TNT spawned");
    }

    @Test
    void scopeWithTheToggleOffIgnoresTheClick() {
        FakePlayer bystander = FakePlayer.connect(instance, new Pos(30.5, 65, 20.5), "TntBystander");
        bystander.player.setItemInMainHand(ItemStack.of(Material.FLINT_AND_STEEL));
        polyp.profiles().setPlayer(bystander.player, MechanicsProfile.builder()
                .set(MechanicsKeys.VRI, VriConfig.builder().build()).build());
        BlockVec pos = new BlockVec(32, 65, 20);
        instance.setBlock(pos, Block.TNT);

        click(bystander, pos);

        assertEquals(Block.TNT, instance.getBlock(pos), "off-scope click leaves the block");
        polyp.profiles().setPlayer(bystander.player, null);
    }

    @Test
    void aCancelledPrimeLeavesTheBlock() {
        var veto = EventListener.of(TntPrimeEvent.class, e -> e.setCancelled(true));
        MinecraftServer.getGlobalEventHandler().addListener(veto);
        FakePlayer igniter = FakePlayer.connect(instance, new Pos(40.5, 65, 20.5), "TntVetoed");
        igniter.player.setItemInMainHand(ItemStack.of(Material.FLINT_AND_STEEL));
        BlockVec pos = new BlockVec(42, 65, 20);
        instance.setBlock(pos, Block.TNT);
        try {
            click(igniter, pos);
            assertEquals(Block.TNT, instance.getBlock(pos), "cancelled prime keeps the block");
            assertFalse(instance.getEntities().stream().anyMatch(e -> e instanceof PrimedTnt
                    && e.getPosition().sameBlock(new Pos(42.5, 65, 20.5))), "and spawns nothing");
        } finally {
            MinecraftServer.getGlobalEventHandler().removeListener(veto);
        }
    }
}
