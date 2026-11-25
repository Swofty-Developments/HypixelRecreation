package net.swofty.type.bedwarsconfigurator.autosetup;

import lombok.Getter;
import lombok.Setter;
import net.minestom.server.instance.Instance;
import net.swofty.commons.bedwars.BedWarsGameType;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.*;
import net.swofty.commons.mc.HypixelPosition;
import net.swofty.commons.mc.Vec3i;

import java.util.*;

@Getter
@Setter
public class AutoSetupSession {
    private static final Map<UUID, AutoSetupSession> sessions = new HashMap<>();

    private final UUID playerUuid;
    private Instance instance;

    private String mapId;
    private String mapName;
    private final List<BedWarsGameType> gameTypes = new ArrayList<>();
    private Double minX, maxX, minY, maxY, minZ, maxZ;
    private final Map<TeamKey, TeamConfig> teams = new EnumMap<>(TeamKey.class);
    private final List<HypixelPosition> diamondGenerators = new ArrayList<>();
    private final List<HypixelPosition> emeraldGenerators = new ArrayList<>();
    private final List<BedWarsMapsConfig.SprayPosition> sprays = new ArrayList<>();
    private GeneratorSpeed generatorSpeed = GeneratorSpeed.SLOW;

    private HypixelPosition waitingLocation;
    private HypixelPosition spectatorLocation;
    private Vec3i waitingLobbyMin;
    private Vec3i waitingLobbyMax;

    public AutoSetupSession(UUID playerUuid, Instance instance) {
        this.playerUuid = playerUuid;
        this.instance = instance;
    }

    public static AutoSetupSession getOrCreate(UUID playerUuid, Instance instance) {
        AutoSetupSession existing = sessions.get(playerUuid);
        if (existing != null) {
            // Update instance if player switched maps
            existing.setInstance(instance);
            return existing;
        }
        AutoSetupSession session = new AutoSetupSession(playerUuid, instance);
        sessions.put(playerUuid, session);
        return session;
    }

    public static AutoSetupSession get(UUID playerUuid) {
        return sessions.get(playerUuid);
    }

    public static void remove(UUID playerUuid) {
        sessions.remove(playerUuid);
    }

    public static Collection<AutoSetupSession> getAllSessions() {
        return sessions.values();
    }

    public boolean hasBounds() {
        return minX != null && maxX != null && minY != null && maxY != null && minZ != null && maxZ != null;
    }

    public void setBoundsMin(double x, double y, double z) {
        this.minX = x;
        this.minY = y;
        this.minZ = z;
    }

    public void setBoundsMax(double x, double y, double z) {
        this.maxX = x;
        this.maxY = y;
        this.maxZ = z;
    }

    public TeamConfig getOrCreateTeam(TeamKey key) {
        return teams.computeIfAbsent(key, _ -> new TeamConfig());
    }

    public void clear() {
        mapId = null;
        mapName = null;
        gameTypes.clear();
        minX = maxX = minY = maxY = minZ = maxZ = null;
        teams.clear();
        diamondGenerators.clear();
        emeraldGenerators.clear();
        sprays.clear();
        generatorSpeed = GeneratorSpeed.SLOW;
        waitingLocation = null;
        spectatorLocation = null;
        waitingLobbyMin = waitingLobbyMax = null;
    }

    public void removeTeam(TeamKey key) {
        teams.remove(key);
    }

    public void loadFromMapEntry(BedWarsMapsConfig.MapEntry entry) {
        clear();

        this.mapId = entry.getId();
        this.mapName = entry.getName();

        MapEntry.MapConfiguration config = entry.getConfiguration();
        if (config == null) return;

        // Load game types
        if (config.getTypes() != null) {
            gameTypes.addAll(config.getTypes());
        }

        // Load bounds
        if (config.getBounds() != null) {
            MapEntry.MapConfiguration.MapBounds bounds = config.getBounds();
            if (bounds.getX() != null) {
                minX = bounds.getX().min();
                maxX = bounds.getX().max();
            }
            if (bounds.getY() != null) {
                minY = bounds.getY().min();
                maxY = bounds.getY().max();
            }
            if (bounds.getZ() != null) {
                minZ = bounds.getZ().min();
                maxZ = bounds.getZ().max();
            }
        }

        // Load generator settings
        if (config.getGeneratorSpeed() != null) {
            generatorSpeed = config.getGeneratorSpeed();
        }

        // Load teams
        if (config.getTeams() != null) {
            for (Map.Entry<TeamKey, MapTeam> teamEntry : config.getTeams().entrySet()) {
                TeamKey teamKey = teamEntry.getKey();
                MapTeam mapTeam = teamEntry.getValue();
                TeamConfig teamConfig = getOrCreateTeam(teamKey);

                if (mapTeam.getSpawn() != null) {
                    teamConfig.setSpawn(mapTeam.getSpawn());
                }
                if (mapTeam.getBed() != null) {
                    teamConfig.setBedFeet(mapTeam.getBed().feet());
                    teamConfig.setBedHead(mapTeam.getBed().head());
                }
                if (mapTeam.getGenerator() != null) {
                    teamConfig.setGenerator(mapTeam.getGenerator());
                }
                if (mapTeam.getShop() != null) {
                    teamConfig.setItemShop(mapTeam.getShop().item());
                    teamConfig.setTeamShop(mapTeam.getShop().team());
                }
            }
        }

        // Load locations
        if (config.getLocations() != null) {
            var locations = config.getLocations();
            if (locations.getWaiting() != null) {
                HypixelPosition w = locations.getWaiting();
                waitingLocation = new HypixelPosition(w.x(), w.y(), w.z(), 0, 0);
            }
            if (locations.getSpectator() != null) {
                HypixelPosition s = locations.getSpectator();
                spectatorLocation = new HypixelPosition(s.x(), s.y(), s.z(), 0, 0);
            }
            if (locations.getWaitingLobby() != null) {
                waitingLobbyMin = locations.getWaitingLobby().min();
                waitingLobbyMax = locations.getWaitingLobby().max();
            }
        }

        // Load global generators
        if (config.getGlobalGenerator() != null) {
            var diamondGen = config.getGlobalGenerator().get(BedWarsMapsConfig.GlobalGeneratorKey.DIAMOND);
            if (diamondGen != null) {
                if (diamondGen.getLocations() != null) {
                    diamondGenerators.addAll(diamondGen.getLocations());
                }
            }
            var emeraldGen = config.getGlobalGenerator().get(BedWarsMapsConfig.GlobalGeneratorKey.EMERALD);
            if (emeraldGen != null) {
                if (emeraldGen.getLocations() != null) {
                    emeraldGenerators.addAll(emeraldGen.getLocations());
                }
            }
        }
        if (config.getSprays() != null) sprays.addAll(config.getSprays());
    }

