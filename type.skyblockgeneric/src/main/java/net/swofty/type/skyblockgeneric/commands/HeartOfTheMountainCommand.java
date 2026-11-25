package net.swofty.type.skyblockgeneric.commands;

import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.skills.GUIHeartOfTheMountain;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(
        description = "Opens the Heart of the Mountain",
        usage = "/hotm",
        permission = Rank.DEFAULT,
        labels = "hotm",
        allowsConsole = false
)
public class HeartOfTheMountainCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, _) -> {
            if (!permissionCheck(sender)) return;
            ((SkyBlockPlayer) sender).openView(new GUIHeartOfTheMountain(true));
        });
    }
}
