package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.item.ItemAttributeHandler;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CommandParameters(labels = "setpetrarity",
        description = "Sets the rarity of the pet in your hand",
        usage = "/setpetrarity <rarity>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class SetPetRarityCommand extends HypixelCommand {
    private static final List<Rarity> PET_RARITIES = Arrays.stream(Rarity.values())
            .filter(rarity -> rarity.ordinal() <= Rarity.MYTHIC.ordinal())
            .toList();

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentWord rarityArgument = new ArgumentWord("rarity");
        rarityArgument.setSuggestionCallback((sender, context, suggestion) ->
                PET_RARITIES.forEach(rarity ->
                        suggestion.addEntry(new SuggestionEntry(rarity.name()))));

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            Rarity rarity = Rarity.getRarity(context.get(rarityArgument));
            if (rarity == null || !PET_RARITIES.contains(rarity)) {
                sender.sendMessage(Text.of("<c>Invalid rarity. Use {}.", PET_RARITIES.stream()
                        .map(Enum::name).collect(Collectors.joining("/"))));
                return;
            }
            SkyBlockPlayer player = (SkyBlockPlayer) sender;

            player.updateItem(PlayerItemOrigin.MAIN_HAND, (item) -> {
                ItemAttributeHandler handler = item.getAttributeHandler();
                if (!handler.isPet()) {
                    sender.sendMessage("<c>The item in your hand is not a pet.");
                    return;
                }
                handler.setRarity(rarity);
                sender.sendMessage(Text.of("<a>Set rarity to <color:{}>{}<a>.",
                        rarity.getColor(), rarity.name()));
            });
        }, rarityArgument);
    }
}
