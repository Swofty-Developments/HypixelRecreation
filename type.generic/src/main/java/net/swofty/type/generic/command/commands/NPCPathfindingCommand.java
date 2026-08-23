package net.swofty.type.generic.command.commands;

import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec3;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.utils.location.RelativeVec;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;

import java.util.List;

@CommandParameters(labels = "npcpathfinding", allowsConsole = false, description = "NPC Pathfinding test", permission = Rank.STAFF, usage = "/npcpathfinding <npc>")
public class NPCPathfindingCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        Argument<String> npcArgument = ArgumentType.String("npc").setSuggestionCallback((_, _, suggestion) -> {
            HypixelNPC.getRegisteredNPCs().forEach((npc) -> {
                suggestion.addEntry(new SuggestionEntry(npc.getName().replace(" ", "_")));
            });
        });

        ArgumentRelativeVec3 pos = ArgumentType.RelativeVec3("pos");
        command.addSyntax((sender, context) -> {
            if (!(sender instanceof HypixelPlayer player)) return;
            String npcName = context.get(npcArgument);
            HypixelNPC npc = HypixelNPC.getRegisteredNPCs().stream().filter(n -> n.getName().equalsIgnoreCase(npcName.replace("_", " "))).findFirst().orElse(null);

            if (npc == null) {
                player.sendMessage("<c>No NPC found with the name '{}'.", npcName);
                return;
            }

            RelativeVec s = context.get(pos);
            npc.walkPath(player, List.of(s.from(player.getPosition()).asPos()));
        }, npcArgument, pos);
    }
}
