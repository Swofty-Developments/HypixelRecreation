package io.github.term4.polyp.platform.fixes.client;

import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.MetadataDef;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Which metadata entries are suppressed to the player they belong to (the self-echo "meta fix"): modern clients
 * predict state (sneak, sprint, pose, item use) locally, and the server echoing it back causes a one-tick stutter.
 * Viewers always get the full packet.
 *
 * @see io.github.term4.polyp.platform.player.OptimizedPlayer
 */
public final class SelfMetaFilter {

    private final Set<Integer> suppressedIndices = new HashSet<>();
    private final Map<Integer, Byte> suppressedBits = new HashMap<>();
    private boolean suppressAttributes = false;

    /** Suppresses an entire metadata index (e.g. {@link MetadataDef#POSE}) from the self-echo. */
    public SelfMetaFilter suppressIndex(MetadataDef.Entry<?> entry) {
        suppressedIndices.add(entry.index());
        return this;
    }

    /** Suppresses specific bits of a flag byte; other bits at the same index (on-fire, glowing) pass through. */
    public SelfMetaFilter suppressBit(MetadataDef.Entry.BitMask entry) {
        int idx = entry.index();
        byte existing = suppressedBits.getOrDefault(idx, (byte) 0);
        suppressedBits.put(idx, (byte) (existing | entry.bitMask()));
        return this;
    }

    /** Suppresses {@code EntityAttributesPacket} echoes (the movement-speed update on sprint start/stop). */
    public SelfMetaFilter suppressAttributes(boolean suppress) {
        this.suppressAttributes = suppress;
        return this;
    }

    public boolean suppressAttributes() {
        return suppressAttributes;
    }

    /** Filtered copy of {@code entries} with the configured data stripped, or {@code null} if nothing matched. */
    public Map<Integer, Metadata.Entry<?>> filter(Map<Integer, Metadata.Entry<?>> entries) {
        Map<Integer, Metadata.Entry<?>> result = null;
        boolean modified = false;

        for (var e : entries.entrySet()) {
            int idx = e.getKey();
            Byte suppressMask = suppressedBits.get(idx);
            if (suppressMask != null && e.getValue().value() instanceof Byte flags) {
                byte serverOnly = (byte) (flags & ~suppressMask);
                if (!modified) {
                    result = new HashMap<>(entries);
                    modified = true;
                }
                if (serverOnly != 0) {
                    result.put(idx, Metadata.Byte(serverOnly));
                } else {
                    result.remove(idx);
                }
            } else if (suppressedIndices.contains(idx)) {
                if (!modified) {
                    result = new HashMap<>(entries);
                    modified = true;
                }
                result.remove(idx);
            }
        }
        return result;
    }

    public static SelfMetaFilter defaultPlayerFilter() {
        return new SelfMetaFilter()
                .suppressBit((MetadataDef.Entry.BitMask) MetadataDef.IS_CROUCHING)
                .suppressBit((MetadataDef.Entry.BitMask) MetadataDef.IS_SPRINTING)
                .suppressIndex(MetadataDef.POSE)
                .suppressIndex(MetadataDef.LivingEntity.LIVING_ENTITY_FLAGS)
                .suppressAttributes(true);
    }
}
