package net.swofty.type.ravengardgeneric.hud;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.swofty.type.ravengardgeneric.gui.RavengardFont;
import org.tinylog.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class RavengardHudComposer {
    /** Dungeon servers plug live minimap icons in here; the packed colour on each
     * glyph carries its offset from the player, decoded by the pack's text shader. */
    public static volatile java.util.function.Function<RavengardHudState, Component> dungeonMinimapIcons;
    /** Dungeon servers plug the fullscreen tab-map composer in here; glyph colours
     * carry absolute positions on the 512px map canvas centred on the layout. */
    public static volatile java.util.function.Function<RavengardHudState, Component> dungeonTabMap;

    private static final Gson GSON = new Gson();
    private static final Map<String, String> TEMPLATES = new HashMap<>();

    static {
        try (InputStream stream = RavengardHudComposer.class.getResourceAsStream("/hud/lines.json")) {
            if (stream == null) {
                throw new IllegalStateException("Missing /hud/lines.json");
            }
            JsonObject root = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
            for (String team : root.keySet()) {
                TEMPLATES.put(team, GSON.toJson(root.get(team)));
            }
            Logger.info("Loaded {} Ravengard HUD line templates", TEMPLATES.size());
        } catch (Exception exception) {
            Logger.error(exception, "Failed to load Ravengard HUD line templates");
        }
    }

    private RavengardHudComposer() {
    }

    public static Map<RavengardHudLayer, Component> compose(RavengardHudState state) {
        Map<RavengardHudLayer, Component> layers = new EnumMap<>(RavengardHudLayer.class);
        for (RavengardHudLayer layer : RavengardHudLayer.values()) {
            if (layer == RavengardHudLayer.MINIMAP_TILES && state.isDungeon()) {
                // generated dungeons have no pre-rendered map image; the live room
                // icons on the marker line carry the map instead
                layers.put(layer, Component.empty());
                continue;
            }
            if (layer == RavengardHudLayer.STATS && state.isDungeon()) {
                layers.put(layer, dungeonStats(state));
                continue;
            }
            if ("team_5".equals(layer.getTeamName())) {
                layers.put(layer, spellSlots(state));
                continue;
            }
            String template = TEMPLATES.get(layer.getTeamName());
            if (template == null) {
                continue;
            }
            Component rendered = render(template, state);
            if (layer == RavengardHudLayer.PLAYER_MARKER && state.isDungeon()
                    && dungeonMinimapIcons != null) {
                Component icons = dungeonMinimapIcons.apply(state);
                if (icons != null) {
                    rendered = rendered.append(icons);
                }
            }
            layers.put(layer, rendered);
        }
        return layers;
    }

    private static Component dungeonStats(RavengardHudState state) {
        Component result = Component.empty();
        if (!state.isSpectating()) {
            result = result.append(doubledFull(Component.text(state.healthText(), TextColor.color(0xFEFD04))))
                    .append(doubledFull(Component.text(state.staminaText(), TextColor.color(0xFEFD06))));
        }
        result = result.append(tripledRuns(TextColor.color(0xFEFD08),
                new String[]{"\uE002", state.getClock() + "  "},
                new String[]{"\uE003", state.getKillCount() + "  "},
                new String[]{"\uE004", state.getPlayerCount() + "  "},
                new String[]{"\uE005", state.getTeamCount() + "  "},
                new String[]{"\uE00E", String.valueOf(state.getPortalCount())}));
        if (state.isSpectating()) {
            result = result.append(tripledRuns(TextColor.color(0xFEFD0A),
                    new String[]{"\uE00F", "SPECTATING"}));
        }
        Component date = Component.text(state.getDate() + " ", TextColor.color(0xFEFD10))
                .append(Component.text(state.getServerId(), TextColor.color(0xFEFD12)));
        result = result.append(date.font(RavengardFont.FULL))
                .append(date.font(RavengardFont.DEFAULT));
        // the captured dungeon hud carries keybind hints and the map/inventory
        // icon block after the date; the shader positions them under the
        // ability slots and hotbar from these exact runs
        result = result.append(keybindHint("key.advancements", 0xD0024))
                .append(keybindHint("key.drop", 0xD0105))
                .append(keybindHint("key.swapOffhand", 0xD0024))
                .append(keybindHint("key.playerlist", 0xCFEB3));
        result = result.append(tripledRuns(TextColor.color(0xFEFE00),
                new String[]{"\uE006" + new String(Character.toChars(0xD0142)) + "\uE007", ""}));
        return result;
    }

    private static Component doubledFull(Component component) {
        return component.font(RavengardFont.FULL)
                .append(component.font(RavengardFont.DEFAULT));
    }

    private static Component keybindHint(String key, int suffixCodepoint) {
        Component hint = Component.keybind(key).color(TextColor.color(0xFEFD18));
        return Component.empty()
                .append(hint.font(RavengardFont.HALF))
                .append(hint.font(RavengardFont.DEFAULT))
                .append(hint.font(RavengardFont.HALF))
                .append(Component.text(new String(Character.toChars(suffixCodepoint)))
                        .font(RavengardFont.RAVENGARD));
    }

    private static Component tripledRuns(TextColor color, String[]... runs) {
        Component half = Component.empty();
        Component visible = Component.empty();
        for (String[] run : runs) {
            half = half.append(Component.text(run[0] + run[1]));
            visible = visible.append(Component.text(run[0]).font(RavengardFont.RAVENGARD));
            if (!run[1].isEmpty()) {
                visible = visible.append(Component.text(run[1]).font(RavengardFont.DEFAULT));
            }
        }
        return Component.empty().color(color)
                .append(half.font(RavengardFont.HALF))
                .append(visible)
                .append(half.font(RavengardFont.HALF));
    }

    // structure taken from the captured team_5 line: per slot a spell background glyph whose tint
    // id addresses the slot, with the ability's ui/spell glyph as a child
    private static final int SPELL_BACKGROUND = 0xE020;
    private static final int SLOT_ONE_BACKGROUND = 0xFEFBFF;
    private static final int SLOT_ONE_ICON = 0xFEFB00;
    private static final int SLOT_TWO_BACKGROUND = 0xFEFCFF;
    private static final int SLOT_TWO_ICON = 0xFEFC00;

    private static Component spellSlots(RavengardHudState state) {
        if (state.getAbilityOne() == 0 && state.getAbilityTwo() == 0) {
            return Component.empty();
        }
        Component line = Component.empty();
        if (state.getAbilityOne() != 0) {
            line = line.append(Component.text(new String(Character.toChars(SPELL_BACKGROUND)))
                    .font(RavengardFont.RAVENGARD)
                    .color(net.kyori.adventure.text.format.TextColor.color(SLOT_ONE_BACKGROUND))
                    .append(Component.text(new String(Character.toChars(state.getAbilityOne())))
                            .color(net.kyori.adventure.text.format.TextColor.color(SLOT_ONE_ICON))));
        }
        if (state.getAbilityTwo() != 0) {
            line = line.append(Component.text(new String(Character.toChars(SPELL_BACKGROUND)))
                    .font(RavengardFont.RAVENGARD)
                    .color(net.kyori.adventure.text.format.TextColor.color(SLOT_TWO_BACKGROUND))
                    .append(Component.text(new String(Character.toChars(state.getAbilityTwo())))
                            .color(net.kyori.adventure.text.format.TextColor.color(SLOT_TWO_ICON))));
        }
        return line;
    }

    private static final double MAP_SCALE = 4.0;
    private static final double MAP_ORIGIN_X = 1931.56;
    private static final double MAP_ORIGIN_Z = 1894.40;
    private static final int MARKER_ICON = 255;
    private static final double YAW_PER_STEP = 360.0 / 64.0;

    public static int mapTint(RavengardHudState state) {
        int x = clamp12((int) Math.round(state.getWorldX() * MAP_SCALE + MAP_ORIGIN_X));
        int z = clamp12((int) Math.round(state.getWorldZ() * MAP_SCALE + MAP_ORIGIN_Z));
        return (x << 12) | z;
    }

    public static int markerTint(RavengardHudState state) {
        float yaw = state.getYaw();
        while (yaw < 0) {
            yaw += 360f;
        }
        int rotation = ((int) Math.round(yaw / YAW_PER_STEP)) & 0x3F;
        return (MARKER_ICON << 15) | (MARKER_ICON << 6) | rotation;
    }

    private static int clamp12(int value) {
        return Math.max(0, Math.min(0xFFF, value));
    }

    private static Component render(String template, RavengardHudState state) {
        String filled = template
                .replace("${health}", state.healthText())
                .replace("${stamina}", state.staminaText())
                .replace("${clock}", state.getClock())
                .replace("${location}", state.getLocation())
                .replace("${crowns}", String.valueOf(state.getCrowns()))
                .replace("${players}", String.valueOf(state.getPlayerCount()))
                .replace("${date}", state.getDate())
                .replace("${server}", state.getServerId())
                .replace("#860794", String.format("#%06X", mapTint(state)))
                .replace("#7FBFE0", String.format("#%06X", markerTint(state)));

        JsonElement tree = GSON.fromJson(filled, JsonElement.class);
        return GsonComponentSerializer.gson().deserializeFromTree(tree);
    }
}
