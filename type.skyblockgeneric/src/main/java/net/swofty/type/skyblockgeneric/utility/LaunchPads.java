package net.swofty.type.skyblockgeneric.utility;

import lombok.Getter;
import lombok.NonNull;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.ServerType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.entity.hologram.PlayerHolograms;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Getter
public enum LaunchPads {
    VILLAGE_TO_FARMING(
        getSlimeBlocksNear(new Pos(87, 71, -154)),
        ServerType.SKYBLOCK_HUB,
        ServerType.SKYBLOCK_THE_FARMING_ISLANDS,
        new Pos(116.5, 74, -210.5),
        player -> player.sendTo(ServerType.SKYBLOCK_THE_FARMING_ISLANDS),
        player -> player.getSkills().getCurrentLevel(SkillCategories.FARMING) >= 5,
        "<c>You must be at least Farming Level V to join this island!",
        player -> {
            boolean hasRequirement = player.getSkills().getCurrentLevel(SkillCategories.FARMING) >= 5;
            if (!hasRequirement) {
                return PlayerHolograms.ExternalPlayerHologram.builder()
                    .pos(new Pos(87.5, 74, -153.5))
                    .player(player)
                    .text(new String[]{"<b>Travel to:", "<a>The Farming Islands", "<c>Requires Farming Level 5"})
                    .build();
            } else {
                return PlayerHolograms.ExternalPlayerHologram.builder()
                    .pos(new Pos(87.5, 74, -153.5))
                    .player(player)
                    .text(new String[]{"<b>Travel to:", "<a>The Farming Islands"})
                    .build();
            }
        }
    ),

    FARMING_TO_VILLAGE(
        getSlimeBlocksNear(new Pos(111, 71, -202)),
        ServerType.SKYBLOCK_THE_FARMING_ISLANDS,
        ServerType.SKYBLOCK_HUB,
        new Pos(74, 72, -180),
        player -> player.sendTo(ServerType.SKYBLOCK_HUB),
        _ -> true,
        "",
        player -> PlayerHolograms.ExternalPlayerHologram.builder()
            .pos(new Pos(111.5, 71.7, -202.5))
            .player(player)
            .text(new String[]{"<b>Travel to:", "<a>The Village"})
            .build()
    ),

    GRAVEYARD_TO_SPIDERS_DEN(
        getSlimeBlocksNear(new Pos(-145, 74, -187)),
        ServerType.SKYBLOCK_HUB,
        ServerType.SKYBLOCK_SPIDERS_DEN,
        new Pos(-202.5, 83, -233.5),
        player -> player.sendTo(ServerType.SKYBLOCK_SPIDERS_DEN),
        player -> player.getSkills().getCurrentLevel(SkillCategories.COMBAT) >= 1,
        "<c>You must be at least Combat Level I to join this island!",
        player -> {
            boolean hasRequirement = player.getSkills().getCurrentLevel(SkillCategories.COMBAT) >= 1;
            if (!hasRequirement) {
                return PlayerHolograms.ExternalPlayerHologram.builder()
                    .pos(new Pos(-145, 77, -186))
                    .player(player)
                    .text(new String[]{"<b>Travel to:", "<a>Spider's Den", "<c>Requires Combat Level 1"})
                    .build();
            } else {
                return PlayerHolograms.ExternalPlayerHologram.builder()
                    .pos(new Pos(-145, 77, -187))
                    .player(player)
                    .text(new String[]{"<b>Travel to:", "<a>Spider's Den"})
                    .build();
            }
        }
    ),

    SPIDERS_DEN_TO_GRAVEYARD(
        getSlimeBlocksNear(new Pos(-197, 83, -228)),
        ServerType.SKYBLOCK_SPIDERS_DEN,
        ServerType.SKYBLOCK_HUB,
        new Pos(-159.5, 73, -158.5),
        player -> player.sendTo(ServerType.SKYBLOCK_HUB),
        _ -> true,
        "",
        player -> PlayerHolograms.ExternalPlayerHologram.builder()
            .pos(new Pos(-197.5, 84, -228.5))
            .player(player)
            .text(new String[]{"<b>Travel to:", "<a>Hub Island"})
            .build()
    ),

