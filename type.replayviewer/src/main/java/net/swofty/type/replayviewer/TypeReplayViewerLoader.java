package net.swofty.type.replayviewer;

import lombok.Getter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceManager;
import net.swofty.commons.CustomWorlds;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.HypixelTypeLoader;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.data.GameDataHandler;
import net.swofty.type.generic.data.handlers.ReplayDataHandler;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.tab.TablistManager;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.replayviewer.item.ReplayItemHandler;
import net.swofty.type.replayviewer.playback.ReplaySession;
import net.swofty.type.game.replay.api.ReplayAdapterRegistry;
import net.swofty.type.game.replay.api.ReplayViewerAdapter;
import net.swofty.type.replayviewer.playback.bedwars.BedWarsViewerAdapter;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TypeReplayViewerLoader implements HypixelTypeLoader {

    @Getter
    private static InstanceManager instanceManager;

    @Getter
    private static ReplayItemHandler itemHandler = new ReplayItemHandler();

    @Getter
    private static final Map<UUID, ReplaySession> activeSessions = new ConcurrentHashMap<>();
    @Getter
    private static final ReplayAdapterRegistry<ReplayViewerAdapter<?, ?>> replayAdapters = new ReplayAdapterRegistry<>();

    @Override
    public ServerType getType() {
        return ServerType.REPLAY_VIEWER;
    }

    @Override
    public void onInitialize(MinecraftServer server) {
        instanceManager = MinecraftServer.getInstanceManager();
        replayAdapters.register(BedWarsViewerAdapter.GAME_TYPE, new BedWarsViewerAdapter());
        Logger.info("Replay Viewer initialized");
    }

    @Override
    public void afterInitialize(MinecraftServer server) {
        HypixelGenericLoader.loopThroughPackage("net.swofty.type.replayviewer.command", HypixelCommand.class).forEach(command -> {
            try {
                MinecraftServer.getCommandManager().register(command.getCommand());
            } catch (Exception e) {
                Logger.error(e, "Failed to register command {} in class {}",
                    command.getCommand().getName(), command.getClass().getSimpleName());
            }
        });
    }

    private static void setItem(HypixelPlayer player, String key, int slot) {
        itemHandler.getItem(key).ifPresent(item -> player.getInventory().setItemStack(slot, item.getItemStack(player)));
    }

    public static void populateInventory(HypixelPlayer player) {
        setItem(player, "teleporter", 0);
        setItem(player, "slower", 2);
        setItem(player, "backward", 3);
        setItem(player, "playback", 4);
        setItem(player, "forward", 5);
        setItem(player, "faster", 6);
        setItem(player, "menu", 8);
    }


    @Override
    public List<ServiceType> getRequiredServices() {
        return List.of(ServiceType.REPLAY);
    }

    @Override
    public TablistManager getTablistManager() {
        return new TablistManager() {
            @Override
            public List<net.swofty.type.generic.tab.TablistModule> getModules() {
                return List.of(new ReplayTablistModule());
            }
        };
    }

    @Override
    public List<Class<? extends GameDataHandler>> getAdditionalDataHandlers() {
        return List.of(ReplayDataHandler.class);
    }

    @Override
    public LoaderValues getLoaderValues() {
        return new LoaderValues(
            serverType -> new Pos(0, 100, 0),
            false
        );
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        return new ArrayList<>(HypixelGenericLoader.loopThroughPackage(
            "net.swofty.type.replayviewer.event",
            HypixelEventClass.class
        ).toList());
    }

    @Override
    public List<HypixelEventClass> getCustomEvents() {
        return List.of();
    }

    @Override
    public List<HypixelNPC> getNPCs() {
        return List.of();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<RedisMessageHandler<?, ?>> getServiceHandlers() {
        return (List) HypixelGenericLoader.loopThroughPackage(
            "net.swofty.type.replayviewer.redis.service",
            RedisMessageHandler.class
        ).toList();
    }

    @Override
    public List<RedisMessageHandler<?, ?>> getProxyHandlers() {
        return List.of();
    }

    @Override
    public @Nullable CustomWorlds getMainInstance() {
        return null; // Each replay creates its own instance
    }

    public static Optional<ReplaySession> getSession(UUID playerId) {
        return Optional.ofNullable(activeSessions.get(playerId));
    }

    public static Optional<ReplaySession> getSession(Player player) {
        return getSession(player.getUuid());
    }

    public static Optional<ReplaySession> getSessionByReplayId(UUID replayId) {
        return activeSessions.values().stream()
            .filter(session -> session.getReplayId().equals(replayId))
            .findFirst();
    }

    public static void registerSession(UUID playerId, ReplaySession session) {
        ReplaySession existingSession = activeSessions.get(playerId);
        if (existingSession != null && !existingSession.getReplayId().equals(session.getReplayId())) {
            existingSession.removeViewer(
                existingSession.getViewers().stream()
                    .filter(v -> v.getUuid().equals(playerId))
                    .findFirst()
                    .orElse(null)
            );
        }
        activeSessions.put(playerId, session);
    }

    public static void removeSession(UUID playerId) {
        activeSessions.remove(playerId);
    }
}
