package net.swofty.type.generic.entity.ai.vanilla;

import net.swofty.type.generic.utility.BlockProps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.pathfinding.PNode;
import net.minestom.server.entity.pathfinding.generators.NodeGenerator;
import net.minestom.server.instance.block.Block;

/**
 * A {@link NodeGenerator} that expands nodes with vanilla Minecraft ground-pathfinding rules,
 * ported from {@code net.minecraft.world.level.pathfinder.WalkNodeEvaluator}. It plugs UNDER
 * Minestom's own {@code Navigator} A* (via {@code getNavigator().setNodeGenerator(...)}), so all
 * of Minestom's {@code GoalSelector}s path with vanilla-quality neighbours.
 *
 * <p>Neighbour shape mirrors {@code GroundNodeGenerator} (the reference Minestom implementation):
 * cardinals first, then diagonals that are only emitted when both orthogonal neighbours are clear
 * (vanilla's {@code isDiagonalValid}, prevents corner-cutting through walls). Step-UP is limited to
 * one block ({@link #createJump}); step-DOWN follows the vanilla drop rules up to
 * {@link #MAX_FALL_DISTANCE}. Block semantics (fences, closed doors, lava, damaging blocks, water)
 * come from {@link BlockProps}/{@link PathType}: a negative {@link PathType#getMalus()} rejects the
 * node, a positive one is added to the move cost.
 */
public class VanillaGroundNodeGenerator implements NodeGenerator {
   /** Maximum safe step-down while walking (vanilla {@code PathfinderMob.getMaxFallDistance}). */
   private static final double MAX_FALL_DISTANCE = 3.0;
   private static final double EPSILON = 1.0E-6;

   /** East, West, South, North — indices referenced by the diagonal component table. */
   private static final int[][] CARDINAL = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
   /** The four diagonals, each paired with the two cardinal indices that must both be clear. */
   private static final int[][] DIAGONAL = {{1, 1}, {-1, 1}, {-1, -1}, {1, -1}};
   private static final int[][] DIAGONAL_COMPONENTS = {{0, 2}, {1, 2}, {1, 3}, {0, 3}};

   private final BoundingBox.PointIterator pointIterator = new BoundingBox.PointIterator();

   @Override
   public Collection<? extends PNode> getWalkable(Block.Getter getter, Set<PNode> visited, PNode current, Point target, BoundingBox boundingBox) {
      List<PNode> nearby = new ArrayList<>();

      // Cardinals first, remembering each so diagonals can gate on them (vanilla isDiagonalValid).
      PNode[] cardinalNodes = new PNode[4];
      for (int i = 0; i < 4; i++) {
         PNode node = expand(getter, current, target, boundingBox, CARDINAL[i][0], CARDINAL[i][1], 0.98, true, visited);
         cardinalNodes[i] = node;
         if (node != null) {
            nearby.add(node);
         }
      }

      // Diagonals only when both orthogonal neighbours are clear and not stepping up (no corner cut).
      double diagonalCost = Math.sqrt(2.0) * 0.98;
      for (int i = 0; i < 4; i++) {
         PNode a = cardinalNodes[DIAGONAL_COMPONENTS[i][0]];
         PNode b = cardinalNodes[DIAGONAL_COMPONENTS[i][1]];
         if (!diagonalAllowed(current, a, b)) {
            continue;
         }
         PNode node = expand(getter, current, target, boundingBox, DIAGONAL[i][0], DIAGONAL[i][1], diagonalCost, false, visited);
         if (node != null) {
            nearby.add(node);
         }
      }

      return nearby;
   }

   /**
    * Produce the best neighbour for one horizontal offset: prefer a same-level / drop WALK node,
    * otherwise (cardinals only) a single-block step-UP.
    */
   private PNode expand(
      Block.Getter getter, PNode current, Point target, BoundingBox boundingBox, int dx, int dz, double baseCost, boolean allowJump, Set<PNode> visited
   ) {
      double px = current.blockX() + 0.5 + dx;
      double pz = current.blockZ() + 0.5 + dz;
      int cy = current.blockY();

      // WALK / FALL: snap to the floor at or below the current feet level.
      OptionalDouble walkY = gravitySnap(getter, px, cy, pz, boundingBox, MAX_FALL_DISTANCE);
      if (walkY.isPresent()) {
         PNode walk = createWalk(getter, new Vec(px, walkY.getAsDouble(), pz), boundingBox, baseCost, current, target, visited);
         if (walk != null) {
            return walk;
         }
      }

      // JUMP: snap from one block up; only accept if it genuinely climbs.
      if (allowJump) {
         OptionalDouble jumpY = gravitySnap(getter, px, cy + 1, pz, boundingBox, MAX_FALL_DISTANCE);
         if (jumpY.isPresent()) {
            Vec jumpPoint = new Vec(px, jumpY.getAsDouble(), pz);
            if (jumpPoint.blockY() > cy) {
               PNode jump = createJump(getter, jumpPoint, boundingBox, baseCost + 0.2, current, target, visited);
               if (jump != null) {
                  return jump;
               }
            }
         }
      }

      return null;
   }

   private PNode createWalk(Block.Getter getter, Point point, BoundingBox boundingBox, double cost, PNode current, Point target, Set<PNode> visited) {
      float malus = malusAt(getter, point);
      if (malus < 0.0F) {
         return null;
      }
      PNode node = newNode(current, cost + malus, point, target);
      if (visited.contains(node)) {
         return null;
      }
      Point start = new Vec(current.x(), current.y(), current.z());
      if (Math.abs(point.y() - current.y()) > EPSILON && point.y() < current.y()) {
         if (current.y() - point.y() > MAX_FALL_DISTANCE) {
            return null;
         }
         if (!canMoveTowards(getter, start, point.withY(current.y()), boundingBox)) {
            return null;
         }
         node.setType(PNode.Type.FALL);
      } else if (!canMoveTowards(getter, start, point, boundingBox)) {
         return null;
      }
      return node;
   }

