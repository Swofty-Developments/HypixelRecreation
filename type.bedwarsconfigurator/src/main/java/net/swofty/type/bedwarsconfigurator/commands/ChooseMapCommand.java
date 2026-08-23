package net.swofty.type.bedwarsconfigurator.commands;

import net.hollowcube.polar.PolarLoader;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.GameMode;
import net.minestom.server.instance.InstanceContainer;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.type.bedwarsconfigurator.TypeBedWarsConfiguratorLoader;
import net.swofty.type.bedwarsconfigurator.autosetup.AutoSetupSession;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@CommandParameters(labels = "choose choosemap selectmap select",
    description = "Choose a BedWars map to configure",
    usage = "/choosemap <map>",
    permission = Rank.STAFF,
    allowsConsole = false)
public class ChooseMapCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        var mapArg = ArgumentType.String("map");
        mapArg.setSuggestionCallback((sender, context, suggestion) -> {
            Set<String> addedIds = new HashSet<>();
            Set<SuggestionEntry> suggestions = new HashSet<>();

            for (BedWarsMapsConfig.MapEntry entry : TypeBedWarsConfiguratorLoader.getMapsConfig().getMaps()) {
                suggestions.add(suggestion(entry.getId(), "{} <7>(configured)", entry.getName()));
                addedIds.add(entry.getId().toLowerCase());
            }

            File bedwarsDir = new File("./configuration/bedwars/");
            if (bedwarsDir.exists() && bedwarsDir.isDirectory()) {
                File[] polarFiles = bedwarsDir.listFiles((_, name) -> name.endsWith(".polar"));
                if (polarFiles != null) {
                    for (File polarFile : polarFiles) {
                        String mapId = polarFile.getName().replace(".polar", "");
                        if (!addedIds.contains(mapId.toLowerCase())) {
                            suggestions.add(suggestion(mapId, "{} <e>(unconfigured)", mapId));
                        }
                    }
                }
            }

            String input = context.getInput();
            String currentInput = input.substring(input.lastIndexOf(" ") + 1).trim().toLowerCase();

            if (currentInput.isEmpty()) {
                suggestions.forEach(suggestion::addEntry);
                return;
            }

            suggestions.stream()
                .filter(entry -> entry.getEntry().toLowerCase().startsWith(currentInput))
                .forEach(suggestion::addEntry);
        });

        command.addSyntax((sender, context) -> {
            if (!(sender instanceof HypixelPlayer player)) {
                sender.sendMessage("<c>This command can only be executed by a player.");
                return;
            }
            String mapId = context.get("map");

            BedWarsMapsConfig.MapEntry selectedMap = null;
            for (BedWarsMapsConfig.MapEntry entry : TypeBedWarsConfiguratorLoader.getMapsConfig().getMaps()) {
                if (entry.getId().equalsIgnoreCase(mapId)) {
                    selectedMap = entry;
                    break;
                }
            }

            File polarFile = new File("./configuration/bedwars/" + mapId + ".polar");
            if (!polarFile.exists()) {
                player.sendMessage("<c>No polar file found for map: {}", mapId);
                return;
            }

            InstanceContainer mapInstance = MinecraftServer.getInstanceManager().createInstanceContainer();
            try {
                mapInstance.setChunkLoader(new PolarLoader(polarFile.toPath()));
            } catch (IOException e) {
                player.sendMessage("<c>Failed to load map: {}", mapId);
                return;
            }

            AutoSetupSession session = AutoSetupSession.getOrCreate(player.getUuid(), mapInstance);
            session.setMapId(mapId);

            if (selectedMap != null) {
                session.setMapName(selectedMap.getName());

                if (selectedMap.getConfiguration() != null) {
                    session.loadFromMapEntry(selectedMap);
                    player.sendMessage("<a>Loaded existing configuration for: {}", selectedMap.getName());
                } else {
                    player.sendMessage("<e>Selected map: {} <7>(no existing config)", selectedMap.getName());
                }
            } else {
                player.setGameMode(GameMode.CREATIVE);
                player.setFlying(true);
                session.setMapName(mapId);
                session.clear();
                session.setMapId(mapId);
                session.setMapName(mapId);
                player.sendMessage("<e>Loaded unconfigured map: <f>{} <7>(starting fresh)", mapId);
                player.sendMessage("<7>Use <b>/autosetup <7>to automatically configure the map, or set things manually.");
            }

            player.setInstance(mapInstance);
        }, mapArg);
    }

}
