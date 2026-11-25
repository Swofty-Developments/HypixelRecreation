package io.github.term4.polyp.fx;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

/**
 * A pluggable audiovisual fx: given an {@link FxContext} (where + who), emits sounds / particles / entity
 * animations to the shard-scoped audience. Registered per {@link Fx key} in an {@link FxRegistry}. Runs
 * on the caller's thread (the shard's clock); its only side effect is packet sends, which are thread-safe.
 */
@FunctionalInterface
public interface FxHandler {

    void play(@NotNull FxContext ctx);

    /** Plays nothing - register a key to this to silence that key. */
    FxHandler NONE = ctx -> {};

    /** A positional sound at the context position, to the shard audience. */
    static @NotNull FxHandler sound(@NotNull SoundEvent sound, @NotNull Sound.Source source, float volume, float pitch) {
        return ctx -> ctx.sound(sound, source, volume, pitch);
    }

    /** A symmetric particle burst at the context position. */
    static @NotNull FxHandler particle(@NotNull Particle particle, int count, double spread, float speed) {
        return ctx -> ctx.particle(particle, count, spread, spread, spread, speed);
    }

    /** An entity animation on the context source, to its viewers + itself. */
    static @NotNull FxHandler entityAnimation(EntityAnimationPacket.@NotNull Animation animation) {
        return ctx -> ctx.entityAnimation(animation);
    }

    /** A hit animation on the context target, to the source's viewers - not the source (it predicts its own). */
    static @NotNull FxHandler hitAnimation(EntityAnimationPacket.@NotNull Animation animation) {
        return ctx -> ctx.hitAnimation(animation);
    }

    /** {@link #hitAnimation} including the source - for a server-filled hit its client predicted nothing. */
    static @NotNull FxHandler hitAnimationAll(EntityAnimationPacket.@NotNull Animation animation) {
        return ctx -> ctx.hitAnimationAll(animation);
    }

    /** This fx, then {@code next}. */
    default @NotNull FxHandler and(@NotNull FxHandler next) {
        return ctx -> { play(ctx); next.play(ctx); };
    }
}
