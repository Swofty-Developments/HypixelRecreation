package net.swofty.type.garden.visitor;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.garden.gui.GUIComposter;
import net.swofty.type.garden.gui.GUIDesk;
import net.swofty.type.garden.gui.GUIVisitorLogbook;
import net.swofty.type.garden.gui.GardenGuiSupport;
import net.swofty.type.garden.npc.GardenNpcAnchorRegistry;
import net.swofty.type.garden.user.SkyBlockGarden;
import net.swofty.type.generic.entity.InteractionEntity;
import net.swofty.type.generic.entity.hologram.ServerHolograms;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.furniture.Furniture;
import net.swofty.type.skyblockgeneric.furniture.FurnitureLoadException;
import net.swofty.type.skyblockgeneric.furniture.FurnitureNotPresent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class GardenBarnRuntime {
    private static final Map<UUID, BarnState> STATES = new ConcurrentHashMap<>();
    private static final Set<UUID> DIRTY_PROFILES = ConcurrentHashMap.newKeySet();

    private GardenBarnRuntime() {
    }

    public static void start(Scheduler scheduler) {
        scheduler.submitTask(() -> {
            tickAllGardens();
            return TaskSchedule.tick(2);
        }, ExecutionType.TICK_END);
    }

    public static void requestImmediateSync(SkyBlockPlayer player) {
        if (player == null || player.getSkyblockDataHandler() == null) {
            return;
        }
        DIRTY_PROFILES.add(player.getSkyblockDataHandler().getCurrentProfileId());
    }

    private static void tickAllGardens() {
        Map<UUID, List<SkyBlockPlayer>> playersByProfile = SkyBlockGenericLoader.getLoadedPlayers().stream()
            .filter(SkyBlockPlayer::isOnGarden)
            .filter(player -> player.getSkyblockDataHandler() != null)
            .collect(Collectors.groupingBy(player -> player.getSkyblockDataHandler().getCurrentProfileId()));

        playersByProfile.forEach(GardenBarnRuntime::tickProfile);

        STATES.entrySet().removeIf(entry -> {
            if (playersByProfile.containsKey(entry.getKey())) {
                return false;
            }
            entry.getValue().destroy();
            return true;
        });
    }

    private static void tickProfile(UUID profileId, List<SkyBlockPlayer> viewers) {
        if (viewers.isEmpty()) {
            return;
        }

        SkyBlockPlayer primary = viewers.getFirst();
        if (!(primary.getSkyBlockGarden() instanceof SkyBlockGarden garden) || garden.getGardenInstance() == null) {
            return;
        }

        BarnState state = STATES.computeIfAbsent(profileId, ignored -> new BarnState());
        Instance instance = garden.getGardenInstance();
        String skinId = GardenGuiSupport.core(primary).getSelectedBarnSkin();
        boolean dirty = DIRTY_PROFILES.remove(profileId);

        if (dirty || state.instance != instance || !Objects.equals(state.skinId, skinId)) {
            state.rebuildStatic(primary, instance, skinId);
        }

    }

    private static final class BarnState {
        private final Map<String, GardenInteractable> interactables = new HashMap<>();
        private Instance instance;
        private String skinId = "";

        private void rebuildStatic(SkyBlockPlayer player, Instance nextInstance, String nextSkinId) {
            destroyStatic();
            instance = nextInstance;
            skinId = nextSkinId;

            createInteractable(player, "desk", "§bDesk");
            createInteractable(player, "composter", "§aComposter");
            createInteractable(player, "visitor_logbook", "§aVisitor Logbook");
        }

        private void createInteractable(SkyBlockPlayer player, String anchorId, String label) {
            GardenNpcAnchorRegistry.getInteractionAnchor(player, anchorId).ifPresent(anchor -> {
                Pos position = anchor.position();
                ServerHolograms.ExternalHologram hologram = ServerHolograms.ExternalHologram.builder()
                    .instance(instance)
                    .pos(position.add(0, anchor.offsetY() != null ? anchor.offsetY() : 0.6, 0))
                    .text(new String[]{label, "§e§lCLICK"})
                    .build();
                ServerHolograms.addExternalHologram(hologram);

                InteractionEntity interaction = new InteractionEntity(1.4f, 2f, (viewer, _) -> {
                    if (!(viewer instanceof SkyBlockPlayer skyBlockPlayer)) {
                        return;
                    }
                    if ("desk".equals(anchorId)) {
                        skyBlockPlayer.openView(new GUIDesk());
                    } else if ("composter".equals(anchorId)) {
                        skyBlockPlayer.openView(new GUIComposter());
                    } else if ("visitor_logbook".equals(anchorId)) {
                        skyBlockPlayer.openView(new GUIVisitorLogbook());
                    }
                });
                interaction.setInstance(instance, position);

                try {
                    Furniture.load(player.getInstance(), anchorId, position);
                } catch (FurnitureLoadException | FurnitureNotPresent _) {
                    Logger.warn("Failed to load Garden furniture");
                }

                interactables.put(anchorId, new GardenInteractable(hologram, interaction));
            });
        }

        private void destroyStatic() {
            interactables.values().forEach(GardenInteractable::destroy);
            interactables.clear();
        }

        private void destroy() {
            destroyStatic();
            instance = null;
            skinId = "";
        }
    }

    private record GardenInteractable(ServerHolograms.ExternalHologram hologram, InteractionEntity interaction) {
        private void destroy() {
            ServerHolograms.removeExternalHologram(hologram);
            interaction.remove();
        }
    }
}
