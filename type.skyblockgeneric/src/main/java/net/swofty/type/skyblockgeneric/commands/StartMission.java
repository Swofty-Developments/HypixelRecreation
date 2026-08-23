package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.mission.MissionSet;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@CommandParameters(
    labels = "startmission",
    description = "Starts a mission for testing purposes",
    usage = "/startmission <mission_id>",
    permission = Rank.STAFF,
    allowsConsole = false
)
public class StartMission extends HypixelCommand {
    @Override
    public void registerUsage(HypixelCommand.MinestomCommand command) {
        ArgumentString className = ArgumentType.String("mission_id");
        className.setSuggestionCallback((sender, context, suggestion) -> {
            for (MissionSet missionSet : MissionSet.values()) {
                for (Class<?> missionClass : missionSet.getMissions()) {
                    suggestion.addEntry(new SuggestionEntry(missionClass.getSimpleName()));
                }
            }
        });
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            String missionId = context.get(className);
            Class<? extends SkyBlockMission> missionClass = null;
            for (MissionSet missionSet : MissionSet.values()) {
                for (Class<? extends SkyBlockMission> cls : missionSet.getMissions()) {
                    if (cls.getSimpleName().equalsIgnoreCase(missionId)) {
                        missionClass = cls;
                        break;
                    }
                }
                if (missionClass != null) break;
            }
            if (missionClass == null) {
                sender.sendMessage(Text.of("<c>Mission with ID <e>{} <c>not found.", missionId));
                return;
            }
            ((SkyBlockPlayer) sender).getMissionData().startMission(missionClass);
        }, className);
    }
}
