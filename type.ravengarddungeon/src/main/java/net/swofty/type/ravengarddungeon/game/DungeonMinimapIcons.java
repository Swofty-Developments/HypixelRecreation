package net.swofty.type.ravengarddungeon.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.TextColor;
import net.swofty.type.ravengardgeneric.gui.RavengardFont;
import net.swofty.type.ravengardgeneric.hud.RavengardHudComposer;
import net.swofty.type.ravengardgeneric.hud.RavengardHudState;

import java.util.UUID;

public final class DungeonMinimapIcons {
    /** The crypt segment tiles: with the city-base layout the world keeps the
     * blueprint's coordinates, so Hypixel's own map art lines up verbatim.
     * Each glyph's colour packs the player position that pans the mosaic. */
    private static final String SEGMENT_TILES = "";

    private DungeonMinimapIcons() {
    }

    public static void register() {
        RavengardHudComposer.dungeonMinimapIcons = DungeonMinimapIcons::render;
    }

    private static Component render(RavengardHudState state) {
        UUID uuid = state.getPlayerUuid();
        if (uuid == null) {
            return null;
        }
        boolean inDungeon = false;
        for (DungeonInstanceRegistry.DungeonInstance candidate : DungeonInstanceRegistry.all()) {
            if (candidate.getPlayers().contains(uuid)) {
                inDungeon = true;
                break;
            }
        }
        if (!inDungeon) {
            return null;
        }

        int packed = (clamp12((int) Math.round((state.getWorldX() + 512) * 4)) << 12)
                | clamp12((int) Math.round((state.getWorldZ() + 512) * 4));
        Component tiles = Component.empty();
        for (int index = 0; index < SEGMENT_TILES.length(); index++) {
            tiles = tiles.append(Component.text(SEGMENT_TILES.charAt(index))
                    .font(RavengardFont.RAVENGARD)
                    .color(TextColor.color(packed))
                    .shadowColor(ShadowColor.shadowColor(0)));
        }
        // the template's own marker draws before the tiles and ends up under
        // them, so a live marker rides after the map: centred, real yaw
        float yaw = state.getYaw();
        while (yaw < 0) {
            yaw += 360f;
        }
        int rotation = ((int) Math.round(yaw / (360.0 / 64.0))) & 0x3F;
        tiles = tiles.append(Component.text(PLAYER_MARKER)
                .font(RavengardFont.RAVENGARD)
                .color(TextColor.color((256 << 15) | (256 << 6) | rotation))
                .shadowColor(ShadowColor.shadowColor(0)));
        return tiles;
    }

    private static final String PLAYER_MARKER = "\uE102";

    private static int clamp12(int value) {
        return Math.clamp(value, 0, 0xFFF);
    }
}
