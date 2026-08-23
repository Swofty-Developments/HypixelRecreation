package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.command.builder.arguments.number.ArgumentLong;
import net.minestom.server.instance.SharedInstance;
import net.minestom.server.instance.block.Block;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.skyblockgeneric.structure.tree.TreeRegistry;
import net.swofty.type.skyblockgeneric.structure.tree.TreeRegistry.RegisteredTree;
import net.swofty.type.skyblockgeneric.structure.tree.TreeRegistry.BlockEntry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.generic.user.categories.Rank;

import java.util.List;

@CommandParameters(
        labels = "regrowtrees",
        description = "Respawns trees in an area with a new seed",
        usage = "/respawntreesinarea <distance> <seed>",
        permission = Rank.STAFF,
        allowsConsole = false
)
public class RespawnTreesInAreaCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentInteger distanceArg = ArgumentType.Integer("distance");
        ArgumentLong seedArg = ArgumentType.Long("seed");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            int distance = context.get(distanceArg);
            long newSeed = context.get(seedArg);

            if (distance <= 0 || distance > 200) {
                player.sendMessage("<c>Distance must be between 1 and 200 blocks!");
                return;
            }

            SharedInstance instance = (SharedInstance) player.getInstance();
            int playerX = player.getPosition().blockX();
            int playerY = player.getPosition().blockY();
            int playerZ = player.getPosition().blockZ();

            // Find trees where at least one block is in range
            List<RegisteredTree> treesInRange = TreeRegistry.getTreesInRange(
                    instance, playerX, playerY, playerZ, distance
            );

            if (treesInRange.isEmpty()) {
                player.sendMessage("<c>No registered trees found within {} blocks!", distance);
                return;
            }

            int treesRespawned = 0;
            int blocksRemoved = 0;
            long currentSeed = newSeed;

            for (RegisteredTree tree : treesInRange) {
                // Step 1: Verify and remove existing blocks
                for (BlockEntry block : tree.allBlocks()) {
                    Block currentBlock = instance.getBlock(
                            block.worldX(), block.worldY(), block.worldZ()
                    );

                    // Only remove if block matches expected type
                    if (currentBlock.compare(block.expectedBlock())) {
                        instance.setBlock(
                                block.worldX(), block.worldY(), block.worldZ(),
                                Block.AIR
                        );
                        blocksRemoved++;
                    }
                }

                // Step 2: Unregister old tree
                TreeRegistry.unregisterTree(instance, tree);

                // Step 3: Regenerate with new seed (incrementing per tree)
                tree.spawnableTree().createAndRegister(
                        tree.baseX(),
                        tree.baseY(),
                        tree.baseZ(),
                        currentSeed++,
                        instance
                );

                treesRespawned++;
            }

            player.sendMessage("<a>Respawned <e>{}<a> tree(s)!", treesRespawned);
            player.sendMessage("<7>Removed <e>{}<7> blocks, regenerated with seeds <e>{}<7>-<e>{}", blocksRemoved, newSeed, (newSeed + treesRespawned - 1));
        }, distanceArg, seedArg);
    }
}
