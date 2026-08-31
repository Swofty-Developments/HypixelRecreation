package net.swofty.type.bedwarsconfigurator.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentDouble;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.swofty.commons.Tuple;
import net.swofty.commons.bedwars.BedWarsGameType;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.GeneratorSpeed;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.TeamKey;
import net.swofty.commons.mc.HypixelPosition;
import net.swofty.commons.mc.Vec3i;
import net.swofty.commons.text.Text;
import net.swofty.type.bedwarsconfigurator.TypeBedWarsConfiguratorLoader;
import net.swofty.type.bedwarsconfigurator.autosetup.AutoSetupSession;
import net.swofty.type.bedwarsconfigurator.autosetup.DebugMarkerManager;
import net.swofty.type.bedwarsconfigurator.autosetup.WorldScanner;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.raycast.Ray;
import net.swofty.type.generic.raycast.RayBlockFinder;
import net.swofty.type.generic.raycast.RayIntersection;
import net.swofty.type.generic.user.categories.Rank;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@CommandParameters(labels = "setup mapsetup autosetup", description = "Automatic BedWars map configuration tool", usage = "/autosetup <subcommand>", permission = Rank.STAFF, allowsConsole = false)
public class AutoSetupCommand extends HypixelCommand {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, _) -> {
            sendHelp(sender);
        });

        registerScanCommand(command);
        registerBoundsCommand(command);
        registerTypeCommand(command);
        registerTeamCommand(command);
        registerGlobalCommand(command);
        registerLocationCommand(command);
        registerShowCommand(command);
        registerHideCommand(command);
        registerSnapCommand(command);
        registerStatusCommand(command);
        registerSaveCommand(command);
        registerGeneratorSettingsCommand(command);
        registerMapInfoCommand(command);
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Text.of("<6><l>=== BedWars Auto Setup ==="));
        sender.sendMessage(Text.of("<e>/autosetup scan <7>- Scan world for beds, generators, etc."));
        sender.sendMessage(Text.of("<e>/autosetup bounds \\<min|max> [x y z] <7>- Set map bounds"));
        sender.sendMessage(Text.of("<e>/autosetup type \\<add|remove> \\<type> <7>- Configure game types"));
        sender.sendMessage(Text.of("<e>/autosetup team \\<team> \\<spawn|bed|generator|itemshop|teamshop> [x y z] <7>- Set team positions"));
        sender.sendMessage(Text.of("<e>/autosetup global \\<diamond|emerald> \\<add|remove> [x y z] <7>- Manage global generators"));
        sender.sendMessage(Text.of("<e>/autosetup waiting [x y z] <7>- Set waiting spawn"));
        sender.sendMessage(Text.of("<e>/autosetup waitinglobby \\<min|max> [x y z] <7>- Select the removable waiting lobby"));
        sender.sendMessage(Text.of("<e>/autosetup spectator [x y z] <7>- Set spectator spawn"));
        sender.sendMessage(Text.of("<e>/autosetup show <7>- Show debug markers"));
        sender.sendMessage(Text.of("<e>/autosetup hide <7>- Hide debug markers"));
        sender.sendMessage(Text.of("<e>/autosetup snap <7>- Snap team spawns and shops to the nearest compass direction"));
        sender.sendMessage(Text.of("<e>/autosetup status <7>- Show current configuration status"));
        sender.sendMessage(Text.of("<e>/autosetup name \\<name> <7>- Set map display name"));
        sender.sendMessage(Text.of("<e>/autosetup generator \\<slow|medium|fast|very_fast> <7>- Configure generator speed"));
        sender.sendMessage(Text.of("<e>/autosetup save <7>- Save configuration to maps.json"));
    }

    private void registerScanCommand(MinestomCommand command) {
        command.addSyntax((sender, _) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            Instance instance = player.getInstance();
            if (instance == null) {
                player.sendMessage(Text.of("<c>You must be in a map instance to scan."));
                return;
            }

            AutoSetupSession session = AutoSetupSession.getOrCreate(player.getUuid(), instance);

            if (!session.hasBounds()) {
                player.sendMessage(Text.of("<c>Please set bounds first using /autosetup bounds min and /autosetup bounds max"));
                return;
            }

            player.sendMessage(Text.of("<e>Scanning world... This may take a moment."));

            WorldScanner scanner = new WorldScanner(instance, session);
            WorldScanner.ScanResult result = scanner.fullScan();

            for (String msg : result.getMessages()) {
                player.sendMessage(Text.of("<a>✔ {}", msg));
            }
            for (String warning : result.getWarnings()) {
                player.sendMessage(Text.of("<6>⚠ {}", warning));
            }
            for (String error : result.getErrors()) {
                player.sendMessage(Text.of("<c>✖ {}", error));
            }

            if (!result.hasErrors()) {
                player.sendMessage(Text.of("<a>Scan complete! Use /autosetup show to visualize, /autosetup status to review."));
            }

            DebugMarkerManager.refreshMarkers(player.getUuid(), session, instance);

        }, ArgumentType.Literal("scan"));
    }

    private void registerBoundsCommand(MinestomCommand command) {
        var cornerArg = ArgumentType.String("corner");
        cornerArg.setSuggestionCallback((_, _, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("min"));
            suggestion.addEntry(new SuggestionEntry("max"));
        });

        // /autosetup bounds <corner> - use player position
        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            String corner = context.get(cornerArg);
            Pos pos = player.getPosition();

            AutoSetupSession session = AutoSetupSession.getOrCreate(player.getUuid(), player.getInstance());

            if (corner.equalsIgnoreCase("min")) {
                session.setBoundsMin(pos.x(), pos.y(), pos.z());
                player.sendMessage(Text.of("<a>Set bounds minimum to {}", formatPos(pos)));
            } else if (corner.equalsIgnoreCase("max")) {
                session.setBoundsMax(pos.x(), pos.y(), pos.z());
                player.sendMessage(Text.of("<a>Set bounds maximum to {}", formatPos(pos)));
            } else {
                player.sendMessage(Text.of("<c>Invalid corner. Use 'min' or 'max'."));
            }

            DebugMarkerManager.refreshMarkers(player.getUuid(), session, player.getInstance());

        }, ArgumentType.Literal("bounds"), cornerArg);

        // /autosetup bounds <corner> <x> <y> <z>
        ArgumentDouble xArg = ArgumentType.Double("x");
        ArgumentDouble yArg = ArgumentType.Double("y");
        ArgumentDouble zArg = ArgumentType.Double("z");

        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            String corner = context.get(cornerArg);
            double x = context.get(xArg);
            double y = context.get(yArg);
            double z = context.get(zArg);

            AutoSetupSession session = AutoSetupSession.getOrCreate(player.getUuid(), player.getInstance());

            if (corner.equalsIgnoreCase("min")) {
                session.setBoundsMin(x, y, z);
                player.sendMessage(Text.of("<a>Set bounds minimum to {}, {}, {}", x, y, z));
            } else if (corner.equalsIgnoreCase("max")) {
                session.setBoundsMax(x, y, z);
                player.sendMessage(Text.of("<a>Set bounds maximum to {}, {}, {}", x, y, z));
            }

            DebugMarkerManager.refreshMarkers(player.getUuid(), session, player.getInstance());
        }, ArgumentType.Literal("bounds"), cornerArg, xArg, yArg, zArg);
    }

    private void registerTypeCommand(MinestomCommand command) {
        var actionArg = ArgumentType.String("action");
        actionArg.setSuggestionCallback((sender, ctx, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("add"));
            suggestion.addEntry(new SuggestionEntry("remove"));
        });

        var typeArg = ArgumentType.String("typeName");
        typeArg.setSuggestionCallback((sender, ctx, suggestion) -> {
            for (BedWarsGameType type : BedWarsGameType.values()) {
                suggestion.addEntry(new SuggestionEntry(type.name()));
            }
        });

        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            String action = context.get(actionArg);
            String typeName = context.get(typeArg);

            BedWarsGameType gameType = BedWarsGameType.from(typeName);
            if (gameType == null) {
                player.sendMessage(Text.of("<c>Invalid game type: {}", typeName));
                return;
            }

            AutoSetupSession session = AutoSetupSession.getOrCreate(player.getUuid(), player.getInstance());

            if (action.equalsIgnoreCase("add")) {
                if (!session.getGameTypes().contains(gameType)) {
                    session.getGameTypes().add(gameType);
                    player.sendMessage(Text.of("<a>Added game type: {}", gameType.getDisplayName()));
                } else {
                    player.sendMessage(Text.of("<e>Game type already added: {}", gameType.getDisplayName()));
                }
            } else if (action.equalsIgnoreCase("remove")) {
                if (session.getGameTypes().remove(gameType)) {
                    player.sendMessage(Text.of("<c>Removed game type: {}", gameType.getDisplayName()));
                } else {
                    player.sendMessage(Text.of("<e>Game type not in list: {}", gameType.getDisplayName()));
                }
            }
        }, ArgumentType.Literal("type"), actionArg, typeArg);
    }

    private void registerTeamCommand(MinestomCommand command) {
        var teamArg = ArgumentType.String("teamName");
        teamArg.setSuggestionCallback((sender, ctx, suggestion) -> {
            Set<SuggestionEntry> entries = new HashSet<>();
            for (TeamKey team : TeamKey.values()) {
                entries.add(new SuggestionEntry(team.name()));
            }

            String input = ctx.getInput();
            String currentInput = input.substring(input.lastIndexOf(" ") + 1).trim().toLowerCase(Locale.ROOT);

            if (currentInput.isEmpty()) {
                entries.forEach(suggestion::addEntry);
                return;
            }

            entries.stream().filter(entry -> entry.getEntry().toLowerCase(Locale.ROOT).startsWith(currentInput)).forEach(suggestion::addEntry);
        });

        var propertyArg = ArgumentType.String("property");
        propertyArg.setSuggestionCallback((_, ctx, suggestion) -> {
            Set<SuggestionEntry> entries = new HashSet<>();
            entries.add(new SuggestionEntry("spawn"));
            entries.add(new SuggestionEntry("bed"));
            entries.add(new SuggestionEntry("generator"));
            entries.add(new SuggestionEntry("itemshop"));
            entries.add(new SuggestionEntry("teamshop"));
            entries.add(new SuggestionEntry("remove"));

            String input = ctx.getInput();
            String currentInput = input.substring(input.lastIndexOf(" ") + 1).trim().toLowerCase(Locale.ROOT);

            if (currentInput.isEmpty()) {
                entries.forEach(suggestion::addEntry);
                return;
            }

            entries.stream().filter(entry -> entry.getEntry().toLowerCase(Locale.ROOT).startsWith(currentInput)).forEach(suggestion::addEntry);
        });

        // /autosetup team <team> <property> - use player position
        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            String teamName = context.get(teamArg);
            String property = context.get(propertyArg);

            TeamKey teamKey;
            try {
                teamKey = TeamKey.valueOf(teamName.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                player.sendMessage(Text.of("<c>Invalid team: {}", teamName));
                return;
            }

            AutoSetupSession session = AutoSetupSession.getOrCreate(player.getUuid(), player.getInstance());
            AutoSetupSession.TeamConfig teamConfig = session.getOrCreateTeam(teamKey);
            Pos pos = player.getPosition();

            setTeamProperty(player, teamKey, teamConfig, property, pos);
            DebugMarkerManager.refreshMarkers(player.getUuid(), session, player.getInstance());
        }, ArgumentType.Literal("team"), teamArg, propertyArg);

        // /autosetup team <team> <property> <x> <y> <z>
        ArgumentDouble xArg = ArgumentType.Double("x");
        ArgumentDouble yArg = ArgumentType.Double("y");
        ArgumentDouble zArg = ArgumentType.Double("z");

        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            String teamName = context.get(teamArg);
            String property = context.get(propertyArg);
            double x = context.get(xArg);
            double y = context.get(yArg);
            double z = context.get(zArg);

            TeamKey teamKey;
            try {
                teamKey = TeamKey.valueOf(teamName.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                player.sendMessage(Text.of("<c>Invalid team: {}", teamName));
                return;
            }

            AutoSetupSession session = AutoSetupSession.getOrCreate(player.getUuid(), player.getInstance());
            AutoSetupSession.TeamConfig teamConfig = session.getOrCreateTeam(teamKey);
            Pos pos = new Pos(x, y, z, player.getPosition().yaw(), player.getPosition().pitch());

            setTeamProperty(player, teamKey, teamConfig, property, pos);
            DebugMarkerManager.refreshMarkers(player.getUuid(), session, player.getInstance());

        }, ArgumentType.Literal("team"), teamArg, propertyArg, xArg, yArg, zArg);
    }

    private void setTeamProperty(Player player, TeamKey key, AutoSetupSession.TeamConfig teamConfig, String property, Pos pos) {
        final HypixelPosition currentPosition = new HypixelPosition(pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch());
        switch (property.toLowerCase(Locale.ROOT)) {
            case "spawn" -> {
                teamConfig.setSpawn(currentPosition);
                player.sendMessage(Text.of("<a>Set team spawn to {}", formatPos(pos)));
            }
            case "bed" -> {
                Optional<Tuple<Vec3i, Vec3i>> positions = calculateBedHead(player);
                positions.ifPresentOrElse(position -> {
                    Vec3i head = position.getKey();
                    Vec3i feet = position.getValue();

                    teamConfig.setBedFeet(feet);
                    teamConfig.setBedHead(head);
                    player.sendMessage(Text.of("<a>Set team bed (feet: {}, head: {})", formatPosition(feet), formatPosition(head)));
                }, () -> player.sendMessage(Text.of("<c>You must be looking at a bed block to set the bed position.")));
            }
            case "generator" -> {
                teamConfig.setGenerator(new HypixelPosition(pos.x(), pos.y(), pos.z()));
                player.sendMessage(Text.of("<a>Set team generator to {}", formatPos(pos)));
            }
            case "itemshop" -> {
                teamConfig.setItemShop(currentPosition);
                player.sendMessage(Text.of("<a>Set item shop to {}", formatPos(pos)));
            }
            case "teamshop" -> {
                teamConfig.setTeamShop(currentPosition);
                player.sendMessage(Text.of("<a>Set team shop to {}", formatPos(pos)));
            }
            case "remove" -> {
                AutoSetupSession.get(player.getUuid()).removeTeam(key);
                player.sendMessage(Text.of("<c>Removed all properties for team"));
            }
            default -> player.sendMessage(Text.of("<c>Unknown property: {}", property));
        }
    }

    private Optional<Tuple<Vec3i, Vec3i>> calculateBedHead(Player player) {
        Pos start = player.getPosition();

        Ray ray = new Ray(start, start.direction().mul(5));

        RayBlockFinder finder = ray.findBlocks(player.getInstance());

        RayIntersection<Block> hit = null;
        while (finder.hasNext()) {
            RayIntersection<Block> result = finder.nextClosest();
            if (result == null) break;

            Block block = result.object();
            if (block.key().value().endsWith("_bed")) {
                hit = result;
                break;
            }
        }

        if (hit == null) return Optional.empty();

        Block bedBlock = hit.object();
        Point hitPoint = hit.point();

        Vec3i hitPos = new Vec3i(hitPoint.blockX(), hitPoint.blockY(), hitPoint.blockZ());

        String part = bedBlock.getProperty("part");
        String facing = bedBlock.getProperty("facing");

        if (part == null || facing == null) {
            Logger.warn("Hit bed block missing 'part' or 'facing'");
            return Optional.empty();
        }

        Vec3i offset = switch (facing) {
            case "north" -> new Vec3i(0, 0, -1);
            case "south" -> new Vec3i(0, 0, 1);
            case "west" -> new Vec3i(-1, 0, 0);
            case "east" -> new Vec3i(1, 0, 0);
            default -> Vec3i.ZERO;
        };

        Vec3i headPos;
        Vec3i footPos;

        if ("head".equals(part)) {
            headPos = hitPos;
            footPos = headPos.sub(offset);
        } else {
            footPos = hitPos;
            headPos = hitPos.add(offset);
        }

        return Optional.of(new Tuple<>(headPos, footPos));
    }

    private void registerGlobalCommand(MinestomCommand command) {
        var genTypeArg = ArgumentType.String("gentype");
        genTypeArg.setSuggestionCallback((sender, ctx, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("diamond"));
            suggestion.addEntry(new SuggestionEntry("emerald"));
        });

        var actionArg = ArgumentType.String("action");
        actionArg.setSuggestionCallback((sender, ctx, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("add"));
            suggestion.addEntry(new SuggestionEntry("remove"));
            suggestion.addEntry(new SuggestionEntry("clear"));
        });

        // /autosetup global <type> <action> - use player position
        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            String genType = context.get(genTypeArg);
            String action = context.get(actionArg);
            Pos pos = player.getPosition();

            AutoSetupSession session = AutoSetupSession.getOrCreate(player.getUuid(), player.getInstance());
            List<HypixelPosition> generators = genType.equalsIgnoreCase("diamond") ? session.getDiamondGenerators() : session.getEmeraldGenerators();

            handleGlobalGeneratorAction(player, generators, action, pos, genType);
            DebugMarkerManager.refreshMarkers(player.getUuid(), session, player.getInstance());

        }, ArgumentType.Literal("global"), genTypeArg, actionArg);

        // /autosetup global <type> <action> <x> <y> <z>
        ArgumentDouble xArg = ArgumentType.Double("x");
        ArgumentDouble yArg = ArgumentType.Double("y");
        ArgumentDouble zArg = ArgumentType.Double("z");

        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            String genType = context.get(genTypeArg);
            String action = context.get(actionArg);
            double x = context.get(xArg);
            double y = context.get(yArg);
            double z = context.get(zArg);

            AutoSetupSession session = AutoSetupSession.getOrCreate(player.getUuid(), player.getInstance());
            List<HypixelPosition> generators = genType.equalsIgnoreCase("diamond") ? session.getDiamondGenerators() : session.getEmeraldGenerators();

            handleGlobalGeneratorAction(player, generators, action, new Pos(x, y, z), genType);
            DebugMarkerManager.refreshMarkers(player.getUuid(), session, player.getInstance());

        }, ArgumentType.Literal("global"), genTypeArg, actionArg, xArg, yArg, zArg);
    }

    private void handleGlobalGeneratorAction(Player player, List<HypixelPosition> generators, String action, Pos pos, String genType) {
        switch (action.toLowerCase(Locale.ROOT)) {
            case "add" -> {
                HypixelPosition newPos = new HypixelPosition(pos.x(), pos.y(), pos.z());
                generators.add(newPos);
                player.sendMessage(Text.of("<a>Added {} generator at {} (Total: {})", genType, formatPos(pos), generators.size()));
            }
            case "remove" -> {
                // Remove nearest generator within 2 blocks
                HypixelPosition toRemove = null;
                double minDist = Double.MAX_VALUE;

                for (HypixelPosition gen : generators) {
                    double dist = Math.sqrt(Math.pow(gen.x() - pos.x(), 2) + Math.pow(gen.y() - pos.y(), 2) + Math.pow(gen.z() - pos.z(), 2));
                    if (dist < minDist && dist < 2) {
                        minDist = dist;
                        toRemove = gen;
                    }
                }

                if (toRemove != null) {
                    generators.remove(toRemove);
                    player.sendMessage(Text.of("<c>Removed nearest {} generator (Total: {})", genType, generators.size()));
                } else {
                    player.sendMessage(Text.of("<c>No {} generator found within 2 blocks", genType));
                }
            }
            case "clear" -> {
                int count = generators.size();
                generators.clear();
                player.sendMessage(Text.of("<c>Cleared all {} {} generators", count, genType));
            }
            default -> player.sendMessage(Text.of("<c>Unknown action: {}", action));
        }
    }

    private void registerLocationCommand(MinestomCommand command) {
        var waitingCornerArg = ArgumentType.String("waitingCorner");
        waitingCornerArg.setSuggestionCallback((_, _, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("min"));
            suggestion.addEntry(new SuggestionEntry("max"));
        });
        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player) || !permissionCheck(sender)) return;
            setWaitingLobbyCorner(player, context.get(waitingCornerArg), player.getPosition());
        }, ArgumentType.Literal("waitinglobby"), waitingCornerArg);

        // /autosetup waiting [x y z]
        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            Pos pos = player.getPosition();
            AutoSetupSession session = AutoSetupSession.getOrCreate(player.getUuid(), player.getInstance());
            session.setWaitingLocation(new HypixelPosition(pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch()));
            player.sendMessage(Text.of("<a>Set waiting spawn to {}", formatPos(pos)));
            DebugMarkerManager.refreshMarkers(player.getUuid(), session, player.getInstance());

        }, ArgumentType.Literal("waiting"));

        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            Pos pos = player.getPosition();
            AutoSetupSession session = AutoSetupSession.getOrCreate(player.getUuid(), player.getInstance());
            session.setSpectatorLocation(new HypixelPosition(pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch()));
            player.sendMessage(Text.of("<a>Set spectator spawn to {}", formatPos(pos)));
            DebugMarkerManager.refreshMarkers(player.getUuid(), session, player.getInstance());

        }, ArgumentType.Literal("spectator"));

        // With coordinates
        ArgumentDouble xArg = ArgumentType.Double("x");
        ArgumentDouble yArg = ArgumentType.Double("y");
        ArgumentDouble zArg = ArgumentType.Double("z");

        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player) || !permissionCheck(sender)) return;
            setWaitingLobbyCorner(player, context.get(waitingCornerArg),
                    new Pos(context.get(xArg), context.get(yArg), context.get(zArg)));
        }, ArgumentType.Literal("waitinglobby"), waitingCornerArg, xArg, yArg, zArg);

        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            double x = context.get(xArg);
            double y = context.get(yArg);
            double z = context.get(zArg);

            AutoSetupSession session = AutoSetupSession.getOrCreate(player.getUuid(), player.getInstance());
            session.setWaitingLocation(new HypixelPosition(x, y, z, 0, 0));
            player.sendMessage(Text.of("<a>Set waiting spawn to {}, {}, {}", x, y, z));
            DebugMarkerManager.refreshMarkers(player.getUuid(), session, player.getInstance());

        }, ArgumentType.Literal("waiting"), xArg, yArg, zArg);

        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            double x = context.get(xArg);
            double y = context.get(yArg);
            double z = context.get(zArg);

            AutoSetupSession session = AutoSetupSession.getOrCreate(player.getUuid(), player.getInstance());
            session.setSpectatorLocation(new HypixelPosition(x, y, z, 0, 0));
            player.sendMessage(Text.of("<a>Set spectator spawn to {}, {}, {}", x, y, z));
            DebugMarkerManager.refreshMarkers(player.getUuid(), session, player.getInstance());

        }, ArgumentType.Literal("spectator"), xArg, yArg, zArg);
    }

    private void setWaitingLobbyCorner(Player player, String corner, Pos pos) {
        AutoSetupSession session = AutoSetupSession.getOrCreate(player.getUuid(), player.getInstance());
        Vec3i block = new Vec3i(pos.blockX(), pos.blockY(), pos.blockZ());
        if (corner.equalsIgnoreCase("min")) session.setWaitingLobbyMin(block);
        else if (corner.equalsIgnoreCase("max")) session.setWaitingLobbyMax(block);
        else {
            player.sendMessage(Text.of("<c>Use min or max."));
            return;
        }
        player.sendMessage(Text.of("<a>Set waiting lobby {} to {}", corner.toLowerCase(Locale.ROOT), formatPosition(block)));
        DebugMarkerManager.refreshMarkers(player.getUuid(), session, player.getInstance());
    }

    private void registerShowCommand(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            AutoSetupSession session = AutoSetupSession.get(player.getUuid());
            if (session == null) {
                player.sendMessage(Text.of("<c>No configuration session active. Use /autosetup scan or set bounds first."));
                return;
            }

            DebugMarkerManager.showMarkers(player.getUuid(), session, player.getInstance());
            player.sendMessage(Text.of("<a>Showing debug markers"));

        }, ArgumentType.Literal("show"));
    }

    private void registerHideCommand(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            DebugMarkerManager.hideMarkers(player.getUuid());
            player.sendMessage(Text.of("<c>Hidden debug markers"));

        }, ArgumentType.Literal("hide"));
    }

    private void registerStatusCommand(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            AutoSetupSession session = AutoSetupSession.get(player.getUuid());
            if (session == null) {
                player.sendMessage(Text.of("<c>No configuration session active."));
                return;
            }

            player.sendMessage(Text.of("<6><l>=== Configuration Status ==="));
            player.sendMessage(Text.of("<e>Map ID: <f>{}", statusValue(session.getMapId())));
            player.sendMessage(Text.of("<e>Map Name: <f>{}", statusValue(session.getMapName())));
            player.sendMessage(Text.of("<e>Bounds: <f>{}", session.hasBounds()
                    ? Text.of("<a>✔ Set")
                    : Text.of("<c>✖ Not set")));
            player.sendMessage(Text.of("<e>Game Types: <f>{}", session.getGameTypes().isEmpty()
                    ? Text.of("<c>(none)")
                    : Text.literal(session.getGameTypes().toString())));
            player.sendMessage(Text.of("<e>Teams Configured: <f>{}", session.getTeams().size()));

            for (var entry : session.getTeams().entrySet()) {
                TeamKey team = entry.getKey();
                AutoSetupSession.TeamConfig config = entry.getValue();
                Text status = Text.of("  <color:{}>{}<7>: ", team.chatColor(), team.getName())
                    .append(config.getSpawn() != null ? "<a>S " : "<c>S ")
                    .append(config.getBedFeet() != null ? "<a>B " : "<c>B ")
                    .append(config.getGenerator() != null ? "<a>G " : "<c>G ")
                    .append(config.getItemShop() != null ? "<a>IS " : "<c>IS ")
                    .append(config.getTeamShop() != null ? "<a>TS" : "<c>TS");
                player.sendMessage(status);
            }

            player.sendMessage(Text.of("<e>Diamond Generators: <f>{}", session.getDiamondGenerators().size()));
            player.sendMessage(Text.of("<e>Emerald Generators: <f>{}", session.getEmeraldGenerators().size()));
            player.sendMessage(Text.of("<e>Waiting Location: <f>{}", session.getWaitingLocation() != null
                    ? Text.of("<a>✔")
                    : Text.of("<c>✖")));
            player.sendMessage(Text.of("<e>Spectator Location: <f>{}", session.getSpectatorLocation() != null
                    ? Text.of("<a>✔")
                    : Text.of("<c>✖")));

        }, ArgumentType.Literal("status"));
    }

    private void registerSnapCommand(MinestomCommand command) {
        command.addSyntax((sender, _) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            AutoSetupSession session = AutoSetupSession.get(player.getUuid());
            if (session == null) {
                player.sendMessage(Text.of("<c>No configuration session active."));
                return;
            }

            int snapped = 0;
            for (AutoSetupSession.TeamConfig config : session.getTeams().values()) {
                if (config.getSpawn() != null) {
                    config.setSpawn(snapDirection(config.getSpawn()));
                    snapped++;
                }
                if (config.getItemShop() != null) {
                    config.setItemShop(snapDirection(config.getItemShop()));
                    snapped++;
                }
                if (config.getTeamShop() != null) {
                    config.setTeamShop(snapDirection(config.getTeamShop()));
                    snapped++;
                }
            }

            DebugMarkerManager.refreshMarkers(player.getUuid(), session, player.getInstance());
            player.sendMessage(Text.of("<a>Snapped {} spawn and shop positions to the nearest of 8 compass directions.", snapped));
        }, ArgumentType.Literal("snap"));
    }

    private HypixelPosition snapDirection(HypixelPosition position) {
        float yaw = Math.round(position.yaw() / 45.0f) * 45.0f;
        if (yaw >= 180.0f) yaw -= 360.0f;
        if (yaw < -180.0f) yaw += 360.0f;
        return new HypixelPosition(position.x(), position.y(), position.z(), yaw, 0);
    }

    private void registerMapInfoCommand(MinestomCommand command) {
        ArgumentString nameArg = ArgumentType.String("name");

        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            AutoSetupSession session = AutoSetupSession.get(player.getUuid());
            if (session == null || session.getMapId() == null) {
                player.sendMessage(Text.of("<c>No map selected. Use /choosemap \\<map> first."));
                return;
            }

            String name = context.get(nameArg);
            session.setMapName(name);

            player.sendMessage(Text.of("<a>Set map name to '{}' (ID: {})", name, session.getMapId()));

        }, ArgumentType.Literal("name"), nameArg);
    }

    private void registerGeneratorSettingsCommand(MinestomCommand command) {
        // Speed setting command
        var speedArg = ArgumentType.String("speed");
        speedArg.setSuggestionCallback((sender, ctx, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("SLOW"));
            suggestion.addEntry(new SuggestionEntry("MEDIUM"));
            suggestion.addEntry(new SuggestionEntry("FAST"));
            suggestion.addEntry(new SuggestionEntry("SUPER_FAST"));
        });

        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            String speedStr = context.get(speedArg);
            AutoSetupSession session = AutoSetupSession.getOrCreate(player.getUuid(), player.getInstance());

            try {
                GeneratorSpeed speed = GeneratorSpeed.valueOf(speedStr.toUpperCase(Locale.ROOT));
                session.setGeneratorSpeed(speed);
                player.sendMessage(Text.of("<a>Set generator speed to {} ({} iron/{}s, {} gold/{}s)",
                        speed.name(), speed.getIronAmount(), speed.getIronDelaySeconds(),
                        speed.getGoldAmount(), speed.getGoldDelaySeconds()));
            } catch (IllegalArgumentException e) {
                player.sendMessage(Text.of("<c>Invalid speed: {}", speedStr));
            }

        }, ArgumentType.Literal("generator"), ArgumentType.Literal("speed"), speedArg);
    }

    private void registerSaveCommand(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            AutoSetupSession session = AutoSetupSession.get(player.getUuid());
            if (session == null) {
                player.sendMessage(Text.of("<c>No configuration session active."));
                return;
            }

            // Validate required fields
            List<String> errors = validateSession(session);
            if (!errors.isEmpty()) {
                player.sendMessage(Text.of("<c>Cannot save - missing required configuration:"));
                for (String error : errors) {
                    player.sendMessage(Text.of("<c>  • {}", error));
                }
                return;
            }

            try {
                saveToConfig(session);
                player.sendMessage(Text.of("<a>✔ Configuration saved to maps.json!"));
                player.sendMessage(Text.of("<7>Map ID: {}", session.getMapId()));
            } catch (Exception e) {
                player.sendMessage(Text.of("<c>Failed to save: {}", e.getMessage()));
                Logger.error("Failed to save map configuration", e);
            }

        }, ArgumentType.Literal("save"));
    }

    private List<String> validateSession(AutoSetupSession session) {
        List<String> errors = new ArrayList<>();

        if (session.getMapId() == null || session.getMapId().isEmpty()) {
            errors.add("No map selected (use /choosemap <map> first)");
        }
        if (session.getMapName() == null || session.getMapName().isEmpty()) {
            errors.add("Map name not set (use /autosetup name <name>)");
        }
        if (!session.hasBounds()) {
            errors.add("Bounds not set");
        }
        if (session.getGameTypes().isEmpty()) {
            errors.add("No game types set");
        }
        if (session.getTeams().isEmpty()) {
            errors.add("No teams configured");
        }
        if (session.getWaitingLocation() == null) {
            errors.add("Waiting location not set");
        }
        if (session.getSpectatorLocation() == null) {
            errors.add("Spectator location not set");
        }

        for (var entry : session.getTeams().entrySet()) {
            AutoSetupSession.TeamConfig config = entry.getValue();
            String teamName = entry.getKey().getName();
            if (config.getSpawn() == null) {
                errors.add(teamName + " team: spawn not set");
            }
            if (config.getBedFeet() == null || config.getBedHead() == null) {
                errors.add(teamName + " team: bed not set");
            }
            if (config.getGenerator() == null) {
                errors.add(teamName + " team: generator not set");
            }
        }

        return errors;
    }

    private void saveToConfig(AutoSetupSession session) throws IOException {
        Path mapsPath = Path.of("./configuration/bedwars/maps.json");

        BedWarsMapsConfig config;
        if (Files.exists(mapsPath)) {
            String json = Files.readString(mapsPath, StandardCharsets.UTF_8);
            config = GSON.fromJson(json, BedWarsMapsConfig.class);
        } else {
            config = new BedWarsMapsConfig();
            config.setMaps(new ArrayList<>());
        }

        // Remove existing entry with same ID
        config.getMaps().removeIf(entry -> entry.getId().equals(session.getMapId()));

        // Add new entry
        config.getMaps().add(session.toMapEntry());

        // Write back
        String output = GSON.toJson(config);
        Files.writeString(mapsPath, output, StandardCharsets.UTF_8);

        // Reload config in memory
        TypeBedWarsConfiguratorLoader.reloadMapsConfig();
    }

    private String formatPos(Pos pos) {
        return String.format("%.2f, %.2f, %.2f", pos.x(), pos.y(), pos.z());
    }

    private Text statusValue(String value) {
        return value == null ? Text.of("<c>(not set)") : Text.literal(value);
    }

    private String formatPosition(Vec3i pos) {
        return String.format("%d, %d, %d", pos.x(), pos.y(), pos.z());
    }
}