    SPIDERS_DEN_TO_THE_END(
        getSlimeBlocksNear(new Pos(-383.5, 119, -261)),
        ServerType.SKYBLOCK_SPIDERS_DEN,
        ServerType.SKYBLOCK_THE_END,
        new Pos(-503, 101, -275),
        player -> player.sendTo(ServerType.SKYBLOCK_THE_END),
        player -> player.getSkills().getCurrentLevel(SkillCategories.COMBAT) >= 12,
        "<c>You must be at least Combat Level XII to join this island!",
        player -> {
            boolean hasRequirement = player.getSkills().getCurrentLevel(SkillCategories.COMBAT) >= 12;
            if (!hasRequirement) {
                return PlayerHolograms.ExternalPlayerHologram.builder()
                    .pos(new Pos(-383, 119.5, -261))
                    .player(player)
                    .text(new String[]{"<b>Travel to:", "<a>The End", "<c>Requires Combat Level 12"})
                    .build();
            } else {
                return PlayerHolograms.ExternalPlayerHologram.builder()
                    .pos(new Pos(-383, 119.5, -261))
                    .player(player)
                    .text(new String[]{"<b>Travel to:", "<a>The End"})
                    .build();
            }
        }
    ),

    THE_END_TO_SPIDERS_DEN(
        getSlimeBlocksNear(new Pos(-197, 83, -228)),
        ServerType.SKYBLOCK_THE_END,
        ServerType.SKYBLOCK_SPIDERS_DEN,
        new Pos(-378, 118.5, -261),
        player -> player.sendTo(ServerType.SKYBLOCK_SPIDERS_DEN),
        _ -> true,
        "",
        player -> PlayerHolograms.ExternalPlayerHologram.builder()
            .pos(new Pos(-495, 101.7, -275))
            .player(player)
            .text(new String[]{"<b>Travel to:", "<a>Spider's Den"})
            .build()
    ),

    SPIDERS_DEN_TO_CRIMSON_ISLE(
        getSlimeBlocksNear(new Pos(-356, 87, -352)),
        ServerType.SKYBLOCK_SPIDERS_DEN,
        ServerType.SKYBLOCK_CRIMSON_ISLE,
        new Pos(-361, 80, -425),
        player -> player.sendTo(ServerType.SKYBLOCK_CRIMSON_ISLE),
        player -> player.getSkills().getCurrentLevel(SkillCategories.COMBAT) >= 22,
        "<c>You must be at least Combat Level XXII to join this island!",
        player -> {
            boolean hasRequirement = player.getSkills().getCurrentLevel(SkillCategories.COMBAT) >= 22;
            if (!hasRequirement) {
                return PlayerHolograms.ExternalPlayerHologram.builder()
                    .pos(new Pos(-356, 87, -353))
                    .player(player)
                    .text(new String[]{"<b>Travel to:", "<a>Crimson Isle", "<c>Requires Combat Level 22"})
                    .build();
            } else {
                return PlayerHolograms.ExternalPlayerHologram.builder()
                    .pos(new Pos(-356, 87, -353))
                    .player(player)
                    .text(new String[]{"<b>Travel to:", "<a>Crimson Isle"})
                    .build();
            }
        }
    ),

    CRIMSON_ISLE_TO_SPIDERS_DEN(
        getSlimeBlocksNear(new Pos(-360, 80, -420)),
        ServerType.SKYBLOCK_CRIMSON_ISLE,
        ServerType.SKYBLOCK_SPIDERS_DEN,
        new Pos(-355, 87, -347),
        player -> player.sendTo(ServerType.SKYBLOCK_SPIDERS_DEN),
        _ -> true,
        "",
        player -> PlayerHolograms.ExternalPlayerHologram.builder()
            .pos(new Pos(-360.5, 80, -420))
            .player(player)
            .text(new String[]{"<b>Travel to:", "<a>Spider's Den"})
            .build()
    ),

