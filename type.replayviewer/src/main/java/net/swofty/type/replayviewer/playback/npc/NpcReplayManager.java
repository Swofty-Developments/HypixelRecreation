package net.swofty.type.replayviewer.playback.npc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.InstanceContainer;
import net.swofty.type.generic.entity.hologram.ServerHolograms;
import net.swofty.type.replayviewer.entity.ReplayEntityManager;
import net.swofty.type.replayviewer.playback.ReplaySession;

import java.util.*;

public class NpcReplayManager {

    private final InstanceContainer instance;
    private final ReplayEntityManager entityManager;
    private final Map<Integer, NpcReplayData> npcData = new HashMap<>();
    private final Map<Integer, ServerHolograms.ExternalHologram> holograms = new HashMap<>();
    private final Map<Integer, Integer> remainingDurations = new HashMap<>();

    public NpcReplayManager(ReplaySession session) {
        this.instance = session.getInstance();
        this.entityManager = session.getEntityManager();
    }

    /**
     * Registers an entity as an NPC.
     *
     * @param entityId the entity ID
     */
    public void registerNpc(int entityId) {
        if (!npcData.containsKey(entityId)) {
            npcData.put(entityId, new NpcReplayData(entityId));
        }
    }

    /**
     * Gets the NPC data for an entity.
     *
     * @param entityId the entity ID
     * @return the NPC data, or null if not an NPC
     */
    public NpcReplayData getNpcData(int entityId) {
        return npcData.get(entityId);
    }

    /**
     * Updates the display name for an NPC.
     *
     * @param entityId    the entity ID
     * @param displayName the display name
     * @param prefix      the prefix
     * @param suffix      the suffix
     * @param nameColor   the name color
     * @param visible     whether the name is visible
     */
    public void updateNpcDisplayName(int entityId, String displayName,
                                      String prefix, String suffix,
                                      int nameColor, boolean visible) {
        NpcReplayData data = npcData.computeIfAbsent(entityId, NpcReplayData::new);
        data.updateDisplayName(displayName, prefix, suffix, nameColor, visible);

        // Apply to entity
        Entity entity = entityManager.getEntity(entityId);
        if (entity != null) {
            if (visible) {
                String fullName = data.getFullDisplayName();
                Component nameComponent = LegacyComponentSerializer.legacySection().deserialize(fullName);
                if (nameColor >= 0) {
                    nameComponent = nameComponent.color(TextColor.color(nameColor));
                }
                entity.set(DataComponents.CUSTOM_NAME, nameComponent);
                entity.setCustomNameVisible(true);
            } else {
                entity.setCustomNameVisible(false);
            }
        }
    }

    /**
     * Updates text lines for an NPC.
     *
     * @param entityId     the entity ID
     * @param textLines    the text lines
     * @param yOffset      the vertical offset
     * @param durationTicks the display duration (0 = indefinite)
     */
    public void updateNpcTextLines(int entityId, List<String> textLines,
                                    double yOffset, int durationTicks) {
        NpcReplayData data = npcData.computeIfAbsent(entityId, NpcReplayData::new);
        data.updateTextLines(textLines, yOffset, durationTicks);

        // Update or create text entity
        if (textLines == null || textLines.isEmpty()) {
            removeTextEntity(entityId);
        } else {
            updateOrCreateTextEntity(entityId, data);
        }
    }

    private void updateOrCreateTextEntity(int entityId, NpcReplayData data) {
        removeTextEntity(entityId);
        Entity npcEntity = entityManager.getEntity(entityId);
        if (npcEntity == null) return;

        ServerHolograms.ExternalHologram hologram = ServerHolograms.ExternalHologram.builder()
                .instance(instance)
                .pos(npcEntity.getPosition().add(0, data.getTextYOffset(), 0))
                .text(data.getTextLines().toArray(String[]::new))
                .build();
        ServerHolograms.addExternalHologram(hologram);
        holograms.put(entityId, hologram);
        if (data.getTextDisplayDurationTicks() > 0) {
            remainingDurations.put(entityId, data.getTextDisplayDurationTicks());
        }
    }

    private void removeTextEntity(int entityId) {
        ServerHolograms.ExternalHologram hologram = holograms.remove(entityId);
        if (hologram != null) ServerHolograms.removeExternalHologram(hologram);
        remainingDurations.remove(entityId);
        NpcReplayData data = npcData.get(entityId);
        if (data != null) {
            data.setTextEntityId(-1);
        }
    }

    /**
     * Updates text entity positions based on NPC movement.
     * Call this when NPCs move.
     *
     * @param entityId the entity ID
     * @param newPos   the new NPC position
     */
    public void updateNpcPosition(int entityId, Pos newPos) {
        NpcReplayData data = npcData.get(entityId);
        if (holograms.containsKey(entityId) && data != null) updateOrCreateTextEntity(entityId, data);
    }

    /**
     * Ticks all NPC text entities.
     * Removes expired text entities.
     */
    public void tick() {
        Set<Integer> expired = new HashSet<>();
        remainingDurations.replaceAll((entityId, remaining) -> {
            if (remaining <= 1) expired.add(entityId);
            return remaining - 1;
        });
        expired.forEach(this::removeTextEntity);
    }

    /**
     * Removes an NPC and its associated text.
     *
     * @param entityId the entity ID
     */
    public void removeNpc(int entityId) {
        removeTextEntity(entityId);
        npcData.remove(entityId);
    }

    /**
     * Gets all registered NPC entity IDs.
     *
     * @return the set of NPC entity IDs
     */
    public Set<Integer> getNpcEntityIds() {
        return new HashSet<>(npcData.keySet());
    }

    /**
     * Cleans up all NPC data and text entities.
     */
    public void cleanup() {
        for (ServerHolograms.ExternalHologram hologram : holograms.values()) {
            ServerHolograms.removeExternalHologram(hologram);
        }
        holograms.clear();
        remainingDurations.clear();
        npcData.clear();
    }
}
