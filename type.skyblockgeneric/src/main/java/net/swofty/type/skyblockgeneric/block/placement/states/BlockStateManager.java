package net.swofty.type.skyblockgeneric.block.placement.states;

import net.minestom.server.instance.block.Block;

public class BlockStateManager {

    public static BlockState get(Block block) {
        if (block == Block.BELL)
            return new BellState(block);
        else if (block == Block.CAMPFIRE || block == Block.SOUL_CAMPFIRE)
            return new CampfireState(block);
        else if (block == Block.LECTERN)
            return new LecternState(block);
        else if (block == Block.PISTON_HEAD)
            return new PistonHeadState(block);
        else if (block == Block.SCAFFOLDING)
            return new ScaffoldingState(block);
        else if (block.name().endsWith("stairs"))
            return new StairsState(block);
        else if (block.name().contains("fence_gate"))
            return new FenceGateState(block);
        else if (isWallBlock(block))
            return new WallState(block);
        else if (block.properties().containsKey("axis"))
            return new AxisBlockBlockState(block);
        return new BlockState(block);
    }

    private static boolean isWallBlock(Block block) {
        var properties = block.properties();
        return properties.containsKey("north")
            && properties.containsKey("east")
            && properties.containsKey("south")
            && properties.containsKey("west")
            && properties.containsKey("up");
    }
}
