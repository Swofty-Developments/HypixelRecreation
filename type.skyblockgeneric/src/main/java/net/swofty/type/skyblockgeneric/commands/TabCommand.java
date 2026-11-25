package net.swofty.type.skyblockgeneric.commands;

import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.gui.inventories.tab.GUITablistWidgets;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(description = "Configure SkyBlock tablist widgets", usage = "/tab", permission = Rank.DEFAULT, labels = "tab tablist", allowsConsole = false)
public final class TabCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, context) -> {
            if (permissionCheck(sender)) ((SkyBlockPlayer) sender).openView(new GUITablistWidgets());
        });
    }
}
