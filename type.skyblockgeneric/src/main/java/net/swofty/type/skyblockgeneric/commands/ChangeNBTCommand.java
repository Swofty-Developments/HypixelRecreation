package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.PlayerHand;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.generic.user.categories.Rank;

@CommandParameters(description = "Modifies the NBT of the item in your hand",
        usage = "/changenbt <key> <value>",
        permission = Rank.STAFF,
        labels = "editnbt",
        allowsConsole = false)
public class ChangeNBTCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentString keyArgument = ArgumentType.String("key");
        ArgumentString valueArgument = ArgumentType.String("value");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            String key = context.get(keyArgument);
            String value = context.get(valueArgument);

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            SkyBlockItem item = new SkyBlockItem(player.getItemInHand(PlayerHand.MAIN));

            ItemAttribute attribute = item.getAttribute(key);
            attribute.setValue(attribute.loadFromString(value));

            player.setItemInHand(PlayerHand.MAIN, item.getItemStack());
            player.sendMessage("<a>Successfully changed NBT key <e>{} <a>to <e>{} <a>.", key, value);
        }, keyArgument, valueArgument);
    }
}
