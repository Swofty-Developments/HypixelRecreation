package net.swofty.type.generic.entity.hologram;

import lombok.Builder;
import lombok.Getter;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.swofty.commons.ServerType;
import net.swofty.type.generic.HypixelConst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public enum ServerHolograms {
    TO_ISLAND(ServerType.SKYBLOCK_HUB, new Pos(0.5, 71, -27.5), "<b>Travel to:", "<a>Your Island"),
    TO_DUNGEON_HUB(ServerType.SKYBLOCK_HUB, new Pos(-33.5, 75, 26.5), "<b>Travel to:", "<a>Dungeon Hub"),
    ;

    private static final Map<ExternalHologram, List<HologramEntity>> externalHolograms = new HashMap<>();

    private final ServerType serverType;
    private final Pos pos;
    private final String[] text;

    ServerHolograms(ServerType serverType, Pos pos, String... text) {
        this.serverType = serverType;
        this.pos = pos;
        this.text = text;
    }

    public static void spawnAll(Instance instance) {
        for (ServerHolograms hologram : values()) {
            if (hologram.serverType != HypixelConst.getTypeLoader().getType()) continue;

            // Calculate the starting Y position based on the text length
            double startY = hologram.text.length * 0.3 - 0.3;
            for (int i = 0; i < hologram.text.length; i++) {
                HologramEntity entity = new HologramEntity(hologram.text[i]);
                entity.setAutoViewable(true);
                entity.setInstance(instance, hologram.pos.add(0, startY - (i * 0.3), 0));
            }
        }
    }

    public static void removeExternalHologram(ExternalHologram hologram) {
        List<HologramEntity> entities = externalHolograms.get(hologram);
        if (entities == null) return;

        for (HologramEntity entity : entities) {
            entity.remove();
        }

        externalHolograms.remove(hologram);
    }

    public static void addExternalHologram(ExternalHologram hologram) {
        List<HologramEntity> entities = new ArrayList<>();
        double startY = hologram.text.length * 0.3 - 0.3;
        for (int i = 0; i < hologram.text.length; i++) {
            HologramEntity entity = new HologramEntity(hologram.text[i]);
            entity.setInstance(hologram.instance, hologram.pos.add(0, startY - (i * 0.3), 0));
            entity.setAutoViewable(true);
            entities.add(entity);
        }

        externalHolograms.put(hologram, entities);
    }

    public static void updateExternalHologramText(UUID uuid, String[] newText) {
        List<HologramEntity> entities = externalHolograms.entrySet()
            .stream()
            .filter(entry -> entry.getKey().getUuid().equals(uuid))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null);

        if (entities == null) return;

        ExternalHologram hologram = externalHolograms.keySet()
            .stream()
            .filter(k -> k.getUuid().equals(uuid))
            .findFirst()
            .orElse(null);

        if (hologram == null) return;

        if (entities.size() < newText.length) {
            double startY = newText.length * 0.3 - 0.3;
            for (int i = entities.size(); i < newText.length; i++) {
                HologramEntity entity = new HologramEntity(newText[i]);
                entity.setInstance(hologram.instance, hologram.pos.add(0, startY - (i * 0.3), 0));
                entity.setAutoViewable(true);
                entities.add(entity);
            }
            for (int i = 0; i < hologram.text.length; i++) {
                entities.get(i).teleport(hologram.pos.add(0, startY - (i * 0.3), 0));
            }
        }
        else if (entities.size() > newText.length) {
            for (int i = newText.length; i < entities.size(); i++) {
                entities.get(i).remove();
            }
            entities.subList(newText.length, entities.size()).clear();
        }

        for (int i = 0; i < newText.length; i++) {
            entities.get(i).setText(newText[i]);
        }
    }

    @Builder
    @Getter
    public static class ExternalHologram {
        @Builder.Default
        private UUID uuid = UUID.randomUUID();
        private final Instance instance;
        private final Pos pos;
        private final String[] text;
    }
}
