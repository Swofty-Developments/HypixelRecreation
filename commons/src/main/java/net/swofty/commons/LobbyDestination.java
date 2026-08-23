package net.swofty.commons;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public enum LobbyDestination {
    BEDWARS(
        ServerType.BEDWARS_LOBBY,
        Set.of("bw", "bedwars"),
        Set.of(ServerType.BEDWARS_LOBBY, ServerType.BEDWARS_GAME, ServerType.BEDWARS_CONFIGURATOR)
    ),
    SKYWARS(
        ServerType.SKYWARS_LOBBY,
        Set.of("sw", "skywars"),
        Set.of(ServerType.SKYWARS_LOBBY, ServerType.SKYWARS_GAME, ServerType.SKYWARS_CONFIGURATOR)
    ),
    MURDER_MYSTERY(
        ServerType.MURDER_MYSTERY_LOBBY,
        Set.of("mm", "murder", "murdermystery", "murder_mystery", "mystery"),
        Set.of(ServerType.MURDER_MYSTERY_LOBBY, ServerType.MURDER_MYSTERY_GAME, ServerType.MURDER_MYSTERY_CONFIGURATOR)
    ),
    SKYBLOCK(
        ServerType.SKYBLOCK_HUB,
        Set.of("sb", "skyblock", "hub"),
        Set.of()
    ),
    PROTOTYPE(
        ServerType.PROTOTYPE_LOBBY,
        Set.of("prototype", "proto", "ptl"),
        Set.of(ServerType.PROTOTYPE_LOBBY, ServerType.REPLAY_VIEWER)
    ),
    MAIN_LOBBY(
        ServerType.MAIN_LOBBY,
        Set.of("main"),
        Set.of(ServerType.MAIN_LOBBY)
    ),
    RAVENGARD(
        ServerType.RAVENGARD_LOBBY,
        Set.of("ravengard", "rg"),
        Set.of()
    );

    private final ServerType destination;
    private final Set<String> aliases;
    private final Set<ServerType> sourceTypes;

    LobbyDestination(ServerType destination, Set<String> aliases, Set<ServerType> sourceTypes) {
        this.destination = destination;
        this.aliases = aliases;
        this.sourceTypes = sourceTypes;
    }

    public ServerType destination() {
        return destination;
    }

    public static @Nullable ServerType resolveFromAlias(String alias) {
        String normalized = normalize(alias);
        if (normalized.isEmpty()) {
            return null;
        }

        for (LobbyDestination value : values()) {
            if (value.aliases.contains(normalized)) {
                return value.destination;
            }
        }

        return null;
    }

    public static ServerType resolveDefaultDestination(@Nullable ServerType currentType) {
        if (currentType == null) {
            return ServerType.PROTOTYPE_LOBBY;
        }

        for (LobbyDestination value : values()) {
            if (value.handlesSource(currentType)) {
                return value.destination;
            }
        }

        return ServerType.PROTOTYPE_LOBBY;
    }

    public static @Nullable ServerType resolveDestination(@Nullable String alias, @Nullable ServerType currentType) {
        if (alias != null && !alias.isBlank()) {
            return resolveFromAlias(alias);
        }
        return resolveDefaultDestination(currentType);
    }

    public static boolean isSessionType(@Nullable ServerType type) {
        if (type == null) {
            return false;
        }

        for (LobbyDestination value : values()) {
            if (value.sourceTypes.contains(type) && value.destination != type) {
                return true;
            }
        }

        return false;
    }

    public static Set<String> allAliases() {
        Set<String> all = new LinkedHashSet<>();
        Arrays.stream(values()).forEach(value -> all.addAll(value.aliases));
        return all;
    }

    private boolean handlesSource(ServerType sourceType) {
        return sourceTypes.contains(sourceType);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value
            .toLowerCase(Locale.ROOT)
            .replace("_", "")
            .replace("-", "")
            .replace(" ", "");
    }
}
