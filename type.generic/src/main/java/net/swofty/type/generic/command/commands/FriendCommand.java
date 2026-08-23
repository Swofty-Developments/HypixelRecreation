package net.swofty.type.generic.command.commands;

import net.swofty.commons.text.Text;

import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.swofty.commons.ServiceType;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.friend.FriendManager;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@CommandParameters(labels = "f friend",
        description = "Friend management commands",
        usage = "/friend <subcommand>",
        permission = Rank.DEFAULT,
        allowsConsole = false)
public class FriendCommand extends HypixelCommand {
    public List<UUID> pendingCommands = new ArrayList<>();

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentString subcommand = ArgumentType.String("subcommand");
        ArgumentString playerName = ArgumentType.String("player");
        ArgumentString extraArg = ArgumentType.String("extra");
        ProxyService friendService = new ProxyService(ServiceType.FRIEND);

        // No args - show help
        command.addSyntax((sender, context) -> {
            showHelp((HypixelPlayer) sender);
        });

        // Single arg (subcommand only)
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            HypixelPlayer player = (HypixelPlayer) sender;

            if (pendingCommands.contains(player.getUuid())) {
                return;
            }

            pendingCommands.add(player.getUuid());

            if (!friendService.isOnline().join()) {
                player.sendMessage(Text.key("commands.common.service_offline_friend"));
                pendingCommands.remove(player.getUuid());
                return;
            }

            String sub = context.get(subcommand);

            switch (sub.toLowerCase()) {
                case "help" -> showHelp(player);
                case "list" -> FriendManager.listFriends(player, 1, false);
                case "notifications" -> FriendManager.toggleNotifications(player);
                case "toggle" -> FriendManager.toggleRequests(player);
                case "removeall" -> FriendManager.removeAllFriends(player);
                case "requests" -> FriendManager.listRequests(player, 1);
                default -> FriendManager.addFriend(player, sub);
            }

            pendingCommands.remove(player.getUuid());
        }, subcommand);

        // Two args (subcommand + player/page)
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            HypixelPlayer player = (HypixelPlayer) sender;

            if (pendingCommands.contains(player.getUuid())) {
                return;
            }

            pendingCommands.add(player.getUuid());

            if (!friendService.isOnline().join()) {
                player.sendMessage(Text.key("commands.common.service_offline_friend"));
                pendingCommands.remove(player.getUuid());
                return;
            }

            String sub = context.get(subcommand);
            String arg = context.get(playerName);

            switch (sub.toLowerCase()) {
                case "accept" -> FriendManager.acceptRequest(player, arg);
                case "add" -> FriendManager.addFriend(player, arg);
                case "best" -> FriendManager.toggleBestFriend(player, arg);
                case "deny" -> FriendManager.denyRequest(player, arg);
                case "remove" -> FriendManager.removeFriend(player, arg);
                case "list" -> {
                    if (arg.equalsIgnoreCase("best")) {
                        FriendManager.listFriends(player, 1, true);
                    } else {
                        int page = parsePageNumber(arg);
                        FriendManager.listFriends(player, page, false);
                    }
                }
                case "requests" -> {
                    int page = parsePageNumber(arg);
                    FriendManager.listRequests(player, page);
                }
                default -> player.sendMessage(Text.key("commands.common.unknown_command_use_help", "friend"));
            }

            pendingCommands.remove(player.getUuid());
        }, subcommand, playerName);

        // Three args (nickname command)
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            HypixelPlayer player = (HypixelPlayer) sender;

            if (pendingCommands.contains(player.getUuid())) {
                return;
            }

            pendingCommands.add(player.getUuid());

            if (!friendService.isOnline().join()) {
                player.sendMessage(Text.key("commands.common.service_offline_friend"));
                pendingCommands.remove(player.getUuid());
                return;
            }

            String sub = context.get(subcommand);
            String target = context.get(playerName);
            String extra = context.get(extraArg);

            if (sub.equalsIgnoreCase("nickname")) {
                FriendManager.setNickname(player, target, extra);
            } else {
                player.sendMessage(Text.key("commands.common.unknown_command_use_help", "friend"));
            }

            pendingCommands.remove(player.getUuid());
        }, subcommand, playerName, extraArg);
    }

    private void showHelp(HypixelPlayer player) {
        player.sendMessage(Text.key("commands.common.separator"));
        player.sendMessage(Text.key("commands.friend.help_header"));
        player.sendMessage(Text.key("commands.friend.help_add"));
        player.sendMessage(Text.key("commands.friend.help_accept"));
        player.sendMessage(Text.key("commands.friend.help_deny"));
        player.sendMessage(Text.key("commands.friend.help_remove"));
        player.sendMessage(Text.key("commands.friend.help_removeall"));
        player.sendMessage(Text.key("commands.friend.help_best"));
        player.sendMessage(Text.key("commands.friend.help_nickname"));
        player.sendMessage(Text.key("commands.friend.help_list"));
        player.sendMessage(Text.key("commands.friend.help_requests"));
        player.sendMessage(Text.key("commands.friend.help_toggle"));
        player.sendMessage(Text.key("commands.friend.help_notifications"));
        player.sendMessage(Text.key("commands.common.separator"));
    }

    private int parsePageNumber(String arg) {
        try {
            return Math.max(1, Integer.parseInt(arg));
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
