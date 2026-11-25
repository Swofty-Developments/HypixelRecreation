package net.swofty.type.murdermysteryconfigurator.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentDouble;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.swofty.commons.mc.HypixelPosition;
import net.swofty.commons.murdermystery.MurderMysteryGameType;
import net.swofty.commons.murdermystery.map.MurderMysteryMapsConfig;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.murdermysteryconfigurator.TypeMurderMysteryConfiguratorLoader;
import net.swofty.type.murdermysteryconfigurator.autosetup.DebugMarkerManager;
import net.swofty.type.murdermysteryconfigurator.autosetup.MurderMysterySetupSession;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@CommandParameters(
    labels = "setup mapsetup autosetup",
        description = "Murder Mystery map configuration tool",
        usage = "/mmsetup <subcommand>",
        permission = Rank.STAFF,
        allowsConsole = false
)
public class AutoSetupCommand extends HypixelCommand {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void registerUsage(MinestomCommand command) {
        command.setDefaultExecutor((sender, context) -> {
            sendHelp(sender);
        });

        registerTypeCommand(command);
        registerLocationCommand(command);
        registerGoldCommand(command);
        registerSpawnCommand(command);
        registerKillZoneCommand(command);
        registerShowCommand(command);
        registerHideCommand(command);
        registerStatusCommand(command);
        registerSaveCommand(command);
        registerMapInfoCommand(command);
    }

    private void sendHelp(net.minestom.server.command.CommandSender sender) {
        sender.sendMessage(Component.text("§6§l=== Murder Mystery Map Setup ==="));
        sender.sendMessage(Component.text("§e/mmsetup type <add|remove> <type> §7- Configure game types"));
        sender.sendMessage(Component.text("§e/mmsetup waiting [x y z] §7- Set waiting spawn"));
        sender.sendMessage(Component.text("§e/mmsetup gold <add|remove|clear> [x y z] §7- Manage gold spawns"));
        sender.sendMessage(Component.text("§e/mmsetup spawn <add|remove|clear> [x y z] §7- Manage player spawns"));
        sender.sendMessage(Component.text("§e/mmsetup killzone <add|setmin|setmax|remove|list|clear> §7- Manage kill zones"));
        sender.sendMessage(Component.text("§e/mmsetup show §7- Show debug markers"));
        sender.sendMessage(Component.text("§e/mmsetup hide §7- Hide debug markers"));
        sender.sendMessage(Component.text("§e/mmsetup status §7- Show current configuration status"));
        sender.sendMessage(Component.text("§e/mmsetup name <name> §7- Set map display name"));
        sender.sendMessage(Component.text("§e/mmsetup save §7- Save configuration to maps.json"));
    }

    private void registerTypeCommand(MinestomCommand command) {
        var actionArg = ArgumentType.String("action");
        actionArg.setSuggestionCallback((sender, ctx, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("add"));
            suggestion.addEntry(new SuggestionEntry("remove"));
        });

        var typeArg = ArgumentType.String("typeName");
        typeArg.setSuggestionCallback((sender, ctx, suggestion) -> {
            for (MurderMysteryGameType type : MurderMysteryGameType.values()) {
                suggestion.addEntry(new SuggestionEntry(type.name()));
            }
        });

        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            String action = context.get(actionArg);
            String typeName = context.get(typeArg);

            MurderMysteryGameType gameType = MurderMysteryGameType.from(typeName);
            if (gameType == null) {
                player.sendMessage(Component.text("§cInvalid game type: " + typeName));
                return;
            }

            MurderMysterySetupSession session = MurderMysterySetupSession.getOrCreate(player.getUuid(), player.getInstance());

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

