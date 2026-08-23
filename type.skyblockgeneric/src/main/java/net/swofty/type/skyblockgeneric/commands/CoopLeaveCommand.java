package net.swofty.type.skyblockgeneric.commands;

import net.swofty.commons.data.SwoftyData;
import net.swofty.commons.skyblock.SkyBlockPlayerProfiles;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.data.mongodb.ProfilesDatabase;
import net.swofty.type.generic.data.mongodb.UserDatabase;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.commons.skyblock.CoopLinks;
import net.swofty.type.skyblockgeneric.data.ProfileSwitcher;
import net.swofty.type.skyblockgeneric.data.monogdb.CoopDatabase;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.UUID;

@CommandParameters(labels = "cooperativeleave",
        description = "Leaves the current coop",
        usage = "/coopleave",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class CoopLeaveCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;

            if (!player.isCoop()) {
                player.sendMessage("<b>[Co-op] <c>You are not on a coop profile!");
                return;
            }

            CoopDatabase.Coop coop = CoopDatabase.getFromMember(player.getUuid());
            if (coop == null) {
                player.sendMessage("<b>[Co-op] <c>You are not part of a co-op!");
                return;
            }

            UUID coopId = coop.coopUUID();
            UUID playerId = player.getUuid();
            UUID profileId = player.getProfiles().getCurrentlySelected();
            UUID fallbackProfileId = player.getProfiles().getProfiles().stream()
                    .filter(candidate -> !candidate.equals(profileId))
                    .findFirst()
                    .orElse(null);

            if (fallbackProfileId == null) {
                player.sendMessage("<b>[Co-op] <c>You cannot leave your last profile!");
                player.sendMessage("<b>[Co-op] <e>Make another profile before deleting this one.");
                return;
            }

            ProfileSwitcher.switchTo(player, fallbackProfileId).thenAccept(result -> {
                if (result != ProfileSwitcher.Result.SWITCHED && result != ProfileSwitcher.Result.TRANSFERRED) return;
                leave(playerId, coopId, profileId);
            });
        });
    }

    private void leave(UUID playerId, UUID coopId, UUID profileId) {
        CoopDatabase.Coop remaining = CoopDatabase.update(coopId, latest -> {
            latest.members().remove(playerId);
            latest.removeInvite(playerId);
            latest.memberProfiles().remove(profileId);
        });

        SwoftyData.profile().unlink(profileId, CoopLinks.COOP);
        if (remaining == null || (remaining.members().isEmpty() && remaining.memberProfiles().isEmpty())) {
            SwoftyData.profile().deleteLink(CoopLinks.COOP, coopId);
        }

        ProfilesDatabase.deleteDocument(profileId.toString());

        SkyBlockPlayerProfiles profiles = new UserDatabase(playerId).getProfiles();
        profiles.removeProfile(profileId);
        new UserDatabase(playerId).saveProfiles(profiles);
    }
}
