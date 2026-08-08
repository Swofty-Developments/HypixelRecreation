package io.github.term4.polyp.mechanics.explosion;

import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.config.FieldValue;
import io.github.term4.polyp.presets.vanilla18.Vanilla18;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Vanilla's ray is verified in {@code docs/HANDOFF-explosion-block-breaking.md}; these pin the behaviours a preset
 * depends on - resistance actually gating, the 1.8 piston override, and per-SOURCE rules (a BedWars fireball eating
 * wood but not end stone while TNT in the same world eats both).
 */
class BlockBreakingTest extends HeadlessServerTest {

    private static final BlockVec CENTER = new BlockVec(600, 64, 600);

    /** Built per case rather than installed: with no scoped profile, resolve() falls back to this install config. */
    private static ExplosionSystem system(BlockBreaking breaking) {
        return new ExplosionSystem(polyp, ExplosionConfig.builder(Vanilla18.explosion())
                .blockBreaking(breaking).build());
    }

    private static Instance world() {
        return flatInstance(MechanicsProfile.builder().build());
    }

    private static void fill(Instance inst, Block block) {
        for (int dx = -2; dx <= 2; dx++)
            for (int dz = -2; dz <= 2; dz++)
                inst.setBlock(CENTER.blockX() + dx, CENTER.blockY(), CENTER.blockZ() + dz, block);
    }

    private static long remaining(Instance inst, Block block) {
        long n = 0;
        for (int dx = -2; dx <= 2; dx++)
            for (int dz = -2; dz <= 2; dz++)
                if (inst.getBlock(CENTER.blockX() + dx, CENTER.blockY(), CENTER.blockZ() + dz).compare(block)) n++;
        return n;
    }

    private static void detonate(ExplosionSystem sys, Instance inst, float power) {
        sys.explode(inst, new Pos(CENTER.blockX() + 0.5, CENTER.blockY() + 1.5, CENTER.blockZ() + 0.5), power);
    }

    @Test
    void dirtBreaksAndObsidianSurvivesTheSameBlast() {
        Instance soft = world();
        fill(soft, Block.DIRT);
        detonate(system(BlockBreaking.builder().model(BlockBreaking.Model.RAY_1_8)
                .interaction(BlockBreaking.Interaction.DESTROY_NO_DROPS).build()), soft, 4.0f);
        assertEquals(0, remaining(soft, Block.DIRT), "a power-4 blast clears dirt (resistance 0.5)");

        Instance hard = world();
        fill(hard, Block.OBSIDIAN);
        detonate(system(BlockBreaking.builder().model(BlockBreaking.Model.RAY_1_8)
                .interaction(BlockBreaking.Interaction.DESTROY_NO_DROPS).build()), hard, 4.0f);
        assertEquals(25, remaining(hard, Block.OBSIDIAN), "obsidian (1200) outlasts it entirely");
    }

    private static long remainingUnder(Instance inst, Block block) {
        long n = 0;
        for (int dx = -2; dx <= 2; dx++)
            for (int dz = -2; dz <= 2; dz++)
                if (inst.getBlock(CENTER.blockX() + dx, CENTER.blockY() - 1, CENTER.blockZ() + dz).compare(block)) n++;
        return n;
    }

    private static void plateUnder(Instance inst, Block block) {
        for (int dx = -2; dx <= 2; dx++)
            for (int dz = -2; dz <= 2; dz++)
                inst.setBlock(CENTER.blockX() + dx, CENTER.blockY() - 1, CENTER.blockZ() + dz, block);
    }

    /**
     * Hypixel blast-proofs glass (measured): it never breaks and, under OCCLUSION, SHADOWS what is behind it. The
     * shadow is a break veto plus a centre-line test, NOT ray-blocking - vanilla18 (ordinary glass) eats straight through.
     */
    @Test
    void hypixelGlassSurvivesAndShadowsWhatIsBehindIt() {
        Instance shielded = world();
        fill(shielded, Block.GLASS);
        plateUnder(shielded, Block.DIRT);
        detonate(new ExplosionSystem(polyp, io.github.term4.polyp.presets.hypixel.Explosion.config()), shielded, 4.0f);
        assertEquals(25, remaining(shielded, Block.GLASS), "glass is blast-proof on Hypixel");
        assertEquals(25, remainingUnder(shielded, Block.DIRT), "and shadows the dirt behind it");

        Instance vanilla = world();
        fill(vanilla, Block.GLASS);
        plateUnder(vanilla, Block.DIRT);
        detonate(new ExplosionSystem(polyp, Vanilla18.explosion()), vanilla, 4.0f);
        assertEquals(0, remaining(vanilla, Block.GLASS), "vanilla glass (0.3) is ordinary");
        assertTrue(remainingUnder(vanilla, Block.DIRT) < 25, "and the blast reaches the dirt through it");
    }