    VILLAGE_TO_GOLD_MINE(
        getSlimeBlocksNear(new Pos(-1, 63, -193)),
        ServerType.SKYBLOCK_HUB, // TODO: UPDATE TO PROPER POSITIONS AND INFO
        ServerType.SKYBLOCK_GOLD_MINE,
        new Pos(-5, 73, -269),
        player -> player.sendTo(ServerType.SKYBLOCK_GOLD_MINE),
        player -> player.getSkills().getCurrentLevel(SkillCategories.MINING) >= 1,
        "<c>You must be at least Mining Level I to join this island!",
        player -> {
            boolean hasRequirement = player.getSkills().getCurrentLevel(SkillCategories.MINING) >= 1;
            if (!hasRequirement) {
                return PlayerHolograms.ExternalPlayerHologram.builder()
                    .pos(new Pos(-0.5, 65.7, -192.5))
                    .player(player)
                    .text(new String[]{"<b>Travel to:", "<a>Gold Mine", "<c>Requires Mining Level 1"})
                    .build();
            } else {
                return PlayerHolograms.ExternalPlayerHologram.builder()
                    .pos(new Pos(-0.5, 65.7, -192))
                    .player(player)
                    .text(new String[]{"<b>Travel to:", "<a>Gold Mine"})
                    .build();
            }
        }
    ),

    GOLD_MINE_TO_VILLAGE(
        getSlimeBlocksNear(new Pos(-4, 74, -272)),
        ServerType.SKYBLOCK_GOLD_MINE,
        ServerType.SKYBLOCK_HUB,
        new Pos(-8, 70, -238),
        player -> player.sendTo(ServerType.SKYBLOCK_HUB),
        _ -> true,
        "",
        player -> PlayerHolograms.ExternalPlayerHologram.builder()
            .pos(new Pos(-4.5, 73.3, -268.5))
            .player(player)
            .text(new String[]{"<b>Travel to:", "<a>The Village"})
            .build()
    ),

    GOLD_MINE_TO_DEEP_CAVERNS(
        getSlimeBlocksNear(new Pos(-7, 67, -396)),
        ServerType.SKYBLOCK_GOLD_MINE,
        ServerType.SKYBLOCK_DEEP_CAVERNS,
        new Pos(-4, 119, -491.5),
        player -> player.sendTo(ServerType.SKYBLOCK_DEEP_CAVERNS),
        player -> player.getSkills().getCurrentLevel(SkillCategories.MINING) >= 5,
        "",
        player -> PlayerHolograms.ExternalPlayerHologram.builder()
            .pos(new Pos(-7, 69, -396))
            .player(player)
            .text(new String[]{"<b>Travel to:", "<a>Deep Caverns"})
            .build()
    ),

    DEEP_CAVERNS_TO_GOLD_MINE(
        getSlimeBlocksNear(new Pos(3, 157, 85)),
        ServerType.SKYBLOCK_DEEP_CAVERNS,
        ServerType.SKYBLOCK_GOLD_MINE,
        new Pos(2, 150, 136),
        player -> player.sendTo(ServerType.SKYBLOCK_GOLD_MINE),
        _ -> true,
        "",
        player -> PlayerHolograms.ExternalPlayerHologram.builder()
            .pos(new Pos(4, 158.5, 89))
            .player(player)
            .text(new String[]{"<b>Travel to:", "<a>Gold Mine"})
            .build()
    ),

    FOREST_TO_BIRCH_PARK(
        getSlimeBlocksNear(new Pos(-224.5, 73, -15.5)),
        ServerType.SKYBLOCK_HUB,
        ServerType.SKYBLOCK_THE_PARK,
        new Pos(-265.5, 79, -17.5),
        player -> player.sendTo(ServerType.SKYBLOCK_THE_PARK),
        player -> player.getSkills().getCurrentLevel(SkillCategories.FORAGING) >= 1,
        "<c>You must be at least Foraging Level I to join this island!",
        player -> {
            boolean hasRequirement = player.getSkills().getCurrentLevel(SkillCategories.FORAGING) >= 1;
            if (!hasRequirement) {
                return PlayerHolograms.ExternalPlayerHologram.builder()
                    .pos(new Pos(-224.5, 75.5, -15.5))
                    .player(player)
                    .text(new String[]{"<b>Travel to:", "<a>The Park", "<c>Requires Foraging Level 1"})
                    .build();
            } else {
                return PlayerHolograms.ExternalPlayerHologram.builder()
                    .pos(new Pos(-224.5, 75.5, -15.5))
                    .player(player)
                    .text(new String[]{"<b>Travel to:", "<a>The Park"})
                    .build();
            }
        }
    ),

