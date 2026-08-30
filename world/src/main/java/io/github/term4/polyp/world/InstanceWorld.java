package io.github.term4.polyp.world;

import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.EntityCollisionResult;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.collision.PhysicsUtils;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Weather;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.utils.PacketSendingUtils;
import net.minestom.server.utils.chunk.ChunkCache;
import net.minestom.server.world.DimensionType;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/** A {@link MechanicsWorld} over a plain Minestom {@link Instance}: every operation delegates 1:1, behavior identical. */
public class InstanceWorld implements MechanicsWorld {

    private final Instance instance;
    // world metadata, NOT the instance's tags: world scope stays distinct even on a plain instance
    private final TagHandler tags = TagHandler.newHandler();

    protected InstanceWorld(Instance instance) {
        this.instance = instance;
    }

    @Override public @NotNull Instance instance() { return instance; }

    @Override public @NotNull TagHandler tagHandler() { return tags; }

    @Override public @NotNull Block getBlock(@NotNull Point pos) { return instance.getBlock(pos); }

    @Override public @NotNull Block getBlock(int x, int y, int z) { return instance.getBlock(x, y, z); }

    @Override public @NotNull Block getBlock(@NotNull Point pos, Block.Getter.@NotNull Condition condition) {
        return instance.getBlock(pos, condition);
    }

    @Override public @NotNull Block getBlock(int x, int y, int z, Block.Getter.@NotNull Condition condition) {
        return instance.getBlock(x, y, z, condition);
    }

    @Override public void setBlock(@NotNull Point pos, @NotNull Block block) { instance.setBlock(pos, block); }

    @Override public boolean isChunkLoaded(@NotNull Point pos) { return instance.isChunkLoaded(pos); }

    @Override public boolean isChunkLoaded(int chunkX, int chunkZ) { return instance.isChunkLoaded(chunkX, chunkZ); }

    @Override public @NotNull DimensionType dimension() { return instance.getCachedDimensionType(); }

    /** The unloaded-chunks-SOLID block view at {@code position} (what the Instance physics overload builds). */
    public static Block.Getter solidGetter(Instance instance, Pos position) {
        return new ChunkCache(instance, instance.getChunkAt(position), Block.STONE);
    }

    /** The unloaded-chunks-EMPTY block view at {@code position} (projectile flight). */
    public static Block.Getter airGetter(Instance instance, Pos position) {
        return new ChunkCache(instance, instance.getChunkAt(position), Block.AIR);
    }

    @Override
    public PhysicsResult sweep(@NotNull BoundingBox box, @NotNull Pos position, @NotNull Vec velocity,
                               @Nullable PhysicsResult last, boolean singleCollision) {
        return CollisionUtils.handlePhysics(solidGetter(instance, position), box, position, velocity, last, singleCollision);
    }

    @Override
    public PhysicsResult sweepLoaded(@NotNull BoundingBox box, @NotNull Pos position, @NotNull Vec velocity,
                                     @Nullable PhysicsResult last, boolean singleCollision) {
        return CollisionUtils.handlePhysics(airGetter(instance, position), box, position, velocity, last, singleCollision);
    }

    @Override
    public Collection<EntityCollisionResult> sweepEntities(@NotNull BoundingBox box, @NotNull Pos position, @NotNull Vec velocity,
                                                           double extendRadius, @NotNull Function<Entity, Boolean> filter,
                                                           @Nullable PhysicsResult physics) {
        return CollisionUtils.checkEntityCollisions(instance, box, position, velocity, extendRadius, filter, physics);
    }

    @Override
    public PhysicsResult simulateMovement(@NotNull Pos position, @NotNull Vec velocityPerTick, @NotNull BoundingBox box,
                                          @NotNull Aerodynamics aerodynamics, boolean noGravity, boolean hasPhysics,
                                          boolean onGround, @Nullable PhysicsResult last) {
        return PhysicsUtils.simulateMovement(position, velocityPerTick, box, instance.getWorldBorder(),
                solidGetter(instance, position), aerodynamics, noGravity, hasPhysics, onGround, false, last);
    }

    @Override public @NotNull WorldBorder worldBorder() { return instance.getWorldBorder(); }

    @Override public boolean isInVoid(@NotNull Point pos) { return instance.isInVoid(pos); }

    @Override public @NotNull Collection<@NotNull Entity> entities() { return instance.getEntities(); }

    @Override public @NotNull Collection<@NotNull Entity> nearbyEntities(@NotNull Point point, double range) {
        return instance.getNearbyEntities(point, range);
    }

    @Override public void nearbyPlayers(@NotNull Point point, double range, @NotNull Consumer<Player> consumer) {
        instance.getEntityTracker().nearbyEntities(point, range, EntityTracker.Target.PLAYERS, consumer);
    }

    @Override public @NotNull CompletableFuture<Void> spawn(@NotNull Entity entity, @NotNull Pos position) {
        return entity.setInstance(instance, position);
    }

    @Override public @NotNull Collection<@NotNull Player> players() { return instance.getPlayers(); }

    @Override public @NotNull Collection<@NotNull Player> watchers() { return instance.getPlayers(); }

    @Override public void broadcast(@NotNull ServerPacket packet) {
        PacketSendingUtils.sendGroupedPacket(instance.getPlayers(), packet);
    }

    @Override public void playSound(@NotNull Sound sound, @NotNull Point pos) {
        instance.playSound(sound, pos.x(), pos.y(), pos.z());
    }

    @Override public @NotNull Weather weather() { return instance.getWeather(); }

    @Override public void setWeather(@NotNull Weather weather) { instance.setWeather(weather); }

    @Override public long time() { return instance.getTime(); }

    @Override public void setTime(long time) { instance.setTime(time); }

    @Override public long worldAge() { return instance.getWorldAge(); }

    @Override public void scheduleNextTick(@NotNull Consumer<MechanicsWorld> task) {
        instance.scheduleNextTick(in -> task.accept(this));
    }

    @Override public boolean equals(Object o) { return o instanceof InstanceWorld s && s.instance == instance; }

    @Override public int hashCode() { return instance.hashCode(); }
}
