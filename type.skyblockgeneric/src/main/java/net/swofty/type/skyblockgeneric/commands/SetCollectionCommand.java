package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.collection.CollectionCategories;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.generic.user.categories.Rank;

@CommandParameters(labels = "updatecollection",
        description = "Updates the collection of a player",
        usage = "/setcollection <item_type> <amount>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class SetCollectionCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentEnum<ItemType> itemType = new ArgumentEnum("item_type", ItemType.class);
        ArgumentInteger amountArgument = new ArgumentInteger("amount");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            ItemType type = context.get(itemType);
            int amount = context.get(amountArgument);

            player.getCollection().set(type, amount);
            player.sendMessage("<a>Updated your <e>{} <a>category for the <c>{} <a>collection to <e>{}<a>.",
                    CollectionCategories.getCategory(type).getName(), type.getDisplayName(), amount);
        }, itemType, amountArgument);
    }
}