    BIRCH_PARK_TO_FOREST(
        getSlimeBlocksNear(new Pos(-261.5, 79, -17.5)),
        ServerType.SKYBLOCK_THE_PARK,
        ServerType.SKYBLOCK_HUB,
        new Pos(-221.5, 73, -15.5),
        player -> player.sendTo(ServerType.SKYBLOCK_HUB),
        _ -> true,
        "",
        player -> PlayerHolograms.ExternalPlayerHologram.builder()
            .pos(new Pos(-261.5, 79.5, -17.5))
            .player(player)
            .text(new String[]{"<b>Travel to:", "<a>Hub Island"})
            .build()
    ),

    BIRCH_PARK_TO_SPRUCE_WOODS(
        getSlimeBlocksNear(new Pos(-314.5, 81, -9.5)),
        ServerType.SKYBLOCK_THE_PARK,
        ServerType.SKYBLOCK_THE_PARK,
        new Pos(-348.5, 90, -4.5),
        player -> player.teleport(new Pos(-348.5, 90, -4.5)),
        _ -> true,
        "",
        player -> PlayerHolograms.ExternalPlayerHologram.builder()
            .pos(new Pos(-314.5, 81.5, -9.5))
            .player(player)
            .text(new String[]{"<b>Travel to:", "<a>Spruce Woods"})
            .build()
    ),

    SPRUCE_WOODS_TO_BIRCH_PARK(
        getSlimeBlocksNear(new Pos(-343.5, 90, -4.5)),
        ServerType.SKYBLOCK_THE_PARK,
        ServerType.SKYBLOCK_THE_PARK,
        new Pos(-306.5, 81, -5.5),
        player -> player.teleport(new Pos(-306.5, 81, -5.5)),
        _ -> true,
        "",
        player -> PlayerHolograms.ExternalPlayerHologram.builder()
            .pos(new Pos(-343.5, 90.5, -4.5))
            .player(player)
            .text(new String[]{"<b>Travel to:", "<a>Birch Park"})
            .build()
    ),

    SPRUCE_WOODS_TO_DARK_THICKET(
        getSlimeBlocksNear(new Pos(-364.5, 90, -18.5)),
        ServerType.SKYBLOCK_THE_PARK,
        ServerType.SKYBLOCK_THE_PARK,
        new Pos(-380.5, 98, -38.5),
        player -> player.teleport(new Pos(-380.5, 98, -38.5)),
        _ -> true,
        "",
        player -> PlayerHolograms.ExternalPlayerHologram.builder()
            .pos(new Pos(-364.5, 90.5, -18.5))
            .player(player)
            .text(new String[]{"<b>Travel to:", "<a>Dark Thicket"})
            .build()
    ),

    DARK_THICKET_TO_SPRUCE_WOODS(
        getSlimeBlocksNear(new Pos(-376.5, 97, -32.5)),
        ServerType.SKYBLOCK_THE_PARK,
        ServerType.SKYBLOCK_THE_PARK,
        new Pos(-361.5, 90, -13.5),
        player -> player.teleport(new Pos(-361.5, 90, -13.5)),
        _ -> true,
        "",
        player -> PlayerHolograms.ExternalPlayerHologram.builder()
            .pos(new Pos(-376.5, 97.5, -32.5))
            .player(player)
            .text(new String[]{"<b>Travel to:", "<a>Spruce Woods"})
            .build()
    ),

    DARK_THICKET_TO_SAVANNA_WOODLAND(
        getSlimeBlocksNear(new Pos(-400.5, 98, -33.5)),
        ServerType.SKYBLOCK_THE_PARK,
        ServerType.SKYBLOCK_THE_PARK,
        new Pos(-418.5, 110, -12.5),
        player -> player.teleport(new Pos(-418.5, 110, -12.5)),
        _ -> true,
        "",
        player -> PlayerHolograms.ExternalPlayerHologram.builder()
            .pos(new Pos(-400.5, 98.5, -33.5))
            .player(player)
            .text(new String[]{"<b>Travel to:", "<a>Savanna Woodland"})
            .build()
    ),

