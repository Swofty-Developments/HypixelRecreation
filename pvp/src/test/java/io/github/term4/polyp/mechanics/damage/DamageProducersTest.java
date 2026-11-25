package io.github.term4.polyp.mechanics.damage;

import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Who environmental producers skip: the dead, creative, spectator and flying - NOT a view-only observer. */
class DamageProducersTest extends HeadlessServerTest {

    /**
     * A shard spectator views a world it isn't bound to. Exempting it made spectators immune to lava, drowning and
     * fall, which is the bug: the view is a DIFF over the base, so it reads the very blocks they stand in, and they
     * collide with exactly what the producers read.
     */
    @Test
    void viewOnlyObserversStillTakeEnvironmentalDamage() {
        var observer = FakePlayer.connect(instance, new Pos(0.5, 66, 0.5), "EnvObserver");
        Instance other = MinecraftServer.getInstanceManager().createInstanceContainer();
        try {
            assertFalse(DamageProducers.exempt(observer.player), "a plain survival player takes environmental damage");

            MechanicsWorld viewed = MechanicsWorld.of(other);
            MechanicsWorld.resolver(new MechanicsWorld.Resolver() {
                @Override public @Nullable MechanicsWorld resolve(@NotNull Entity entity) {
                    return entity.getTag(MechanicsWorld.ENTITY_TAG);
                }
                @Override public @Nullable MechanicsWorld viewedBlocks(@NotNull Player player) {
                    return player == observer.player ? viewed : null;
                }
            });
            assertFalse(MechanicsWorld.viewed(observer.player) == MechanicsWorld.of(observer.player),
                    "the harness really is an observer");
            assertFalse(DamageProducers.exempt(observer.player), "watching a shard grants no environmental immunity");
        } finally {
            MechanicsWorld.resolver(MechanicsWorld.Resolver.DEFAULT);
            observer.player.remove();
            MinecraftServer.getInstanceManager().unregisterInstance(other);
        }
    }

    @Test
    void deadCreativeSpectatorAndFlyingStayExempt() {
        var p = FakePlayer.connect(instance, new Pos(2.5, 66, 2.5), "EnvExempt");
        try {
            p.player.setGameMode(GameMode.CREATIVE);
            assertTrue(DamageProducers.exempt(p.player));
            p.player.setGameMode(GameMode.SPECTATOR);
            assertTrue(DamageProducers.exempt(p.player));
            p.player.setGameMode(GameMode.SURVIVAL);
            p.player.setFlying(true);
            assertTrue(DamageProducers.exempt(p.player));
            p.player.setFlying(false);
            assertFalse(DamageProducers.exempt(p.player));
        } finally {
            p.player.remove();
        }
    }
}
