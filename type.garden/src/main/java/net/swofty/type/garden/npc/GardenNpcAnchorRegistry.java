package net.swofty.type.garden.npc;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.garden.config.GardenConfigRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.Optional;

public final class GardenNpcAnchorRegistry {
    private GardenNpcAnchorRegistry() {
    }

    public static Optional<NpcAnchor> getNpcAnchor(SkyBlockPlayer player, String npcId) {
        return getAnchor(player, "npcs", npcId);
    }

    public static Optional<NpcAnchor> getInteractionAnchor(SkyBlockPlayer player, String anchorId) {
        return getAnchor(player, "interactions", anchorId);
    }

    public static Optional<NpcAnchor> getVisitorSlotAnchor(SkyBlockPlayer player, int slot) {
        return getAnchor(player, "visitor_slots", "slot_" + slot);
    }

    private static Optional<NpcAnchor> getAnchor(SkyBlockPlayer player, String sectionName, String anchorId) {
        Map<String, Object> config = GardenConfigRegistry.getConfig("npc_anchors.yml");
        Map<String, Object> skins = GardenConfigRegistry.getSection(config, "skins");
        String selectedSkin = player == null ? "default" : net.swofty.type.garden.gui.GardenGuiSupport.core(player).getSelectedBarnSkin();

        Map<String, Object> selectedSkinSection = GardenConfigRegistry.getSection(skins, selectedSkin);
        Map<String, Object> selectedAnchors = GardenConfigRegistry.getSection(selectedSkinSection, sectionName);
        Map<String, Object> anchor = GardenConfigRegistry.getSection(selectedAnchors, anchorId);
        if (anchor.isEmpty()) {
            Map<String, Object> defaultSkinSection = GardenConfigRegistry.getSection(skins, "default");
            Map<String, Object> defaultAnchors = GardenConfigRegistry.getSection(defaultSkinSection, sectionName);
            anchor = GardenConfigRegistry.getSection(defaultAnchors, anchorId);
        }

        if (anchor.isEmpty() || !GardenConfigRegistry.getBoolean(anchor, "enabled", true)) {
            return Optional.empty();
        }

        return Optional.of(new NpcAnchor(new Pos(
            GardenConfigRegistry.getDouble(anchor, "x", 0D),
            GardenConfigRegistry.getDouble(anchor, "y", 0D),
            GardenConfigRegistry.getDouble(anchor, "z", 0D),
            (float) GardenConfigRegistry.getDouble(anchor, "yaw", 0D),
            (float) GardenConfigRegistry.getDouble(anchor, "pitch", 0D)
        ), GardenConfigRegistry.getDouble(anchor, "offsetY", 0D)));
    }

    public record NpcAnchor(Pos position, Double offsetY) {
    }
}