    SAVANNA_WOODLAND_TO_DARK_THICKET(
        getSlimeBlocksNear(new Pos(-415.5, 109, -16.5)),
        ServerType.SKYBLOCK_THE_PARK,
        ServerType.SKYBLOCK_THE_PARK,
        new Pos(-397.5, 98, -37.5),
        player -> player.teleport(new Pos(-397.5, 98, -37.5)),
        _ -> true,
        "",
        player -> PlayerHolograms.ExternalPlayerHologram.builder()
            .pos(new Pos(-415.5, 109.5, -16.5))
            .player(player)
            .text(new String[]{"<b>Travel to:", "<a>Dark Thicket"})
            .build()
    ),

    SAVANNA_WOODLAND_TO_JUNGLE_ISLAND(
        getSlimeBlocksNear(new Pos(-439.5, 111, -17.5)),
        ServerType.SKYBLOCK_THE_PARK,
        ServerType.SKYBLOCK_THE_PARK,
        new Pos(-462.5, 121, -32.5),
        player -> player.teleport(new Pos(-462.5, 121, -32.5)),
        _ -> true,
        "",
        player -> PlayerHolograms.ExternalPlayerHologram.builder()
            .pos(new Pos(-439.5, 111.5, -17.5))
            .player(player)
            .text(new String[]{"<b>Travel to:", "<a>Jungle Island"})
            .build()
    ),

    JUNGLE_ISLAND_TO_SAVANNA_WOODLAND(
        getSlimeBlocksNear(new Pos(-461.5, 121, -28.5)),
        ServerType.SKYBLOCK_THE_PARK,
        ServerType.SKYBLOCK_THE_PARK,
        new Pos(-434.5, 110, -13.5),
        player -> player.teleport(new Pos(-434.5, 110, -13.5)),
        _ -> true,
        "",
        player -> PlayerHolograms.ExternalPlayerHologram.builder()
            .pos(new Pos(-461.5, 121.5, -28.5))
            .player(player)
            .text(new String[]{"<b>Travel to:", "<a>Savanna Woodland"})
            .build()
    ),

    JUNGLE_ISLAND_TO_GALATEA(
        getSlimeBlocksNear(new Pos(-488.5, 116, -40)),
        ServerType.SKYBLOCK_THE_PARK,
        ServerType.SKYBLOCK_GALATEA,
        new Pos(-542.5, 108, -26.5),
        player -> player.sendTo(ServerType.SKYBLOCK_GALATEA),
        player -> player.getSkills().getCurrentLevel(SkillCategories.FORAGING) >= 12,
        "<c>You must be at least Foraging Level XII to join this island!",
        player -> {
            boolean hasRequirement = player.getSkills().getCurrentLevel(SkillCategories.FORAGING) >= 12;
            if (!hasRequirement) {
                return PlayerHolograms.ExternalPlayerHologram.builder()
                    .pos(new Pos(-489.5, 116.5, -40))
                    .player(player)
                    .text(new String[]{"<b>Travel to:", "<a>Galatea", "<c>Requires Foraging Level 12"})
                    .build();
            } else {
                return PlayerHolograms.ExternalPlayerHologram.builder()
                    .pos(new Pos(-489.5, 116.5, -40))
                    .player(player)
                    .text(new String[]{"<b>Travel to:", "<a>Galatea"})
                    .build();
            }
        }
    ),

    GALATEA_TO_JUNGLE_ISLAND(
        getSlimeBlocksNear(new Pos(-538.5, 108, -29)),
        ServerType.SKYBLOCK_GALATEA,
        ServerType.SKYBLOCK_THE_PARK,
        new Pos(-483.5, 117, -41.5),
        player -> player.sendTo(ServerType.SKYBLOCK_THE_PARK),
        _ -> true,
        "",
        player -> PlayerHolograms.ExternalPlayerHologram.builder()
            .pos(new Pos(-538.5, 108.5, -28.5))
            .player(player)
            .text(new String[]{"<b>Travel to:", "<a>The Park"})
            .build()
    ),
    ;

    private static final List<LaunchPads> launchPads = new ArrayList<>();

    private final List<Pos> slimeBlocks;
    private final ServerType serverType;
    private final ServerType targetServerType;
    private final Pos destination;
    private final Consumer<SkyBlockPlayer> afterFinished;
    private final Function<SkyBlockPlayer, Boolean> shouldAllow;
    private final String rejectionMessage;
    private final Function<SkyBlockPlayer, PlayerHolograms.ExternalPlayerHologram> hologramDisplay;

