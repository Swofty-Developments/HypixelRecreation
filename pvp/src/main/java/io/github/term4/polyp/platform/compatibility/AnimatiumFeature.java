package io.github.term4.polyp.platform.compatibility;

/**
 * A 1.8-behaviour feature an Animatium client applies natively; {@link #bit} = the bit index in the
 * {@code animatium:set_server_features} BitSet payload (wire contract - keep in sync with the mod). For features a
 * client applies, the matching server-side hack is skipped ({@link CompatAnimatium} maps them from {@link CompatConfig}).
 * Bits 0..9 are stock Animatium, 10+ need the fork; an unknown bit is harmless to a stock client (its reader skips it).
 */
public enum AnimatiumFeature {
    ALL(0),
    MISS_PENALTY(1),
    LEFT_CLICK_ITEM_USAGE(2),
    MINING_ITEM_USAGE(3),
    HIDE_ROD_BOBBER(4),
    PICK_INFLATION(5),
    OLD_SNEAK_HEIGHT(6),
    CLIENTSIDE_ENTITIES(7),
    FIX_SPRINT_ITEM_USE(8),
    FIX_SPRINT_SNEAKING(9),
    DISABLE_SWIM_POSE(10),
    DISABLE_CRAWL_POSE(11),
    DISABLE_ELYTRA_POSE(12),
    OLD_FLUID_PHYSICS(13),
    DISABLE_ELYTRA_FLIGHT(14),
    OLD_FLIGHT(15),
    DISABLE_OFFHAND(16),
    DISABLE_AUTO_SNEAK(17),
    OLD_MOMENTUM(18),
    DISABLE_ENTITY_PUSH(19),
    OLD_PLACEMENT(20),
    DISABLE_BED_BOUNCE(21),
    DISABLE_HONEY_PHYSICS(22),
    DISABLE_BUBBLE_COLUMN(23),
    SHORTS_VELOCITY(24);

    public final int bit;

    AnimatiumFeature(int bit) { this.bit = bit; }
}