    public MapEntry toMapEntry() {
        MapEntry entry = new MapEntry();
        entry.setId(mapId);
        entry.setName(mapName);

        MapEntry.MapConfiguration config = new MapEntry.MapConfiguration();
        config.setTypes(new ArrayList<>(gameTypes));

        // Generator config
        config.setGeneratorSpeed(generatorSpeed);

        // Bounds
        if (hasBounds()) {
            MapEntry.MapConfiguration.MapBounds bounds = new MapEntry.MapConfiguration.MapBounds();
            bounds.setX(new MinMax(minX, maxX));
            bounds.setY(new MinMax(minY, maxY));
            bounds.setZ(new MinMax(minZ, maxZ));
            config.setBounds(bounds);
        }

        // Teams
        Map<TeamKey, MapTeam> teamsMap = new EnumMap<>(TeamKey.class);
        for (Map.Entry<TeamKey, TeamConfig> teamEntry : teams.entrySet()) {
            TeamConfig tc = teamEntry.getValue();
            MapTeam mapTeam = new MapTeam();

            if (tc.getSpawn() != null) {
                mapTeam.setSpawn(tc.getSpawn());
            }
            if (tc.getBedFeet() != null && tc.getBedHead() != null) {
                mapTeam.setBed(new TwoBlockPosition(tc.getBedFeet(), tc.getBedHead()));
            }
            if (tc.getGenerator() != null) {
                mapTeam.setGenerator(tc.getGenerator());
            }
            if (tc.getItemShop() != null || tc.getTeamShop() != null) {
                mapTeam.setShop(new MapTeam.Shops(tc.getItemShop(), tc.getTeamShop()));
            }

            teamsMap.put(teamEntry.getKey(), mapTeam);
        }
        config.setTeams(teamsMap);

        // Locations
        MapEntry.MapConfiguration.MapLocations locations = new MapEntry.MapConfiguration.MapLocations();
        if (waitingLocation != null) {
            locations.setWaiting(new HypixelPosition(waitingLocation.x(), waitingLocation.y(), waitingLocation.z()));
        }
        if (spectatorLocation != null) {
            locations.setSpectator(new HypixelPosition(spectatorLocation.x(), spectatorLocation.y(), spectatorLocation.z()));
        }
        if (waitingLobbyMin != null && waitingLobbyMax != null) {
            locations.setWaitingLobby(new BedWarsMapsConfig.CuboidPosition(waitingLobbyMin, waitingLobbyMax));
        }
        config.setLocations(locations);

        // Global generators
        Map<BedWarsMapsConfig.GlobalGeneratorKey, MapEntry.MapConfiguration.GlobalGenerator> globalGenerators = new HashMap<>();
        if (!diamondGenerators.isEmpty()) {
            MapEntry.MapConfiguration.GlobalGenerator diamondGen = new MapEntry.MapConfiguration.GlobalGenerator();
            diamondGen.setLocations(new ArrayList<>(diamondGenerators));
            globalGenerators.put(BedWarsMapsConfig.GlobalGeneratorKey.DIAMOND, diamondGen);
        }

        if (!emeraldGenerators.isEmpty()) {
            MapEntry.MapConfiguration.GlobalGenerator emeraldGen = new MapEntry.MapConfiguration.GlobalGenerator();
            emeraldGen.setLocations(new ArrayList<>(emeraldGenerators));
            globalGenerators.put(BedWarsMapsConfig.GlobalGeneratorKey.EMERALD, emeraldGen);
        }

        config.setGlobalGenerator(globalGenerators);
        config.setSprays(new ArrayList<>(sprays));
        entry.setConfiguration(config);

        return entry;
    }

    @Getter
    @Setter
    public static class TeamConfig {
        private HypixelPosition spawn;
        private Vec3i bedFeet;
        private Vec3i bedHead;
        private HypixelPosition generator;
        private HypixelPosition itemShop;
        private HypixelPosition teamShop;
    }
}
