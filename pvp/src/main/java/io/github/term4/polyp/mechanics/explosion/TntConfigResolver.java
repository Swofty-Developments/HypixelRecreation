package io.github.term4.polyp.mechanics.explosion;

import io.github.term4.polyp.codegen.CheckResolveOrder;
import io.github.term4.polyp.Services;
import io.github.term4.polyp.api.event.explosion.TntPrimeEvent;
import io.github.term4.polyp.config.FieldValue;
import io.github.term4.polyp.entity.PrimedTnt;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.Nullable;


@CheckResolveOrder
public final class TntConfigResolver {

    /** One prime's inputs; {@code igniter} null for a sourceless prime. */
    public record TntContext(@Nullable Entity igniter, MechanicsWorld world, TntPrimeEvent.Cause cause,
                             Services services) {}

    private TntConfigResolver() {}

    /** {@link #resolve(TntConfig, TntContext)} with the context built inline. */
    public static PrimedTnt.Config resolve(@Nullable TntConfig cfg, @Nullable Entity igniter, MechanicsWorld world,
                                           TntPrimeEvent.Cause cause, Services services) {
        return resolve(cfg, new TntContext(igniter, world, cause, services));
    }

    /** The concrete knobs for one prime; {@code null} config = vanilla. */
    public static PrimedTnt.Config resolve(@Nullable TntConfig cfg, TntContext ctx) {
        PrimedTnt.Config v = PrimedTnt.VANILLA;
        if (cfg == null) return v;
        return new PrimedTnt.Config(
                FieldValue.resolve(cfg.fuseTicks, ctx, v.fuseTicks()),
                FieldValue.resolve(cfg.power, ctx, v.power()),
                FieldValue.resolve(cfg.detonateAtFeet, ctx, v.detonateAtFeet()),
                FieldValue.resolve(cfg.wire, ctx, v.wire()),
                FieldValue.resolve(cfg.bounce, ctx, v.bounce()),
                FieldValue.resolve(cfg.tntVictimScale, ctx, v.tntVictimScale()),
                FieldValue.resolve(cfg.igniteOnPlace, ctx, v.igniteOnPlace()));
    }
}
