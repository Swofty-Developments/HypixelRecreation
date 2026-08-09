package net.swofty.type.island;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.swofty.commons.CustomWorlds;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.type.generic.HypixelTypeLoader;
import net.swofty.type.generic.SkyBlockTypeLoader;

import net.swofty.type.generic.entity.npc.HypixelNPC;

import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.tab.TablistManager;
import net.swofty.type.generic.tab.TablistModule;
import net.swofty.type.island.lifecycle.IslandLifecycleSteps;
import net.swofty.type.island.npcs.NPCSam;
import net.swofty.type.island.tab.IslandGuestsModule;
import net.swofty.type.island.tab.IslandMemberModule;
import net.swofty.type.island.tab.IslandServerModule;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.minion.MinionHandler;
import net.swofty.type.skyblockgeneric.tabmodules.AccountInformationModule;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TypeIslandLoader implements SkyBlockTypeLoader {
    public static final Pos SPAWN_POS = new Pos(7.5, 100, 7.5);
    private final List<HypixelNPC> npcs = List.of(new NPCSam());

    @Override
    public ServerType getType() {
        return ServerType.SKYBLOCK_ISLAND;
    }

    @Override
    public void onInitialize(MinecraftServer server) {
        Logger.info("TypeIslandLoader initialized!");

        IslandLifecycleSteps.register();
        npcs.forEach(HypixelNPC::register);
        new MinionHandler(MinecraftServer.getSchedulerManager()).start();
    }

    @Override
    public void afterInitialize(MinecraftServer server) {

    }

    @Override
    public HypixelTypeLoader.LoaderValues getLoaderValues() {
        return new HypixelTypeLoader.LoaderValues(
            (_) -> SPAWN_POS, // Spawn position
                true // Announce death messages
        );
    }

    @Override
    public TablistManager getTablistManager() {
        return new TablistManager() {
            @Override
            public List<TablistModule> getModules() {
                return new ArrayList<>(List.of(
                        new IslandMemberModule(),
                        new IslandGuestsModule(),
                        new IslandServerModule(),
                        new AccountInformationModule()
                ));
            }
        };
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        return SkyBlockGenericLoader.loopThroughPackage(
                "net.swofty.type.island.events.traditional",
                HypixelEventClass.class
        ).collect(Collectors.toList());
    }

    @Override
    public List<HypixelEventClass> getCustomEvents() {
        return List.of();
    }

    @Override
    public List<HypixelNPC> getNPCs() {
        return npcs;
    }


    @Override
    public List<RedisMessageHandler<?, ?>> getProxyHandlers() {
        return List.of();
    }

    @Override
    public List<ServiceType> getRequiredServices() {
        return List.of(ServiceType.AUCTION_HOUSE, ServiceType.ITEM_TRACKER, ServiceType.DATA_MUTEX);
    }

    @Override
    public @Nullable CustomWorlds getMainInstance() {
        return null;
    }
}
