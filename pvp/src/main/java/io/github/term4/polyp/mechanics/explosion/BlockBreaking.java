package io.github.term4.polyp.mechanics.explosion;

import io.github.term4.polyp.mechanics.explosion.ExplosionConfigResolver.ExplosionContext;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * What an explosion does to blocks. Absent from an {@link ExplosionConfig} = it breaks none.
 *
 * <p>{@link Resistance} and {@link BreakRule} both receive the {@link ExplosionContext}, whose
 * {@code source()} is the exploding entity - so one config can break different blocks per source (a BedWars
 * fireball eating wood but not end stone, while TNT in the same world eats both).
 */
public final class BlockBreaking {

    /** How the destroyed set is chosen. Both rays are vanilla's 16³ shell; they differ only in what resists. */
    public enum Model {
        /** 1.8: resistance is the block's alone, and a ray never leaves the world. */
        RAY_1_8,
        /** 26.1: {@code max(block, fluid)} resistance, and a ray stops at the world border. */
        RAY_MODERN,
        /** Every breakable block within {@code power}; no ray, no shadowing. Cheap enough to spam. */
        SPHERE
    }

    /** How a block's {@link Builder#charge} meets the ray. */
    public enum Charging {
        /** Vanilla: subtracted every 0.3 sample inside the block (~3.33x per block traversed). */
        PER_STEP,
        /** Subtracted once, on entering the block (MineMen TNT; ~3.33x deeper reach at equal resistance). */
        PER_BLOCK,
        /** A gate, not a cost: a block whose charge exceeds the ray's remaining intensity STOPS it (shielding whatever
         *  is behind); anything weaker breaks and the ray flies on, spending only distance decay (MineMen fireball). */
        THRESHOLD
    }

    /**
     * How a {@link Builder#neverBreaks blast-proof} block protects what is behind it - a second dimension over
     * {@link Model}. Rays pass THROUGH such a block (it keeps its natural resistance); this only decides whether it
     * also casts a shadow.
     */
    public enum Shielding {
        /** Vanilla: nothing casts a shadow. A ray reaches whatever it reaches - straight through a blast-proof block
         *  just the same - and every cell it selects breaks. */
        NONE,
        /** Hypixel (capture-verified): a blast-proof block casts a HARD shadow. A selected cell is dropped if the
         *  straight line from the blast centre to its centre crosses one - the blast never wraps a glass corner. */
        OCCLUSION
    }

    /**
     * What happens to a selected block - vanilla's {@code Explosion.BlockInteraction} plus the no-drops case it
     * expresses per-block. Selection runs either way, so {@link #KEEP} is how you get FIRE without destruction.
     */
    public enum Interaction {
        /** Selected but left standing. */
        KEEP,
        /** Destroyed, drops nothing. */
        DESTROY_NO_DROPS,
        /** Destroyed; each item survives with probability {@code 1/power} (vanilla TNT). */
        DESTROY_WITH_DECAY,
        /** Destroyed with full drops. */
        DESTROY_WITH_DROPS;

        boolean destroys() { return this != KEEP; }
    }

    /** Blast resistance of {@code block}, in vanilla units (stone 6, obsidian 1200). {@code Double.POSITIVE_INFINITY} = never breaks. */
    @FunctionalInterface
    public interface Resistance {
        double of(@NotNull Block block, @NotNull ExplosionContext ctx);
    }

    /**
     * Final say once the ray has already reached {@code pos} with power to spare. It does NOT affect propagation -
     * a block spared here still shadows whatever is behind it. Use {@link Builder#onlyBreaks}/{@link Builder#neverBreaks}
     * to change what a blast can punch THROUGH.
     */
    @FunctionalInterface
    public interface BreakRule {
        boolean canBreak(@NotNull Block block, @NotNull Point pos, @NotNull ExplosionContext ctx);
    }

    /** Registry blast resistance - the modern value, correct for every block that still exists. */
    public static final Resistance VANILLA_RESISTANCE = (block, ctx) -> block.registry().explosionResistance();

    // the only blocks whose blast resistance actually changed since 1.8 (148 of 155 match - see
    // docs/HANDOFF-explosion-block-breaking.md). moving_piston is not a typo: 1.8's c(-1.0F) never raises
    // durability, leaving it unbreakable by tools yet free to explosions
    private static final double[] LEGACY_OVERRIDES = overrides(Map.of(
            Block.PISTON, 0.5, Block.STICKY_PISTON, 0.5,
            Block.PISTON_HEAD, 0.5, Block.MOVING_PISTON, 0.0));

    /** 1.8 blast resistance: {@link #VANILLA_RESISTANCE} plus the piston overrides. */
    public static final Resistance LEGACY_RESISTANCE = (block, ctx) -> {
        int id = block.id();
        double override = id < LEGACY_OVERRIDES.length ? LEGACY_OVERRIDES[id] : Double.NaN;
        return Double.isNaN(override) ? block.registry().explosionResistance() : override;
    };

