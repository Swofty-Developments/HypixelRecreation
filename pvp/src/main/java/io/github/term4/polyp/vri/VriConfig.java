package io.github.term4.polyp.vri;

import io.github.term4.polyp.entity.DroppedItemEntity;
import org.jetbrains.annotations.Nullable;

/** Toggles for the VRI (Vanilla Re-Implemented) behaviors, per scope via {@code MechanicsKeys.VRI}; the set grows as chests / deaths etc. land. */
public final class VriConfig {

    /** Default off. */
    public final boolean blockBreakProgress;
    /** {@code null} = off; compose custom loot with {@link BlockDrops#chain}. */
    public final @Nullable BlockDrops.DropRule blockDrops;
    /** {@code null} resolves {@code MechanicsKeys.ITEM_PHYSICS} from the profile (LEGACY fallback). */
    public final @Nullable DroppedItemEntity.Model itemPhysics;
    /** Default off. */
    public final boolean itemPickup;
    /** Q / drag-out. Default off. */
    public final boolean itemDrop;
    /** Fire parity on breaks: direct-break fizz + orphaned-fire removal. Default off. */
    public final boolean fireBreaks;

    private VriConfig(Builder b) {
        blockBreakProgress = b.blockBreakProgress;
        blockDrops = b.blockDrops;
        itemPhysics = b.itemPhysics;
        itemPickup = b.itemPickup;
        itemDrop = b.itemDrop;
        fireBreaks = b.fireBreaks;
    }

    public static Builder builder() { return new Builder(); }

    /** Everything on: {@link BlockDrops#VANILLA} drops, item physics from the profile. */
    public static VriConfig all() {
        return builder().blockBreakProgress(true)
                .blockDrops(BlockDrops.VANILLA).itemPickup(true).itemDrop(true).fireBreaks(true).build();
    }

    public static final class Builder {
        private boolean blockBreakProgress;
        private @Nullable BlockDrops.DropRule blockDrops;
        private @Nullable DroppedItemEntity.Model itemPhysics;
        private boolean itemPickup;
        private boolean itemDrop;
        private boolean fireBreaks;

        Builder() {}

        public Builder blockBreakProgress(boolean v) { blockBreakProgress = v; return this; }
        public Builder blockDrops(@Nullable BlockDrops.DropRule v) { blockDrops = v; return this; }
        public Builder itemPhysics(@Nullable DroppedItemEntity.Model v) { itemPhysics = v; return this; }
        public Builder itemPickup(boolean v) { itemPickup = v; return this; }
        public Builder itemDrop(boolean v) { itemDrop = v; return this; }
        public Builder fireBreaks(boolean v) { fireBreaks = v; return this; }

        public VriConfig build() { return new VriConfig(this); }
    }
}
