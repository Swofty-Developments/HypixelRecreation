package net.swofty.type.skyblockgeneric.commands;

import net.swofty.commons.text.Text;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.skyblockgeneric.item.ItemAttributeHandler;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.generic.user.categories.Rank;

@CommandParameters(labels = "recomb",
        description = "Recombobulates the item in the players hand",
        usage = "/recombobulate",
        permission = Rank.STAFF,
        allowsConsole = false)
public class RecombobulateCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            ((SkyBlockPlayer) sender).updateItem(PlayerItemOrigin.MAIN_HAND, (item) -> {
                ItemAttributeHandler itemAttributeHandler = item.getAttributeHandler();
                if (itemAttributeHandler.isPet()) {
                    sender.sendMessage("<c>Pets cannot be recombobulated!");
                    return;
                }
                itemAttributeHandler.setRecombobulated(!itemAttributeHandler.isRecombobulated());
                sender.sendMessage(Text.of("<a>Recombobulated: <d>{}", itemAttributeHandler.isRecombobulated()));
            });
        });
    }
}