package net.swofty.type.skyblockgeneric.commands;
import net.swofty.commons.text.Text;

import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.generic.user.categories.Rank;

@CommandParameters(labels = "displaymissiondata",
        description = "Displays the mission data of a player",
        usage = "/displaymissiondata",
        permission = Rank.STAFF,
        allowsConsole = false)
public class PrintMissionDataCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            MissionData data = player.getMissionData();

            player.sendMessage("<a>Mission Data:");
            player.sendMessage("<7>Active Missions:");
            data.getActiveMissions().forEach((mission) -> {
                player.sendMessage("<e>{}<7>:", mission.toString());
                mission.getCustomData().forEach((key, value) -> {
                    player.sendMessage("<7>{}<e>: {}", key, value);
                });
            });

            player.sendMessage("<7>Completed Missions:");
            data.getCompletedMissions().forEach((mission) -> {
                player.sendMessage("<e>{}", mission.toString());
                mission.getCustomData().forEach((key, value) -> {
                    player.sendMessage("<7>{}<e>: {}", key, value);
                });
            });
        });
    }
}
