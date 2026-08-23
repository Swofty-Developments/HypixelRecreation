package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributePetData;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(labels = "updatepetlevel setpetlevel",
        description = "Sets the level of the pet",
        usage = "/setpetlevel <level>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class SetPetLevel extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentInteger level = new ArgumentInteger("level");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            int levelAsInt = context.get(level);

            player.updateItem(PlayerItemOrigin.MAIN_HAND, (item) -> {
                ItemAttributePetData.PetData data = item.getAttributeHandler().getPetData();
                data.setLevel(levelAsInt, Rarity.COMMON);
                item.getAttributeHandler().setPetData(data);
            });

            player.sendMessage("<a>Updated the level of the pet to <e>{}<a>.", levelAsInt);
        }, level);
    }
}
