package net.swofty.type.replayviewer.playback.display;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.swofty.type.generic.entity.FloatingBlockEntity;
import net.swofty.type.replayviewer.entity.ReplayTextDisplayEntity;
import net.swofty.type.replayviewer.playback.ReplaySession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DynamicTextManager {

    private final ReplaySession session;
    private final InstanceContainer instance;
    private final Map<Integer, ReplayTextDisplayEntity> displayEntities = new HashMap<>();
    private final Map<Integer, DynamicTextSource> textSources = new HashMap<>();
    private final Map<Integer, Integer> lastUpdateTicks = new HashMap<>();
    private final Map<Integer, FloatingBlockEntity> blockDisplays = new HashMap<>();

    public DynamicTextManager(ReplaySession session) {
        this.session = session;
        this.instance = session.getInstance();
    }

    /**
     * Creates a new dynamic text display entity.
     *
     * @param entityId    the entity ID
     * @param uuid        the entity UUID
     * @param position    the position
     * @param initialText the initial text lines
     * @param displayType the display type
     * @param identifier  the unique identifier
     */
    public void createDisplay(int entityId, UUID uuid, Pos position,
                               List<String> initialText, String displayType, String identifier) {
        createDisplay(entityId, uuid, position, initialText, displayType, identifier, 0);
    }

    public void createDisplay(int entityId, UUID uuid, Pos position,
                               List<String> initialText, String displayType, String identifier, int startTick) {
        // Remove existing if present
        removeDisplay(entityId);

        // Create entity
        ReplayTextDisplayEntity entity = new ReplayTextDisplayEntity(
            entityId, uuid, initialText, displayType, identifier
        );
        entity.setInstance(instance, position);
        displayEntities.put(entityId, entity);

        if ("generator".equals(displayType)) {
            Block block = identifier.toUpperCase().startsWith("DIAMOND") ? Block.DIAMOND_BLOCK : Block.EMERALD_BLOCK;
            blockDisplays.put(entityId, new FloatingBlockEntity(block, 0.6f, instance, position.add(0, -0.95, 0)));
        }

        // Create a recorded source so replay text matches recorded updates
        RecordedTextSource source = new RecordedTextSource(identifier, displayType, initialText, startTick);
        textSources.put(entityId, source);

        lastUpdateTicks.put(entityId, startTick);
    }

    /**
     * Updates a display's text.
     *
     * @param entityId     the entity ID
     * @param newTextLines the new text lines
     * @param replaceAll   whether to replace all lines
     * @param startIndex   the starting index for partial updates
     */
    public void updateDisplayText(int entityId, List<String> newTextLines,
                                   boolean replaceAll, int startIndex) {
        updateDisplayText(entityId, newTextLines, replaceAll, startIndex, session.getCurrentTick());
    }

    public void updateDisplayText(int entityId, List<String> newTextLines,
                                   boolean replaceAll, int startIndex, int tick) {
        ReplayTextDisplayEntity entity = displayEntities.get(entityId);
        if (entity == null) return;

        if (replaceAll) {
            entity.updateTextLines(newTextLines);
        } else {
            entity.updateTextLines(newTextLines, startIndex);
        }

        DynamicTextSource source = textSources.get(entityId);
        if (source instanceof RecordedTextSource recordedSource) {
            recordedSource.recordUpdate(tick, newTextLines, replaceAll, startIndex);
        }
    }

    /**
     * Updates all displays for the current tick.
     * Called each tick during playback.
     *
     * @param currentTick the current replay tick
     */
    public void tick(int currentTick) {
        for (Map.Entry<Integer, DynamicTextSource> entry : textSources.entrySet()) {
            int entityId = entry.getKey();
            DynamicTextSource source = entry.getValue();
            int lastTick = lastUpdateTicks.getOrDefault(entityId, 0);

            if (source.hasChangedSince(lastTick, currentTick)) {
                ReplayTextDisplayEntity entity = displayEntities.get(entityId);
                if (entity != null) {
                    entity.updateTextLines(source.getTextAt(currentTick));
                    lastUpdateTicks.put(entityId, currentTick);
                }
            }
        }
    }

    /**
     * Removes a display entity.
     *
     * @param entityId the entity ID to remove
     */
    public void removeDisplay(int entityId) {
        ReplayTextDisplayEntity entity = displayEntities.remove(entityId);
        if (entity != null) {
            entity.remove();
        }
        textSources.remove(entityId);
        lastUpdateTicks.remove(entityId);
        FloatingBlockEntity blockDisplay = blockDisplays.remove(entityId);
        if (blockDisplay != null) blockDisplay.remove();
    }

    /**
     * Gets a display entity by ID.
     *
     * @param entityId the entity ID
     * @return the display entity, or null
     */
    public ReplayTextDisplayEntity getDisplay(int entityId) {
        return displayEntities.get(entityId);
    }

    /**
     * Gets a text source by entity ID.
     *
     * @param entityId the entity ID
     * @return the text source, or null
     */
    public DynamicTextSource getTextSource(int entityId) {
        return textSources.get(entityId);
    }

    /**
     * Updates the position of a display entity.
     *
     * @param entityId the entity ID
     * @param position the new position
     */
    public void updateDisplayPosition(int entityId, Pos position) {
        ReplayTextDisplayEntity entity = displayEntities.get(entityId);
        if (entity != null) {
            entity.teleport(position);
        }
    }

    /**
     * Registers a custom text source for an entity.
     * Use this for game-mode specific text sources.
     *
     * @param entityId the entity ID
     * @param source   the text source
     */
    public void registerTextSource(int entityId, DynamicTextSource source) {
        textSources.put(entityId, source);
    }

    /**
     * Cleans up all display entities.
     */
    public void cleanup() {
        for (ReplayTextDisplayEntity entity : displayEntities.values()) {
            entity.remove();
        }
        displayEntities.clear();
        for (FloatingBlockEntity entity : blockDisplays.values()) entity.remove();
        blockDisplays.clear();
        textSources.clear();
        lastUpdateTicks.clear();
    }

}
