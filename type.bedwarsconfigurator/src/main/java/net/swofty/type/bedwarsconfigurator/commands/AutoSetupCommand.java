package net.swofty.type.bedwarsconfigurator.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.text.Component;
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
import java.util.*;

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
        sender.sendMessage(Component.text("§6§l=== BedWars Auto Setup ==="));
        sender.sendMessage(Component.text("§e/autosetup scan §7- Scan world for beds, generators, etc."));
        sender.sendMessage(Component.text("§e/autosetup bounds <min|max> [x y z] §7- Set map bounds"));
        sender.sendMessage(Component.text("§e/autosetup type <add|remove> <type> §7- Configure game types"));
        sender.sendMessage(Component.text("§e/autosetup team <team> <spawn|bed|generator|itemshop|teamshop> [x y z] §7- Set team positions"));
        sender.sendMessage(Component.text("§e/autosetup global <diamond|emerald> <add|remove> [x y z] §7- Manage global generators"));
        sender.sendMessage(Component.text("§e/autosetup waiting [x y z] §7- Set waiting spawn"));
        sender.sendMessage(Component.text("§e/autosetup waitinglobby <min|max> [x y z] §7- Select the removable waiting lobby"));
        sender.sendMessage(Component.text("§e/autosetup spectator [x y z] §7- Set spectator spawn"));
        sender.sendMessage(Component.text("§e/autosetup show §7- Show debug markers"));
        sender.sendMessage(Component.text("§e/autosetup hide §7- Hide debug markers"));
        sender.sendMessage(Component.text("§e/autosetup snap §7- Snap team spawns and shops to the nearest compass direction"));
        sender.sendMessage(Component.text("§e/autosetup status §7- Show current configuration status"));
        sender.sendMessage(Component.text("§e/autosetup name <name> §7- Set map display name"));
        sender.sendMessage(Component.text("§e/autosetup generator <slow|medium|fast|very_fast> §7- Configure generator speed"));
        sender.sendMessage(Component.text("§e/autosetup save §7- Save configuration to maps.json"));
    }

    private void registerScanCommand(MinestomCommand command) {
        command.addSyntax((sender, _) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            Instance instance = player.getInstance();
            if (instance == null) {
                player.sendMessage(Component.text("§cYou must be in a map instance to scan."));
                return;
            }

            AutoSetupSession session = AutoSetupSession.getOrCreate(player.getUuid(), instance);

            if (!session.hasBounds()) {
                player.sendMessage(Component.text("§cPlease set bounds first using /autosetup bounds min and /autosetup bounds max"));
                return;
            }

            player.sendMessage(Component.text("§eScanning world... This may take a moment."));

            WorldScanner scanner = new WorldScanner(instance, session);
            WorldScanner.ScanResult result = scanner.fullScan();

            // Send results
            for (String msg : result.getMessages()) {
                player.sendMessage(Component.text("§a✔ " + msg));
            }
            for (String warning : result.getWarnings()) {
                player.sendMessage(Component.text("§6⚠ " + warning));
            }
            for (String error : result.getErrors()) {
                player.sendMessage(Component.text("§c✖ " + error));
            }

            if (!result.hasErrors()) {
                player.sendMessage(Component.text("§aScan complete! Use /autosetup show to visualize, /autosetup status to review."));
            }

            // Refresh markers if shown
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
                player.sendMessage(Component.text("§aSet bounds minimum to " + formatPos(pos)));
            } else if (corner.equalsIgnoreCase("max")) {
                session.setBoundsMax(pos.x(), pos.y(), pos.z());
                player.sendMessage(Component.text("§aSet bounds maximum to " + formatPos(pos)));
            } else {
                player.sendMessage(Component.text("§cInvalid corner. Use 'min' or 'max'."));
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
                player.sendMessage(Component.text("§aSet bounds minimum to " + x + ", " + y + ", " + z));
            } else if (corner.equalsIgnoreCase("max")) {
                session.setBoundsMax(x, y, z);
                player.sendMessage(Component.text("§aSet bounds maximum to " + x + ", " + y + ", " + z));
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
                player.sendMessage(Component.text("§cInvalid game type: " + typeName));
                return;
            }

            AutoSetupSession session = AutoSetupSession.getOrCreate(player.getUuid(), player.getInstance());

            if (action.equalsIgnoreCase("add")) {
                if (!session.getGameTypes().contains(gameType)) {
                    session.getGameTypes().add(gameType);
                    player.sendMessage(Component.text("§aAdded game type: " + gameType.getDisplayName()));
                } else {
                    player.sendMessage(Component.text("§eGame type already added: " + gameType.getDisplayName()));
                }
            } else if (action.equalsIgnoreCase("remove")) {
                if (session.getGameTypes().remove(gameType)) {
                    player.sendMessage(Component.text("§cRemoved game type: " + gameType.getDisplayName()));
                } else {
                    player.sendMessage(Component.text("§eGame type not in list: " + gameType.getDisplayName()));
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
            String currentInput = input.substring(input.lastIndexOf(" ") + 1).trim().toLowerCase();

            if (currentInput.isEmpty()) {
                entries.forEach(suggestion::addEntry);
                return;
            }

            entries.stream().filter(entry -> entry.getEntry().toLowerCase().startsWith(currentInput)).forEach(suggestion::addEntry);
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
            String currentInput = input.substring(input.lastIndexOf(" ") + 1).trim().toLowerCase();

            if (currentInput.isEmpty()) {
                entries.forEach(suggestion::addEntry);
                return;
            }

            entries.stream().filter(entry -> entry.getEntry().toLowerCase().startsWith(currentInput)).forEach(suggestion::addEntry);
        });

        // /autosetup team <team> <property> - use player position
        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            String teamName = context.get(teamArg);
            String property = context.get(propertyArg);

            TeamKey teamKey;
            try {
                teamKey = TeamKey.valueOf(teamName.toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage(Component.text("§cInvalid team: " + teamName));
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
                teamKey = TeamKey.valueOf(teamName.toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage(Component.text("§cInvalid team: " + teamName));
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
        switch (property.toLowerCase()) {
            case "spawn" -> {
                teamConfig.setSpawn(currentPosition);
                player.sendMessage(Component.text("§aSet team spawn to " + formatPos(pos)));
            }
            case "bed" -> {
                Optional<Tuple<Vec3i, Vec3i>> positions = calculateBedHead(player);
                positions.ifPresentOrElse(position -> {
                    Vec3i head = position.getKey();
                    Vec3i feet = position.getValue();

                    teamConfig.setBedFeet(feet);
                    teamConfig.setBedHead(head);
                    player.sendMessage(Component.text("§aSet team bed (feet: " + formatPosition(feet) + ", head: " + formatPosition(head) + ")"));
                }, () -> player.sendMessage(Component.text("§cYou must be looking at a bed block to set the bed position.")));
            }
            case "generator" -> {
                teamConfig.setGenerator(new HypixelPosition(pos.x(), pos.y(), pos.z()));
                player.sendMessage(Component.text("§aSet team generator to " + formatPos(pos)));
            }
            case "itemshop" -> {
                teamConfig.setItemShop(currentPosition);
                player.sendMessage(Component.text("§aSet item shop to " + formatPos(pos)));
            }
            case "teamshop" -> {
                teamConfig.setTeamShop(currentPosition);
                player.sendMessage(Component.text("§aSet team shop to " + formatPos(pos)));
            }
            case "remove" -> {
                AutoSetupSession.get(player.getUuid()).removeTeam(key);
                player.sendMessage(Component.text("§cRemoved all properties for team"));
            }
            default -> player.sendMessage(Component.text("§cUnknown property: " + property));
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
        switch (action.toLowerCase()) {
            case "add" -> {
                HypixelPosition newPos = new HypixelPosition(pos.x(), pos.y(), pos.z());
                generators.add(newPos);
                player.sendMessage(Component.text("§aAdded " + genType + " generator at " + formatPos(pos) + " (Total: " + generators.size() + ")"));
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
                    player.sendMessage(Component.text("§cRemoved nearest " + genType + " generator (Total: " + generators.size() + ")"));
                } else {
                    player.sendMessage(Component.text("§cNo " + genType + " generator found within 2 blocks"));
                }
            }
            case "clear" -> {
                int count = generators.size();
                generators.clear();
                player.sendMessage(Component.text("§cCleared all " + count + " " + genType + " generators"));
            }
            default -> player.sendMessage(Component.text("§cUnknown action: " + action));
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
            player.sendMessage(Component.text("§aSet waiting spawn to " + formatPos(pos)));
            DebugMarkerManager.refreshMarkers(player.getUuid(), session, player.getInstance());

        }, ArgumentType.Literal("waiting"));

        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            Pos pos = player.getPosition();
            AutoSetupSession session = AutoSetupSession.getOrCreate(player.getUuid(), player.getInstance());
            session.setSpectatorLocation(new HypixelPosition(pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch()));
            player.sendMessage(Component.text("§aSet spectator spawn to " + formatPos(pos)));
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
            player.sendMessage(Component.text("§aSet waiting spawn to " + x + ", " + y + ", " + z));
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
            player.sendMessage(Component.text("§aSet spectator spawn to " + x + ", " + y + ", " + z));
            DebugMarkerManager.refreshMarkers(player.getUuid(), session, player.getInstance());

        }, ArgumentType.Literal("spectator"), xArg, yArg, zArg);
    }

    private void setWaitingLobbyCorner(Player player, String corner, Pos pos) {
        AutoSetupSession session = AutoSetupSession.getOrCreate(player.getUuid(), player.getInstance());
        Vec3i block = new Vec3i(pos.blockX(), pos.blockY(), pos.blockZ());
        if (corner.equalsIgnoreCase("min")) session.setWaitingLobbyMin(block);
        else if (corner.equalsIgnoreCase("max")) session.setWaitingLobbyMax(block);
        else {
            player.sendMessage(Component.text("§cUse min or max."));
            return;
        }
        player.sendMessage(Component.text("§aSet waiting lobby " + corner.toLowerCase() + " to " + formatPosition(block)));
        DebugMarkerManager.refreshMarkers(player.getUuid(), session, player.getInstance());
    }

    private void registerShowCommand(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            AutoSetupSession session = AutoSetupSession.get(player.getUuid());
            if (session == null) {
                player.sendMessage(Component.text("§cNo configuration session active. Use /autosetup scan or set bounds first."));
                return;
            }

            DebugMarkerManager.showMarkers(player.getUuid(), session, player.getInstance());
            player.sendMessage(Component.text("§aShowing debug markers"));

        }, ArgumentType.Literal("show"));
    }

    private void registerHideCommand(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            DebugMarkerManager.hideMarkers(player.getUuid());
            player.sendMessage(Component.text("§cHidden debug markers"));

        }, ArgumentType.Literal("hide"));
    }

    private void registerStatusCommand(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            AutoSetupSession session = AutoSetupSession.get(player.getUuid());
            if (session == null) {
                player.sendMessage(Component.text("§cNo configuration session active."));
                return;
            }

            player.sendMessage(Component.text("§6§l=== Configuration Status ==="));
            player.sendMessage(Component.text("§eMap ID: §f" + (session.getMapId() != null ? session.getMapId() : "§c(not set)")));
            player.sendMessage(Component.text("§eMap Name: §f" + (session.getMapName() != null ? session.getMapName() : "§c(not set)")));
            player.sendMessage(Component.text("§eBounds: §f" + (session.hasBounds() ? "✔ Set" : "§c✖ Not set")));
            player.sendMessage(Component.text("§eGame Types: §f" + (session.getGameTypes().isEmpty() ? "§c(none)" : session.getGameTypes().toString())));
            player.sendMessage(Component.text("§eTeams Configured: §f" + session.getTeams().size()));

            for (var entry : session.getTeams().entrySet()) {
                TeamKey team = entry.getKey();
                AutoSetupSession.TeamConfig config = entry.getValue();
                String status = team.chatColor() + team.getName() + "§7: ";
                status += (config.getSpawn() != null ? "§aS" : "§cS") + " ";
                status += (config.getBedFeet() != null ? "§aB" : "§cB") + " ";
                status += (config.getGenerator() != null ? "§aG" : "§cG") + " ";
                status += (config.getItemShop() != null ? "§aIS" : "§cIS") + " ";
                status += (config.getTeamShop() != null ? "§aTS" : "§cTS");
                player.sendMessage(Component.text("  " + status));
            }

            player.sendMessage(Component.text("§eDiamond Generators: §f" + session.getDiamondGenerators().size()));
            player.sendMessage(Component.text("§eEmerald Generators: §f" + session.getEmeraldGenerators().size()));
            player.sendMessage(Component.text("§eWaiting Location: §f" + (session.getWaitingLocation() != null ? "✔" : "§c✖")));
            player.sendMessage(Component.text("§eSpectator Location: §f" + (session.getSpectatorLocation() != null ? "✔" : "§c✖")));

        }, ArgumentType.Literal("status"));
    }

    private void registerSnapCommand(MinestomCommand command) {
        command.addSyntax((sender, _) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            AutoSetupSession session = AutoSetupSession.get(player.getUuid());
            if (session == null) {
                player.sendMessage(Component.text("§cNo configuration session active."));
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
            player.sendMessage(Component.text("§aSnapped " + snapped + " spawn and shop positions to the nearest of 8 compass directions."));
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
                player.sendMessage(Component.text("§cNo map selected. Use /choosemap <map> first."));
                return;
            }

            String name = context.get(nameArg);
            session.setMapName(name);

            player.sendMessage(Component.text("§aSet map name to '" + name + "' (ID: " + session.getMapId() + ")"));

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
                GeneratorSpeed speed = GeneratorSpeed.valueOf(speedStr.toUpperCase());
                session.setGeneratorSpeed(speed);
                player.sendMessage(Component.text("§aSet generator speed to " + speed.name() + " (" + speed.getIronAmount() + " iron/" + speed.getIronDelaySeconds() + "s, " + speed.getGoldAmount() + " gold/" + speed.getGoldDelaySeconds() + "s)"));
            } catch (IllegalArgumentException e) {
                player.sendMessage(Component.text("§cInvalid speed: " + speedStr));
            }

        }, ArgumentType.Literal("generator"), ArgumentType.Literal("speed"), speedArg);
    }

    private void registerSaveCommand(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            AutoSetupSession session = AutoSetupSession.get(player.getUuid());
            if (session == null) {
                player.sendMessage(Component.text("§cNo configuration session active."));
                return;
            }

            // Validate required fields
            List<String> errors = validateSession(session);
            if (!errors.isEmpty()) {
                player.sendMessage(Component.text("§cCannot save - missing required configuration:"));
                for (String error : errors) {
                    player.sendMessage(Component.text("§c  • " + error));
                }
                return;
            }

            try {
                saveToConfig(session);
                player.sendMessage(Component.text("§a✔ Configuration saved to maps.json!"));
                player.sendMessage(Component.text("§7Map ID: " + session.getMapId()));
            } catch (Exception e) {
                player.sendMessage(Component.text("§cFailed to save: " + e.getMessage()));
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

    private String formatPosition(Vec3i pos) {
        return String.format("%d, %d, %d", pos.x(), pos.y(), pos.z());
    }
}
