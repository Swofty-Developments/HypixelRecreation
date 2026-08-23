package net.swofty.type.ravengardlobby;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.advancements.AdvancementRoot;
import net.minestom.server.advancements.AdvancementTab;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.item.Material;
import net.swofty.commons.CustomWorlds;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.Tuple;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.RavengardTypeLoader;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.tab.TablistManager;
import net.swofty.type.generic.tab.TablistModule;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.ravengardgeneric.RavengardGenericLoader;
import net.swofty.type.ravengardgeneric.entity.RavengardNPC;
import net.swofty.type.ravengardgeneric.entity.animation.RavengardAnimationRegistry;
import net.swofty.type.ravengardgeneric.user.RavengardPlayer;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TypeRavengardLobbyLoader implements RavengardTypeLoader {

    // the tab that is used to detect when player opens the advancement menu
    public static AdvancementTab detectorTab;

    @Override
    public ServerType getType() {
        return ServerType.RAVENGARD_LOBBY;
    }

    @Override
    public void onInitialize(MinecraftServer server) {
        var advancementManager = MinecraftServer.getAdvancementManager();

        // TODO: use recreationmod "/recreation copyadvancements" to make this look the same as theirs.
        AdvancementRoot root = new AdvancementRoot(
                Component.text("."),
                Component.text("."),
                Material.COMPASS,
                FrameType.TASK,
                0f,
                0f,
                "minecraft:textures/gui/advancements/backgrounds/stone.png"
        );

        detectorTab = advancementManager.createTab(
                "test:t",
                root
        );

        new RavengardNPC(RavengardNPC.Configuration.builder()
                .position(new Pos(-57.5, 65, 33.5))
                .name("Castle Guard")
                .bottom("Guard")
                .areaEffectCloud(new RavengardNPC.AreaEffectCloudData(new Vec(0, 0.9375, 0), 0f, -1, 20, 0f, 0f, 0, ""))
                .areaEffectCloud(new RavengardNPC.AreaEffectCloudData(new Vec(0, -0.5, 0), 0f, -1, 20, 0f, 0f, 0, ""))
                .itemDisplay(new RavengardNPC.ItemDisplayData(new Vec(0, 1.4375, 0), Material.LEATHER_BOOTS, "hypixel_ravengard:entity/guard_ducal/head", List.of(), List.of(), List.of(), List.of(), 16777215, null, "HEAD", new RavengardNPC.DisplayData(new Vec(0, 0.0618, -0), new Vec(1, 1, 1), new float[]{-0.001f, 0.7071f, 0.001f, 0.7071f}, new float[]{-0f, -0f, -0f, 1f}, 0, 0, 0, "FIXED", -1, 0f, 1f, 16777215)))
                .itemDisplay(new RavengardNPC.ItemDisplayData(new Vec(0, 1.4375, 0), Material.LEATHER_BOOTS, "hypixel_ravengard:entity/guard_ducal/shield", List.of(), List.of(), List.of(), List.of(), 16777215, null, "HEAD", new RavengardNPC.DisplayData(new Vec(-0.0444, -0.551, -0.8124), new Vec(0.001, 0.001, 0.001), new float[]{0.0093f, 0.7057f, -0.0524f, 0.7065f}, new float[]{-0f, -0f, -0f, 1f}, 0, 0, 0, "FIXED", -1, 0f, 1f, 16777215)))
                .itemDisplay(new RavengardNPC.ItemDisplayData(new Vec(0, 1.4375, 0), Material.LEATHER_BOOTS, "hypixel_ravengard:entity/guard_ducal/spear", List.of(), List.of(), List.of(), List.of(), 16777215, null, "HEAD", new RavengardNPC.DisplayData(new Vec(0.3677, -0.3574, 0.3378), new Vec(2, 2, 2), new float[]{-0.0099f, 0.7254f, 0.0274f, 0.6877f}, new float[]{-0f, -0f, -0f, 1f}, 0, 0, 0, "FIXED", -1, 0f, 1f, 16777215)))
                .itemDisplay(new RavengardNPC.ItemDisplayData(new Vec(0, 1.4375, 0), Material.LEATHER_BOOTS, "hypixel_ravengard:entity/guard_ducal/shield2", List.of(), List.of(), List.of(), List.of(), 16777215, null, "HEAD", new RavengardNPC.DisplayData(new Vec(-0.1875, -0.314, 0), new Vec(1, 1, 1), new float[]{0f, 0.7071f, 0f, 0.7071f}, new float[]{-0f, -0f, -0f, 1f}, 0, 0, 0, "FIXED", -1, 0f, 1f, 16777215)))
                .itemDisplay(new RavengardNPC.ItemDisplayData(new Vec(0, 1.4375, 0), Material.LEATHER_BOOTS, "hypixel_ravengard:entity/guard_ducal/left_leg", List.of(), List.of(), List.of(), List.of(), 16777215, null, "HEAD", new RavengardNPC.DisplayData(new Vec(0, -0.6903, -0.1187), new Vec(1, 1, 1), new float[]{0f, 0.766f, 0f, 0.6428f}, new float[]{-0f, -0f, -0f, 1f}, 0, 0, 0, "FIXED", -1, 0f, 1f, 16777215)))
                .itemDisplay(new RavengardNPC.ItemDisplayData(new Vec(0, 1.4375, 0), Material.LEATHER_BOOTS, "hypixel_ravengard:entity/guard_ducal/left_arm", List.of(), List.of(), List.of(), List.of(), 16777215, null, "HEAD", new RavengardNPC.DisplayData(new Vec(0, -0.0632, -0.3125), new Vec(1, 1, 1), new float[]{0.0152f, 0.7056f, -0.0465f, 0.7069f}, new float[]{-0f, -0f, -0f, 1f}, 0, 0, 0, "FIXED", -1, 0f, 1f, 16777215)))
                .itemDisplay(new RavengardNPC.ItemDisplayData(new Vec(0, 1.4375, 0), Material.LEATHER_BOOTS, "hypixel_ravengard:entity/guard_ducal/right_leg", List.of(), List.of(), List.of(), List.of(), 16777215, null, "HEAD", new RavengardNPC.DisplayData(new Vec(0, -0.6903, 0.1187), new Vec(1, 1, 1), new float[]{0f, 0.6593f, 0f, 0.7518f}, new float[]{-0f, -0f, -0f, 1f}, 0, 0, 0, "FIXED", -1, 0f, 1f, 16777215)))
                .itemDisplay(new RavengardNPC.ItemDisplayData(new Vec(0, 1.4375, 0), Material.LEATHER_BOOTS, "hypixel_ravengard:entity/guard_ducal/torso", List.of(), List.of(), List.of(), List.of(), 16777215, null, "HEAD", new RavengardNPC.DisplayData(new Vec(-0.1875, -0.314, 0), new Vec(1, 1, 1), new float[]{0f, 0.7071f, 0f, 0.7071f}, new float[]{-0f, -0f, -0f, 1f}, 0, 0, 0, "FIXED", -1, 0f, 1f, 16777215)))
                .itemDisplay(new RavengardNPC.ItemDisplayData(new Vec(0, 1.4375, 0), Material.LEATHER_BOOTS, "hypixel_ravengard:entity/guard_ducal/rightarm", List.of(), List.of(), List.of(), List.of(), 16777215, null, "HEAD", new RavengardNPC.DisplayData(new Vec(0, -0.1077, 0.3125), new Vec(1, 1, 1), new float[]{-0.1981f, 0.6802f, 0.2556f, 0.6578f}, new float[]{-0f, -0f, -0f, 1f}, 0, 0, 0, "FIXED", -1, 0f, 1f, 16777215)))
                .build()) {
            @Override
            public void onClick(RavengardPlayer player) {
            }
        }.spawn(HypixelConst.getInstanceContainer());

        RavengardAnimationRegistry.spawnAll(HypixelConst.getInstanceContainer());
        net.swofty.type.ravengardgeneric.entity.NevermoreNoticeBoard.spawn(HypixelConst.getInstanceContainer());
        net.swofty.type.ravengardgeneric.music.RavengardMusic.start();
    }

    @Override
    public void afterInitialize(MinecraftServer server) {

    }

    @Override
    public List<ServiceType> getRequiredServices() {
        return List.of();
    }

    @Override
    public TablistManager getTablistManager() {
        return new TablistManager() {
            @Override
            public List<TablistModule> getModules() {
                return List.of(
                        new TablistModule() {
                            @Override
                            public List<TablistEntry> getEntries(HypixelPlayer player) {
                                return List.of();
                            }
                        }
                );
            }
        };
    }

    @Override
    public LoaderValues getLoaderValues() {
        return new LoaderValues(
                (_) -> new Pos(52.5, 66, 13.5, -180, 0),
                false
        );
    }

    @Override
    public List<HypixelEventClass> getTraditionalEvents() {
        return RavengardGenericLoader.loopThroughPackage(
                "net.swofty.type.ravengardlobby.events",
                HypixelEventClass.class
        ).collect(Collectors.toList());
    }

    @Override
    public List<HypixelEventClass> getCustomEvents() {
        return new ArrayList<>();
    }

    @Override
    public List<HypixelNPC> getNPCs() {
        return new ArrayList<>();
    }


    @Override
    public List<RedisMessageHandler<?, ?>> getProxyHandlers() {
        return List.of();
    }

    @Override
    public @Nullable CustomWorlds getMainInstance() {
        return CustomWorlds.RAVENGARD_LOBBY;
    }

    @Override
    public boolean headerFooterPerPlayer() {
        return true;
    }

    @Override
    public @NonNull Optional<Tuple<Text, Text>> headerFooter() {
        return Optional.of(
                new Tuple<>(
                        Text.of("{}", net.kyori.adventure.text.Component
                                .text("\uE120\uE121\uE122\uE123\uE124\uE125\n\uE102\n")
                                .font(net.swofty.type.ravengardgeneric.gui.RavengardFont.RAVENGARD)
                                .color(net.kyori.adventure.text.format.NamedTextColor.WHITE)),
                        Text.empty()));
    }
}
