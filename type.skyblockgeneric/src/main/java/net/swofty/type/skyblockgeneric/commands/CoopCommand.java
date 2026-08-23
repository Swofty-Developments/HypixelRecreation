package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.swofty.proxyapi.ProxyPlayer;
import net.swofty.proxyapi.ProxyPlayerSet;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.data.mongodb.ProfilesDatabase;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.data.monogdb.CoopDatabase;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@CommandParameters(labels = "cooperative coop",
        description = "Primary coop command",
        usage = "/coop",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class CoopCommand extends HypixelCommand {
    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentStringArray args = ArgumentType.StringArray("player");

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;

            SkyBlockPlayer player = (SkyBlockPlayer) sender;

            if (checkIfAlreadyExisting(player)) return;

            player.sendMessage("<c>You don't have an outgoing co-op invite!");
            player.sendMessage("<e>Use <b>/coop \\<player 1> \\<player 2>... <e>to create one!");
            player.sendMessage("<e>Use <a>/coopadd \\<player> <e>to add a player to your current co-op!");
        });

        command.addSyntax((sender, context) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) sender;
            String[] players = context.get(args);

            if (checkIfAlreadyExisting(player)) return;

            player.sendMessage("<7>Validating invite...");

            if (Arrays.stream(players).anyMatch(player1 -> player1.equalsIgnoreCase(player.getUsername()))) {
                player.sendMessage("<c>You can't invite yourself to a co-op!");
                return;
            }

            ProxyPlayerSet chosenPlayers;

            try {
                chosenPlayers = new ProxyPlayerSet(Arrays.stream(players).map(
                        ProfilesDatabase::fetchUUID
                ).collect(Collectors.toList()));
            } catch (Exception e) {
                player.sendMessage("<b>[Co-op] <c>One or more of the players you specified are not online!");
                return;
            }

            if (chosenPlayers.getPlayers().stream().anyMatch(Objects::isNull)) {
                player.sendMessage("<b>[Co-op] <c>One or more of the players you specified are not online!");
                return;
            }

            if (chosenPlayers.asProxyPlayers().stream().anyMatch(player1 -> !player1.isOnline().join())) {
                player.sendMessage("<b>[Co-op] <c>One or more of the players you specified are not online!");
                return;
            }

            if (chosenPlayers.asProxyPlayers().stream().anyMatch(player1 -> CoopDatabase.getFromMember(player1.uuid()) != null)) {
                player.sendMessage("<b>[Co-op] <c>One or more of the players you specified already have a co-op or an invite pending!");
                return;
            }

            // Check if player put same player name in twice
            if (Arrays.stream(players).distinct().count() != Arrays.stream(players).count()) {
                player.sendMessage("<b>[Co-op] <c>You can't invite the same player twice!");
                return;
            }

            // Check if there are more than 4 names
            if (players.length > 4) {
                player.sendMessage("<b>[Co-op] <c>You can't invite more than 4 players!");
                return;
            }

            CoopDatabase.Coop coop = CoopDatabase.getClean(player.getUuid());

            int i = 0;
            for (ProxyPlayer invitedPlayer : chosenPlayers.asProxyPlayers()) {
                coop.addInvite(invitedPlayer.uuid());
                player.sendMessage("<b>[Co-op] <e>You invited {} to a SkyBlock co-op!", players[i]);
                i++;

                if (!invitedPlayer.isOnline().join()) continue;

                invitedPlayer.sendMessage("<b>----------------------------------------");
                invitedPlayer.sendMessage("{} <e>invited you to a SkyBlock co-op!",
                        player.getFullDisplayName());
                invitedPlayer.sendMessage("<hover:'<e>Click here to view the invite'><click:run:'/coopcheck'>" +
                        "<6>Click here <e>to view!</click></hover>");
                invitedPlayer.sendMessage("<b>----------------------------------------");
            }

            player.sendMessage("<hover:'<e>Click here to view the invite'><click:run:'/coopcheck'>" +
                    "<e>Use <b>/coop <e>or <a><l>CLICK THIS </l><e>for status!</click></hover>");

            coop.save();
        }, args);
    }

    private boolean checkIfAlreadyExisting(SkyBlockPlayer player) {
        CoopDatabase.Coop coop = CoopDatabase.getFromMember(player.getUuid());

        if (coop != null) {
            if (coop.members().contains(player.getUuid())) {
                player.sendMessage("<c>You are already in a co-op!");
                player.sendMessage("<e>Run <a>/coopleave <e>to leave your current co-op.");
                return true;
            }

            boolean isOriginator = coop.isOriginator(player.getUuid());

            if (isOriginator) {
                player.sendMessage("<hover:'<e>Click here to view the invite'><click:run:'/coopcheck'>" +
                        "<e>You already have an outgoing co-op invite! <a><l>CLICK TO VIEW!</click></hover>");
            } else {
                player.sendMessage("<hover:'<e>Click here to view the invite'><click:run:'/coopcheck'>" +
                        "<c>You already have an incoming co-op invite! <a><l>CLICK TO VIEW!</click></hover>");
            }
            return true;
        }
        return false;
    }
}