    /** Block-id indexed, {@code NaN} = none. Every sampled cell probes this, so it must not hash a key string. */
    private static double[] overrides(Map<Block, Double> byBlock) {
        double[] out = new double[byBlock.keySet().stream().mapToInt(Block::id).max().orElse(0) + 1];
        Arrays.fill(out, Double.NaN);
        byBlock.forEach((block, value) -> out[block.id()] = value);
        return out;
    }

    /** Membership by block TYPE id (all states), the same reason: an array probe, not a key hash. */
    private static boolean[] idMask(Set<Block> blocks) {
        boolean[] mask = new boolean[blocks.stream().mapToInt(Block::id).max().orElse(-1) + 1];
        for (Block block : blocks) mask[block.id()] = true;
        return mask;
    }

    private static boolean masked(boolean[] mask, Block block) {
        int id = block.id();
        return id < mask.length && mask[id];
    }

    private static final BreakRule ANY = (block, pos, ctx) -> true;

    private final Model model;
    private final Interaction interaction;
    private final Resistance resistance;
    private final BreakRule breakRule;
    private final Charging charging;
    private final Shielding shielding;
    private final boolean[] shadowCasters;
    private final int rayGrid;
    private final java.util.function.DoubleUnaryOperator charge;
    private final double rollMin, rollMax;
    private final boolean rollPerHeading;
    private final float[] intensityTable;
    private final float tableMax;
    private final double originLift;
    private final double intensityNoise;

    private BlockBreaking(Builder b) {
        this.model = b.model;
        this.interaction = b.interaction;
        this.resistance = b.resistance;
        this.breakRule = b.breakRule;
        this.charging = b.charging;
        this.shielding = b.shielding;
        this.shadowCasters = b.shadowCasters;
        this.rayGrid = b.rayGrid;
        this.charge = b.charge;
        this.rollMin = b.rollMin;
        this.rollMax = b.rollMax;
        this.rollPerHeading = b.rollPerHeading;
        this.intensityTable = b.intensityTable;
        this.originLift = b.originLift;
        this.intensityNoise = b.intensityNoise;
        float max = 0;
        if (intensityTable != null) {
            int shell = rayGrid * rayGrid * rayGrid - (rayGrid - 2) * (rayGrid - 2) * (rayGrid - 2);
            if (intensityTable.length != shell)
                throw new IllegalArgumentException("intensityTable length " + intensityTable.length
                        + " != " + shell + " rays of a " + rayGrid + " grid");
            for (float v : intensityTable) max = Math.max(max, v);
        }
        this.tableMax = max;
    }

    public @NotNull Model model() { return model; }
    public @NotNull Interaction interaction() { return interaction; }
    @NotNull Charging charging() { return charging; }
    @NotNull Shielding shielding() { return shielding; }
    int rayGrid() { return rayGrid; }
    double charge(double resistance) { return charge.applyAsDouble(resistance); }
    /** One ray's intensity: {@code power} x a uniform roll in {@code [rollMin, rollMax]}. */
    double rollIntensity(float power, java.util.concurrent.ThreadLocalRandom rnd) {
        return power * (rollMin == rollMax ? rollMin : rollMin + rnd.nextDouble() * (rollMax - rollMin));
    }
    boolean rollPerHeading() { return rollPerHeading; }
    float @Nullable [] intensityTable() { return intensityTable; }
    double originLift() { return originLift; }
    double intensityNoise() { return intensityNoise; }
    /** Hottest possible launch intensity - bounds any reach-derived scan (seals). */
    double maxIntensity(float power) {
        return (intensityTable != null ? tableMax : power * rollMax) + intensityNoise;
    }

    double resistance(@NotNull Block block, @NotNull ExplosionContext ctx) { return resistance.of(block, ctx); }

    /** A {@link Builder#neverBreaks blast-proof} block - one that casts the {@link Shielding#OCCLUSION} shadow. */
    boolean castsShadow(@NotNull Block block) { return masked(shadowCasters, block); }

    boolean canBreak(@NotNull Block block, @NotNull Point pos, @NotNull ExplosionContext ctx) {
        return breakRule.canBreak(block, pos, ctx);
    }

    /** The block's own item, 1x; blocks with no item form drop nothing. Loot beyond this is the app's {@link BreakRule} job. */
    static @NotNull List<ItemStack> dropsOf(@NotNull Block block) {
        Material material = Material.fromKey(block.key());
        return material == null ? List.of() : List.of(ItemStack.of(material));
    }

    public static @NotNull Builder builder() { return new Builder(); }

    /** Layers onto an existing policy - a preset extending another's (Hypixel's blast-proof glass over the 1.8 baseline). */
    public @NotNull Builder toBuilder() { return new Builder(this); }

    public static final class Builder {
        private Model model = Model.RAY_MODERN;
        private Interaction interaction = Interaction.DESTROY_WITH_DECAY;
        private Resistance resistance = VANILLA_RESISTANCE;
        private BreakRule breakRule = ANY;
        private Charging charging = Charging.PER_STEP;
        private Shielding shielding = Shielding.NONE;
        private boolean[] shadowCasters = new boolean[0];
        private int rayGrid = 16;
        private java.util.function.DoubleUnaryOperator charge = r -> (r + 0.3) * 0.3;
        private double rollMin = 0.7, rollMax = 1.3;
        private boolean rollPerHeading;
        private float[] intensityTable;
        private double originLift;
        private double intensityNoise;

