package io.github.term4.polyp.fx;

import io.github.term4.polyp.world.MechanicsWorld;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Where + who an {@link FxHandler} plays for: a {@code position} in a shard-scoped {@link MechanicsWorld}, an optional
 * {@code source} entity (entity animations ride its viewers), and an optional {@code target} (hit feedback). The emit
 * helpers route through the world / the source's viewers, so the audience follows the shard.
 */
public final class FxContext {

    private final MechanicsWorld world;
    private final Point position;
    private final @Nullable Entity source;
    private final @Nullable Entity target;

    private FxContext(MechanicsWorld world, Point position, @Nullable Entity source, @Nullable Entity target) {
        this.world = world;
        this.position = position;
        this.source = source;
        this.target = target;
    }

    /** At {@code source}'s position, with animations riding its viewers. */
    public static @NotNull FxContext of(@NotNull Entity source) {
        return new FxContext(MechanicsWorld.of(source), source.getPosition(), source, null);
    }

    /** From {@code source} onto {@code target} - hit feedback ({@code source} = attacker, {@code target} = victim). */
    public static @NotNull FxContext of(@NotNull Entity source, @NotNull Entity target) {
        return new FxContext(MechanicsWorld.of(source), source.getPosition(), source, target);
    }

    /** A positional fx at {@code position} in {@code world} (no source entity). */
    public static @NotNull FxContext at(@NotNull MechanicsWorld world, @NotNull Point position) {
        return new FxContext(world, position, null, null);
    }

    /** A positional fx with a {@code source} for registry scope resolution (an explosion at its center, scoped by its igniter). */
    public static @NotNull FxContext at(@NotNull MechanicsWorld world, @NotNull Point position, @Nullable Entity source) {
        return new FxContext(world, position, source, null);
    }

    public @NotNull MechanicsWorld world() { return world; }
    public @NotNull Point position() { return position; }
    public @Nullable Entity source() { return source; }
    public @Nullable Entity target() { return target; }

    /** A positional sound at {@link #position()} to the shard audience. */
    public void sound(@NotNull SoundEvent sound, @NotNull Sound.Source src, float volume, float pitch) {
        world.playSound(Sound.sound(sound.key(), src, volume, pitch), position);
    }

    /**
     * A positional sound at {@link #position()} to the {@code source}'s viewers but NOT the source itself - the sound
     * analogue of {@link #hitAnimation}: the eater's own client predicts the chew locally, so echoing it would double it.
     * No-op without a source.
     */
    public void viewerSound(@NotNull SoundEvent sound, @NotNull Sound.Source src, float volume, float pitch) {
        if (source != null) source.sendPacketToViewers(
                new SoundEffectPacket(sound, src, position, volume, pitch, ThreadLocalRandom.current().nextLong()));
    }

    /**
     * A sound to the {@code source} entity ONLY, if it's a player (the arrow hit-marker "ding" to the shooter).
     * Positional at the source, so that one player hears it at full volume.
     */
    public void sourceSound(@NotNull SoundEvent sound, @NotNull Sound.Source src, float volume, float pitch) {
        if (source instanceof Player p)
            p.sendPacket(new SoundEffectPacket(sound, src, source.getPosition(), volume, pitch, ThreadLocalRandom.current().nextLong()));
    }

    /** The sound to every player in the world, positioned at each LISTENER so distance never attenuates it.
     *  One shared seed: variant-picking sounds pick the same variant for everyone. */
    public void globalSound(@NotNull SoundEvent sound, @NotNull Sound.Source src, float volume, float pitch) {
        long seed = ThreadLocalRandom.current().nextLong();
        for (Player p : world.players()) {
            p.sendPacket(new SoundEffectPacket(sound, src, p.getPosition(), volume, pitch, seed));
        }
    }

    /** A particle burst at {@link #position()} to the shard audience. */
    public void particle(@NotNull Particle particle, int count, double offsetX, double offsetY, double offsetZ, float speed) {
        world.broadcast(new ParticlePacket(particle, position.x(), position.y(), position.z(),
                (float) offsetX, (float) offsetY, (float) offsetZ, speed, count));
    }

    /** An entity animation on the {@code source} to its viewers + itself; no-op without a source. */
    public void entityAnimation(EntityAnimationPacket.@NotNull Animation animation) {
        if (source != null) source.sendPacketToViewersAndSelf(new EntityAnimationPacket(source.getEntityId(), animation));
    }

    /**
     * A hit animation on the {@code target} (crit / magic-crit sparkle) to everyone tracking the {@code source} but NOT
     * the source itself: both the 1.8 and 26.1 client predict their own crit locally ({@code EntityPlayerSP.onCriticalHit}
     * / {@code LocalPlayer.crit}), so echoing it back doubles the particles. Vanilla sends to self anyway and thus
     * doubles; polyp doesn't. Universal - not version-gated.
     */
    public void hitAnimation(EntityAnimationPacket.@NotNull Animation animation) {
        if (source != null && target != null) {
            source.sendPacketToViewers(new EntityAnimationPacket(target.getEntityId(), animation));
        }
    }

    /**
     * {@link #hitAnimation} including the source itself: a server-filled (fake) hit never registered on the attacker's
     * client, so it predicts nothing and must be sent the sparkle too.
     */
    public void hitAnimationAll(EntityAnimationPacket.@NotNull Animation animation) {
        if (source != null && target != null) {
            source.sendPacketToViewersAndSelf(new EntityAnimationPacket(target.getEntityId(), animation));
        }
    }
}
