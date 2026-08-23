package net.swofty.type.ravengardgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.ravengardgeneric.classes.RavengardAbility;
import net.swofty.type.ravengardgeneric.classes.RavengardClass;
import net.swofty.type.ravengardgeneric.user.RavengardPlayer;

import java.util.Arrays;
import java.util.stream.Collectors;

@CommandParameters(
        labels = "class",
        description = "Sets your Ravengard class",
        usage = "/class <knight|warrior|hunter|assassin>",
        permission = Rank.DEFAULT,
        allowsConsole = false
)
public class ClassCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        var classArg = ArgumentType.String("class");

        command.addSyntax((sender, context) -> {
            RavengardPlayer player = (RavengardPlayer) sender;
            RavengardClass chosen = RavengardClass.fromKey(context.get(classArg));

            if (chosen == null) {
                player.sendMessage("<c>Unknown class. Options: {}", Arrays.stream(RavengardClass.values())
                        .map(RavengardClass::getDisplayName)
                        .collect(Collectors.joining(", ")));
                return;
            }

            player.setRavengardClass(chosen);
            player.sendMessage("{} <f>You are now a <e>{}<f>.", chosen.getIcon(), chosen.getDisplayName());
            for (RavengardAbility ability : chosen.getAbilities()) {
                player.sendMessage("  {} <7>{} <8>({})", ability.getIcon(), ability.getDisplayName(),
                        ability.getCooldownText());
            }
        }, classArg);

        command.addSyntax((sender, context) -> {
            RavengardPlayer player = (RavengardPlayer) sender;
            RavengardClass current = player.getRavengardClass();
            if (current == null) {
                player.sendMessage("<7>You have not chosen a class yet.");
            } else {
                player.sendMessage("{} <f>You are a <e>{}<f>.", current.getIcon(), current.getDisplayName());
            }
        });
    }
}
