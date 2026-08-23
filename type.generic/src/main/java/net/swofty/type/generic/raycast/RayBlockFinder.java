package net.swofty.type.generic.raycast;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.ShapeImpl;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.block.BlockIterator;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public record RayBlockFinder(Ray ray, BlockIterator blockIterator, Block.Getter blockGetter,
                             Function<Block, Collection<BoundingBox>> hitboxGetter) implements Iterator<Collection<RayIntersection<Block>>> {

    public static final Function<Block, Collection<BoundingBox>> SOLID_BLOCK_HITBOXES = block -> ((ShapeImpl) block.collisionShape()).boundingBoxes();
    private static final Collection<BoundingBox> CUBE = List.of(new BoundingBox(Vec.ZERO, Vec.ONE));
    public static final Function<Block, Collection<BoundingBox>> SOLID_CUBE_HITBOXES = block -> (block.solid() ? CUBE : List.of());
    public static final Function<Block, Collection<BoundingBox>> CUBE_HITBOXES = block -> (!block.air() ? CUBE : List.of());

    public RayBlockFinder(Ray ray, BlockIterator blockIterator, Block.Getter blockGetter, Function<Block, Collection<BoundingBox>> hitboxGetter) {
        this.ray = Objects.requireNonNull(ray, "ray");
        this.blockIterator = Objects.requireNonNull(blockIterator, "blockIterator");
        this.blockGetter = Objects.requireNonNull(blockGetter, "blockGetter");
        this.hitboxGetter = Objects.requireNonNull(hitboxGetter, "hitboxGetter");
    }

    @Override
    public boolean hasNext() {
        return blockIterator.hasNext();
    }

    @Override
    public List<RayIntersection<Block>> next() {
        if (!blockIterator.hasNext()) return List.of();

        Point point = blockIterator.next();
        return intersectionsAt(point);
    }

    private List<RayIntersection<Block>> intersectionsAt(Point point) {
        Block block = blockGetter.getBlock(point);
        Collection<BoundingBox> hitboxes = hitboxGetter.apply(block);
        if (hitboxes.isEmpty()) {
            return List.of();
        }

        ArrayList<RayIntersection<Block>> results = new ArrayList<>(hitboxes.size());
        for (BoundingBox hitbox : hitboxes) {
            RayIntersection<BoundingBox> result = ray.cast(hitbox, point.asVec());
            if (result != null) {
                results.add(result.withObject(block));
            }
        }
        if (results.isEmpty()) {
            return List.of();
        }

        results.sort(null);
        return results;
    }

    public @Nullable RayIntersection<Block> nextClosest() {
        while (blockIterator.hasNext()) {
            List<RayIntersection<Block>> results = next();
            if (!results.isEmpty()) {
                return results.getFirst();
            }
        }
        return null;
    }
}
