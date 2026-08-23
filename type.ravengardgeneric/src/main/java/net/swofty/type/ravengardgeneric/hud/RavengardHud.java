package net.swofty.type.ravengardgeneric.hud;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.scoreboard.Sidebar;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.ServerType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.user.HypixelPlayer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class RavengardHud {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yy");
    private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("mm:ss");

    private static final Map<UUID, RavengardHudState> STATES = new ConcurrentHashMap<>();
    private static final Map<UUID, Sidebar> SIDEBARS = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<RavengardHudLayer, Component>> LAST_SENT = new ConcurrentHashMap<>();

    private RavengardHud() {
    }

    public static RavengardHudState state(HypixelPlayer player) {
        return STATES.computeIfAbsent(player.getUuid(), ignored -> new RavengardHudState());
    }

    public static void start() {
        MinecraftServer.getSchedulerManager()
                .buildTask(RavengardHud::tick)
                .repeat(TaskSchedule.tick(1))
                .schedule();
    }

    public static void attach(HypixelPlayer player) {
        Sidebar sidebar = new Sidebar(RavengardSidebarTitle.frame(0));

        for (RavengardHudLayer layer : RavengardHudLayer.values()) {
            sidebar.createLine(new Sidebar.ScoreboardLine(
                    layer.name(),
                    Component.empty(),
                    layer.getScore(),
                    Sidebar.NumberFormat.blank()));
        }

        SIDEBARS.put(player.getUuid(), sidebar);
        LAST_SENT.put(player.getUuid(), new ConcurrentHashMap<>());
        sidebar.addViewer(player);
    }

    public static void detach(HypixelPlayer player) {
        Sidebar sidebar = SIDEBARS.remove(player.getUuid());
        if (sidebar != null) {
            sidebar.removeViewer(player);
        }
        STATES.remove(player.getUuid());
        LAST_SENT.remove(player.getUuid());
    }

    private static final AtomicInteger TITLE_FRAME = new AtomicInteger();

    private static void tick() {
        TITLE_FRAME.incrementAndGet();
        for (HypixelPlayer player : HypixelGenericLoader.getLoadedPlayers()) {
            Sidebar sidebar = SIDEBARS.get(player.getUuid());
            Map<RavengardHudLayer, Component> previous = LAST_SENT.get(player.getUuid());
            if (sidebar == null || previous == null) {
                continue;
            }

            RavengardHudState state = state(player);
            refresh(player, state);
            sidebar.setTitle(RavengardSidebarTitle.frame(TITLE_FRAME.get()));

            sendTabMap(player, state);
            hideTabList(player);

            Map<RavengardHudLayer, Component> composed = RavengardHudComposer.compose(state);
            for (Map.Entry<RavengardHudLayer, Component> entry : composed.entrySet()) {
                if (entry.getValue().equals(previous.get(entry.getKey()))) {
                    continue;
                }
                previous.put(entry.getKey(), entry.getValue());
                sidebar.updateLineContent(entry.getKey().name(), entry.getValue());
            }
        }

        SIDEBARS.keySet().removeIf(uuid ->
                MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid) == null);
        STATES.keySet().removeIf(uuid ->
                MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid) == null);
        LAST_SENT.keySet().removeIf(uuid ->
                MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid) == null);
        LAST_TAB_HEADER.keySet().removeIf(uuid ->
                MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid) == null);
    }

    private static final String FULLSCREEN_TILES = "\uE120\uE121\uE122\uE123\uE124\uE125";
    private static final String DUNGEON_TILES = "\uE110\uE111\uE112\uE113\uE114\uE115";
    private static final String PLAYER_MARKER = "\uE102";
    // map pixels equal world coordinates on the crypt canvas (verified against
    // a live hypixel capture), so the packed value is world + canvas centre
    private static final double TAB_MAP_ORIGIN_X = 256.0;
    private static final double TAB_MAP_ORIGIN_Z = 256.0;
    private static final double YAW_PER_STEP = 360.0 / 64.0;

    private static final Map<java.util.UUID, Component> LAST_TAB_HEADER = new ConcurrentHashMap<>();

    private static void sendTabMap(HypixelPlayer player, RavengardHudState state) {
        if (state.isDungeon() && RavengardHudComposer.dungeonTabMap != null) {
            Component dungeonHeader = RavengardHudComposer.dungeonTabMap.apply(state);
            if (dungeonHeader != null) {
                if (!dungeonHeader.equals(LAST_TAB_HEADER.put(player.getUuid(), dungeonHeader))) {
                    player.sendPlayerListHeaderAndFooter(dungeonHeader, Component.empty());
                }
                return;
            }
        }
        int iconX = clamp9((int) Math.round(state.getWorldX() + TAB_MAP_ORIGIN_X));
        int iconY = clamp9((int) Math.round(state.getWorldZ() + TAB_MAP_ORIGIN_Z));

        float yaw = state.getYaw();
        while (yaw < 0) {
            yaw += 360f;
        }
        int rotation = ((int) Math.round(yaw / YAW_PER_STEP)) & 0x3F;
        int tint = (iconX << 15) | (iconY << 6) | rotation;

        Component header = Component.empty()
                .append(Component.text(state.isDungeon() ? DUNGEON_TILES : FULLSCREEN_TILES)
                        .font(net.swofty.type.ravengardgeneric.gui.RavengardFont.RAVENGARD)
                        .color(net.kyori.adventure.text.format.NamedTextColor.WHITE)
                        .shadowColor(net.kyori.adventure.text.format.ShadowColor.shadowColor(0)))
                .append(Component.newline())
                .append(Component.text(PLAYER_MARKER)
                        .font(net.swofty.type.ravengardgeneric.gui.RavengardFont.RAVENGARD)
                        .color(net.kyori.adventure.text.format.TextColor.color(tint))
                        .shadowColor(net.kyori.adventure.text.format.ShadowColor.shadowColor(0)))
                .append(Component.newline());

        player.sendPlayerListHeaderAndFooter(header, Component.empty());
    }

    private static void hideTabList(HypixelPlayer player) {
        for (var online : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            player.sendPacket(new net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket(
                    net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket.Action.UPDATE_LISTED,
                    new net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket.Entry(
                            online.getUuid(), online.getUsername(), java.util.List.of(),
                            false, 0, net.minestom.server.entity.GameMode.SURVIVAL,
                            Component.empty(), null, 0, false)));
        }
    }

    private static int clamp9(int value) {
        return Math.clamp(value, 0, 0x1FF);
    }

    private static void refresh(HypixelPlayer player, RavengardHudState state) {
        boolean dungeon = HypixelConst.getTypeLoader().getType() == ServerType.RAVENGARD_DUNGEON;
        state.setDungeon(dungeon);
        float fraction = player.getAttributeValue(Attribute.MAX_HEALTH) <= 0
                ? 1f
                : player.getHealth() / (float) player.getAttributeValue(Attribute.MAX_HEALTH);
        state.setHealth(Math.round(state.getMaxHealth() * fraction));

        // the pack redraws the vanilla exp bar as the stamina bar, so its fill has to be pushed there
        float staminaFraction = state.getMaxStamina() <= 0
                ? 0f
                : state.getStamina() / (float) state.getMaxStamina();
        staminaFraction = Math.clamp(staminaFraction, 0f, 1f);
        if (player.getExp() != staminaFraction) {
            player.setExp(staminaFraction);
        }
        if (player.getLevel() != 0) {
            player.setLevel(0);
        }

        int abilityOne = 0;
        int abilityTwo = 0;
        if (player instanceof net.swofty.type.ravengardgeneric.user.RavengardPlayer ravengardPlayer
                && !ravengardPlayer.isTutorial()) {
            net.swofty.type.ravengardgeneric.classes.RavengardClass playerClass =
                    ravengardPlayer.getRavengardClass();
            if (playerClass != null) {
                var abilities = playerClass.defaultAbilities();
                if (!abilities.isEmpty()) abilityOne = abilities.getFirst().getHudCodePoint();
                if (abilities.size() > 1) abilityTwo = abilities.get(1).getHudCodePoint();
            }
        }
        state.setAbilityOne(abilityOne);
        state.setAbilityTwo(abilityTwo);

        if (player instanceof net.swofty.type.ravengardgeneric.user.RavengardPlayer ravengardPlayer) {
            state.setCrowns(ravengardPlayer.getCrowns());
            net.swofty.type.ravengardgeneric.region.RavengardRegion region =
                    ravengardPlayer.getRegion();
            if (!dungeon) {
                state.setLocation(region != null ? region.getType().getDisplayName() : "Ravenport");
            }
        }

        state.setPlayerUuid(player.getUuid());
        state.setWorldX(player.getPosition().x());
        state.setWorldZ(player.getPosition().z());
        state.setYaw(player.getPosition().yaw());
        if (!dungeon) {
            state.setClock(LocalTime.now().format(CLOCK_FORMAT));
            state.setPlayerCount(MinecraftServer.getConnectionManager().getOnlinePlayers().size());
        }
        state.setDate(LocalDate.now().format(DATE_FORMAT));
        if (state.getServerId().isEmpty()) {
            state.setServerId(HypixelConst.getShortenedServerName());
        }
    }
}
