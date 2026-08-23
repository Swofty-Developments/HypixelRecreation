package net.swofty.type.skyblockgeneric.structure.structures;

import lombok.Getter;
import lombok.Setter;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.swofty.type.skyblockgeneric.structure.SkyBlockStructure;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class IslandPortal extends SkyBlockStructure {
    private PortalType type;

    public IslandPortal(int rotation, int x, int y, int z) {
        super(rotation, x, y, z);
    }

    @Override
    public void setBlocks(Instance instance) {
        fill(instance,
                -1, 0, 0,
                1, 4, 0,
                Block.NETHER_PORTAL);

        fill(instance,
                -2, 0, 0,
                -2, 4, 0,
                Block.POLISHED_ANDESITE);

        fill(instance,
                2, 0, 0,
                2, 4, 0,
                Block.POLISHED_ANDESITE);

        fill(instance,
                2, 5, 0,
                -2, 5, 0,
                Block.STONE);

        set(instance, 1, 5, 1, Block.COBBLESTONE_WALL);
        set(instance, -1, 5, 1, Block.COBBLESTONE_WALL);
        set(instance, 1, 5, -1, Block.COBBLESTONE_WALL);
        set(instance, -1, 5, -1, Block.COBBLESTONE_WALL);

        set(instance, 3, 5, 1, Block.COBBLESTONE_WALL);
        set(instance, -3, 5, 1, Block.COBBLESTONE_WALL);
        set(instance, 3, 5, -1, Block.COBBLESTONE_WALL);
        set(instance, -3, 5, -1, Block.COBBLESTONE_WALL);

        fill(instance,
                -3, 6, -1,
                3, 6, 1,
                Block.STONE_BRICK_SLAB);

        fill(instance,
                -3, 4, -1,
                3, 4, -1,
                Block.STONE_BRICK_SLAB.withProperties(Map.of("type", "top")));
        fill(instance,
                -3, 4, 1,
                3, 4, 1,
                Block.STONE_BRICK_SLAB.withProperties(Map.of("type", "top")));

        set(instance, 2, 0, 1, Block.COBBLESTONE_WALL);
        set(instance, -2, 0, 1, Block.COBBLESTONE_WALL);
        set(instance, 2, 1, 1, Block.TORCH);
        set(instance, -2, 1, 1, Block.TORCH);
    }

    @Override
    public List<StructureHologram> getHolograms() {
        return List.of(
                new StructureHologram(new String[]{"<b>Travel to:", "<a>Hub Island"}, 0, 1, 1)
        );
    }

    public enum PortalType {
        HUB
    }
}
