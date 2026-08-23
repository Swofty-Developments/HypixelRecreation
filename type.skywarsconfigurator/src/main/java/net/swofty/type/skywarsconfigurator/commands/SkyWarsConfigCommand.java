package net.swofty.type.skywarsconfigurator.commands;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skywars.SkywarsGameType;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skywarsconfigurator.MapConfigurationSession;
import net.swofty.type.skywarsconfigurator.TypeSkyWarsConfiguratorLoader;

import java.util.stream.Collectors;

/**
 * Command for configuring SkyWars maps.
 */
@CommandParameters(
        labels = "swconfig",
        description = "Configure SkyWars maps",
        usage = "/swconfig <subcommand>",
        permission = Rank.STAFF,
        allowsConsole = false
)
public class SkyWarsConfigCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        // Default - show usage
        command.setDefaultExecutor((sender, context) -> {
            sender.sendMessage("<e>Usage:");
            sender.sendMessage("<7>/swconfig new \\<id> \\<name> <f>- Start new session");
            sender.sendMessage("<7>/swconfig type \\<type> <f>- Toggle game type");
            sender.sendMessage("<7>/swconfig center <f>- Set map center");
            sender.sendMessage("<7>/swconfig void \\<y> <f>- Set void Y level");
            sender.sendMessage("<7>/swconfig bounds \\<minX> \\<minZ> \\<maxX> \\<maxZ> <f>- Set bounds");
            sender.sendMessage("<7>/swconfig island <f>- Add island spawn");
            sender.sendMessage("<7>/swconfig save <f>- Save configuration");
            sender.sendMessage("<7>/swconfig status <f>- Show current status");
            sender.sendMessage("<8>(Chests are auto-detected at runtime)");
        });

        // /swconfig new <id> <name>
        var newLit = ArgumentType.Literal("new");
        var idArg = ArgumentType.String("id");
        var nameArg = ArgumentType.String("name");
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            HypixelPlayer player = (HypixelPlayer) sender;
            String id = context.get(idArg);
            String name = context.get(nameArg);
            MapConfigurationSession session = new MapConfigurationSession(id, name);
            TypeSkyWarsConfiguratorLoader.setCurrentSession(session);
            player.sendMessage("<a>Started new configuration session for map: {} (id: {})", name, id);
        }, newLit, idArg, nameArg);

        // /swconfig type <type>
        var typeLit = ArgumentType.Literal("type");
        var typeArg = ArgumentType.String("gameType");
        typeArg.setSuggestionCallback((sender, context, suggestion) -> {
            for (SkywarsGameType type : SkywarsGameType.values()) {
                suggestion.addEntry(new SuggestionEntry(type.name()));
            }
        });
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            HypixelPlayer player = (HypixelPlayer) sender;
            MapConfigurationSession session = TypeSkyWarsConfiguratorLoader.getCurrentSession();
            if (session == null) {
                player.sendMessage("<c>No active session!");
                return;
            }
            String typeName = context.get(typeArg);
            SkywarsGameType type = SkywarsGameType.from(typeName);
            if (type == null) {
                player.sendMessage("<c>Invalid type! Available: SOLO_NORMAL, SOLO_INSANE, DOUBLES_NORMAL, SOLO_LUCKY_BLOCK");
                return;
            }
            if (session.getTypes().contains(type)) {
                session.removeType(type);
                player.sendMessage("<c>Removed type: <f>{}", type.name());
            } else {
                session.addType(type);
                player.sendMessage("<a>Added type: <f>{}", type.name());
            }
            player.sendMessage("<7>Current types: <f>{}", session.getTypes().stream()
                    .map(Enum::name).collect(Collectors.joining(", ")));
        }, typeLit, typeArg);

        // /swconfig center
        var centerLit = ArgumentType.Literal("center");
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            if (!(sender instanceof HypixelPlayer player)) return;
            MapConfigurationSession session = TypeSkyWarsConfiguratorLoader.getCurrentSession();
            if (session == null) {
                player.sendMessage("<c>No active session! Use /swconfig new \\<id> \\<name> first.");
                return;
            }
            Pos pos = player.getPosition();
            session.setCenter(pos.x(), pos.y(), pos.z());
            player.sendMessage("<a>Set map center to {}", formatPos(pos));
        }, centerLit);

        // /swconfig void <y>
        var voidLit = ArgumentType.Literal("void");
        var yArg = ArgumentType.Integer("y");
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            HypixelPlayer player = (HypixelPlayer) sender;
            MapConfigurationSession session = TypeSkyWarsConfiguratorLoader.getCurrentSession();
            if (session == null) {
                player.sendMessage("<c>No active session!");
                return;
            }
            int y = context.get(yArg);
            session.setVoidY(y);
            player.sendMessage("<a>Set void Y level to {}", y);
        }, voidLit, yArg);

        // /swconfig bounds <minX> <minZ> <maxX> <maxZ>
        var boundsLit = ArgumentType.Literal("bounds");
        var minXArg = ArgumentType.Integer("minX");
        var minZArg = ArgumentType.Integer("minZ");
        var maxXArg = ArgumentType.Integer("maxX");
        var maxZArg = ArgumentType.Integer("maxZ");
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            HypixelPlayer player = (HypixelPlayer) sender;
            MapConfigurationSession session = TypeSkyWarsConfiguratorLoader.getCurrentSession();
            if (session == null) {
                player.sendMessage("<c>No active session!");
                return;
            }
            int minX = context.get(minXArg);
            int minZ = context.get(minZArg);
            int maxX = context.get(maxXArg);
            int maxZ = context.get(maxZArg);
            session.setBounds(minX, minZ, maxX, maxZ);
            player.sendMessage("<a>Set bounds: ({}, {}) to ({}, {})", minX, minZ, maxX, maxZ);
        }, boundsLit, minXArg, minZArg, maxXArg, maxZArg);

        // /swconfig island
        var islandLit = ArgumentType.Literal("island");
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            if (!(sender instanceof HypixelPlayer player)) return;
            MapConfigurationSession session = TypeSkyWarsConfiguratorLoader.getCurrentSession();
            if (session == null) {
                player.sendMessage("<c>No active session!");
                return;
            }
            Pos pos = player.getPosition();
            session.addIsland(pos);
            player.sendMessage("<a>Added island #{} at {}", session.getIslands().size() - 1, formatPos(pos));
        }, islandLit);

        // /swconfig save
        var saveLit = ArgumentType.Literal("save");
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            MapConfigurationSession session = TypeSkyWarsConfiguratorLoader.getCurrentSession();
            if (session == null) {
                sender.sendMessage("<c>No active session!");
                return;
            }
            session.saveToFile();
            sender.sendMessage("<a>Saved configuration to file!");
        }, saveLit);

        // /swconfig status
        var statusLit = ArgumentType.Literal("status");
        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            HypixelPlayer player = (HypixelPlayer) sender;
            MapConfigurationSession session = TypeSkyWarsConfiguratorLoader.getCurrentSession();
            if (session == null) {
                player.sendMessage("<c>No active session!");
                return;
            }
            player.sendMessage("<e>=== Configuration Status ===");
            player.sendMessage("<7>Map ID: <f>{}", session.getMapId());
            player.sendMessage("<7>Map Name: <f>{}", session.getMapName());
            player.sendMessage("<7>Types: <f>{}", session.getTypes().isEmpty() ? "SOLO_NORMAL (default)" :
                    session.getTypes().stream().map(Enum::name).collect(Collectors.joining(", ")));
            player.sendMessage("<7>Islands: <f>{}", session.getIslands().size());
            player.sendMessage("<7>Center: <f>({}, {}, {})", session.getCenterX(), session.getCenterY(), session.getCenterZ());
            player.sendMessage("<7>Void Y: <f>{}", session.getVoidY());
            player.sendMessage("<8>(Chests are auto-detected at runtime)");
        }, statusLit);
    }

    private static String formatPos(Pos pos) {
        return String.format("(%.1f, %.1f, %.1f)", pos.x(), pos.y(), pos.z());
    }
}
