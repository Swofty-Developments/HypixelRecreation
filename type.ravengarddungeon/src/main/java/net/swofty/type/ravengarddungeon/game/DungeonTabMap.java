package net.swofty.type.ravengarddungeon.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.swofty.type.ravengardgeneric.gui.RavengardFont;
import net.swofty.type.ravengardgeneric.hud.RavengardHudComposer;
import net.swofty.type.ravengardgeneric.hud.RavengardHudState;

import java.util.UUID;

public final class DungeonTabMap {
    private static final int TILE = 13;
    private static final int MAP_CENTER = 256;
    private static final String PLAYER_MARKER = "";
    private static final String TEAMMATE_MARKER = "";
    private static final double YAW_PER_STEP = 360.0 / 64.0;

    private DungeonTabMap() {
    }

    public static void register() {
        RavengardHudComposer.dungeonTabMap = DungeonTabMap::render;
    }

    private static Component render(RavengardHudState state) {
        UUID uuid = state.getPlayerUuid();
        if (uuid == null) {
            return null;
        }
        DungeonInstanceRegistry.DungeonInstance dungeonInstance = null;
        for (DungeonInstanceRegistry.DungeonInstance candidate : DungeonInstanceRegistry.all()) {
            if (candidate.getPlayers().contains(uuid)) {
                dungeonInstance = candidate;
                break;
            }
        }
        if (dungeonInstance == null) {
            return null;
        }

        var placements = dungeonInstance.getGenerated().dungeon().getPlacements();
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (var placement : placements) {
            minX = Math.min(minX, placement.originX());
            maxX = Math.max(maxX, placement.originX() + placement.getFootprintWidth());
            minZ = Math.min(minZ, placement.originZ());
            maxZ = Math.max(maxZ, placement.originZ() + placement.getFootprintDepth());
        }
        double centerX = (minX + maxX) / 2, centerZ = (minZ + maxZ) / 2;

        Component header = Component.empty();
        for (int index = 0; index < placements.size(); index++) {
            var placement = placements.get(index);
            DungeonMapGlyphs.RoomArt art = DungeonMapGlyphs.artFor(
                    placement.room().getId(), placement.rotation().getDegrees());
            if (art == null) {
                continue;
            }
            for (int row = 0; row < art.rows(); row++) {
                for (int col = 0; col < art.cols(); col++) {
                    Integer codepoint = art.tiles().get(row).get(col);
                    if (codepoint == null) {
                        continue;
                    }
                    double tileCenterX = placement.originX() + col * TILE + TILE / 2.0;
                    double tileCenterZ = placement.originZ() + row * TILE + TILE / 2.0;
                    header = header.append(Component.text(Character.toString(codepoint))
                            .font(RavengardFont.RAVENGARD)
                            .color(TextColor.color(packed(tileCenterX, tileCenterZ, centerX, centerZ, 0)))
                            .shadowColor(ShadowColor.shadowColor(0)));
                }
            }
        }
        header = header.append(Component.newline());

        for (UUID memberId : dungeonInstance.getPlayers()) {
            Player member = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(memberId);
            if (member == null) {
                continue;
            }
            boolean self = memberId.equals(uuid);
            float yaw = member.getPosition().yaw();
            while (yaw < 0) {
                yaw += 360f;
            }
            int rotation = ((int) Math.round(yaw / YAW_PER_STEP)) & 0x3F;
            header = header.append(Component.text(self ? PLAYER_MARKER : TEAMMATE_MARKER)
                    .font(RavengardFont.RAVENGARD)
                    .color(TextColor.color(packed(member.getPosition().x(), member.getPosition().z(),
                            centerX, centerZ, rotation)))
                    .shadowColor(ShadowColor.shadowColor(0)));
        }
        return header.append(Component.newline());
    }

    private static int packed(double worldX, double worldZ, double centerX, double centerZ, int rotation) {
        int mapX = clamp9((int) Math.round(MAP_CENTER + (worldX - centerX)));
        int mapZ = clamp9((int) Math.round(MAP_CENTER + (worldZ - centerZ)));
        return (mapX << 15) | (mapZ << 6) | rotation;
    }

    private static int clamp9(int value) {
        return Math.clamp(value, 0, 0x1FF);
    }
}
