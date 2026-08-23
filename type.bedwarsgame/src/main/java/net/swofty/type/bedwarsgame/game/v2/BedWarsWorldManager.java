package net.swofty.type.bedwarsgame.game.v2;

import lombok.RequiredArgsConstructor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.VillagerProfession;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.MapTeam;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.TeamKey;
import net.swofty.commons.mc.HypixelPosition;
import net.swofty.commons.mc.Vec3i;
import net.swofty.type.bedwarsgame.gui.GUIItemShop;
import net.swofty.type.bedwarsgame.gui.GUITeamShop;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.generic.collectibles.bedwars.BedWarsShopkeeperAppearanceService;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.AnimalConfiguration;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.entity.npc.configuration.VillagerConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class BedWarsWorldManager {
    private final BedWarsGame game;
    private final List<RecordedShopNpc> recordedShopNPCs = new ArrayList<>();
    private boolean shopNPCsRecorded = false;

    public void clearExistingBeds() {
        Instance instance = game.getInstance();
        Map<TeamKey, MapTeam> teams = game.getMapEntry().getConfiguration().getTeams();

        for (MapTeam team : teams.values()) {
            BedWarsMapsConfig.TwoBlockPosition bedPos = team.getBed();
            if (bedPos != null) {
                clearBedBlocks(instance, bedPos);
            }
        }
    }

    private void clearBedBlocks(final @NotNull Instance instance, BedWarsMapsConfig.TwoBlockPosition bedPos) {
        if (bedPos.feet() != null) {
            instance.setBlock(
                bedPos.feet().x(),
                bedPos.feet().y(),
                bedPos.feet().z(),
                Block.AIR
            );
        }
        if (bedPos.head() != null) {
            instance.setBlock(
                bedPos.head().x(),
                bedPos.head().y(),
                bedPos.head().z(),
                Block.AIR
            );
        }
    }

    public void placeBeds(Map<TeamKey, MapTeam> activeTeams) {
        activeTeams.forEach(this::placeBedForTeam);
    }

    /**
     * Places a bed for a specific team.
     */
    public void placeBedForTeam(TeamKey teamKey, MapTeam team) {
        BedWarsMapsConfig.TwoBlockPosition bedPos = team.getBed();
        if (bedPos == null || bedPos.feet() == null || bedPos.head() == null) {
            Logger.warn("Bed position not defined for team: {}", teamKey.getName());
            return;
        }

        final Instance instance = game.getInstance();
        Vec3i feetPos = bedPos.feet();
        Vec3i headPos = bedPos.head();

        try {
            Material bedMaterial = teamKey.bedMaterial();
            String facing = calculateBedFacing(feetPos, headPos);

            Block footBlock = bedMaterial.block()
                .withProperty("part", "foot")
                .withProperty("facing", facing);
            Block headBlock = bedMaterial.block()
                .withProperty("part", "head")
                .withProperty("facing", facing);

            instance.setBlock(feetPos.x(), feetPos.y(), feetPos.z(), footBlock);
            instance.setBlock(headPos.x(), headPos.y(), headPos.z(), headBlock);

            // Mark bed as alive in team
            game.getTeam(teamKey.name()).ifPresent(t -> t.setBedAlive(true));

        } catch (Exception e) {
            Logger.error("Error placing bed for team {}: {}", teamKey.getName(), e.getMessage());
        }
    }

    private String calculateBedFacing(Vec3i feet, Vec3i head) {
        double dx = head.x() - feet.x();
        double dz = head.z() - feet.z();

        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? "east" : "west";
        } else {
            return dz > 0 ? "south" : "north";
        }
    }

    public void spawnShopNPCs(Map<TeamKey, MapTeam> activeTeams) {
        activeTeams.forEach((teamKey, team) -> {
            if (team.getShop() == null) return;

            ShopkeeperSources sources = resolveShopkeeperSources(teamKey);

            HypixelPosition itemShopPos = team.getShop().item();
            HypixelPosition teamShopPos = team.getShop().team();

            if (itemShopPos != null) {
                Pos npcPos = new Pos(itemShopPos.x(), itemShopPos.y(), itemShopPos.z(),
                    itemShopPos.yaw(), itemShopPos.pitch());
                String[] holograms = new String[]{"<b>ITEM SHOP", "<e><l>RIGHT CLICK"};

                new Pos(0, 0, 0, 0, 0);

                BedWarsShopkeeperAppearanceService.ShopkeeperAppearance appearance =
                    resolveShopkeeperAppearance(sources.itemShopPlayer());

                HypixelNPC shopNpc = createShopNpc(npcPos, holograms, appearance, event -> {
                    BedWarsPlayer bwPlayer = (BedWarsPlayer) event.player();
                    bwPlayer.openView(new GUIItemShop(bwPlayer.getGame()));
                });
                shopNpc.register();

                int npcEntityId = 10000 + teamKey.ordinal() * 2; // Generate unique entity ID
                recordedShopNPCs.add(new RecordedShopNpc(
                    npcEntityId,
                    npcPos,
                    holograms,
                    "ITEM_SHOP",
                    appearance.replayEntityTypeId(),
                    appearance.textureValue(),
                    appearance.textureSignature()
                ));
            }

            if (teamShopPos != null) {
                Pos npcPos = new Pos(teamShopPos.x(), teamShopPos.y(), teamShopPos.z(),
                    teamShopPos.yaw(), teamShopPos.pitch());
                String[] holograms = new String[]{"<b>TEAM", "<b>UPGRADES", "<e><l>RIGHT CLICK"};

                BedWarsShopkeeperAppearanceService.ShopkeeperAppearance appearance =
                    resolveShopkeeperAppearance(sources.teamShopPlayer());

                HypixelNPC teamNpc = createShopNpc(npcPos, holograms, appearance, event -> {
                    BedWarsPlayer bwPlayer = (BedWarsPlayer) event.player();
                    bwPlayer.openView(new GUITeamShop());
                });
                teamNpc.register();

                int npcEntityId = 10000 + teamKey.ordinal() * 2 + 1; // Generate unique entity ID
                recordedShopNPCs.add(new RecordedShopNpc(
                    npcEntityId,
                    npcPos,
                    holograms,
                    "TEAM_SHOP",
                    appearance.replayEntityTypeId(),
                    appearance.textureValue(),
                    appearance.textureSignature()
                ));
            }
        });
    }

    private BedWarsShopkeeperAppearanceService.ShopkeeperAppearance resolveShopkeeperAppearance(BedWarsPlayer sourcePlayer) {
        if (sourcePlayer == null) {
            return BedWarsShopkeeperAppearanceService.defaultAppearance();
        }
        return BedWarsShopkeeperAppearanceService.resolveSelected(sourcePlayer);
    }

    private ShopkeeperSources resolveShopkeeperSources(TeamKey teamKey) {
        List<BedWarsPlayer> teamPlayers = new ArrayList<>(game.getPlayersOnTeam(teamKey));
        if (teamPlayers.isEmpty()) {
            return new ShopkeeperSources(null, null);
        }

        if (teamPlayers.size() == 1) {
            BedWarsPlayer onlyPlayer = teamPlayers.getFirst();
            return new ShopkeeperSources(onlyPlayer, onlyPlayer);
        }

        Collections.shuffle(teamPlayers, ThreadLocalRandom.current());
        return new ShopkeeperSources(teamPlayers.get(0), teamPlayers.get(1));
    }

    private HypixelNPC createShopNpc(
        Pos npcPos,
        String[] holograms,
        BedWarsShopkeeperAppearanceService.ShopkeeperAppearance appearance,
        Consumer<NPCInteractEvent> clickAction
    ) {
        return switch (appearance.kind()) {
            case HUMAN -> new HypixelNPC(new HumanConfiguration() {
                @Override
                public String[] holograms(HypixelPlayer player) {
                    return holograms;
                }

                @Override
                public Pos position(HypixelPlayer player) {
                    return npcPos;
                }

                @Override
                public String texture(HypixelPlayer player) {
                    return appearance.textureValue();
                }

                @Override
                public String signature(HypixelPlayer player) {
                    return appearance.textureSignature() == null ? "" : appearance.textureSignature();
                }

                @Override
                public boolean looking(HypixelPlayer player) {
                    return true;
                }

                @Override
                public Instance instance() {
                    return game.getInstance();
                }
            }) {
                @Override
                public void onClick(NPCInteractEvent event) {
                    clickAction.accept(event);
                }
            };
            case VILLAGER -> new HypixelNPC(new VillagerConfiguration() {
                @Override
                public String[] holograms(HypixelPlayer player) {
                    return holograms;
                }

                @Override
                public Pos position(HypixelPlayer player) {
                    return npcPos;
                }

                @Override
                public VillagerProfession profession() {
                    return appearance.villagerProfession();
                }

                @Override
                public boolean looking(HypixelPlayer player) {
                    return true;
                }

                @Override
                public Instance instance() {
                    return game.getInstance();
                }
            }) {
                @Override
                public void onClick(NPCInteractEvent event) {
                    clickAction.accept(event);
                }
            };
            case MOB -> new HypixelNPC(new AnimalConfiguration() {
                @Override
                public String[] holograms(HypixelPlayer player) {
                    return holograms;
                }

                @Override
                public Pos position(HypixelPlayer player) {
                    return npcPos;
                }

                @Override
                public EntityType entityType() {
                    return appearance.entityType();
                }

                @Override
                public boolean looking(HypixelPlayer player) {
                    return true;
                }

                @Override
                public Instance instance() {
                    return game.getInstance();
                }
            }) {
                @Override
                public void onClick(NPCInteractEvent event) {
                    clickAction.accept(event);
                }
            };
        };
    }

    public void spawnTemporaryItemShop(Pos position) {
        BedWarsShopkeeperAppearanceService.ShopkeeperAppearance appearance =
            BedWarsShopkeeperAppearanceService.defaultAppearance();
        HypixelNPC shopNpc = createShopNpc(
            position,
            new String[]{"<b>ITEM SHOP", "<e><l>RIGHT CLICK", "<7>Disappears in 30 seconds"},
            appearance,
            event -> {
                BedWarsPlayer player = (BedWarsPlayer) event.player();
                if (player.getGame() == game && game.getState() == net.swofty.type.game.game.GameState.IN_PROGRESS) {
                    player.openView(new GUIItemShop(game));
                }
            }
        );
        shopNpc.register();
        net.minestom.server.MinecraftServer.getSchedulerManager().buildTask(shopNpc::unregister)
            .delay(net.minestom.server.timer.TaskSchedule.seconds(30))
            .schedule();
    }

    public void recordShopNpcsForReplay() {
        if (shopNPCsRecorded || !game.getReplayManager().isRecording()) {
            return;
        }
        for (RecordedShopNpc npc : recordedShopNPCs) {
            game.getReplayManager().recordShopNpc(
                npc.entityId(),
                npc.position(),
                npc.holograms(),
                npc.npcType(),
                npc.replayEntityTypeId(),
                npc.replayTextureValue(),
                npc.replayTextureSignature()
            );
        }
        shopNPCsRecorded = true;
    }

    private record ShopkeeperSources(BedWarsPlayer itemShopPlayer, BedWarsPlayer teamShopPlayer) {
    }

    private record RecordedShopNpc(
        int entityId,
        Pos position,
        String[] holograms,
        String npcType,
        int replayEntityTypeId,
        String replayTextureValue,
        String replayTextureSignature
    ) {
    }
}