        private Builder() {}

        private Builder(BlockBreaking c) {
            model = c.model;
            interaction = c.interaction;
            resistance = c.resistance;
            breakRule = c.breakRule;
            charging = c.charging;
            shielding = c.shielding;
            shadowCasters = c.shadowCasters;
            rayGrid = c.rayGrid;
            charge = c.charge;
            rollMin = c.rollMin;
            rollMax = c.rollMax;
            rollPerHeading = c.rollPerHeading;
            intensityTable = c.intensityTable;
            originLift = c.originLift;
            intensityNoise = c.intensityNoise;
        }

        public Builder model(@NotNull Model v) { this.model = v; return this; }
        public Builder interaction(@NotNull Interaction v) { this.interaction = v; return this; }
        public Builder resistance(@NotNull Resistance v) { this.resistance = v; return this; }

        /** When a ray pays resistance; default {@link Charging#PER_STEP} (vanilla). */
        public Builder charging(@NotNull Charging v) { this.charging = v; return this; }

        /** Ray lattice edge; only the shell is cast. Vanilla 16 (1352 rays); MineMen 8 (296 - sparser rim, softer edge). */
        public Builder rayGrid(int v) { this.rayGrid = v; return this; }

        /** Intensity a ray pays for (or must beat, under {@link Charging#THRESHOLD}) a block, from its resistance;
         *  default vanilla {@code (r+0.3)*0.3} (MineMen TNT {@code r*0.0775}; its fireball the fitted gate law). */
        public Builder charge(@NotNull java.util.function.DoubleUnaryOperator v) { this.charge = v; return this; }

        /** Per-ray intensity roll bounds (x power); vanilla {@code 0.7, 1.3} (default), equal bounds = deterministic. */
        public Builder intensityRoll(double min, double max) { this.rollMin = min; this.rollMax = max; return this; }

        /** One roll per horizontal heading instead of per ray (the vertical fan moves together) - a coherent rim
         *  wiggle instead of per-ray salt-and-pepper (MineMen fireball). */
        public Builder rollPerHeading(boolean v) { this.rollPerHeading = v; return this; }

        /** Frozen per-ray launch intensities in lattice order (x-y-z shell walk), replacing power x roll entirely:
         *  the same blast at the same sub-block phase breaks the same cells. Length must match the {@link #rayGrid} shell. */
        public Builder intensityTable(float @Nullable [] v) { this.intensityTable = v; return this; }

        /** Raises the ray origin above the blast centre for BLOCK selection only (KB/damage/packet unaffected);
         *  MineMen fireball 0.25 - their footprints shrink faster with standoff than the flat-origin geometry. */
        public Builder originLift(double v) { this.originLift = v; return this; }

        /** Per-shot, per-ray uniform {@code [-v, v]} added to the launch intensity - with an {@link #intensityTable},
         *  near-threshold rim cells flicker shot to shot while the core repeats (MineMen fireball). */
        public Builder intensityNoise(double v) { this.intensityNoise = v; return this; }

        /** How unbreakable blocks shield what is behind them; default {@link Shielding#NONE} (vanilla). */
        public Builder shielding(@NotNull Shielding v) { this.shielding = v; return this; }

        /** Replaces the rule; compose with {@code &&} yourself when you want several. */
        public Builder breakRule(@NotNull BreakRule v) { this.breakRule = v; return this; }

        /**
         * Only these break, whatever their own resistance - the minigame shape (BedWars wool/wood). Matched by type,
         * any state. Implemented as RESISTANCE, not a veto: listed blocks offer none (so a blast tunnels through a
         * whitelisted wall) and everything else is infinite (so it shields, exactly like obsidian in BedWars).
         */
        public Builder onlyBreaks(@NotNull Set<Block> blocks) {
            boolean[] mask = idMask(blocks);
            return resistance((block, ctx) -> masked(mask, block) ? 0.0 : Double.POSITIVE_INFINITY);
        }

        /**
         * These never break, and under {@link Shielding#OCCLUSION} cast a HARD shadow over whatever is behind them
         * (Hypixel's blast-proof glass). They keep their natural resistance, so a ray still passes THROUGH them - the
         * shadow, not ray-blocking, is what shields. A block that should also STOP rays needs an ∞ {@link #resistance}.
         */
        public Builder neverBreaks(@NotNull Set<Block> blocks) {
            boolean[] mask = idMask(blocks);
            this.shadowCasters = mask;
            BreakRule base = this.breakRule;
            this.breakRule = (block, pos, ctx) -> !masked(mask, block) && base.canBreak(block, pos, ctx);
            return this;
        }

        public @NotNull BlockBreaking build() { return new BlockBreaking(this); }
    }
}
