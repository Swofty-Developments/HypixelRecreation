package net.swofty.type.ravengardgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.ravengardgeneric.entity.animation.RavengardMobClip;
import net.swofty.type.ravengardgeneric.entity.mob.RavengardMob;
import net.swofty.type.ravengardgeneric.user.RavengardPlayer;

import java.util.List;

@CommandParameters(
        labels = "spawn",
        description = "Spawns a Ravengard mob at your position",
        usage = "/spawn <mob>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class SpawnCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        var mobArg = ArgumentType.Word("mob").setSuggestionCallback((sender, context, suggestion) ->
                RavengardMobClip.available().forEach(name -> suggestion.addEntry(new SuggestionEntry(name))));

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            RavengardPlayer player = (RavengardPlayer) sender;
            String name = context.get(mobArg);
            List<String> available = RavengardMobClip.available();
            if (!available.contains(name)) {
                player.sendMessage("<c>Unknown mob. Options: {}", String.join(", ", available));
                return;
            }
            RavengardMobClip clip = RavengardMobClip.load(name);
            RavengardMob mob = new RavengardMob(clip, player.getPosition());
            mob.spawnMob(player.getInstance());
            player.sendMessage("<a>Spawned <f>{}<a>.", name);
        }, mobArg);
    }
}