    /** Dirt at CENTER boxed on all 6 faces by {@code shell}, TNT fired from the diagonal corner. Returns whether the
     *  dirt survived. Only OCCLUSION over a shadow-casting shell (glass) spares it; without occlusion the ray reaches
     *  the dirt anyway - through the passable glass, or tunnelling an obsidian corner. */
    private static boolean boxedSurvives(Block shell, BlockBreaking.Shielding mode) {
        Instance inst = world();
        inst.setBlock(CENTER, Block.DIRT);
        for (int[] d : new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}}) {
            inst.setBlock(CENTER.blockX() + d[0], CENTER.blockY() + d[1], CENTER.blockZ() + d[2], shell);
        }
        system(BlockBreaking.builder().model(BlockBreaking.Model.RAY_1_8).neverBreaks(Set.of(Block.GLASS))
                .shielding(mode).interaction(BlockBreaking.Interaction.DESTROY_NO_DROPS).build())
                .explode(inst, new Pos(CENTER.blockX() + 1.5, CENTER.blockY() + 1.5, CENTER.blockZ() + 1.5), 4.0f);
        return inst.getBlock(CENTER).compare(Block.DIRT);
    }

    /**
     * The capture-measured Hypixel case: only {@link BlockBreaking.Shielding#OCCLUSION}'s hard shadow saves the boxed
     * dirt. Without it the ray reaches the dirt regardless - through the passable glass, or tunnelling an obsidian corner.
     */
    @Test
    void occlusionShadowSavesTheBoxedDirtButVanillaTunnelsIn() {
        assertTrue(boxedSurvives(Block.GLASS, BlockBreaking.Shielding.OCCLUSION), "the centre->dirt line crosses glass, so it is shadowed");
        assertFalse(boxedSurvives(Block.GLASS, BlockBreaking.Shielding.NONE), "without occlusion the ray passes through the glass: the dirt breaks");
        assertFalse(boxedSurvives(Block.OBSIDIAN, BlockBreaking.Shielding.OCCLUSION), "obsidian casts no shadow, so the corner ray still tunnels in");
    }

    /** The shadow is a geometric line-of-sight test, not inferred from where random-intensity rays landed, so the box
     *  is protected on EVERY detonation - not just the ones the RNG spared by luck. */
    @Test
    void occlusionShadowIsDeterministicAcrossDetonations() {
        for (int i = 0; i < 25; i++) assertTrue(boxedSurvives(Block.GLASS, BlockBreaking.Shielding.OCCLUSION), "detonation " + i + " stayed shadowed");
    }

    /** Dirt one cell behind a single glass block, TNT axially in line beyond it (no diagonal = no corner-clip): the
     *  ONLY path to the dirt is straight through the glass. */
    private static boolean dirtBehindGlassSurvives(BlockBreaking.Shielding mode) {
        Instance inst = world();
        inst.setBlock(CENTER, Block.DIRT);
        inst.setBlock(CENTER.blockX(), CENTER.blockY(), CENTER.blockZ() + 1, Block.GLASS);
        system(BlockBreaking.builder().model(BlockBreaking.Model.RAY_1_8).neverBreaks(Set.of(Block.GLASS))
                .shielding(mode).interaction(BlockBreaking.Interaction.DESTROY_NO_DROPS).build())
                .explode(inst, new Pos(CENTER.blockX() + 0.5, CENTER.blockY() + 0.5, CENTER.blockZ() + 2.5), 4.0f);
        return inst.getBlock(CENTER).compare(Block.DIRT);
    }

    /**
     * The capture-verified crux (docs/HANDOFF): Hypixel's rays are 100% vanilla, so glass is PASSABLE - a block right
     * behind glass is still reached and broken. Only OCCLUSION's centre-line spares it. The earlier model made glass
     * ∞-resistant, which wrongly stopped the ray and spared the block even with no occlusion; this pins the difference.
     */
    @Test
    void glassIsPassableToRaysButShadowsOnlyUnderOcclusion() {
        assertFalse(dirtBehindGlassSurvives(BlockBreaking.Shielding.NONE), "a vanilla ray passes through glass and breaks the block behind it");
        assertTrue(dirtBehindGlassSurvives(BlockBreaking.Shielding.OCCLUSION), "the centre->block line crosses that glass, so occlusion spares it");
    }

    /** Dirt diagonally past the shared edge of an obsidian and a glass block, so the centre->dirt line threads that
     *  edge. Returns whether the dirt broke, for a blast jittered {@code (jx,jz)} off the block centre. */
    private static boolean grazeTargetBreaks(double jx, double jz) {
        Instance inst = world();
        int bx = CENTER.blockX(), by = CENTER.blockY(), bz = CENTER.blockZ();
        inst.setBlock(bx - 1, by, bz, Block.OBSIDIAN);
        inst.setBlock(bx, by, bz - 1, Block.GLASS);
        inst.setBlock(bx - 1, by, bz - 1, Block.DIRT);
        system(BlockBreaking.builder().model(BlockBreaking.Model.RAY_1_8).neverBreaks(Set.of(Block.GLASS))
                .shielding(BlockBreaking.Shielding.OCCLUSION).interaction(BlockBreaking.Interaction.DESTROY_NO_DROPS).build())
                .explode(inst, new Pos(bx + 0.5 + jx, by + 0.5, bz + 0.5 + jz), 4.0f);
        return inst.getBlock(bx - 1, by, bz - 1).isAir();
    }

    /**
     * The hypixel3 structure case (capture-verified): when the centre->target line threads the shared edge of a glass
     * and an obsidian block, Hypixel resolves the graze to the NON-glass side - the block breaks - and does not
     * coin-flip it on sub-block jitter in the blast centre. Only a jitter big enough to genuinely swing the line into
     * the glass shadows it.
     */
    @Test
    void occlusionResolvesGlassObsidianEdgeGrazeRobustly() {
        for (double[] j : new double[][]{{0, 0}, {2e-4, 0}, {0, 2e-4}, {3e-4, -3e-4}})
            assertTrue(grazeTargetBreaks(j[0], j[1]), "edge graze resolves to the obsidian side, jitter=" + j[0] + "," + j[1]);
        assertFalse(grazeTargetBreaks(0, -0.06), "a line swung well into the glass is genuinely shadowed");
    }

    /** Keyed, so stained and tinted variants ride along with plain glass - but FULL blocks only, never the panes. */
    @Test
    void hypixelBlastProofingCoversFullGlassOnly() {
        for (Block glass : Set.of(Block.GLASS, Block.WHITE_STAINED_GLASS, Block.TINTED_GLASS)) {
            Instance inst = world();
            fill(inst, glass);
            detonate(new ExplosionSystem(polyp, io.github.term4.polyp.presets.hypixel.Explosion.config()), inst, 4.0f);
            assertEquals(25, remaining(inst, glass), glass.key() + " must be blast-proof");
        }
        for (Block pane : Set.of(Block.GLASS_PANE, Block.RED_STAINED_GLASS_PANE)) {
            Instance inst = world();
            fill(inst, pane);
            detonate(new ExplosionSystem(polyp, io.github.term4.polyp.presets.hypixel.Explosion.config()), inst, 4.0f);
            assertEquals(0, remaining(inst, pane), pane.key() + " is ordinary - panes are not blast-proof");
        }
    }

    /** No policy on the config = the blast is entity-only, which is the pre-existing behaviour. */
    @Test
    void withoutAPolicyNothingBreaks() {
        Instance inst = world();
        fill(inst, Block.DIRT);
        detonate(new ExplosionSystem(polyp, ExplosionConfig.builder().build()), inst, 4.0f);
        assertEquals(25, remaining(inst, Block.DIRT), "no blockBreaking -> blocks untouched");
    }

    /**
     * The requirement: one world, two exploders, different blocks break. The rule reads {@code ctx.source()}, so a
     * fireball spares end stone while TNT levels it - no second config, no second explosion system.
     */
    @Test
    void breakRulesSeeTheExplosionSource() {
        BlockBreaking perSource = BlockBreaking.builder()
                .model(BlockBreaking.Model.RAY_1_8)
                .interaction(BlockBreaking.Interaction.DESTROY_NO_DROPS)
                .breakRule((block, pos, ctx) -> {
                    boolean fireball = ctx.source() != null && ctx.source().getEntityType() == EntityType.FIREBALL;
                    return !fireball || !block.compare(Block.END_STONE);
                })
                .build();

        Instance inst = world();
        ExplosionSystem sys = system(perSource);
        Entity fireball = new Entity(EntityType.FIREBALL);
        fireball.setInstance(inst, new Pos(CENTER.blockX() + 0.5, CENTER.blockY() + 1.5, CENTER.blockZ() + 0.5)).join();
        Entity tnt = new Entity(EntityType.TNT);
        tnt.setInstance(inst, new Pos(CENTER.blockX() + 0.5, CENTER.blockY() + 1.5, CENTER.blockZ() + 0.5)).join();
        try {
            fill(inst, Block.END_STONE);
            sys.explode(inst, fireball.getPosition(), 4.0f, fireball);
            assertEquals(25, remaining(inst, Block.END_STONE), "the fireball spares end stone");

            fill(inst, Block.OAK_PLANKS);
            sys.explode(inst, fireball.getPosition(), 4.0f, fireball);
            assertEquals(0, remaining(inst, Block.OAK_PLANKS), "but still eats wood");

            fill(inst, Block.END_STONE);
            sys.explode(inst, tnt.getPosition(), 4.0f, tnt);
            assertTrue(remaining(inst, Block.END_STONE) < 25, "TNT in the SAME world does break it");
        } finally {
            fireball.remove();
            tnt.remove();
        }
    }

    /** {@code onlyBreaks} is the minigame shape: a whitelist beats resistance in both directions. */
    @Test
    void onlyBreaksIgnoresResistanceEntirely() {
        Instance inst = world();
        fill(inst, Block.OBSIDIAN);
        detonate(system(BlockBreaking.builder().model(BlockBreaking.Model.RAY_1_8)
                .interaction(BlockBreaking.Interaction.DESTROY_NO_DROPS).onlyBreaks(Set.of(Block.OBSIDIAN)).build()), inst, 4.0f);
        assertEquals(0, remaining(inst, Block.OBSIDIAN), "whitelisting drops obsidian's resistance to zero");

        Instance spared = world();
        fill(spared, Block.DIRT);
        detonate(system(BlockBreaking.builder().model(BlockBreaking.Model.RAY_1_8)
                .interaction(BlockBreaking.Interaction.DESTROY_NO_DROPS).onlyBreaks(Set.of(Block.OBSIDIAN)).build()), spared, 4.0f);
        assertEquals(25, remaining(spared, Block.DIRT), "and dirt survives because it is not on the list");
    }

    private static long fires(Instance inst) {
        long n = 0;
        for (int dx = -3; dx <= 3; dx++)
            for (int dy = 0; dy <= 3; dy++)
                for (int dz = -3; dz <= 3; dz++)
                    if (inst.getBlock(CENTER.blockX() + dx, CENTER.blockY() + dy, CENTER.blockZ() + dz).compare(Block.FIRE)) n++;
        return n;
    }

    /** Vanilla's incendiary pass: 1-in-3 over the SELECTED cells that are air over something solid. */
    @Test
    void fireLightsOverTheSelectedSet() {
        boolean lit = false;
        for (int attempt = 0; attempt < 8 && !lit; attempt++) { // 1-in-3 roll per cell
            Instance inst = world();
            fill(inst, Block.STONE);
            ExplosionSystem sys = new ExplosionSystem(polyp, ExplosionConfig.builder(Vanilla18.explosion())
                    .fire(true)
                    .blockBreaking(BlockBreaking.builder().model(BlockBreaking.Model.RAY_1_8)
                            .interaction(BlockBreaking.Interaction.KEEP).build())
                    .build());
            detonate(sys, inst, 4.0f);
            assertEquals(25, remaining(inst, Block.STONE), "KEEP selects without destroying");
            lit = fires(inst) > 0;
        }
        assertTrue(lit, "and fire still lands on the selected air cells - vanilla lights fire under KEEP");
    }

    /** {@code fire} is a FieldValue, so incendiary-ness follows the source exactly like the ghast/TNT split. */
    @Test
    void incendiaryFollowsTheSource() {
        Instance inst = world();
        ExplosionSystem sys = new ExplosionSystem(polyp, ExplosionConfig.builder(Vanilla18.explosion())
                .fire(FieldValue.of(ctx -> ctx.source() != null && ctx.source().getEntityType() == EntityType.FIREBALL))
                .blockBreaking(BlockBreaking.builder().model(BlockBreaking.Model.RAY_1_8)
                        .interaction(BlockBreaking.Interaction.KEEP).build())
                .build());
        Entity fireball = new Entity(EntityType.FIREBALL);
        Entity tnt = new Entity(EntityType.TNT);
        Pos at = new Pos(CENTER.blockX() + 0.5, CENTER.blockY() + 1.5, CENTER.blockZ() + 0.5);
        fireball.setInstance(inst, at).join();
        tnt.setInstance(inst, at).join();
        try {
            fill(inst, Block.STONE);
            sys.explode(inst, at, 4.0f, tnt);
            assertEquals(0, fires(inst), "TNT is not incendiary");

            sys.explode(inst, at, 4.0f, fireball);
            assertTrue(fires(inst) > 0, "the fireball is - same config, same world");
        } finally {
            fireball.remove();
            tnt.remove();
        }
    }

    /**
     * Every 1.8-derived preset inherits vanilla18's block breaking + incendiary. Guards the builder COPY-CTOR:
     * a hand-written object member left out of it silently resets to default, which is how this shipped broken once.
     */
    @Test
    void legacyPresetsInheritVanilla18BlockBreaking() {
        var ctx = ExplosionConfigResolver.ExplosionContext.of(instance, CENTER, null, services);
        assertNotNull(Vanilla18.explosion().blockBreaking, "vanilla18 itself declares one");
        record Preset(String name, ExplosionConfig config) {}
        for (Preset preset : java.util.List.of(
                new Preset("hypixel", io.github.term4.polyp.presets.hypixel.Explosion.config()),
                new Preset("mmc18", io.github.term4.polyp.presets.mmc18.Explosion.config()),
                new Preset("mmc18 fireball-fight", io.github.term4.polyp.presets.mmc18.Explosion.fireballFight()))) {
            BlockBreaking b = FieldValue.resolve(preset.config().blockBreaking, ctx);
            assertNotNull(b, preset.name() + " inherits vanilla18's block breaking");
            assertEquals(BlockBreaking.Model.RAY_1_8, b.model(), preset.name() + " keeps the 1.8 ray for a sourceless blast");
        }
        // both keep the raw 1.8 table (mmc18's scaling lives in the charge, not the resistance); only mmc18 drops nothing
        BlockBreaking hy = FieldValue.resolve(io.github.term4.polyp.presets.hypixel.Explosion.config().blockBreaking, ctx);
        BlockBreaking polyp = FieldValue.resolve(io.github.term4.polyp.presets.mmc18.Explosion.config().blockBreaking, ctx);
        assertEquals(0.5, hy.resistance(Block.PISTON, ctx), 1e-9);
        assertEquals(BlockBreaking.Interaction.DESTROY_WITH_DECAY, hy.interaction());
        assertEquals(0.5, polyp.resistance(Block.PISTON, ctx), 1e-9);
        assertEquals(BlockBreaking.Interaction.DESTROY_NO_DROPS, polyp.interaction());
        // fireScope must survive builder(base) WITHOUT being re-set - the copy-ctor omission guard (bit twice before)
        ExplosionConfig base = ExplosionConfig.builder().fireScope(ExplosionConfig.FireScope.BROKEN).build();
        ExplosionConfig derived = ExplosionConfig.builder(base).power(5.0).build();
        assertEquals(ExplosionConfig.FireScope.BROKEN, derived.fireScope, "builder(base) carries fireScope");
        assertEquals(ExplosionConfig.FireScope.BROKEN, base.toBuilder().build().fireScope, "toBuilder carries fireScope");
    }

    /**
     * The regression: NOTHING set {@code fire} until 2026-07-21, so no explosion was ever incendiary. Asserts the
     * preset makes it SOURCE-dependent rather than a constant - the live fireball path is covered by
     * {@link #incendiaryFollowsTheSource} (building a real FireballEntity needs a snapshot + type config).
     */
    @Test
    void presetsMakeIncendiarySourceDependent() {
        // mmc18 is deliberately NOT here - its fireballs never ignite (a CONSTANT false, not source-dependent)
        for (ExplosionConfig cfg : java.util.List.of(Vanilla18.explosion(),
                io.github.term4.polyp.presets.vanilla.Explosion.config(),
                io.github.term4.polyp.presets.hypixel.Explosion.config())) {
            assertNotNull(cfg.fire, "the preset sets fire at all");
            assertNull(cfg.fire.constantOrNull(), "and makes it depend on the source, not a hardcoded flag");
            var tntCtx = ExplosionConfigResolver.ExplosionContext.of(instance, CENTER, null, services);
            assertFalse(cfg.fire.resolve(tntCtx), "a non-fireball source is never incendiary");
        }
    }

    /**
     * Hypixel fireballs light fire ONLY in cells a block was broken in - never on intact ground. A blast over a flat
     * unbreakable plate breaks nothing, so it lights nothing (the vanilla SELECTED scope WOULD light the air above it).
     */
    @Test
    void brokenScopeLightsNothingOverAnUnbreakablePlate() {
        // one plate of obsidian (never breaks), fire ON, source above it
        var breaking = BlockBreaking.builder().model(BlockBreaking.Model.RAY_1_8)
                .interaction(BlockBreaking.Interaction.DESTROY_NO_DROPS).build();

        Instance vanilla = world();
        fill(vanilla, Block.OBSIDIAN);
        detonateFire(vanilla, breaking, ExplosionConfig.FireScope.SELECTED);
        assertTrue(fires(vanilla) > 0, "vanilla scope lights the air the blast reached above the plate");

        Instance hypixel = world();
        fill(hypixel, Block.OBSIDIAN);
        detonateFire(hypixel, breaking, ExplosionConfig.FireScope.BROKEN);
        assertEquals(0, fires(hypixel), "BROKEN scope: nothing broke, so nothing burns");
    }

    /** BROKEN scope lights only vacated cells: a breakable floor DOES catch, because those cells were broken. */
    @Test
    void brokenScopeLightsWhereBlocksBroke() {
        // the incendiary pass rolls 1-in-3 per cell, so a single blast can legitimately light nothing
        boolean lit = false;
        for (int attempt = 0; attempt < 8 && !lit; attempt++) {
            Instance inst = world();
            fill(inst, Block.DIRT); // breaks under a power-4 blast
            detonateFire(inst, BlockBreaking.builder().model(BlockBreaking.Model.RAY_1_8)
                    .interaction(BlockBreaking.Interaction.DESTROY_NO_DROPS).build(), ExplosionConfig.FireScope.BROKEN);
            lit = fires(inst) > 0;
        }
        assertTrue(lit, "the broken dirt cells are lit");
    }

    private void detonateFire(Instance inst, BlockBreaking breaking, ExplosionConfig.FireScope scope) {
        ExplosionSystem sys = new ExplosionSystem(polyp, ExplosionConfig.builder(Vanilla18.explosion())
                .fire(true).fireScope(scope).blockBreaking(breaking).build());
        // above the plate so rays clear the surface and the incendiary pass has candidates
        sys.explode(inst, new Pos(CENTER.blockX() + 0.5, CENTER.blockY() + 1.5, CENTER.blockZ() + 0.5), 4.0f);
    }

    /** MineMen fireballs never ignite - both the normal and Fireball-Fight explosion configs must resolve fire=false. */
    @Test
    void mmc18FireballsAreNeverIncendiary() {
        // a CONSTANT false overriding vanilla18's fireball predicate - so no source (fireball or not) ever ignites
        assertEquals(Boolean.FALSE, io.github.term4.polyp.presets.mmc18.Explosion.config().fire.constantOrNull(),
                "mmc18 normal explosion never ignites");
        assertEquals(Boolean.FALSE, io.github.term4.polyp.presets.mmc18.Explosion.fireballFight().fire.constantOrNull(),
                "mmc18 Fireball-Fight never ignites either");
        // guard against a global regression: vanilla18 keeps its SOURCE-dependent predicate (not overridden to a constant)
        assertNull(Vanilla18.explosion().fire.constantOrNull(), "vanilla18 keeps the fireball incendiary predicate");
    }

    /** The 1.8 piston override - the one blast-resistance value that genuinely changed. */
    @Test
    void legacyResistanceOverridesPistons() {
        var ctx = ExplosionConfigResolver.ExplosionContext.of(instance, CENTER, null, services);
        assertEquals(0.5, BlockBreaking.LEGACY_RESISTANCE.of(Block.PISTON, ctx), 1e-9);
        assertEquals(1.5, BlockBreaking.VANILLA_RESISTANCE.of(Block.PISTON, ctx), 1e-9);
        assertEquals(0.0, BlockBreaking.LEGACY_RESISTANCE.of(Block.MOVING_PISTON, ctx), 1e-9,
                "1.8 c(-1.0F) never raised durability: unbreakable by tools, free to explosions");
        // everything else agrees, so the override table stays tiny
        assertEquals(BlockBreaking.VANILLA_RESISTANCE.of(Block.STONE, ctx),
                BlockBreaking.LEGACY_RESISTANCE.of(Block.STONE, ctx), 1e-9);
        assertFalse(Double.isNaN(BlockBreaking.VANILLA_RESISTANCE.of(Block.OBSIDIAN, ctx)));
    }
}
