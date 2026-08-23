package net.swofty.type.replayviewer.entity;

import lombok.Getter;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.swofty.commons.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class ReplayTextDisplayEntity extends Entity {
    private final int recordedEntityId;
    private final UUID recordedUuid;
    private final String displayType;
    private final String displayIdentifier;
    private List<String> currentTextLines;

    public ReplayTextDisplayEntity(int recordedEntityId, UUID recordedUuid,
                                    List<String> initialTextLines,
                                    String displayType, String displayIdentifier) {
        super(EntityType.TEXT_DISPLAY);
        this.recordedEntityId = recordedEntityId;
        this.recordedUuid = recordedUuid;
        this.displayType = displayType != null ? displayType : "";
        this.displayIdentifier = displayIdentifier != null ? displayIdentifier : "";
        this.currentTextLines = initialTextLines != null ? new ArrayList<>(initialTextLines) : new ArrayList<>();

        // Configure entity
        setNoGravity(true);
        setGlowing(true);

        // Set initial text
        updateTextDisplay();
    }

    /**
     * Updates all text lines.
     *
     * @param newLines the new text lines to display
     */
    public void updateTextLines(List<String> newLines) {
        this.currentTextLines = newLines != null ? new ArrayList<>(newLines) : new ArrayList<>();
        updateTextDisplay();
    }

    /**
     * Updates specific lines starting at an index.
     *
     * @param newLines   the new lines to insert
     * @param startIndex the starting index
     */
    public void updateTextLines(List<String> newLines, int startIndex) {
        if (newLines == null || newLines.isEmpty()) return;

        // Ensure list is large enough
        while (currentTextLines.size() < startIndex + newLines.size()) {
            currentTextLines.add("");
        }

        // Update lines
        for (int i = 0; i < newLines.size(); i++) {
            currentTextLines.set(startIndex + i, newLines.get(i));
        }

        updateTextDisplay();
    }

    private void updateTextDisplay() {
        if (this.entityMeta instanceof TextDisplayMeta textMeta) {
            String combinedText = String.join("\n", currentTextLines);
            textMeta.setText(Text.read(combinedText).asComponent());
            textMeta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.CENTER);
            textMeta.setSeeThrough(true);
            textMeta.setBackgroundColor(0);
        }
    }

    @Override
    public void updateNewViewer(@NotNull Player player) {
        super.updateNewViewer(player);
        // Force metadata update for new viewers
        updateTextDisplay();
    }

    /**
     * Gets the current display text as a single string.
     *
     * @return the combined text
     */
    public String getCombinedText() {
        return String.join("\n", currentTextLines);
    }
}
