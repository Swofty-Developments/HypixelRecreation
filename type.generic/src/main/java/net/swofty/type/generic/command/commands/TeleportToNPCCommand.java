package net.swofty.type.generic.command.commands;

import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;

@CommandParameters(
    usage = "/tpnpc <npc>",
    labels = "tpnpc teleportnpc",
    permission = Rank.STAFF,
    description = "Teleports you to the specified NPC.",
    allowsConsole = false
)
public class TeleportToNPCCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        Argument<String[]> npcArgument = ArgumentType.StringArray("npc").setSuggestionCallback((_, _, suggestion) -> {
            HypixelNPC.getRegisteredNPCs().forEach((npc) -> {
                suggestion.addEntry(new SuggestionEntry(npc.getName()));
            });
        });

        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            String npcName = String.join(" ", context.get(npcArgument));
            HypixelNPC npc = HypixelNPC.getRegisteredNPCs().stream().filter(n -> n.getName().equalsIgnoreCase(npcName)).findFirst().orElse(null);

            if (npc == null) {
                ((HypixelPlayer) player).sendMessage("<c>No NPC found with the name '{}'.", npcName);
                return;
            }
            player.teleport(npc.getParameters().position((HypixelPlayer) player));
        }, npcArgument);
    }
}