    private void registerLocationCommand(MinestomCommand command) {
        // /mmsetup waiting
        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            Pos pos = player.getPosition();
            MurderMysterySetupSession session = MurderMysterySetupSession.getOrCreate(player.getUuid(), player.getInstance());
            session.setWaitingLocation(new HypixelPosition(pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch()));
            player.sendMessage(Component.text("§aSet waiting spawn to " + formatPos(pos)));
            DebugMarkerManager.refreshMarkers(player.getUuid(), session, player.getInstance());

        }, ArgumentType.Literal("waiting"));

        // With coordinates
        ArgumentDouble xArg = ArgumentType.Double("x");
        ArgumentDouble yArg = ArgumentType.Double("y");
        ArgumentDouble zArg = ArgumentType.Double("z");

        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            double x = context.get(xArg);
            double y = context.get(yArg);
            double z = context.get(zArg);

            MurderMysterySetupSession session = MurderMysterySetupSession.getOrCreate(player.getUuid(), player.getInstance());
            session.setWaitingLocation(new HypixelPosition(x, y, z, 0, 0));
            player.sendMessage(Component.text("§aSet waiting spawn to " + x + ", " + y + ", " + z));
            DebugMarkerManager.refreshMarkers(player.getUuid(), session, player.getInstance());

        }, ArgumentType.Literal("waiting"), xArg, yArg, zArg);
    }

    private void registerGoldCommand(MinestomCommand command) {
        var actionArg = ArgumentType.String("action");
        actionArg.setSuggestionCallback((sender, ctx, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("add"));
            suggestion.addEntry(new SuggestionEntry("remove"));
            suggestion.addEntry(new SuggestionEntry("clear"));
        });

        // /mmsetup gold <action> - use player position
        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            String action = context.get(actionArg);
            Pos pos = player.getPosition();

            MurderMysterySetupSession session = MurderMysterySetupSession.getOrCreate(player.getUuid(), player.getInstance());
            handleSpawnAction(player, session.getGoldSpawns(), action, pos, "gold spawn");
            DebugMarkerManager.refreshMarkers(player.getUuid(), session, player.getInstance());

        }, ArgumentType.Literal("gold"), actionArg);

        // /mmsetup gold <action> <x> <y> <z>
        ArgumentDouble xArg = ArgumentType.Double("x");
        ArgumentDouble yArg = ArgumentType.Double("y");
        ArgumentDouble zArg = ArgumentType.Double("z");

        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            String action = context.get(actionArg);
            double x = context.get(xArg);
            double y = context.get(yArg);
            double z = context.get(zArg);

            MurderMysterySetupSession session = MurderMysterySetupSession.getOrCreate(player.getUuid(), player.getInstance());
            handleSpawnAction(player, session.getGoldSpawns(), action, new Pos(x, y, z), "gold spawn");
            DebugMarkerManager.refreshMarkers(player.getUuid(), session, player.getInstance());

        }, ArgumentType.Literal("gold"), actionArg, xArg, yArg, zArg);
    }

    private void registerSpawnCommand(MinestomCommand command) {
        var actionArg = ArgumentType.String("action");
        actionArg.setSuggestionCallback((sender, ctx, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("add"));
            suggestion.addEntry(new SuggestionEntry("remove"));
            suggestion.addEntry(new SuggestionEntry("clear"));
        });

        // /mmsetup spawn <action> - use player position
        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            String action = context.get(actionArg);
            Pos pos = player.getPosition();

            MurderMysterySetupSession session = MurderMysterySetupSession.getOrCreate(player.getUuid(), player.getInstance());
            handleSpawnAction(player, session.getPlayerSpawns(), action, pos, "player spawn");
            DebugMarkerManager.refreshMarkers(player.getUuid(), session, player.getInstance());

        }, ArgumentType.Literal("spawn"), actionArg);

        // /mmsetup spawn <action> <x> <y> <z>
        ArgumentDouble xArg = ArgumentType.Double("x");
        ArgumentDouble yArg = ArgumentType.Double("y");
        ArgumentDouble zArg = ArgumentType.Double("z");

        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            String action = context.get(actionArg);
            double x = context.get(xArg);
            double y = context.get(yArg);
            double z = context.get(zArg);

            MurderMysterySetupSession session = MurderMysterySetupSession.getOrCreate(player.getUuid(), player.getInstance());
            handleSpawnAction(player, session.getPlayerSpawns(), action, new Pos(x, y, z), "player spawn");
            DebugMarkerManager.refreshMarkers(player.getUuid(), session, player.getInstance());

        }, ArgumentType.Literal("spawn"), actionArg, xArg, yArg, zArg);
    }

    private void handleSpawnAction(Player player, List<HypixelPosition> spawns, String action, Pos pos, String spawnType) {
        switch (action.toLowerCase()) {
            case "add" -> {
                HypixelPosition newPos = new HypixelPosition(pos.x(), pos.y(), pos.z());
                spawns.add(newPos);
                player.sendMessage(Component.text("§aAdded " + spawnType + " at " + formatPos(pos) + " (Total: " + spawns.size() + ")"));
            }
            case "remove" -> {
                // Remove nearest spawn within 2 blocks
                HypixelPosition toRemove = null;
                double minDist = Double.MAX_VALUE;

                for (HypixelPosition spawn : spawns) {
                    double dist = Math.sqrt(Math.pow(spawn.x() - pos.x(), 2) + Math.pow(spawn.y() - pos.y(), 2) + Math.pow(spawn.z() - pos.z(), 2));
                    if (dist < minDist && dist < 2) {
                        minDist = dist;
                        toRemove = spawn;
                    }
                }

                if (toRemove != null) {
                    spawns.remove(toRemove);
                    player.sendMessage(Component.text("§cRemoved nearest " + spawnType + " (Total: " + spawns.size() + ")"));
                } else {
                    player.sendMessage(Component.text("§cNo " + spawnType + " found within 2 blocks"));
                }
            }
            case "clear" -> {
                int count = spawns.size();
                spawns.clear();
                player.sendMessage(Component.text("§cCleared all " + count + " " + spawnType + "(s)"));
            }
            default -> player.sendMessage(Component.text("§cUnknown action: " + action));
        }
    }

    private void registerKillZoneCommand(MinestomCommand command) {
        var actionArg = ArgumentType.String("action");
        actionArg.setSuggestionCallback((sender, ctx, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("add"));
            suggestion.addEntry(new SuggestionEntry("setmin"));
            suggestion.addEntry(new SuggestionEntry("setmax"));
            suggestion.addEntry(new SuggestionEntry("remove"));
            suggestion.addEntry(new SuggestionEntry("list"));
            suggestion.addEntry(new SuggestionEntry("clear"));
        });

        var nameArg = ArgumentType.String("name");
        nameArg.setSuggestionCallback((sender, ctx, suggestion) -> {
            if (sender instanceof Player player) {
                MurderMysterySetupSession session = MurderMysterySetupSession.get(player.getUuid());
                if (session != null) {
                    for (String name : session.getKillRegions().keySet()) {
                        suggestion.addEntry(new SuggestionEntry(name));
                    }
                }
            }
        });

        // /mmsetup killzone <action>
        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            String action = context.get(actionArg);
            MurderMysterySetupSession session = MurderMysterySetupSession.getOrCreate(player.getUuid(), player.getInstance());

            switch (action.toLowerCase()) {
                case "list" -> {
                    if (session.getKillRegions().isEmpty()) {
                        player.sendMessage(Component.text("§eNo kill zones defined."));
                    } else {
                        player.sendMessage(Component.text("§6§l=== Kill Zones ==="));
                        for (var entry : session.getKillRegions().entrySet()) {
                            var region = entry.getValue();
                            String status = region.isComplete() ? "§a✔ Complete" : "§c✖ Incomplete";
                            String minStr = region.getMinPos() != null ? formatPosition(region.getMinPos()) : "not set";
                            String maxStr = region.getMaxPos() != null ? formatPosition(region.getMaxPos()) : "not set";
                            player.sendMessage(Component.text("§e" + entry.getKey() + " §7- " + status));
                            player.sendMessage(Component.text("  §7Min: " + minStr + " | Max: " + maxStr));
                        }
                    }
                }
                case "clear" -> {
                    int count = session.getKillRegions().size();
                    session.getKillRegions().clear();
                    player.sendMessage(Component.text("§cCleared all " + count + " kill zone(s)"));
                    DebugMarkerManager.refreshMarkers(player.getUuid(), session, player.getInstance());
                }
                default -> player.sendMessage(Component.text("§cUsage: /mmsetup killzone <add|setmin|setmax|remove|list|clear> [name]"));
            }

        }, ArgumentType.Literal("killzone"), actionArg);

        // /mmsetup killzone <action> <name>
        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            String action = context.get(actionArg);
            String name = context.get(nameArg);
            Pos pos = player.getPosition();

            MurderMysterySetupSession session = MurderMysterySetupSession.getOrCreate(player.getUuid(), player.getInstance());

            switch (action.toLowerCase()) {
                case "add" -> {
                    if (session.getKillRegions().containsKey(name)) {
                        player.sendMessage(Component.text("§cKill zone '" + name + "' already exists."));
                    } else {
                        session.getKillRegions().put(name, new MurderMysterySetupSession.EditableKillRegion(name));
                        player.sendMessage(Component.text("§aCreated kill zone '" + name + "'. Now use /mmsetup killzone setmin " + name + " and setmax " + name));
                    }
                }
                case "setmin" -> {
                    var region = session.getKillRegions().get(name);
                    if (region == null) {
                        player.sendMessage(Component.text("§cKill zone '" + name + "' not found. Create it first with /mmsetup killzone add " + name));
                    } else {
                        region.setMinPos(new HypixelPosition(pos.x(), pos.y(), pos.z()));
                        player.sendMessage(Component.text("§aSet min corner of '" + name + "' to " + formatPos(pos)));
                        DebugMarkerManager.refreshMarkers(player.getUuid(), session, player.getInstance());
                    }
                }
                case "setmax" -> {
                    var region = session.getKillRegions().get(name);
                    if (region == null) {
                        player.sendMessage(Component.text("§cKill zone '" + name + "' not found. Create it first with /mmsetup killzone add " + name));
                    } else {
                        region.setMaxPos(new HypixelPosition(pos.x(), pos.y(), pos.z()));
                        player.sendMessage(Component.text("§aSet max corner of '" + name + "' to " + formatPos(pos)));
                        DebugMarkerManager.refreshMarkers(player.getUuid(), session, player.getInstance());
                    }
                }
                case "remove" -> {
                    if (session.getKillRegions().remove(name) != null) {
                        player.sendMessage(Component.text("§cRemoved kill zone '" + name + "'"));
                        DebugMarkerManager.refreshMarkers(player.getUuid(), session, player.getInstance());
                    } else {
                        player.sendMessage(Component.text("§cKill zone '" + name + "' not found."));
                    }
                }
                default -> player.sendMessage(Component.text("§cUsage: /mmsetup killzone <add|setmin|setmax|remove|list|clear> [name]"));
            }

        }, ArgumentType.Literal("killzone"), actionArg, nameArg);
    }

    private String formatPosition(HypixelPosition pos) {
        return String.format("%.2f, %.2f, %.2f", pos.x(), pos.y(), pos.z());
    }

    private void registerShowCommand(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            MurderMysterySetupSession session = MurderMysterySetupSession.get(player.getUuid());
            if (session == null) {
                player.sendMessage(Component.text("§cNo configuration session active. Use /choosemap first."));
                return;
            }

            DebugMarkerManager.showMarkers(player.getUuid(), session, player.getInstance());
            player.sendMessage(Component.text("§aShowing debug markers"));

        }, ArgumentType.Literal("show"));
    }

    private void registerHideCommand(MinestomCommand command) {
        command.addSyntax((sender, _) -> {
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

            MurderMysterySetupSession session = MurderMysterySetupSession.get(player.getUuid());
            if (session == null) {
                player.sendMessage(Component.text("§cNo configuration session active."));
                return;
            }

            player.sendMessage(Component.text("§6§l=== Configuration Status ==="));
            player.sendMessage(Component.text("§eMap ID: §f" + (session.getMapId() != null ? session.getMapId() : "§c(not set)")));
            player.sendMessage(Component.text("§eMap Name: §f" + (session.getMapName() != null ? session.getMapName() : "§c(not set)")));
            player.sendMessage(Component.text("§eGame Types: §f" + (session.getGameTypes().isEmpty() ? "§c(none)" : session.getGameTypes().toString())));
            player.sendMessage(Component.text("§eGold Spawns: §f" + session.getGoldSpawns().size()));
            player.sendMessage(Component.text("§ePlayer Spawns: §f" + session.getPlayerSpawns().size()));
            player.sendMessage(Component.text("§eWaiting Location: §f" + (session.getWaitingLocation() != null ? "§a✔" : "§c✖")));

            // Kill zones summary
            long completeZones = session.getKillRegions().values().stream().filter(MurderMysterySetupSession.EditableKillRegion::isComplete).count();
            int totalZones = session.getKillRegions().size();
            player.sendMessage(Component.text("§eKill Zones: §f" + completeZones + "/" + totalZones + " complete §7(optional)"));

        }, ArgumentType.Literal("status"));
    }

    private void registerMapInfoCommand(MinestomCommand command) {
        ArgumentString nameArg = ArgumentType.String("name");

        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            MurderMysterySetupSession session = MurderMysterySetupSession.get(player.getUuid());
            if (session == null || session.getMapId() == null) {
                player.sendMessage(Component.text("§cNo map selected. Use /choosemap <map> first."));
                return;
            }

            String name = context.get(nameArg);
            session.setMapName(name);

            player.sendMessage(Component.text("§aSet map name to '" + name + "' (ID: " + session.getMapId() + ")"));

        }, ArgumentType.Literal("name"), nameArg);
    }

    private void registerSaveCommand(MinestomCommand command) {
        command.addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            if (!permissionCheck(sender)) return;

            MurderMysterySetupSession session = MurderMysterySetupSession.get(player.getUuid());
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

    private List<String> validateSession(MurderMysterySetupSession session) {
        List<String> errors = new ArrayList<>();

        if (session.getMapId() == null || session.getMapId().isEmpty()) {
            errors.add("No map selected (use /choosemap <map> first)");
        }
        if (session.getMapName() == null || session.getMapName().isEmpty()) {
            errors.add("Map name not set (use /mmsetup name <name>)");
        }
        if (session.getGameTypes().isEmpty()) {
            errors.add("No game types set");
        }
        if (session.getGoldSpawns().isEmpty()) {
            errors.add("No gold spawns set");
        }
        if (session.getPlayerSpawns().size() < 4) {
            errors.add("Need at least 4 player spawns (have " + session.getPlayerSpawns().size() + ")");
        }
        if (session.getWaitingLocation() == null) {
            errors.add("Waiting location not set");
        }
        // Note: Kill zones are optional, no validation needed

        return errors;
    }

    private void saveToConfig(MurderMysterySetupSession session) throws IOException {
        Path mapsPath = Path.of("./configuration/murdermystery/maps.json");

        MurderMysteryMapsConfig config;
        if (Files.exists(mapsPath)) {
            String json = Files.readString(mapsPath, StandardCharsets.UTF_8);
            config = GSON.fromJson(json, MurderMysteryMapsConfig.class);
            if (config == null) {
                config = new MurderMysteryMapsConfig();
            }
        } else {
            config = new MurderMysteryMapsConfig();
        }

        if (config.getMaps() == null) {
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
        TypeMurderMysteryConfiguratorLoader.reloadMapsConfig();
    }

    private String formatPos(Pos pos) {
        return String.format("%.2f, %.2f, %.2f", pos.x(), pos.y(), pos.z());
    }
}
