package net.swofty.type.garden;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.swofty.commons.CustomWorlds;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.type.garden.commands.GardenAdminCommand;
import net.swofty.type.garden.npc.NPCAnita;
import net.swofty.type.garden.npc.NPCGardenVisitorSlot;
import net.swofty.type.garden.npc.NPCJacob;
import net.swofty.type.garden.npc.NPCJeff;
import net.swofty.type.garden.npc.NPCPamela;
import net.swofty.type.garden.npc.NPCPesthunterPhillip;
import net.swofty.type.garden.npc.NPCSam;
import net.swofty.type.garden.pest.GardenPestRuntime;
import net.swofty.type.garden.user.SkyBlockGarden;
import net.swofty.type.garden.visitor.GardenBarnRuntime;
import net.swofty.type.garden.visitor.GardenVisitorRuntime;
import net.swofty.type.generic.SkyBlockTypeLoader;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.tab.TablistManager;
import net.swofty.type.generic.tab.TablistModule;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.tabmodules.AccountInformationModule;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TypeGardenLoader implements SkyBlockTypeLoader {
    private final List<HypixelNPC> npcs = List.of(
        new NPCSam(),
        new NPCAnita(),
        new NPCPamela(),
        new NPCJacob(),
        new NPCJeff(),
        new NPCPesthunterPhillip(),
        new NPCGardenVisitorSlot(1),
        new NPCGardenVisitorSlot(2),
        new NPCGardenVisitorSlot(3),
        new NPCGardenVisitorSlot(4),
        new NPCGardenVisitorSlot(5)
    );

    @Override
    public ServerType getType() {
        return ServerType.SKYBLOCK_GARDEN;
    }

    @Override
    public void onInitialize(MinecraftServer server) {
        Logger.info("TypeGardenLoader initialized!");
        GardenServices.initialize();
        MinecraftServer.getCommandManager().register(new GardenAdminCommand().getCommand());
        npcs.forEach(HypixelNPC::register);
        GardenPestRuntime.start(MinecraftServer.getSchedulerManager());
        GardenVisitorRuntime.start(MinecraftServer.getSchedulerManager());
        GardenBarnRuntime.start(MinecraftServer.getSchedulerManager());
        SkyBlockGarden.runVacantLoop(MinecraftServer.getSchedulerManager());
    }

    @Override
    public void afterInitialize(MinecraftServer server) {
    }

    @Override
    public LoaderValues getLoaderValues() {
        return new LoaderValues((_) -> new Pos(-5.5, 71, 17.5, 180, 0), true);
    }

    @Override
    public List<ServiceType> getRequiredServices() {
        return List.of(
            ServiceType.AUCTION_HOUSE,
            ServiceType.ITEM_TRACKER,
            ServiceType.DATA_MUTEX,
            ServiceType.JACOBS_CONTEST
        );
    }

    @Override
    public TablistManager getTablistManager() {
        return new TablistManager() {
            @Override
            public List<TablistModule> getModules() {
                return new ArrayList<>(List.of(new AccountInformationModule()));
            }
        };
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        return SkyBlockGenericLoader.loopThroughPackage(
            "net.swofty.type.garden.events.traditional",
            HypixelEventClass.class
        ).collect(Collectors.toList());
    }

    @Override
    public List<HypixelEventClass> getCustomEvents() {
        return SkyBlockGenericLoader.loopThroughPackage(
            "net.swofty.type.garden.events.custom",
            HypixelEventClass.class
        ).collect(Collectors.toList());
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
    public @Nullable CustomWorlds getMainInstance() {
        return null;
    }
}
