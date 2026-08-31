package net.swofty.type.skywarsgame.util;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.api.event.explosion.TntPrimeEvent;
import io.github.term4.polyp.entity.PrimedTnt;
import io.github.term4.polyp.mechanics.explosion.ExplosionSystem;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.Nullable;

public final class PolypTnt {

    private PolypTnt() {
    }

    public static @Nullable PrimedTnt spawn(Entity igniter, Point position, int fuseTicks) {
        if (igniter.getInstance() == null) return null;

        ExplosionSystem explosion = Polyp.getInstance().services().explosion();
        if (explosion == null) return null;

        MechanicsWorld world = MechanicsWorld.of(igniter);
        PrimedTnt.Config base = explosion.resolveTnt(igniter, world, TntPrimeEvent.Cause.API);
        PrimedTnt.Config config = new PrimedTnt.Config(
                fuseTicks,
                base.power(),
                base.detonateAtFeet(),
                base.wire(),
                base.bounce(),
                base.tntVictimScale(),
                base.igniteOnPlace()
        );
        PrimedTnt tnt = PrimedTnt.spawn(explosion, world,
                new BlockVec(position.blockX(), position.blockY(), position.blockZ()),
                config, igniter, TntPrimeEvent.Cause.API);
        if (tnt != null) tnt.teleport(new Pos(position.x(), position.y(), position.z()));
        return tnt;
    }
}
