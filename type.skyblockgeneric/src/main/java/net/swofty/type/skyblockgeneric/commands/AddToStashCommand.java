package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointStash;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "addtostash",
        description = "Force add an item to your stash",
        usage = "/addtostash <item> [amount]",
        permission = Rank.STAFF,
        allowsConsole = false)
public class AddToStashCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentEnum<ItemType> itemArgument = ArgumentType.Enum("item", ItemType.class);
        ArgumentInteger amountArgument = ArgumentType.Integer("amount");

        // Single item syntax
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            ItemType itemType = context.get(itemArgument);

            addItemToStash(player, itemType, 1);
        }, itemArgument);

        // With amount syntax
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            ItemType itemType = context.get(itemArgument);
            int amount = context.get(amountArgument);

            addItemToStash(player, itemType, amount);
        }, itemArgument, amountArgument);
    }

    private void addItemToStash(SkyBlockPlayer player, ItemType itemType, int amount) {
        DatapointStash.PlayerStash stash = player.getStash();
        SkyBlockItem item = new SkyBlockItem(itemType);

        // Check if stackable (maxStackSize > 1 means material stash)
        if (item.getMaterial().maxStackSize() > 1) {
            stash.addToMaterialStash(itemType, amount);
            player.sendMessage("<a>Added <e>{}x {} <a>to your material stash.", amount, itemType.getDisplayName());
        } else {
            // Non-stackable items go to item stash
            int added = 0;
            for (int i = 0; i < amount; i++) {
                if (stash.addToItemStash(new SkyBlockItem(itemType))) {
                    added++;
                } else {
                    break;
                }
            }
            if (added > 0) {
                player.sendMessage("<a>Added <e>{}x {} <a>to your item stash.", added, itemType.getDisplayName());
            }
            if (added < amount) {
                player.sendMessage("<c>Couldn't add {} items - item stash is at limit (720).", amount - added);
            }
        }
    }
}
