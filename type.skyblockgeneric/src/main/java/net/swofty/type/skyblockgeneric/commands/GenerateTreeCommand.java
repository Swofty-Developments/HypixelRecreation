package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.instance.SharedInstance;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.skyblockgeneric.structure.tree.SkyBlockTree;
import net.swofty.type.skyblockgeneric.structure.tree.TreeConfig;
import net.swofty.type.skyblockgeneric.structure.tree.TreeType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.generic.user.categories.Rank;

@CommandParameters(
        labels = "tree",
        description = "Generates a tree at your location",
        usage = "/generatetree <type> [minHeight maxHeight] [minWidth maxWidth]",
        permission = Rank.STAFF,
        allowsConsole = false
)
public class GenerateTreeCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentEnum<TreeType> treeTypeArg = ArgumentType.Enum("type", TreeType.class);
        ArgumentInteger minHeightArg = ArgumentType.Integer("minHeight");
        ArgumentInteger maxHeightArg = ArgumentType.Integer("maxHeight");
        ArgumentInteger minWidthArg = ArgumentType.Integer("minWidth");
        ArgumentInteger maxWidthArg = ArgumentType.Integer("maxWidth");

        // Just type - use defaults
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            TreeType type = context.get(treeTypeArg);

            generateTree(player, type, type.getDefaultConfig());
        }, treeTypeArg);

        // Type + height
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            TreeType type = context.get(treeTypeArg);
            int minHeight = context.get(minHeightArg);
            int maxHeight = context.get(maxHeightArg);

            if (minHeight > maxHeight) {
                player.sendMessage("<c>Minimum height cannot be greater than maximum height!");
                return;
            }

            if (minHeight < 2 || maxHeight > 50) {
                player.sendMessage("<c>Height must be between 2 and 50!");
                return;
            }

            TreeConfig customConfig = type.getDefaultConfig()
                    .withMinHeight(minHeight)
                    .withMaxHeight(maxHeight);

            generateTree(player, type, customConfig);
        }, treeTypeArg, minHeightArg, maxHeightArg);

        // Type + height + width
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            TreeType type = context.get(treeTypeArg);
            int minHeight = context.get(minHeightArg);
            int maxHeight = context.get(maxHeightArg);
            int minWidth = context.get(minWidthArg);
            int maxWidth = context.get(maxWidthArg);

            if (minHeight > maxHeight) {
                player.sendMessage("<c>Minimum height cannot be greater than maximum height!");
                return;
            }

            if (minWidth > maxWidth) {
                player.sendMessage("<c>Minimum width cannot be greater than maximum width!");
                return;
            }

            if (minHeight < 2 || maxHeight > 50) {
                player.sendMessage("<c>Height must be between 2 and 50!");
                return;
            }

            if (minWidth < 1 || maxWidth > 20) {
                player.sendMessage("<c>Width must be between 1 and 20!");
                return;
            }

            TreeConfig customConfig = type.getDefaultConfig()
                    .withMinHeight(minHeight)
                    .withMaxHeight(maxHeight)
                    .withMinWidth(minWidth)
                    .withMaxWidth(maxWidth);

            generateTree(player, type, customConfig);
        }, treeTypeArg, minHeightArg, maxHeightArg, minWidthArg, maxWidthArg);
    }

    private void generateTree(SkyBlockPlayer player, TreeType type, TreeConfig config) {
        SkyBlockTree tree = new SkyBlockTree(
                0,
                player.getPosition().blockX(),
                player.getPosition().blockY(),
                player.getPosition().blockZ(),
                type,
                config
        );

        tree.build((SharedInstance) player.getInstance());

        player.sendMessage("<a>Generated {} tree! Height: {}-{}, Width: {}-{}",
                type.name().toLowerCase().replace("_", " "),
                config.minHeight(), config.maxHeight(), config.minWidth(), config.maxWidth());
    }
}