   private PNode createJump(Block.Getter getter, Point point, BoundingBox boundingBox, double cost, PNode current, Point target, Set<PNode> visited) {
      if (Math.abs(point.y() - current.y()) < EPSILON || point.y() - current.y() > 2.0) {
         return null;
      }
      float malus = malusAt(getter, point);
      if (malus < 0.0F) {
         return null;
      }
      PNode node = newNode(current, cost + malus, point, target);
      if (visited.contains(node)) {
         return null;
      }
      // The destination body space and the space directly above the head at the start must be free.
      if (pointInvalid(getter, point, boundingBox) || pointInvalid(getter, new Vec(current.x(), current.y() + 1.0, current.z()), boundingBox)) {
         return null;
      }
      node.setType(PNode.Type.JUMP);
      return node;
   }

   private PNode newNode(PNode current, double cost, Point point, Point target) {
      return new PNode(point, current.g() + cost, heuristic(point, target), PNode.Type.WALK, current);
   }

   /** A diagonal is valid when both orthogonal neighbours exist and neither steps up above current. */
   private boolean diagonalAllowed(PNode current, PNode a, PNode b) {
      return a != null && b != null && a.y() <= current.y() + EPSILON && b.y() <= current.y() + EPSILON;
   }

   private float malusAt(Block.Getter getter, Point point) {
      return pathType(getter, point.blockX(), point.blockY(), point.blockZ()).getMalus();
   }

   @Override
   public boolean hasGravitySnap() {
      return true;
   }

   @Override
   public OptionalDouble gravitySnap(Block.Getter getter, double pointOrgX, double pointOrgY, double pointOrgZ, BoundingBox boundingBox, double maxFall) {
      double pointX = (int) Math.floor(pointOrgX) + 0.5;
      double pointY = (int) Math.floor(pointOrgY);
      double pointZ = (int) Math.floor(pointOrgZ) + 0.5;

      for (int axis = 1; axis <= maxFall; axis++) {
         this.pointIterator.reset(boundingBox, pointX, pointY, pointZ, BoundingBox.AxisMask.Y, -axis);
         while (this.pointIterator.hasNext()) {
            BoundingBox.MutablePoint block = this.pointIterator.next();
            if (getter.getBlock(block.blockX(), block.blockY(), block.blockZ(), Block.Getter.Condition.TYPE).solid()) {
               return OptionalDouble.of(block.blockY() + 1);
            }
         }
      }

      return OptionalDouble.empty();
   }

   // ----- vanilla block classification (port of WalkNodeEvaluator.getPathType*) -----

   /** Classify the node a mob would stand at, deriving WALKABLE/danger types from the block below. */
   private static PathType pathType(Block.Getter getter, int x, int y, int z) {
      PathType type = pathTypeFromState(getter.getBlock(x, y, z));
      if (type == PathType.OPEN) {
         return switch (pathTypeFromState(getter.getBlock(x, y - 1, z))) {
            case OPEN, WATER, LAVA, WALKABLE -> PathType.OPEN;
            case FIRE -> PathType.FIRE;
            case DAMAGING -> PathType.DAMAGING;
            case STICKY_HONEY -> PathType.STICKY_HONEY;
            case POWDER_SNOW -> PathType.ON_TOP_OF_POWDER_SNOW;
            case DAMAGE_CAUTIOUS -> PathType.DAMAGE_CAUTIOUS;
            case TRAPDOOR -> PathType.ON_TOP_OF_TRAPDOOR;
            default -> PathType.WALKABLE;
         };
      }
      return type;
   }

   /** Port of vanilla {@code getPathTypeFromState}, classifying a single block by key/properties. */
   private static PathType pathTypeFromState(Block block) {
      if (block.air()) {
         return PathType.OPEN;
      } else if (BlockProps.isTrapdoor(block)) {
         return PathType.TRAPDOOR;
      } else if (BlockProps.isPowderSnow(block)) {
         return PathType.POWDER_SNOW;
      } else if (BlockProps.isCactusOrBerryBush(block)) {
         return PathType.DAMAGING;
      } else if (BlockProps.isHoney(block)) {
         return PathType.STICKY_HONEY;
      } else if (BlockProps.isCocoa(block)) {
         return PathType.COCOA;
      } else if (BlockProps.isWitherRose(block) || BlockProps.isSpeleothem(block)) {
         return PathType.DAMAGE_CAUTIOUS;
      } else if (BlockProps.isLava(block)) {
         return PathType.LAVA;
      } else if (BlockProps.isBurningBlock(block)) {
         return PathType.FIRE;
      } else if (BlockProps.isDoor(block)) {
         if (BlockProps.isOpen(block)) {
            return PathType.DOOR_OPEN;
         }
         return BlockProps.isWoodenDoor(block) ? PathType.DOOR_WOOD_CLOSED : PathType.DOOR_IRON_CLOSED;
      } else if (BlockProps.isRail(block)) {
         return PathType.RAIL;
      } else if (BlockProps.isLeaves(block)) {
         return PathType.LEAVES;
      } else if (BlockProps.isFence(block) || (BlockProps.isFenceGate(block) && !BlockProps.isOpen(block))) {
         return PathType.FENCE;
      } else if (!BlockProps.isPathfindable(block, PathComputationType.LAND)) {
         return PathType.BLOCKED;
      } else {
         return BlockProps.isWater(block) ? PathType.WATER : PathType.OPEN;
      }
   }
}
