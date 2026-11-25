package net.swofty.type.skyblockgeneric.block.placement;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.swofty.type.skyblockgeneric.block.placement.rules.*;

import java.util.function.Function;
import java.util.function.Predicate;

public enum BlockPlacementManager {
    FACING(
            block -> block.properties().containsKey("facing"),
            FacingPlacement::new
    ),
    AXIS(
            block -> block.properties().containsKey("axis"),
            AxisPlacement::new
    ),
    WATERLOGGED(
            block -> block.properties().containsKey("waterlogged"),
            DisableWaterLogPlacement::new
    ),
    ROTATION(
            block -> block.properties().containsKey("rotation"),
            RotationPlacement::new
    ),
    DOUBLE_PLANT(
            DoublePlantPlacement::isDoublePlant,
            DoublePlantPlacement::new
    ),
    STAIRS(
            StairsPlacement::isStairs,
            StairsPlacement::new
    ),
    CARDINAL(
            CardinalPlacement::isCardinalBlock,
            CardinalPlacement::new
    ),
    GRINDSTONE(
            block -> block == Block.GRINDSTONE,
            _ -> new GrindstonePlacement()
    ),
    REDSTONE_WIRE(
            block -> block == Block.REDSTONE_WIRE,
            _ -> new RedstonePlacement()
    ),
    BAMBOO(
            block -> block == Block.BAMBOO,
            _ -> new BambooPlacement()
    ),
    BELL(
            block -> block == Block.BELL,
            _ -> new BellPlacement()
    ),
    TWISTING_VINES(
            block -> block == Block.TWISTING_VINES,
            _ -> new TwistingVinesPlacement()
    ),
    WEEPING_VINES(
            block -> block == Block.WEEPING_VINES,
            _ -> new WeepingVinesPlacement()
    ),
    SNOW(
            block -> block == Block.SNOW,
            _ -> new SnowLayerPlacement()
    ),
    TURTLE_EGG(
            block -> block == Block.TURTLE_EGG,
            _ -> new TurtleEggPlacement()
    ),
    PLAYER_HEAD(
            block -> block == Block.PLAYER_HEAD,
            block -> new HeadPlacement(block, Block.PLAYER_WALL_HEAD)
    ),
    ZOMBIE_HEAD(
            block -> block == Block.ZOMBIE_HEAD,
            block -> new HeadPlacement(block, Block.ZOMBIE_WALL_HEAD)
    ),
    CREEPER_HEAD(
            block -> block == Block.CREEPER_HEAD,
            block -> new HeadPlacement(block, Block.CREEPER_WALL_HEAD)
    ),
    DRAGON_HEAD(
            block -> block == Block.DRAGON_HEAD,
            block -> new HeadPlacement(block, Block.DRAGON_WALL_HEAD)
    ),
    SKELETON_SKULL(
            block -> block == Block.SKELETON_SKULL,
            block -> new HeadPlacement(block, Block.SKELETON_WALL_SKULL)
    ),
    LANTERN(
            block -> block == Block.LANTERN || block == Block.SOUL_LANTERN,
            LanternPlacement::new
    ),
    TORCH(
            block -> block == Block.TORCH,
            block -> new TorchPlacement(block, Block.WALL_TORCH)
    ),
    REDSTONE_TORCH(
            block -> block == Block.REDSTONE_TORCH,
            block -> new TorchPlacement(block, Block.REDSTONE_WALL_TORCH)
    ),
    SOUL_TORCH(
            block -> block == Block.SOUL_TORCH,
            block -> new TorchPlacement(block, Block.SOUL_WALL_TORCH)
    ),
    CHEST(
            block -> block == Block.CHEST || block == Block.TRAPPED_CHEST,
            ChestBlockPlacement::new
    ),
    WALL(
        BlockPlacementManager::isWallBlock,
            WallPlacement::new
    ),
    SLAB(
            block -> block.name().toLowerCase().contains("slab"),
            SlabPlacement::new
    ),
    ANVIL(
            block -> block.name().toLowerCase().contains("anvil"),
            AnvilPlacement::new
    ),
    BED(
            block -> block.name().toLowerCase().contains("bed") && block != Block.BEDROCK,
            BedPlacement::new
    ),
    TRAPDOOR(
            block -> block.name().toLowerCase().contains("trapdoor"),
            TrapdoorPlacement::new
    ),
    DOOR(
            block -> block.name().toLowerCase().contains("door") && !block.name().toLowerCase().contains("trapdoor"),
            DoorPlacement::new
    ),
    BUTTON_AND_LEVER(
            block -> block.name().toLowerCase().contains("button") || block == Block.LEVER,
            ButtonAndLeverPlacement::new
    ),
    FENCE_GATE(
            block -> block.name().toLowerCase().contains("fence_gate"),
            FenceGatePlacement::new
    ),
    UNKNOWN(
            _ -> true, // Default rule for any block that doesn't match the above conditions
            UnknownPlacement::new
    );

    private final Predicate<Block> condition;
    private final Function<Block, PlacementRule> placementSupplier;

    BlockPlacementManager(Predicate<Block> condition, Function<Block, PlacementRule> placementSupplier) {
        this.condition = condition;
        this.placementSupplier = placementSupplier;
    }

    private static boolean isWallBlock(Block block) {
        var properties = block.properties();
        return properties.containsKey("north")
            && properties.containsKey("east")
            && properties.containsKey("south")
            && properties.containsKey("west")
            && properties.containsKey("up");
    }

    public static void register(Block block) {
        MultiplePlacementRules placementRules = new MultiplePlacementRules(block);

        for (BlockPlacementManager rule : values()) {
            if (rule.condition.test(block)) {
                placementRules.addBlockPlacement(rule.placementSupplier.apply(block));
            }
        }

        MinecraftServer.getBlockManager().registerBlockPlacementRule(placementRules);
    }

    public static void registerAll() {
        for (Block block : Block.values()) {
            register(block);
        }
    }
}