    LaunchPads(
        List<Pos> slimeBlocks,
        ServerType serverType,
        ServerType targetServerType,
        Pos destination,
        Consumer<@NonNull SkyBlockPlayer> afterFinished,
        Function<SkyBlockPlayer, Boolean> shouldAllow,
        String rejectionMessage,
        Function<@NonNull SkyBlockPlayer, PlayerHolograms.ExternalPlayerHologram> hologramDisplay
    ) {
        this.slimeBlocks = slimeBlocks;
        this.serverType = serverType;
        this.targetServerType = targetServerType;
        this.destination = destination;
        this.afterFinished = afterFinished;
        this.shouldAllow = shouldAllow;
        this.rejectionMessage = rejectionMessage;
        this.hologramDisplay = hologramDisplay;
    }

    public static void register(Scheduler scheduler) {
        launchPads.addAll(Arrays.asList(LaunchPads.values()));
        launchPads.removeIf(launchPad -> launchPad.serverType != HypixelConst.getTypeLoader().getType());
        Map<UUID, List<PlayerHolograms.ExternalPlayerHologram>> hologramMap = new HashMap<>();

        scheduler.scheduleTask(() -> {
            List<UUID> updated = new ArrayList<>();

            SkyBlockGenericLoader.getLoadedPlayers().forEach(player -> {
                if (hologramMap.containsKey(player.getUuid())) {
                    hologramMap.get(player.getUuid()).forEach(PlayerHolograms::removeExternalPlayerHologram);
                    hologramMap.remove(player.getUuid());
                }

                List<PlayerHolograms.ExternalPlayerHologram> holograms = new ArrayList<>();
                for (LaunchPads launchPad : launchPads) {
                    PlayerHolograms.ExternalPlayerHologram hologram = launchPad.hologramDisplay.apply(player);
                    holograms.add(hologram);
                    PlayerHolograms.addExternalPlayerHologram(hologram);
                    updated.add(player.getUuid());
                }
                hologramMap.put(player.getUuid(), holograms);
            });

            hologramMap.keySet().removeIf(uuid -> !updated.contains(uuid));
        }, TaskSchedule.seconds(2), TaskSchedule.seconds(2), ExecutionType.TICK_END);

        scheduler.scheduleTask(() -> {
            for (LaunchPads launchPad : launchPads) {
                for (Pos slimeBlock : launchPad.getSlimeBlocks()) {
                    SkyBlockGenericLoader.getLoadedPlayers().forEach(player -> {
                        if (slimeBlock.distance(player.getPosition()) <= 15) {
                            player.sendPacket(new ParticlePacket(
                                Particle.COMPOSTER,
                                false,
                                false,
                                slimeBlock.x(),
                                slimeBlock.y() + 1,
                                slimeBlock.z(),
                                0.1f,
                                0.1f,
                                0.1f,
                                0f,
                                3
                            ));
                        }
                    });
                }
            }
        }, TaskSchedule.seconds(2), TaskSchedule.tick(5), ExecutionType.TICK_END);
    }

    public static @Nullable LaunchPads getLaunchPadInRange(Pos pos, double range) {
        if (launchPads.isEmpty()) return null;

        for (LaunchPads launchPad : launchPads) {
            for (Pos slimeBlock : launchPad.getSlimeBlocks()) {
                if (slimeBlock.distance(pos) <= range) {
                    return launchPad;
                }
            }
        }
        return null;
    }

    private static List<Pos> getSlimeBlocksNear(Pos pos) {
        Instance instance = HypixelConst.getInstanceContainer();
        if (instance == null) return new ArrayList<>();
        List<Pos> toReturn = new ArrayList<>();

        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                for (int y = -5; y <= 5; y++) {
                    Pos check = new Pos(pos.x() + x, pos.y() + y, pos.z() + z);
                    instance.loadChunk(check).join();

                    if (instance.getBlock(check) == Block.SLIME_BLOCK)
                        toReturn.add(check);
                    if (instance.getBlock(check) == Block.BARRIER)
                        instance.setBlock(check, Block.AIR);
                }
            }
        }

        return toReturn;
    }
}
