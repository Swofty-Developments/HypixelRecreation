package io.github.term4.polyp.mechanics.projectile.types;

import io.github.term4.polyp.codegen.GenerateBuilder;
import io.github.term4.polyp.config.TypeConfig;
import io.github.term4.polyp.config.FieldValue;
import io.github.term4.polyp.mechanics.damage.types.DamageType;
import io.github.term4.polyp.mechanics.knockback.KnockbackConfig;
import io.github.term4.polyp.mechanics.projectile.ProjectileBehavior;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfig;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfigResolver.ProjectileContext;
import net.kyori.adventure.key.Key;
import net.minestom.server.collision.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Per-type projectile config, keyed by {@link #key()}: every value is a {@link FieldValue} resolved against a
 * {@link ProjectileContext} (constant or per-launch lambda), unset fields falling back per-type override -&gt;
 * {@link ProjectileConfig#defaults()} -&gt; the type's {@code defaultConfig()} -&gt; hard fallbacks. Launchers resolve it
 * once per launch and stamp the entity. Covers spawn/physics, hit knockback + damage, deflect, removal, behavior.
 */
@GenerateBuilder
public final class ProjectileTypeConfig extends TypeConfig<ProjectileContext, ProjectileTypeConfig> {

    /**
     * Where a hit's knockback originates (orthogonal to the {@link KnockbackConfig}'s {@code yawWeight}):
     * {@link #PROJECTILE} = origin the projectile, basis its flight (vanilla); {@link #SHOOTER} = origin the shooter,
     * basis its facing (carries the shooter as source like melee, so {@code yawWeight} picks aim vs direction).
     */
    public enum KnockbackSource { PROJECTILE, SHOOTER }

    /**
     * How a projectile responds to a hit it doesn't deal normally ({@link #selfHit}, or {@link InvulnResponse} when a hit
     * is rejected as invulnerable):
     * <ul>
     *   <li>{@link #HIT} - the normal hit (damage/KB/impact/break).</li>
     *   <li>{@link #PASS_THROUGH} - ignore the entity, keep flying unchanged (the 1.8 pearl through its thrower).</li>
     *   <li>{@link #DEFLECT} - bounce off per {@link #deflect}; no damage/KB/break.</li>
     *   <li>{@link #DESTROY} - break the projectile (impact effect fires, then removed); the vanilla throwable response.</li>
     * </ul>
     * "Bypass" (deal the hit despite invulnerability) lives on the damage type, not here.
     */
    public enum HitResponse { HIT, PASS_THROUGH, DEFLECT, DESTROY }

    /**
     * The {@link HitResponse} for the two invulnerability cases vanilla distinguishes: {@link #invulWindow} (i-frame window;
     * 1.8 + 26.1 arrow {@code DEFLECT}) and {@link #immune} (creative/spectator; 1.8 arrow {@code PASS_THROUGH}, 26.1 {@code DEFLECT}).
     * Creative/spectator aren't split (use a lambda if needed). Set via {@code invulnHit(...)} or {@link #of}.
     */
    public record InvulnResponse(HitResponse invulWindow, HitResponse immune) {
        /** The same response for both cases (e.g. {@code of(DESTROY)} = vanilla throwable, {@code of(DEFLECT)} = 26.1 arrow). */
        public static InvulnResponse of(HitResponse all) { return new InvulnResponse(all, all); }
    }

    /** Per-tick integration order: {@link #DRAG_AFTER_MOVE} = 1.8 (move, drag, gravity), {@link #DRAG_BEFORE_MOVE} =
     *  26.1 throwable (gravity, drag, move). 26.1 arrows kept the 1.8 order. */
    public enum PhysicsOrder { DRAG_AFTER_MOVE, DRAG_BEFORE_MOVE }

    /** Client wire the silent-flight spawn state snaps to (the sim must integrate what the client decoded):
     *  {@link #LEGACY_1_8} = 1/32 position + Via-translated velocity shorts; {@link #MODERN} = double position + LP velocity. */
    public enum WireGrid { LEGACY_1_8, MODERN }

    /**
     * Pickup geometry for a collectable projectile (arrows): collected when its {@code boxWidth x boxHeight} box
     * intersects a player's bbox inflated by {@code (inflateH, inflateV, inflateH)} (vanilla {@code grow(1, 0.5, 1)}).
     */
    public record PickupBox(double inflateH, double inflateV, double boxWidth, double boxHeight) {
        /** Vanilla 1.8 / 26.1 pickup geometry: player box inflated (1, 0.5, 1), arrow box 0.5 x 0.5. */
        public static final PickupBox VANILLA = new PickupBox(1.0, 0.5, 0.5, 0.5);
    }

    /**
     * Rod retrieve pull on the hooked entity: {@code motion += (angler - bobber) * factor}, plus
     * {@code sqrt(dist) * yBoost} on Y. {@code pullPlayers} gates player victims. {@code wireVelocity}: send the
     * pull to a hooked player - 1.8 never did (no {@code velocityChanged}; the pull lives in server-tracked motion
     * and folds into the next hit), 26.1 makes the victim's client apply it via entity event 31.
     */
    public record RodPull(double factor, double yBoost, boolean pullPlayers, boolean wireVelocity) {
        public static final RodPull VANILLA_1_8 = new RodPull(0.1, 0.08, true, false);
        /** 26.1 dropped the sqrt-distance Y boost. */
        public static final RodPull MODERN = new RodPull(0.1, 0.0, true, true);
    }

    /** Rod durability cost on retrieve: a hooked {@code entity}, or stuck in {@code ground}. */
    public record RodDurability(int entity, int ground) {
        public static final RodDurability VANILLA_1_8 = new RodDurability(3, 2);
        public static final RodDurability MODERN = new RodDurability(5, 2);
    }

    /**
     * How a {@link HitResponse#DEFLECT} transforms the velocity: scale by {@code multiplier} (negative reverses all axes),
     * then rotate the heading by {@code turn} + a random {@code [minJitter, maxJitter]} wobble (degrees). 1.8 = {@code deflect(-0.1)};
     * 26.1 = {@code deflect(-0.5, 0, -10, 10)}.
     */
    public record Deflect(double multiplier, double turn, double minJitter, double maxJitter) {
        /** Plain reverse + damp with no extra turn: {@code motion *= multiplier} (negative reverses). */
        public static Deflect of(double multiplier) { return new Deflect(multiplier, 0.0, 0.0, 0.0); }
    }

    public final @Nullable FieldValue<ProjectileContext, Boolean> enabled;
    public final @Nullable FieldValue<ProjectileContext, BoundingBox> boundingBox;
    public final @Nullable FieldValue<ProjectileContext, Double> gravity;
    public final @Nullable FieldValue<ProjectileContext, Double> horizontalDrag;
    public final @Nullable FieldValue<ProjectileContext, Double> verticalDrag;
    /** Per-tick drag while in water, all axes, replacing the air drags (1.8/26.1: arrow {@code 0.6}, others {@code 0.8}); {@code 1} = unchanged. */
    public final @Nullable FieldValue<ProjectileContext, Double> waterDrag;
    /** Water-current impulse per tick along the flow (vanilla {@code 0.014}); {@code 0} = off. */
    public final @Nullable FieldValue<ProjectileContext, Double> waterPush;
    /** How water is sensed ({@link WaterModel}): 1.8's inset box (inverts on short entities - detection
     *  flickers with height, vanilla-exact) vs 26.1's fluid-height sampling (no flicker, height-scaled flow). */
    public final @Nullable FieldValue<ProjectileContext, WaterModel> waterModel;

    /** 1.8 {@code Entity.W()} inset-box sensing vs 26.1 {@code EntityFluidInteraction} height sampling. */
    public enum WaterModel { LEGACY, MODERN }

    /** 1.8 {@code rayTraceBlocks} parity: blocks with collision taller than 1 (fences, walls, gates) ray as their
     *  SELECTION box - the connection envelope at height 1.0 - so projectiles fly over the 1.0-1.5 band and die on
     *  envelope corners the exact shapes let through. {@code false} (default) = exact collision shapes (modern). */
    public final @Nullable FieldValue<ProjectileContext, Boolean> legacyBlockRay;

    /** Forward spawn offset along the shooter's look direction (blocks). */
    public final @Nullable FieldValue<ProjectileContext, Double> spawnOffsetForward;
    /** Vertical spawn offset from the shooter's eye height (blocks). */
    public final @Nullable FieldValue<ProjectileContext, Double> spawnOffsetVertical;
    /** Sideways spawn offset perpendicular to the look (vanilla 1.8 throwing-hand shift, {@code 0.16}; 26.1: 0). */
    public final @Nullable FieldValue<ProjectileContext, Double> spawnOffsetSideways;
    public final @Nullable FieldValue<ProjectileContext, Double> speed;
    /** Degrees added to the aim pitch for the launch heading only (vanilla {@code shootFromRotation} shape - the horizontal keeps the un-offset pitch). Splash potion / XP bottle {@code -20}; {@code 0} = straight aim. */
    public final @Nullable FieldValue<ProjectileContext, Double> launchPitchOffset;
    public final @Nullable FieldValue<ProjectileContext, Double> spread;
    /** Constant added to EVERY launch-direction axis pre-scale - the vanilla spread term with the gaussian pinned
     *  to {@code +1} (MineMen arrows: {@code 0.0075}, deterministic). {@code 0} = off. */
    public final @Nullable FieldValue<ProjectileContext, Double> spreadBias;
    /** Minimum |motY| of every BROADCAST velocity (spawn/corrections; the sim is untouched): a flatter wire vy is
     *  clamped to this, sign-preserving, exactly {@code 0} clamps up. {@code 0} = off (vanilla). MineMen throwables: {@code 0.05}. */
    public final @Nullable FieldValue<ProjectileContext, Double> wireMotYFloor;
    /** Fraction of the shooter's horizontal velocity (x/z) folded into the launch velocity. {@code 0} = none (1.8), {@code 1} = full (26.1). */
    public final @Nullable FieldValue<ProjectileContext, Double> momentumHorizontal;
    /** Fraction of the shooter's vertical velocity (y) folded in. {@code 0} = none (1.8); 26.1 folds it only when airborne (a lambda). */
    public final @Nullable FieldValue<ProjectileContext, Double> momentumVertical;
    public final @Nullable FieldValue<ProjectileContext, Integer> shooterImmunityTicks;
    /** Entity-hit margin: the target's bbox grows by this each side for the hit ray-test (vanilla {@code 0.3}). */
    public final @Nullable FieldValue<ProjectileContext, Double> entityHitGrow;
    /** What the projectile does when it hits its own shooter ({@link HitResponse}; default {@code HIT} = vanilla). */
    public final @Nullable FieldValue<ProjectileContext, HitResponse> selfHit;
    /** What the projectile does when it hits any other entity (default {@code HIT}); {@code DESTROY} = a pure impact trigger, no hit damage/KB (MineMen fireball). */
    public final @Nullable FieldValue<ProjectileContext, HitResponse> entityHit;
    /** Per-tick movement packets to viewers; {@code false} (default) = silent between position syncs, clients predict
     *  from the spawn velocity. The vanilla tracker sends projectiles neither - only the {@code syncInterval}/{@code velocitySyncInterval} corrections. */
    public final @Nullable FieldValue<ProjectileContext, Boolean> broadcastMovement;
    /** Position-correction interval (ticks): {@code <= 0} = never (fully client-predicted, event-driven broadcasts only). */
    public final @Nullable FieldValue<ProjectileContext, Integer> syncInterval;
    /** In-flight velocity broadcast interval (ticks): {@code <= 0} = never (vanilla arrow, the edge-slide fix), {@code N} = every N ticks. */
    public final @Nullable FieldValue<ProjectileContext, Integer> velocitySyncInterval;
    /** When drag + gravity apply relative to the move ({@link PhysicsOrder}): 1.8 = {@code DRAG_AFTER_MOVE}, 26.1 = {@code DRAG_BEFORE_MOVE}. */
    public final @Nullable FieldValue<ProjectileContext, PhysicsOrder> physicsOrder;
    /** Client wire the silent-flight spawn state snaps to ({@link WireGrid}); default {@code LEGACY_1_8}. */
    public final @Nullable FieldValue<ProjectileContext, WireGrid> wireGrid;
    /** Snap the spawn state onto the {@link WireGrid} so the server sim runs in lockstep with the predicting client;
     *  unset = only when there is no per-tick velocity sync. {@code true} forces it on a tracker wire (fishing bobber). */
    public final @Nullable FieldValue<ProjectileContext, Boolean> wireLockstep;
    /** Shooter immunity model: {@code false} (1.8) = fixed {@link #shooterImmunityTicks}; {@code true} (26.1) = until the projectile leaves the shooter's box. */
    public final @Nullable FieldValue<ProjectileContext, Boolean> leftOwnerImmunity;
    /** Distance a stuck projectile is pulled back along its flight dir so the tip pokes out of the block face (vanilla {@code 0.05}). */
    public final @Nullable FieldValue<ProjectileContext, Double> stickPullback;
    /** Pickup cooldown (ticks) after a collectable projectile sticks (vanilla arrow {@code shake} = {@code 7}); arrow-only. */
    public final @Nullable FieldValue<ProjectileContext, Integer> shakeTicks;
    /** Explosion power on detonation (fireball-only); vanilla ghast {@code 1.0}, Hypixel {@code 2.0}. */
    public final @Nullable FieldValue<ProjectileContext, Double> explosionPower;
    /** Moving ticks a fireball coasts at its launch speed before igniting; {@code 0} (default) = propel at once. Fireball-only. */
    public final @Nullable FieldValue<ProjectileContext, Integer> coastTicks;
    /** Speed a fireball snaps to when the {@link #coastTicks} coast ends; {@code 0} = none. Fireball-only. */
    public final @Nullable FieldValue<ProjectileContext, Double> cruiseSpeed;
    /** Full-draw crit chance the bow rolls ({@code [0,1]}); vanilla {@code 1.0} = always. Arrow-launcher knob. */
    public final @Nullable FieldValue<ProjectileContext, Double> critChance;
    /** Fall damage dealt to a player shooter when the ender pearl lands (vanilla {@code 5}); {@code 0} = none. Ender-pearl only. */
    public final @Nullable FieldValue<ProjectileContext, Double> teleportDamage;
    /** Pluggable {@link ProjectileBehavior} layered over the built-in effects (no subclassing); on fireball/pearl it
     *  OWNS the impact effect. Default {@link ProjectileBehavior#NONE}. */
    public final @Nullable FieldValue<ProjectileContext, ProjectileBehavior> behavior;
    /** Splash-potion particle palette for MODERN viewers: {@code true} = the 1.8 liquid colors, {@code false} (default) = modern. Legacy viewers always get the raw 1.8 potion value (their client picks its own colors). */
    public final @Nullable FieldValue<ProjectileContext, Boolean> legacyPotionColors;
    /** 26.1 splash semantics (box-distance intensity, 2007 for instants); {@code false} (default) = the 1.8 model. Splash-potion only. */
    public final @Nullable FieldValue<ProjectileContext, Boolean> modernSplash;
    /** Retrieve pull on the hooked entity ({@link RodPull}); default 1.8. Fishing-bobber only. */
    public final @Nullable FieldValue<ProjectileContext, RodPull> rodPull;
    /** Rod durability cost on retrieve ({@link RodDurability}); default 1.8 (3 entity / 2 ground). Fishing-bobber only. */
    public final @Nullable FieldValue<ProjectileContext, RodDurability> rodDurability;
    /** Distance from the angler past which the bobber discards (vanilla 32). Fishing-bobber only. */
    public final @Nullable FieldValue<ProjectileContext, Double> lineSnapDistance;
    /** Publish the hooked entity on the bobber metadata - the modern glued-bobber visual (26.1 {@code true}). A 1.8
     *  server through Via never emits it; {@code false} = the authentic vanilla18 look, hook mechanics unchanged
     *  (the pin position IS the 1.8 visual). Fishing-bobber only. */
    public final @Nullable FieldValue<ProjectileContext, Boolean> hookedMetadata;
    /** Halt + zero the bobber on the hook tick. Default follows the physics order: 26.1 {@code true} (FLYING ->
     *  HOOKED_IN_ENTITY zeroes deltaMovement), 1.8 {@code false} (the hook tick's move completes THROUGH the victim,
     *  the pin starts next tick). mmc18 opts in. Fishing-bobber only. */
    public final @Nullable FieldValue<ProjectileContext, Boolean> hookHalt;
    /** Whether the bobber senses water at all ({@code false} = neither vanilla nor MineMen; thin-water
     *  punch-through is plain 1.8 momentum). Default on. */
    public final @Nullable FieldValue<ProjectileContext, Boolean> hookWater;

    /** Freeze the bobber INTO the block it hits (arrow-like) instead of the vanilla slide/damp (1.8 ray-holds + damps
     *  down the face, 26.1 stops dead + falls). Default {@code false}. Fishing-bobber only. */
    public final @Nullable FieldValue<ProjectileContext, Boolean> hookStick;
    public final @Nullable FieldValue<ProjectileContext, KnockbackConfig> knockback;
    public final @Nullable FieldValue<ProjectileContext, KnockbackSource> knockbackSource;
    public final @Nullable FieldValue<ProjectileContext, Double> damage;
    public final @Nullable FieldValue<ProjectileContext, DamageType> damageType;
    public final @Nullable FieldValue<ProjectileContext, Boolean> removeOnEntityHit;
    public final @Nullable FieldValue<ProjectileContext, Boolean> removeOnBlockHit;
    /** Response for a hit the target rejects as invulnerable ({@link InvulnResponse}). 1.8 arrow = {@code invulnHit(DEFLECT, PASS_THROUGH)}; throwables {@code invulnHit(DESTROY)} (default). */
    public final @Nullable FieldValue<ProjectileContext, InvulnResponse> invulnHit;
    /** How a {@link HitResponse#DEFLECT} transforms the velocity ({@link Deflect}). 1.8 = {@code deflect(-0.1)}, 26.1 = {@code deflect(-0.5, 0, -10, 10)}. Default {@code deflect(-0.1)}. */
    public final @Nullable FieldValue<ProjectileContext, Deflect> deflect;
    /** Pickup geometry (collectable projectiles only, e.g. arrows); default {@link PickupBox#VANILLA}. */
    public final @Nullable FieldValue<ProjectileContext, PickupBox> pickupBox;

    ProjectileTypeConfig(Builder b) {
        super(b.key, b.subConfig);
        enabled = b.enabled;
        boundingBox = b.boundingBox;
        gravity = b.gravity;
        horizontalDrag = b.horizontalDrag;
        verticalDrag = b.verticalDrag;
        waterDrag = b.waterDrag;
        waterPush = b.waterPush;
        waterModel = b.waterModel;
        legacyBlockRay = b.legacyBlockRay;
        spawnOffsetForward = b.spawnOffsetForward;
        spawnOffsetVertical = b.spawnOffsetVertical;
        spawnOffsetSideways = b.spawnOffsetSideways;
        speed = b.speed;
        launchPitchOffset = b.launchPitchOffset;
        spread = b.spread;
        spreadBias = b.spreadBias;
        wireMotYFloor = b.wireMotYFloor;
        momentumHorizontal = b.momentumHorizontal;
        momentumVertical = b.momentumVertical;
        shooterImmunityTicks = b.shooterImmunityTicks;
        entityHitGrow = b.entityHitGrow;
        selfHit = b.selfHit;
        entityHit = b.entityHit;
        broadcastMovement = b.broadcastMovement;
        syncInterval = b.syncInterval;
        velocitySyncInterval = b.velocitySyncInterval;
        physicsOrder = b.physicsOrder;
        wireGrid = b.wireGrid;
        wireLockstep = b.wireLockstep;
        leftOwnerImmunity = b.leftOwnerImmunity;
        stickPullback = b.stickPullback;
        shakeTicks = b.shakeTicks;
        explosionPower = b.explosionPower;
        coastTicks = b.coastTicks;
        cruiseSpeed = b.cruiseSpeed;
        critChance = b.critChance;
        teleportDamage = b.teleportDamage;
        behavior = b.behavior;
        legacyPotionColors = b.legacyPotionColors;
        modernSplash = b.modernSplash;
        rodPull = b.rodPull;
        rodDurability = b.rodDurability;
        lineSnapDistance = b.lineSnapDistance;
        hookedMetadata = b.hookedMetadata;
        hookHalt = b.hookHalt;
        hookWater = b.hookWater;
        hookStick = b.hookStick;
        knockback = b.knockback;
        knockbackSource = b.knockbackSource;
        damage = b.damage;
        damageType = b.damageType;
        removeOnEntityHit = b.removeOnEntityHit;
        removeOnBlockHit = b.removeOnBlockHit;
        invulnHit = b.invulnHit;
        deflect = b.deflect;
        pickupBox = b.pickupBox;
    }

    /** Merges this config over {@code base}: this config's set fields win, unset fields fall back per resolution. */
    public ProjectileTypeConfig fromBase(ProjectileTypeConfig base) {
        Builder b = new Builder(key() != null ? key() : base.key());
        b.subConfig = subConfig != null ? subConfig : base.subConfig;
        b.mergeKnobs(this, base);
        return b.build();
    }

    /**
     * Builder for the generic (key-less) default config - the base every type in a {@link ProjectileConfig}
     * inherits unless it overrides a knob. Presets define their projectile baseline here.
     */
    public static Builder builder() { return new Builder((Key) null); }
    public static Builder builder(Key key) { return new Builder(key); }
    /** Builder seeded from {@code base}'s fields, e.g. {@code builder(Projectiles.defaults()).damage(5.0)}. */
    public static Builder builder(ProjectileTypeConfig base) { return new Builder(base); }
    /** A builder pre-filled with this config's fields. */
    public Builder toBuilder() { return new Builder(this); }

    /** Builder. Each knob takes a constant or a {@link ProjectileContext} lambda (per-launch). */
    public static final class Builder extends ProjectileTypeConfigBuilderBase<Builder> {

        @Override protected Builder self() { return this; }

        private Key key;
        private Function<ProjectileContext, ProjectileTypeConfig> subConfig;

        Builder(Key key) { this.key = key; }

        Builder(ProjectileTypeConfig c) {
            super(c);
            key = c.key();
            subConfig = c.subConfig;
        }

        public Builder key(Key k) { this.key = k; return this; }
        public Builder boundingBox(double width, double height, double depth) { boundingBox = FieldValue.constant(new BoundingBox(width, height, depth)); return this; }
        /** Sets all three spawn offsets at once: {@code forward} along the look, {@code vertical} from the eye, {@code sideways} perpendicular. */
        public Builder spawnOffset(double forward, double vertical, double sideways) {
            spawnOffsetForward = FieldValue.constant(forward);
            spawnOffsetVertical = FieldValue.constant(vertical);
            spawnOffsetSideways = FieldValue.constant(sideways);
            return this;
        }
        /** Sets both shooter-momentum scales at once (fraction of the shooter's client motion folded in: x/z, y). */
        public Builder momentum(double horizontal, double vertical) { momentumHorizontal = FieldValue.constant(horizontal); momentumVertical = FieldValue.constant(vertical); return this; }
        /** Rejected-hit response, same for the invul window and an immune target; e.g. {@code invulnHit(DESTROY)} (throwable). */
        public Builder invulnHit(HitResponse all) { invulnHit = FieldValue.constant(InvulnResponse.of(all)); return this; }
        /** Rejected-hit response split: invul window vs immune (creative/spectator); e.g. 1.8 arrow {@code invulnHit(DEFLECT, PASS_THROUGH)}. */
        public Builder invulnHit(HitResponse invulWindow, HitResponse immune) { invulnHit = FieldValue.constant(new InvulnResponse(invulWindow, immune)); return this; }
        /** A {@code DEFLECT} that just scales velocity by {@code multiplier} (negative reverses; vanilla 1.8 = {@code deflect(-0.1)}). */
        public Builder deflect(double multiplier) { deflect = FieldValue.constant(Deflect.of(multiplier)); return this; }
        /** A {@code DEFLECT} scaling velocity by {@code multiplier} + rotating the heading by {@code turn} +- a random {@code [min,max]} wobble (26.1 = {@code deflect(-0.5, 0, -10, 10)}). */
        public Builder deflect(double multiplier, double turn, double min, double max) { deflect = FieldValue.constant(new Deflect(multiplier, turn, min, max)); return this; }
        public Builder subConfig(Function<ProjectileContext, ProjectileTypeConfig> fn) { subConfig = fn; return this; }

        public ProjectileTypeConfig build() { return new ProjectileTypeConfig(this); }
    }
}
