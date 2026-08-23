package net.swofty.type.generic.command.commands;

import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.guild.GuildData;
import net.swofty.commons.guild.GuildMember;
import net.swofty.commons.guild.GuildRank;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.guild.GuildManager;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@CommandParameters(labels = "g guild",
    description = "Guild management commands",
    usage = "/guild <subcommand>",
    permission = Rank.DEFAULT,
    allowsConsole = false)
public class GuildCommand extends HypixelCommand {
    private final List<UUID> pendingCommands = new ArrayList<>();

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentStringArray arguments = ArgumentType.StringArray("arguments");
        ProxyService guildService = new ProxyService(ServiceType.GUILD);

        command.addSyntax((sender, context) -> {
            showHelp((HypixelPlayer) sender);
        });

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            HypixelPlayer player = (HypixelPlayer) sender;
            if (pendingCommands.contains(player.getUuid())) return;
            pendingCommands.add(player.getUuid());

            Thread.startVirtualThread(() -> {
                try {
                    if (!guildService.isOnline().join()) {
                        player.sendMessage("<c>Guild service is currently offline!");
                        return;
                    }

                    String[] args = context.get(arguments);
                    String sub = args[0].toLowerCase();
                    String argument = args.length > 1 ? args[1] : null;
                    String message = args.length > 1
                        ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length))
                        : "";

                    dispatch(player, sub, argument, message, args);
                } finally {
                    pendingCommands.remove(player.getUuid());
                }
            });
        }, arguments);
    }

    private void dispatch(HypixelPlayer player, String sub, String argument, String message, String[] args) {
        switch (sub) {
            case "help" -> showHelp(player);
            case "log", "history" -> showGuildLog(player);
            case "leave" -> GuildManager.leaveGuild(player);
            case "disband" -> GuildManager.disbandGuild(player);
            case "list", "info" -> showGuildInfo(player);
            case "online", "members" -> showOnlineMembers(player);
            case "member" -> showMember(player, argument);
            case "mypermissions", "permissions" -> showPermissions(player);
            case "top" -> showTop(player);
            case "discord" -> showDiscord(player, message);
            case "join" -> player.sendMessage("<c>Guild join requests are not currently accepted by this guild.");
            case "menu" -> player.sendMessage("<e>Open your profile and select <a>Guild <e>to open the Guild Menu.");
            case "party" -> player.sendMessage("<c>No online guild party could be formed.");
            case "quest" -> player.sendMessage("<e>Your guild does not currently have an active Guild Quest.");
            case "notifications", "slow", "onlinemode", "toggle" -> GuildManager.changeSetting(player, sub, "toggle");
            case "create" ->
                withArgument(player, argument, "/guild create <name>", value -> GuildManager.createGuild(player, value));
            case "invite" ->
                withArgument(player, argument, "/guild invite <player>", value -> GuildManager.invitePlayer(player, value));
            case "accept" ->
                withArgument(player, argument, "/guild accept <player>", value -> GuildManager.acceptInvite(player, value));
            case "promote" ->
                withArgument(player, argument, "/guild promote <player>", value -> GuildManager.promotePlayer(player, value));
            case "demote" ->
                withArgument(player, argument, "/guild demote <player>", value -> GuildManager.demotePlayer(player, value));
            case "transfer" ->
                withArgument(player, argument, "/guild transfer <player>", value -> GuildManager.transferOwnership(player, value));
            case "tag", "tagcolor", "rename", "finder" ->
                withArgument(player, argument, "/guild " + sub + " <value>", value -> GuildManager.changeSetting(player, sub, value));
            case "settings" -> {
                if (args.length < 3) player.sendMessage("<c>Usage: {}", "/guild settings <setting> <value>");
                else
                    GuildManager.changeSetting(player, args[1], String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length)));
            }
            case "chat" ->
                withArgument(player, argument, "/guild chat <message>", value -> GuildManager.sendChat(player, message, false));
            case "officerchat", "oc" ->
                withArgument(player, argument, "/guild officerchat <message>", value -> GuildManager.sendChat(player, message, true));
            case "motd" -> handleMotd(player, args);
            case "description" ->
                withArgument(player, argument, "/guild " + sub + " <message>", value -> GuildManager.changeSetting(player, sub, message));
            case "mute" -> GuildManager.mutePlayer(player, argument == null ? "everyone" : argument,
                args.length > 2 ? parseDuration(args[2]) : 3600000);
            case "unmute" -> GuildManager.unmutePlayer(player, argument == null ? "everyone" : argument);
            case "kick" -> withArgument(player, argument, "/guild kick <player> [reason]", value -> {
                String reason = args.length > 2
                    ? String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length))
                    : "";
                GuildManager.kickPlayer(player, value, reason);
            });
            case "setrank" -> {
                if (args.length < 3) player.sendMessage("<c>Usage: {}", "/guild setrank <player> <rank>");
                else GuildManager.setRank(player, args[1], args[2]);
            }
            default -> {
                player.sendMessage("<c>Unknown guild command. Use /guild for help.");
            }
        }
    }

    private long parseDuration(String input) {
        try {
            long amount = Long.parseLong(input.substring(0, input.length() - 1));
            return amount * switch (input.charAt(input.length() - 1)) {
                case 's' -> 1000;
                case 'm' -> 60000;
                case 'h' -> 3600000;
                case 'd' -> 86400000;
                default -> 60000;
            };
        } catch (RuntimeException ignored) {
            return 3600000;
        }
    }

    private void showMember(HypixelPlayer player, String name) {
        GuildData guild = GuildManager.getGuildFromPlayer(player);
        UUID uuid = name == null ? player.getUuid() : net.swofty.type.generic.data.HypixelDataHandler.getPotentialUUIDFromName(name);
        GuildMember member = guild == null || uuid == null ? null : guild.getMember(uuid);
        if (member == null) {
            player.sendMessage("<c>That player is not in your guild!");
            return;
        }
        player.sendMessage("<sep>");
        player.sendMessage("<a>{}", HypixelPlayer.getRawName(uuid));
        player.sendMessage("<7>Rank: <e>{}", member.getRankName());
        player.sendMessage("<7>Weekly GEXP: <e>{:,}", member.getWeeklyGexp());
        player.sendMessage("<7>Total GEXP: <e>{:,}", member.getTotalGexp());
        player.sendMessage("<sep>");
    }

    private void showPermissions(HypixelPlayer player) {
        GuildData guild = GuildManager.getGuildFromPlayer(player);
        GuildRank rank = guild == null ? null : guild.getMemberRank(player.getUuid());
        if (rank == null) {
            player.sendMessage("<c>You are not in a guild!");
            return;
        }
        player.sendMessage("<sep>");
        player.sendMessage("<6>Permissions for <e>{}", rank.getName());
        rank.getPermissions().forEach(permission -> player.sendMessage("<a>✔ <7>{}", permission.name()));
        player.sendMessage("<sep>");
    }

    private void showTop(HypixelPlayer player) {
        GuildData guild = GuildManager.getGuildFromPlayer(player);
        if (guild == null) {
            player.sendMessage("<c>You are not in a guild!");
            return;
        }
        player.sendMessage("<sep>");
        player.sendMessage("<6>Guild Experience Top");
        guild.getMembers().stream().sorted(java.util.Comparator.comparingLong(GuildMember::getTotalGexp).reversed()).limit(10)
            .forEach(member -> player.sendMessage("<e>{} <7>- <a>{:,}",
                HypixelPlayer.getRawName(member.getUuid()), member.getTotalGexp()));
        player.sendMessage("<sep>");
    }

    private void showDiscord(HypixelPlayer player, String value) {
        if (!value.isBlank()) {
            GuildManager.changeSetting(player, "discord", value);
            return;
        }
        GuildData guild = GuildManager.getGuildFromPlayer(player);
        if (guild == null || guild.getDiscordLink().isBlank()) {
            player.sendMessage("<c>Your guild has not set a Discord link!");
        } else {
            player.sendMessage("<6>Guild Discord: <b>{}", guild.getDiscordLink());
        }
    }

    private void handleMotd(HypixelPlayer player, String[] args) {
        if (args.length == 1 || args[1].equalsIgnoreCase("help")) {
            showMotdHelp(player);
            return;
        }
        GuildData guild = GuildManager.getGuildFromPlayer(player);
        if (guild == null) {
            player.sendMessage("<c>You are not in a guild!");
            return;
        }
        List<String> lines = new ArrayList<>(guild.getMotd() == null || guild.getMotd().isBlank()
            ? List.of() : List.of(guild.getMotd().split("\\n", -1)));
        switch (args[1].toLowerCase()) {
            case "list", "preview" -> {
                player.sendMessage("<sep>");
                lines.forEach(line -> player.sendMessage("<f>{}", line));
                player.sendMessage("<sep>");
            }
            case "clear" -> GuildManager.changeSetting(player, "motd", "");
            case "add" -> {
                if (args.length < 3) player.sendMessage("<c>Usage: {}", "/guild motd add <text>");
                else {
                    lines.add(String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length)));
                    GuildManager.changeSetting(player, "motd", String.join("\n", lines));
                }
            }
            case "set" -> {
                if (args.length < 4) {
                    player.sendMessage("<c>Usage: {}", "/guild motd set <line> <text>");
                    return;
                }
                try {
                    int line = Integer.parseInt(args[2]) - 1;
                    if (line < 0 || line >= lines.size()) throw new IndexOutOfBoundsException();
                    lines.set(line, String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length)));
                    GuildManager.changeSetting(player, "motd", String.join("\n", lines));
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    player.sendMessage("<c>That MOTD line does not exist!");
                }
            }
            default -> showMotdHelp(player);
        }
    }

    private void showGuildLog(HypixelPlayer player) {
        GuildData guild = GuildManager.getGuildFromPlayer(player);
        player.sendMessage("<sep>");
        player.sendMessage("                            <6>Guild Log (Page 1 of 1)");
        player.sendMessage("");
        if (guild == null || guild.getAuditLog().isEmpty()) player.sendMessage("<7>There are no guild log entries.");
        else guild.getAuditLog().stream().limit(10)
                .forEach(player::sendMessage);
        player.sendMessage("<sep>");
    }

    private void withArgument(HypixelPlayer player, String argument, String usage, java.util.function.Consumer<String> action) {
        if (argument == null) {
            player.sendMessage("<c>Usage: {}", usage);
            return;
        }
        action.accept(argument);
    }

    private void showGuildInfo(HypixelPlayer player) {
        GuildData guild = GuildManager.getGuildFromPlayer(player);
        if (guild == null) {
            player.sendMessage("<sep>");
            player.sendMessage("<c>You are not in a guild!");
            player.sendMessage("<sep>");
            return;
        }

        player.sendMessage("<sep>");
        Text name = Text.of("<6><l>{}</l>", guild.getName());
        if (guild.getTag() != null) {
            name = name.append(" <7>[{}]", Text.legacy(guild.getTagColor() + guild.getTag()));
        }
        player.sendMessage(name);
        player.sendMessage("<7>Level: <a>{}", guild.getLevel());
        player.sendMessage("<7>Members: <a>{}/{}", guild.getMembers().size(), GuildData.MAX_MEMBERS);
        player.sendMessage("<7>GEXP: <a>{}", guild.getTotalGexp());
        if (guild.getMotd() != null && !guild.getMotd().isEmpty()) {
            player.sendMessage("<7>MOTD: <f>{}", guild.getMotd());
        }
        if (guild.getDescription() != null && !guild.getDescription().isEmpty()) {
            player.sendMessage("<7>Description: <f>{}", guild.getDescription());
        }
        player.sendMessage("<sep>");
    }

    private void showOnlineMembers(HypixelPlayer player) {
        GuildData guild = GuildManager.getGuildFromPlayer(player);
        if (guild == null) {
            player.sendMessage("<sep>");
            player.sendMessage("<c>You are not in a guild!");
            player.sendMessage("<sep>");
            return;
        }

        player.sendMessage("<sep>");
        player.sendMessage("<6>Guild Members - <a>{}", guild.getMembers().size());
        Text memberList = Text.join(Text.of("<7>, "), guild.getMembers().stream()
            .map(m -> HypixelPlayer.getDisplayName(m.getUuid()))
            .toList());
        player.sendMessage(memberList);
        player.sendMessage("<sep>");
    }

    private void showHelp(HypixelPlayer player) {
        player.sendMessage("<sep>");
        player.sendMessage("Guild Commands:");
        String[][] commands = {
            {"accept", "Accepts a guild invitation"}, {"chat <chat message>", "Send a chat message to your guild chat channel"},
            {"create <name>", "Creates a guild with the specified name"}, {"demote <player>", "Demotes the player to the previous rank"},
            {"disband", "Disbands the guild"}, {"discord", "Set or view the guild's discord link"}, {"help", "Prints this help message"},
            {"history", "View the last 24 hours of guild events"}, {"info", "Prints information about your guild"},
            {"invite <player>", "Invites the player to your guild"}, {"join <guild>", "Request to join the specified guild"},
            {"kick <player> <reason>", "Kicks the player from your guild"}, {"leave", "Leaves your current guild"},
            {"log", "View the audit log"}, {"member", "Displays information about the Guild Member"}, {"members", "Lists players in your guild"},
            {"menu", "Opens the Guild Menu"}, {"motd", "Modifies the MOTD for the Guild"}, {"mute <player/everyone> <time>", "Mutes a player or the whole guild"},
            {"mypermissions", "View your rank's permissions"}, {"notifications", "Toggle guild join/leave notifications"},
            {"officerchat", "Send a chat message to your guild officer chat channel"}, {"online", "Show the current online members of your guild"},
            {"onlinemode", "Toggle if offline players are displayed in the guild list"}, {"party", "Forms a party from your online Guild Members"},
            {"permissions", "Modify a rank's permissions"}, {"promote <player>", "Promotes the player to the next rank"},
            {"quest", "Shows information regarding the current Guild Quest"}, {"rename <name>", "Renames the Guild"},
            {"setrank <player> <rank>", "Sets a player's rank"}, {"settings <setting> <value>", "Modify settings for your guild"},
            {"slow", "Toggle slow chat, requiring guild members to wait 10 seconds between messages"}, {"tag", "Sets the guild [TAG]"},
            {"tagcolor", "Sets the guild tag color"}, {"toggle", "Toggle guild chat for yourself"}, {"top", "Lists the players with most experience earned"},
            {"transfer <player>", "Transfers ownership of the guild to another player"}, {"unmute <player/everyone>", "Unmute a player or the whole guild"}
        };
        for (String[] entry : commands) sendClickableCommand(player, entry[0], entry[1]);
        player.sendMessage("<sep>");
    }

    private void showMotdHelp(HypixelPlayer player) {
        player.sendMessage("<sep>");
        player.sendMessage("Guild MOTD Commands: ");
        sendClickableCommand(player, "motd add <text>", "Adds a line in the MOTD");
        sendClickableCommand(player, "motd clear", "Clears the MOTD");
        sendClickableCommand(player, "motd help", "Prints this help message");
        sendClickableCommand(player, "motd list", "List lines in the MOTD");
        sendClickableCommand(player, "motd preview", "Preview what the MOTD will look like to players");
        sendClickableCommand(player, "motd set <line> <text>", "Sets a line in the MOTD");
        player.sendMessage("<sep>");
    }

    private void sendClickableCommand(HypixelPlayer player, String command, String description) {
        String fullCommand = "/guild " + command;
        player.sendMessage("<click:suggest:'{0}'><hover:'<7>Click to put the command in chat'><e>{0}<7> - <b>{1}</hover></click>",
                fullCommand, description);
    }
}
