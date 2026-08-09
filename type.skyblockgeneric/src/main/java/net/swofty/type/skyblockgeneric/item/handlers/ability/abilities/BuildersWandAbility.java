package net.swofty.type.skyblockgeneric.item.handlers.ability.abilities;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class BuildersWandAbility extends RegisteredAbility {

    public BuildersWandAbility() {
        super("GRAND_ARCHITECT", "Grand Architect",
                (player, item) -> "Right-click the face of a block to extend all connected block faces.",
                AbilityActivation.RIGHT_CLICK_BLOCK, 0, new RegisteredAbility.NoAbilityCost(),
                (player, item, origin, face) -> {
                    return fillConnectedFaces(player, new Pos(origin), face);
                });
    }

    private static boolean fillConnectedFaces(SkyBlockPlayer player, Pos origin, BlockFace face) {
        Material fillMaterial = Material.fromKey(player.getInstance().getBlock(origin).key());
        int blocksInInventory = player.countItem(ItemType.fromMaterial(fillMaterial));
        int blockLimit = 164;
        if (blocksInInventory < blockLimit)
            blockLimit = blocksInInventory;

        List<Pos> blocks = new ArrayList<>();
        List<Pos> blocksForUndo = new ArrayList<>(); // NOTE: To be used later
        blocks.add(origin);
        Instance w = player.getInstance();
        Vec[] check;
        Vec translate;
        int blocksPlaced = 0;

        check = switch (face) {
            case NORTH, SOUTH ->
                    new Vec[]{new Vec(-1, -1, 0), new Vec(-1, 0, 0), new Vec(-1, 1, 0), new Vec(0, -1, 0), new Vec(0, 1, 0), new Vec(1, -1, 0), new Vec(1, 0, 0), new Vec(1, 1, 0)};
            case EAST, WEST ->
                    new Vec[]{new Vec(0, -1, -1), new Vec(0, -1, 0), new Vec(0, -1, 1), new Vec(0, 0, -1), new Vec(0, 0, 1), new Vec(0, 1, -1), new Vec(0, 1, 0), new Vec(0, 1, 1)};
            case TOP, BOTTOM ->
                    new Vec[]{new Vec(-1, 0, -1), new Vec(-1, 0, 0), new Vec(-1, 0, 1), new Vec(0, 0, -1), new Vec(0, 0, 1), new Vec(1, 0, -1), new Vec(1, 0, 0), new Vec(1, 0, 1)};
        };
        translate = switch (face) {
            case NORTH -> new Vec(0, 0, -1);
            case SOUTH -> new Vec(0, 0, 1);
            case EAST -> new Vec(1, 0, 0);
            case WEST -> new Vec(-1, 0, 0);
            case TOP -> new Vec(0, 1, 0);
            case BOTTOM -> new Vec(0, -1, 0);
        };
        while (!blocks.isEmpty() && blockLimit > 0) {
            Pos l = (blocks.getFirst());
            for (Vec vector : check) {
                if (w.getBlock(l.add(vector)).key().equals(fillMaterial.key()) && w
                        .getBlock(l.add(vector).add(translate)).key().equals(Material.AIR.key())) {
                    blocks.add(l.add(vector));
                }
            }
            Pos fillBlock = l.add(translate);
            if (HypixelConst.isIslandServer() || HypixelConst.isGarden()) {
                blocks.removeIf(blocks.getFirst()::equals);
                if (!player.getInstance().getBlock(fillBlock).key().equals(fillMaterial.key())) {
                    player.getInstance().setBlock(fillBlock, Block.fromKey(fillMaterial.key()));

                    blockLimit--;
                    blocksPlaced++;
                    blocksForUndo.add(fillBlock);
                }
                if (blocksPlaced == blockLimit)
                    break;
                continue;
            }
            blocks.removeIf(blocks.getFirst()::equals);
            blockLimit--;
        }
        if (blocksPlaced == 0) {
            player.sendMessage("§cYou cannot place any blocks! You do not have enough blocks to place with your Builder's wand!");
            return false;
        }
        player.takeItem(ItemType.fromMaterial(fillMaterial), blocksPlaced);
        player.sendMessage("&eYou built &a" + blocksPlaced + "&e blocks!");
        return true;
    }
}
