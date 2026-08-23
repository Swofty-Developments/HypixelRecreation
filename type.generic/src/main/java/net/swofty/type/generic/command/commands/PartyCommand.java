package net.swofty.type.generic.command.commands;

import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.party.FullParty;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.party.PartyManager;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@CommandParameters(labels = "p party",
        description = "Party management commands",
        usage = "/party <subcommand>",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class PartyCommand extends HypixelCommand {
    public List<UUID> pendingCommands = new ArrayList<>();

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentString subcommand = ArgumentType.String("subcommand");
        ArgumentString playerName = ArgumentType.String("player");
        ProxyService partyService = new ProxyService(ServiceType.PARTY);

        command.addSyntax((sender, context) -> {
            showHelp((HypixelPlayer) sender);
        });

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            HypixelPlayer player = (HypixelPlayer) sender;

            if (pendingCommands.contains(player.getUuid())) {
                return;
            }

            pendingCommands.add(player.getUuid());

            if (!partyService.isOnline().join()) {
                player.sendMessage(Text.key("commands.common.service_offline_party"));
                pendingCommands.remove(player.getUuid());
                return;
            }

            String sub = context.get(subcommand);

            switch (sub.toLowerCase()) {
                case "list" -> {
                    if (!PartyManager.isInParty(player)) {
                        player.sendMessage(Text.key("commands.common.separator"));
                        player.sendMessage(Text.key("commands.party.not_in_party"));
                        player.sendMessage(Text.key("commands.common.separator"));
                        return;
                    }
                    FullParty party = PartyManager.getPartyFromPlayer(player);

                    int partySize = party.getMembers().size();
                    player.sendMessage(Text.key("commands.common.separator"));
                    player.sendMessage(Text.key("commands.party.list_header", partySize));
                    player.sendMessage(Text.key("commands.common.empty_line"));

                    FullParty.Member leader = party.getLeader();
                    player.sendMessage(Text.key("commands.party.list_leader", HypixelPlayer.getDisplayName(leader.getUuid())));

                    boolean hasMods = false;
                    for (FullParty.Member member : party.getMembers()) {
                        if (member.getRole() == FullParty.Role.MODERATOR) {
                            hasMods = true;
                            break;
                        }
                    }

                    if (hasMods) {
                        player.sendMessage(Text.key("commands.common.empty_line"));
                        Text modList = Text.join(Text.of(", "), party.getMembers().stream()
                                .filter(member -> member.getRole() == FullParty.Role.MODERATOR)
                                .map(member -> HypixelPlayer.getDisplayName(member.getUuid()))
                                .toList());
                        player.sendMessage(Text.key("commands.party.list_moderators", modList));
                    }

                    boolean hasMembers = false;
                    for (FullParty.Member member : party.getMembers()) {
                        if (member.getRole() != FullParty.Role.LEADER && member.getRole() != FullParty.Role.MODERATOR) {
                            hasMembers = true;
                            break;
                        }
                    }

                    Text memberList = Text.join(Text.of(", "), party.getMembers().stream()
                            .filter(member -> member.getRole() != FullParty.Role.LEADER && member.getRole() != FullParty.Role.MODERATOR)
                            .map(member -> HypixelPlayer.getDisplayName(member.getUuid()))
                            .toList());
                    if (hasMembers) {
                        player.sendMessage(Text.key("commands.common.empty_line"));
                        player.sendMessage(Text.key("commands.party.list_members", memberList));
                    }
                    player.sendMessage(Text.key("commands.common.separator"));
                }
                case "leave" -> PartyManager.leaveParty(player);
                case "disband" -> PartyManager.disbandParty(player);
                case "warp" -> PartyManager.warpParty(player);
                default -> PartyManager.invitePlayer(player, sub);
            }

            pendingCommands.remove(player.getUuid());
        }, subcommand);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            HypixelPlayer player = (HypixelPlayer) sender;

            if (pendingCommands.contains(player.getUuid())) {
                return;
            }

            pendingCommands.add(player.getUuid());

            if (!partyService.isOnline().join()) {
                player.sendMessage(Text.key("commands.common.service_offline_party"));
                pendingCommands.remove(player.getUuid());
                return;
            }

            String sub = context.get(subcommand);
            String target = context.get(playerName);

            switch (sub.toLowerCase()) {
                case "invite" -> PartyManager.invitePlayer(player, target);
                case "accept" -> PartyManager.acceptInvite(player, target);
                case "kick" -> PartyManager.kickPlayer(player, target);
                case "transfer" -> PartyManager.transferLeadership(player, target);
                case "promote" -> PartyManager.promotePlayer(player, target);
                case "demote" -> PartyManager.demotePlayer(player, target);
                case "hijack" -> PartyManager.hijackParty(player, target);
                case "chat" -> PartyManager.sendChat(player, target);
                case "movetoserver" -> {
                    UUID targetServer = UUID.fromString(target);
                    player.asProxyPlayer().transferToWithIndication(targetServer);
                }
                default -> player.sendMessage(Text.key("commands.common.unknown_command_use_help", "party"));
            }

            pendingCommands.remove(player.getUuid());
        }, subcommand, playerName);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            HypixelPlayer player = (HypixelPlayer) sender;

            if (pendingCommands.contains(player.getUuid())) {
                return;
            }

            pendingCommands.add(player.getUuid());

            if (!partyService.isOnline().join()) {
                player.sendMessage(Text.key("commands.common.service_offline_party_alt"));
                pendingCommands.remove(player.getUuid());
                return;
            }

            String sub = context.get(subcommand);
            String message = Arrays.toString(context.get(new ArgumentStringArray("message")));

            switch (sub.toLowerCase()) {
                case "chat" -> PartyManager.sendChat(player, message);
                default -> player.sendMessage(Text.key("commands.common.unknown_command_use_help", "party"));
            }

            pendingCommands.remove(player.getUuid());
        }, subcommand, new ArgumentStringArray("message"));
    }

    private void showHelp(HypixelPlayer player) {
        player.sendMessage(Text.key("commands.common.separator"));
        player.sendMessage(Text.key("commands.party.help_header"));
        player.sendMessage(Text.key("commands.party.help_accept"));
        player.sendMessage(Text.key("commands.party.help_invite"));
        player.sendMessage(Text.key("commands.party.help_list"));
        player.sendMessage(Text.key("commands.party.help_leave"));
        player.sendMessage(Text.key("commands.party.help_warp"));
        player.sendMessage(Text.key("commands.party.help_disband"));
        player.sendMessage(Text.key("commands.party.help_transfer"));
        player.sendMessage(Text.key("commands.party.help_kick"));
        player.sendMessage(Text.key("commands.party.help_promote"));
        player.sendMessage(Text.key("commands.party.help_demote"));
        player.sendMessage(Text.key("commands.party.help_chat"));
        if (player.getRank().isEqualOrHigherThan(Rank.STAFF)) {
            player.sendMessage(Text.key("commands.party.help_hijack"));
        }
        player.sendMessage(Text.key("commands.common.separator"));
    }
}
