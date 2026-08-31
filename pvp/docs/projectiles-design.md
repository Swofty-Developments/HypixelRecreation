# Projectiles: design + vanilla parity reference

Design record for the projectile system (config / resolver / snapshot / context / event layer / system /
producers), ported from the old prototype. Section 1 is the fix LEDGER from that port - kept as the
regression reference for why the current code does what it does.

## 0. Decisions (ratified) + methodology

- **Knockback**: plain `KnockbackConfig` through `KnockbackSystem.apply` - no separate projectile KB config.
- **Per-item overrides**: config lambdas via `ctx.item()`. The old item-NBT tag system is dropped;
  TODO(attributes): per-item custom configs (custom items carrying their own projectile tuning) belong to
  the future attributes system - revisit there, not in this port.
- **TPS scaling**: not ported yet, but keep it addable: all timing constants stay tick-denominated and
  centralized (no baked-in per-second math scattered through logic), velocities flow through the existing
  b/t vs b/s conventions, so a later TickScaler pass is mechanical.
- **Methodology - SIMPLE FIRST**: the old fixes were not necessarily necessary nor optimal. Section 1 is a
  *ledger*, not a checklist to blindly port: implement the simple/idiomatic path first, then run each
  ledger entry's edge-case test, and only apply (or re-derive) the fix where the simple path actually
  fails. Every applied fix should cite its ledger entry.

## 1. Fix ledger (what the old impl fixed, how, and how to re-test each)

Each entry: the problem, the old fix, and the edge-case test that decides whether the new impl needs it.

### Physics + entity core (`systems/projectile/entities/CustomEntityProjectile.java`)
- **Zero-size bounding box** (`setBoundingBox(0,0,0)`): collision points resolve exactly on block
  boundaries. Even `0.01` overshoots and breaks the modern client's `floor(pos) -> block -> inGround` check.
- **Block collision** via `PhysicsResult.collisionShapes()[axis]` -> hit block / point / axis; the entity is
  placed at `physicsResult.newPosition()` (resolved), NOT the collision point — fixes modern clients seeing
  the arrow float in front of the block face.
- **Stick is "radio silence"**: when stuck, `super.tick()` never runs -> zero periodic syncs. Modern clients
  hold the arrow via `inGround` metadata alone. Subclass `update()` still runs (pickup, despawn timers).
  **⚠️ SUPERSEDED (see status "STUCK-ARROW 1.8 DESYNC"):** radio silence was WRONG - it broke 1.8 self-heal. Vanilla
  never silences a stuck arrow; it re-asserts pos+rotation every `updateInterval`. We now keep movement frozen but do
  a periodic `resyncStuck()` re-assert (+ modern inGround metadata). Movement/scheduler still don't run while stuck.
- **Stick sync is delayed one tick** (Atlas trick): teleport scheduled next tick so a client/server
  hit-disagreement doesn't snap the arrow to the hit position prematurely. **(Now subsumed: the periodic
  `resyncStuck()` naturally fires its first teleport the tick after sticking; the explicit scheduler block was removed.)**
- **Entity collision**: bbox `growSymmetrically(0.3, 0.3, 0.3)` swept from `position` (= 1.8's target `grow(0.3)`
  each side, see §5 - was the `(0.1,0.3,0.1)`/`pos-0.3` desync bug; `expand` ≠ `growSymmetrically`), shooter immune
  for the first ~5 ticks (`SHOOTER_COLLISION_DELAY_TICKS`).
- **`synchronizePosition()` override**: absolute `EntityTeleportPacket` + velocity packet, and
  `lastSyncedPosition` updated manually. Minestom's default relative-move sync is wrong for 1.8 clients
  through Via — they never see the projectile move. This + the spawn-velocity policy is "the smooth vs
  jerky projectile" optimization.
- **`updateNewViewer()`** (relog / chunk-cross viewers): version-branched spawn packets. The spawn packet's
  `data` field must be `> 0` for ViaVersion to include velocity bytes in the 1.8 SpawnObject.
- **Rotation** is displacement-based (yaw/pitch from movement delta); latched at impact when sticking.
- **Unstuck check**: block at `collisionPoint + faceNormal * 0.5` is air. (An intersect-box check
  false-unsticks on fences/slabs.)

### 1.8 stuck-arrow desync fix (`compatibility/legacy_1_8/fix/LegacyProjectileCompat.java`)
> **⚠️ SUPERSEDED - this entire LegacyProjectileCompat approach (F7/F8/F12) is NOT being ported.** It worked around
> our F2 radio silence. The real fix is to match vanilla and never silence a stuck arrow (periodic `resyncStuck()`
> re-assert; see status "STUCK-ARROW 1.8 DESYNC"). The per-tick hints, edge pullback, and edge filter below are kept
> only as historical reference. Re-open ONLY if an in-game test shows a case the periodic re-assert can't heal.
- **Root cause**: the 1.8 client raycasts `pos -> pos + motion` to detect block hits. Stuck in a wall or
  ceiling, gravity points motion away from the surface -> the client never detects the hit -> arrow
  floats/slides.
- **Center hits**: per-tick velocity "hint" packets (flight direction, magnitude 1.0) to legacy viewers
  only. No correction visible — the arrow just stops.
- **Edge hits, two phases**: (1) NO packets — the 1.8 client predicts a natural arc; (2) after
  ~20 ticks, a one-time pullback teleport (0.1 blocks back along flight, original rotation preserved) and
  the hint switches to the face normal.
- Edge detection is shape-relative (works on fences/slabs), threshold 0.35 — **known-broken filter, port
  candidate for a rework** (`// TODO: this doesn't filter properly`).
- Relog: hint embedded in the spawn packet + immediate velocity reinforcement; zero view angles make the
  1.8 client derive rotation from motion.
- `stickGeneration` counter invalidates stale scheduled pullbacks after unstick/restick.
- User assessment: "good ish fix, could need optimizing."

### Fishing bobber (`entities/FishingBobber.java`)
- **Sink-into-floor desync fix** (legacy clients): spawn packet carries ZERO velocity so the client never
  predicts; the server's absolute teleport syncs drive all visible motion; explicit zero-velocity packet on
  landing stops residual prediction.
- **Medium-cast overshoot fix**: the server applies gravity BEFORE moving, the 1.8 client applies the
  received velocity as-is — so the client runs `gravity/TPS = 0.04` blocks/tick fast. Pre-apply the next
  tick's gravity to the velocity packet. (User: "there may be a better / more optimized solution" for the
  bobber overall.)
- Gravity constants: legacy 0.04 / modern 0.03, drag 0.92, gravity added pre-tick.
- **Minemen pseudo-hook** ("bobbers don't stick to players in 1.8"): on player hit -> hook for 1 tick (the
  client renders the line) -> unhook; re-hook for 1 tick whenever the victim MOVES (position, not look)
  until the rod retracts. `PSEUDO_HOOKED_BY` tag set on the victim; LEGACY mode also stops the bobber
  hooking other players after the first pseudo-hook. MODERN mode pins the bobber at the victim's face.
- Hooked non-players get pulled `0.1 * delta * TPS`; player-pulling is a config toggle.

### Launch / items (`features/Bow.java`, `features/FishingRod.java`, `components/ProjectileCreator.java`)
- The ITEM causes the SERVER to spawn the entity: bow uses `PlayerBeginItemUseEvent` /
  `PlayerCancelItemUseEvent` / slot-change; rod toggles cast/retrieve on `PlayerUseItemEvent`.
- Bow power `(s^2 + 2s)/3` capped at 1 (s = use seconds); < 0.1 cancels; >= 1.0 = critical.
- Arrow selection offhand -> main -> inventory scan; infinity/creative; enchants (Power -> base damage,
  Punch -> KB level, Flame -> fire ticks); pickup modes.
- **Spawn positions** (`utils/ProjectileCalculator.java`): arrows eye − 0.1; throwables eye + 0.1 forward
  − 0.05 down; bobber 0.3 in front at eye height (uses the legacy eye-height system).
- Projectile yaw/pitch derived FROM the final velocity (after spread/momentum) so visuals match flight.
- Spread = gaussian × 0.0075 × multiplier; optional shooter-momentum inheritance (horizontal only when
  grounded).
- Cleanup listeners: disconnect clears bow+rod state; death clears bow only (bobbers persist).

### Edge-case test matrix (run per fix before porting it)

| # | Old fix | Edge-case test that justifies it |
|---|---------|----------------------------------|
| F1 | Zero-size bounding box | Modern client: arrow shot into a wall - does it render `inGround` flush with the face, or float/jitter? Repeat with bbox 0.01/0.25. |
| F2 | Radio silence when stuck | Modern client: stuck arrow for 60s+ - any twitch when Minestom's periodic sync fires? |
| F3 | Resolved-position placement (not collision point) | Modern client: shots into walls/ceilings at shallow angles - arrow floating in front of the face? |
| F4 | One-tick delayed stick sync (Atlas) | Point-blank wall shots at high ping - does the arrow visibly snap/jump to the hit position? |
| F5 | Absolute-teleport position sync | 1.8 client via Via: does a flying arrow visibly move with Minestom's default relative sync? (Old result: it never moves.) |
| F6 | Spawn `data > 0` for Via velocity bytes | 1.8 client: does a freshly spawned projectile fly smoothly or sit still until first teleport? |
| F7 | Legacy stuck hints (center) | 1.8 client: arrow into wall/ceiling center - float/slide without per-tick hints? (Root cause is structural: client raycast needs motion into the block. Likely needed.) |
| F8 | Edge pullback two-phase | 1.8 client: arrow into block edges/corners - with only F7 hints, does the client miss the surface? Old filter is known-broken; re-derive threshold. |
| F9 | Bobber zero-velocity spawn + teleport-driven motion | 1.8 client: cast bobber - does it sink into the floor with normal velocity-driven sync? |
| F10 | Bobber gravity pre-application in velocity packets | 1.8 client: medium-range cast - does the bobber visually overshoot the landing point by ~0.04 b/t drift? |
| F11 | Pseudo-hook re-hook on move | Visual: does the line render persistently on a moving victim with plain hook metadata? (This is a feature - Minemen mode - not a fix.) |
| F12 | Stuck-arrow relog spawn (hint-in-spawn, zero view) | 1.8 client: relog / walk far away and return while an arrow is stuck - floats or sticks? |
| F13 | Chunk-cross viewer handling (`updateNewViewer`) | Both clients: shoot across a chunk border into an unseen chunk; walk in - projectile state correct? |

## 2. Target architecture (this library's idiom)

```
mechanics/projectile/
  ProjectileSystem           install(polyp, ProjectileConfig, Shootable...) — config enables self-launching types
                             (typeConfigs presence, like DamageSystem.install); Shootables are the item launchers
  ProjectileConfig           generic defaults + per-type ProjectileTypeConfig map (extends Config; mirrors DamageConfig)
  ProjectileTypeConfig       per-type FieldValue knobs, extends Config<ProjectileContext, Self> like AttackConfig
  ProjectileConfigResolver   ProjectileContext -> ResolvedFlight (launch) + ResolvedHit (impact)
  ProjectileSnapshot         shooter, item, type, power, spawn/velocity overrides, config override
  shootables/                pluggable item launchers (the attack `HitDetection` analog): Shootable, Bow
  types/                     projectile identity + behavior: ProjectileType, Arrow (pure identity),
                             ThrowableItemType (self-launching base) + Snowball/Egg/Pearl
  entities/                  ProjectileEntity, ManagedProjectile, ArrowEntity, PearlEntity (egg uses the default
                             ManagedProjectile - its chicken easter egg is now an example ProjectileBehavior)
api/event/                   ProjectileLaunchEvent (spawn: cancellable, spawn/velocity mutable, resolvedFlight()),
                             ProjectileHitEvent (entity/block: cancellable, resolvedHit() + per-hit overrides)
```

> **Launcher vs self-launching type (the key split):** a bow/crossbow/rod is an ITEM that fires a projectile -
> modeled as a pluggable `Shootable` (mirrors `AttackSystem`'s `HitDetection`), passed to `install(polyp, cfg, new Bow())`.
> A snowball/egg/pearl IS the thrown item - a self-launching `ProjectileType` (`ThrowableItemType`) that wires its own
> use trigger and is enabled by its config entry. Arrow is pure identity; the `Bow` Shootable fires it.
>
> **Three event surfaces (consistent with Damage/Knockback/Attack):** SPAWN = `ProjectileLaunchEvent` (cancel /
> redirect spawn+velocity / attach cosmetics, `resolvedFlight()` preview); FLIGHT folds INTO spawn (attach a trail or
> use `ProjectileBehavior.onTick` - no separate per-tick event); HIT = `ProjectileHitEvent` (`resolvedHit()` preview +
> per-hit overrides: `damage`, `knockback`/`knockbackSource`, `removeOnHit`, and `response(...)` to force the outcome).
>
> **Per-projectile behavior without subclassing:** attach a `ProjectileBehavior` (no-op default lifecycle hooks
> `onSpawn`/`onTick`/`onImpact`/`onDeflect`/`onStuck`/`onUnstuck`/`onRemove`) as the config `behavior` knob or per-launch
> via `ProjectileSnapshot.withBehavior`; `ManagedProjectile` delegates to it additively (the type's built-in effects
> still run). The projectile analog of the attack `Ruleset`. `ProjectileTypeConfig` extends the generic `Config<Ctx,Self>`
> base like `AttackConfig` (public `FieldValue` fields, `merge`); knobs include `momentumHorizontal`/`momentumVertical`
> (scales over a `VelocityRule` source, resolved config -> shooter profile -> DEFAULT like knockback), `deflect(...)`,
> `physicsOrder`, `leftOwnerImmunity`, `stickPullback`, `shakeTicks`, and the renamed `spawnOffset(forward, vertical, sideways)`.
> (The `deflectParticles` cosmetic crit-trail moved OUT to the `compatibility/` package - `LegacyArrowVisibilityConfig.deflectParticles`,
> resolved per-shooter - since it's a 1.8-visual compat cosmetic, grouped with the arrow-visibility fix.)

**Per-type configurability requirement** (parity with the old system, expressed as `FieldValue` knobs on
`ProjectileTypeConfig` - constants or per-context lambdas like every other config in the lib):
bounding box size, spawn offset (forward/vertical, eye-height source), initial speed/power curve,
spread, shooter-momentum inheritance, aerodynamics (gravity + horizontal/vertical drag), shooter-immunity
ticks, sync interval, knockback config, damage amount/type, pickup mode + delay, despawn ticks, and the
legacy-compat toggles (hint magnitude / pullback / edge threshold; bobber fix mode). The entity classes
stay dumb - launchers resolve the config and stamp the entity (aerodynamics, bbox, sync interval) at spawn.
Performance: resolve once per launch, not per tick; per-tick reads (aerodynamics) live on the entity.

Integration decisions (keep responsibilities where they already live):
- **Knockback**: NO separate ProjectileKnockbackConfig. Hits route through `KnockbackSystem.apply` with a
  `KnockbackSnapshot` (origin = projectile position or shooterOriginPos, direction = flight direction,
  melee = false) carrying a per-type `KnockbackConfig`. quantizeVelocity / entityPush / velocity-rule knobs
  come along free. (Old lib's bobber-relative vs shooter-relative KB = snapshot origin choice.)
- **Damage**: arrow/thrown hits emit `DamageSnapshot`s through new `DamageType`s (`minecraft:arrow`,
  `minecraft:thrown` ...), so invul windows, overdamage, hurt broadcast, and the event layer all apply
  unchanged. Punch/Power live in the type's producer like melee's crit/sprint logic.
- **Version branching**: `ClientInfoTracker.getProtocol` replaces the old ClientVersionDetector.
- **Velocity tracking**: projectiles are server-authoritative entities — `polyp.velocity()` and MotionTracker
  are NOT involved; the entity's own velocity is the truth (the simulated() non-player path already returns it).
- **No TickScaler port** initially: the lib elsewhere assumes `ServerFlag.SERVER_TICKS_PER_SECOND` (see
  DamageSystem's TPS TODO); TPS scaling is one coherent later pass.
- **Per-item overrides**: prefer config lambdas reading `ctx.item()` (consistent with everything else)
  over the old item-NBT-tag system (`ProjectileTagRegistry`/`VelocityTagValue`) — revisit only if per-item
  runtime data (not config) is actually needed.

## Decisions locked (from the user)
1. Projectile KB = plain `KnockbackConfig` through `KnockbackSystem.apply`. 2. Per-item override = config
lambdas; **base/vanilla configs must stay constant-only (no lambdas)**; custom-item NBT deferred to the future
attributes system. 3. No TickScaler yet, but all timing stays tick-denominated so it's addable.
4. **Velocity tracking = one per-player rule set ONCE on a profile scope** (instance/global/player), inherited by
every system (melee KB, projectile KB, hurt broadcast). `KnockbackConfig.velocity` stays as a rare per-config
override; components read the resolved rule via `ctx.victimVelocity()` (no static pinning). NOT building a registry
of stateful per-scope trackers - the single `MotionTracker` + stateless `VelocityRule` reads already give "one
velocity per player" for free. Revisit only if a future mode needs its own per-tick state.
5. **Projectile config lives in the PRESETS, never in the type class.** Types are identity + behavior (config-free
`ProjectileType(key, name, entityType)`); `Vanilla18`/`Minemen` own the tuning. A `ProjectileConfig` carries a
generic `defaults` (the shared baseline, applied to every type) plus sparse per-type overrides, so a type entry
restates only its deltas (`ProjectileTypeConfig.builder(KEY).damage(x)`) or is empty just to enable. Resolution:
per-type -> generic `defaults` -> type intrinsic -> hard fallbacks. Spawn offset is `spawnOffsetH`/`spawnOffsetV`
(+ combined `spawnOffset(h,v)`).
6. **Projectile config resolves in TWO phases**: flight knobs at launch, hit knobs at IMPACT (against a
`ProjectileContext` with `target`/`isSelfHit`/`throwOrigin`/`hitPos`). Self-vs-other and throw-time behavior are
plain config lambdas (`ctx -> ctx.isSelfHit() ? ...`), NOT a dedicated structure or the event API; `selfHits(false)`
is the one constant knob (pass-through-self) so vanilla stays constant-only. KB yaw is `KnockbackSource.SHOOTER`
(source = shooter) + `yawWeight`, never a `SHOOTER_LOOK` enum. The event API stays for genuinely dynamic per-hit logic.

## Stuck-arrow sync model (root-caused; the enduring protocol fact)

A stuck arrow is NEVER silent in vanilla. `ServerEntity.sendChanges` (26.1) explicitly forces arrows into the
position+rotation re-assert branch every `updateInterval` (note the `!(entity instanceof AbstractArrow)` guard on the
pos-only branch), plus a forced position update every 60t, a full teleport every 400t, and a one-time zero-velocity
motion when it stops. That continuous re-assert is the ONLY thing that corrects a 1.8 client - its `inGround` is
self-raytraced and a fresh spawn re-derives rotation from motion, so a mispredicted stuck arrow (relog angle, edge-hit
overshoot) never recovers without periodic re-syncs. Our early "radio silence when stuck" broke exactly that self-heal;
the fix is the vanilla-style periodic `resyncStuck()` re-assert (`ProjectileEntity`), with the modern client frozen via
inGround metadata so each re-assert is a no-op for it. Accepted quirk: a RELOGGED 1.8 client's stuck arrow holds a stale
client-side inGround and ignores teleports until re-stuck.

## API surface for consumers
**Available now:**
- **Events** - `ProjectileLaunchEvent` (spawn/flight: cancel, `setSpawnPos`/`setVelocity`, `behavior(...)`, the typed
  `projectile()` handle for physics setters, `resolvedFlight()`); `ProjectileHitEvent` (impact: `damage`/`knockback`/
  `knockbackSource`/`response`/`removeOnHit` overrides, `resolvedHit()`, `target`/`hitPoint`/`isBlockHit`/`isSelfHit`).
- **Entity queries** - `getSpawnPosition()` (where fired), `isStuck()` + `getStuckPoint()`/`getStuckBlockPosition()`/
  `getStuckBlock()` (where it landed / what it stuck in), `velocityBt()` + `getAerodynamics()` + `getPosition()` (live
  physics for trajectory particles), `getShooter()`.
- `StuckArrows` (arrows-in-body get/set/add/clear). Custom items / minigame registry: `docs/projectiles-custom-items.md`.

**Proposed / NEEDS-CONFIRM (not built - confirm before implementing):**
- **`ProjectileSystem.predictPath(snapshot)`** -> simulate the arc (e.g. `List<Pos>`) WITHOUT spawning, for a pre-shot
  particle trajectory preview (aiming aid). Needs the single-tick step (`movementTick`/`applyDragGravity`) extracted to
  a pure function run against throwaway state. High value for minigames.
- A despawn/expire **event or hook** distinct from a hit (currently only `ProjectileBehavior.onRemove`).
- Config/`ResolvedFlight` introspection for a type without launching (for UIs / debug overlays).
- A `ProjectileSnapshot` builder convenience for non-item launches (turrets, spells) - currently `of(shooter, type)` + withers.


## Open TODOs (unverified - cross-check the live task list before acting)

- Arrow/bow: Infinity + Flame enchants, draw gated on having arrows, bow durability + shoot sound, dedicated
  `minecraft:arrow` damage type, pickup nits (inventory-full, offhand-first, pop sound).
- Pearl: cross-instance teleport, dedicated ender-pearl damage type (GenericDamage stand-in), 5% endermite.
- Snowball: 3 damage to a Blaze (needs entity-type-aware damage).
- Spread distribution: 1.8 gaussian*0.0075 vs 26.1 `triangle(0, 0.0172275*uncertainty)` - a distribution knob + the
  26.1 value would make the modern preset exact.
- Max-age despawn knob (vanilla stuck arrows ~1200t; flying throwables die after 1200t).
- Unbuilt launchers/types: crossbow (Multishot/Quick Charge), trident (Loyalty/Riptide), lingering potion +
  AreaEffectCloud, XP bottle, wither skull / eye of ender / firework / mob projectiles.
- `Vanilla.projectiles()` (modern preset) has never had a full 26.1-client in-game pass.
- Proposed API (confirm before building): `predictPath(snapshot)` arc simulation; a despawn/expire event distinct
  from hit; config introspection without launching; a snapshot builder for non-item launches.

## 5. Vanilla parity notes (researched from 1.8 PaperSpigot + 26.1 source)

Source paths: 1.8 = `...\paper_source_1.8.8\...\net\minecraft\server\Entity{Projectile,Snowball,Egg,EnderPearl}.java`;
26.1 = `...\26.1-src\net\minecraft\world\entity\projectile\{Projectile,ThrowableProjectile,throwableitemprojectile\*}.java`.

### Momentum inheritance (shooter velocity folded into the throw)
- **1.8** (`EntityProjectile` ctor + `shoot()`): **NONE.** A thrown snowball/egg/pearl gets velocity =
  `look x 1.5 + gaussian(0.0075)` only - the shooter's `motX/Y/Z` is never added. So vanilla-1.8
  `inheritMomentum = FALSE`.
- **26.1** (`Projectile.shootFromRotation`): `delta += source.getKnownMovement().x/z` always, `.y` only when the
  shooter is airborne (`source.onGround() ? 0 : y`). Critically it reads **`getKnownMovement()`** (the entity's
  REAL tracked movement), NOT server velocity. Our analog is **`MotionTracker.positionDelta(shooter)`** (b/t
  move-delta), NOT `Entity.getVelocity()` (which is ~0/stale for client-driven players - the "feels off" bug).
- **Decision**: `inheritMomentum` defaults FALSE (vanilla 1.8 + resolver fallback). When TRUE (modern presets),
  the momentum source is `MotionTracker.positionDelta(shooter)`; horizontal always, vertical only if airborne.

### Self-hit vs other-entity hit (via hit-time resolution, NOT a dedicated record)
The point is NOT immunity - a projectile can behave DIFFERENTLY on its thrower vs any other entity. After exploring
a `SelfHit` record (rejected as "preset-enum stiff" + limiting), the clean solution is to **resolve the hit knobs at
IMPACT** against a context that carries the target, so plain config lambdas express any self-vs-other difference.
- **Resolution split** (`ProjectileConfigResolver`): `resolveFlight(tc, ctx)` at launch (spawn/physics);
  `resolveHit(tc, ctx)` at impact, where `ctx = ProjectileContext.of(snap, services).atHit(target, throwOrigin, hitPos)`.
  `ManagedProjectile` stores the merged `effectiveConfig` + snapshot and resolves the hit knobs each impact (rare, so
  late resolution is cheap). A hit lambda reads `ctx.isSelfHit()`, `ctx.target()`, `ctx.throwOrigin()`.
- **Vanilla has NO self-immunity** (the user confirmed: singleplayer only). 1.8 excludes the shooter for the first
  5 ticks (`ar < 5`); after that a self-hit is normal - a snowball thrown straight up hits you on the descent. So the
  default is just NORMAL. The **ender pearl** is the one genuine 1.8 self-ignore (`if (hit.entity == shooter) return;`).
- **Native knobs (no lambda for the common case)**:
  - `selfHit` (`HitResponse` knob {HIT, PASS_THROUGH, DEFLECT}, default HIT): the response when the projectile hits
    its OWN shooter. `PASS_THROUGH` = ignore + keep flying (1.8 pearl, Hypixel "self does nothing"); `DEFLECT` =
    bounce off (reverse+damp velocity, no break/dmg/KB). Constant-friendly, so vanilla stays constant-only -
    `Vanilla18.pearl()` = `selfHit(PASS_THROUGH)`. The same `HitResponse` enum generalizes to any future "not allowed
    to hit this entity" case (team filters, etc.).
  - Everything else self-specific is a lambda on the existing hit knobs: Minemen self-KB-along-yaw =
    `knockback(ctx -> ctx.isSelfHit() ? KnockbackConfig.builder(kb).yawWeight(1).build() : kb)`.
- **Yaw, no `SHOOTER_LOOK`**: `KnockbackSource.SHOOTER` now carries `source = shooter` in the snapshot (like melee),
  so the KB calculator has the shooter's look and `yawWeight` does the work - `yawWeight(1)` pushes along the yaw.
  `PROJECTILE` is unchanged (origin = projectile, dir = flight). Dropped `SHOOTER_LOOK`.
- **Throw-time snapshot**: `shooterOriginPos` (captured at spawn) is now exposed as `ctx.throwOrigin()` and on
  `ProjectileHitEvent` (+ `isSelfHit()`), so lambdas AND the event can push KB/teleport from the throw pose.

### Spawn position, launch velocity, collision box (1.8 `EntityProjectile` ctor + `shoot`)
- **Spawn**: shooter eye height, then offset `(-cos(yaw)*0.16, -0.1, -sin(yaw)*0.16)` - a **0.16 LATERAL** shift
  (perpendicular to look = the throwing-hand offset) and **0.1 DOWN**, NO forward. Our config had only the -0.1
  vertical (no lateral) - that's a Hypixel-style accuracy trim, not vanilla. Added `spawnOffsetLateral` (vanilla
  `0.16`). 26.1 (`ThrowableItemProjectile`): eye `- 0.1`, NO lateral.
- **Velocity**: `look_unit * 1.5 + gaussian*0.0075` (speed `j()=1.5`, uncertainty `1.0` -> **spread `0.0075`**). Our
  config had `spread = 0` (Hypixel/accuracy); vanilla is `0.0075`. Gravity `m()=0.03`, drag `0.99` (`0.8` in water).
  Same constants in 26.1.
- **Collision box**: entity SIZE `0.25 x 0.25` (1.8 `setSize`, 26.1 `sized(0.25,0.25)`) is render/broad-phase only.
  BLOCK collision is a POINT raytrace (center -> center+motion) in both - our `bbox = 0` (F1) matches it and is the
  reason we keep size 0 (modern client `inGround` flush). ENTITY collision: 1.8 grows the TARGET by `0.3` on EACH side
  (`Entity{Arrow,Projectile}`: `f=0.3F`, both verified) and ray-tests the path. **FIXED (was the bug):** we now
  `growSymmetrically(grow, grow, grow)` the zero projectile box from the un-offset `position` (Minkowski dual of
  target+0.3), where `grow` is the per-type `ProjectileTypeConfig.entityHitGrow` knob (default `0.3`, stamped on the
  entity at launch like `shooterImmunityTicks`; `DEFAULT_ENTITY_HIT_GROW`). GOTCHA: Minestom's `expand(0.3)` only adds
  0.3 to the TOTAL size (±0.15) and offsets y, so a first attempt with `expand` reached only ~0.45 to the side and
  looked unchanged; `growSymmetrically` adds 0.3 to BOTH sides (±0.3, centered, = 1.8's `grow`). The old `(0.1,0.3,0.1)`
  from `pos-0.3` was too tight: arrows the 1.8 CLIENT predicts as a hit flew right past on the server -> "arrow
  disappears for shooter / bounces for target" desync (1.8-vs-1.8 only; modern's hitbox matches the server). 26.1 uses
  a different margin -> set `entityHitGrow` in the future modern preset.
- **Physics order**: 1.8 moves THEN applies drag+gravity (post-move); 26.1 applies gravity+inertia THEN moves
  (pre-move). Ours = 1.8. Modern preset needs a pre-move toggle later (ties into bobber F10).

### 1.8 -> 26.1 deltas the system must express natively (the modern preset is `Vanilla.projectiles()`)
> All deltas below are now expressible as knobs, and **`Vanilla.projectiles()`/`projectileDefaults()`/`arrow()`** wire
> them (base on the `Vanilla18` config + override the deltas): `momentum(1) + velocity(delta())` (airborne-vertical
> lambda), `spawnOffsetSideways(0)`, `deflect(-0.5, 0, -10, 10)`, `physicsOrder(DRAG_BEFORE_MOVE)`, `leftOwnerImmunity(true)`,
> arrow `invulnHit(PASS_THROUGH)`. Still deferred: dedicated `enderPearl`/`minecraft:arrow` damage types (on the
> damage-type TODO).
>
> **FUTURE (not implemented) - path prediction:** keep `ProjectileSystem.spawnPos`/`launchVelocity` and the single-tick
> physics step (`ProjectileEntity.movementTick` now factors `applyDragGravity()`) pure/reusable so a future
> `ProjectileSystem.predictPath(snapshot)` can simulate the arc WITHOUT spawning an entity - for a client-side particle
> preview of where a throw/shot will land. No API added yet; just don't entangle the launch math with entity state.
| Aspect | 1.8 | 26.1 | Knob today |
|---|---|---|---|
| Momentum | none | `getKnownMovement` (h always, v airborne) | `momentumHorizontal`/`momentumVertical` scales over a `VelocityRule` source (config `velocity` rule -> shooter profile -> DEFAULT; 26.1 = 1/1 + `velocity(delta())`, airborne-vertical via a lambda) |
| Spawn lateral | 0.16 | 0 | `spawnOffsetSideways` |
| Deflect | `motion *= -0.1` | `REVERSE` `*= -0.5` + random +-10 deg | `deflect(mul, turn, min, max)` knob (`ProjectileTypeConfig.Deflect` nested record, like `PickupBox`): 1.8 = `deflect(-0.1)`, 26.1 = `deflect(-0.5, 0, -10, 10)` |
| Spread | 0.0075 | 0.0075 | `spread` |
| Self-hit (pearl) | pass-through | self-teleport | `selfHits(false)` + hit-knob lambdas on `ctx.isSelfHit()` |
| Shooter immunity | 5 ticks | `leftOwner` (geometric) | `shooterImmunityTicks` (1.8) / `leftOwnerImmunity(true)` (26.1) |
| Physics order | post-move drag/grav | pre-move grav/inertia | `physicsOrder` (`DRAG_AFTER_MOVE` 1.8 / `DRAG_BEFORE_MOVE` 26.1) |
| Block collision | per-tick `world.rayTrace` | swept AABB (server-auth) | swept-only (a RAYTRACE knob was built then deleted - indistinguishable; see 3f) |
| Pearl dmg type | FALL, 5 | `enderPearl`, 5 | amount yes; type = TODO (GenericDamage stand-in) |
| Pearl teleport target | pre-move pos | `oldPosition()` | ~same (pre-move) |
| Sync interval | n/a here | `updateInterval(10)` | `syncInterval` (ours 20) |

### Egg / pearl impact + pearl damage (both fire on entity AND block hit)
- **Egg**: 0 dmg to a hit entity; `1/8` chance baby chicken (`1/32` -> 4), break. (26.1 adds a chicken-variant
  component - skipped.)
- **Snowball**: 0 dmg, `3` to a Blaze (defer - needs entity-type-aware damage), break.
- **Pearl damage is a CONSTANT, not distance-based** (answers the open question): 1.8
  `entityliving.fallDistance = 0; damageEntity(DamageSource.FALL, 5.0F)` - it ZEROES the fall distance then deals a
  flat **5**, typed as FALL (feather-falling/resistance apply, armor does not). 26.1:
  `hurtServer(damageSources().enderPearl(), 5.0F)` - flat **5** via a DEDICATED `enderPearl` type. So the amount is
  constant 5 in both; only the damage TYPE differs (FALL vs enderPearl). We now call
  `FallDamage.resetFallDistance(shooter)` (already public "for ender pearls") + deal a flat 5 (currently via
  `GenericDamage` - no armor model yet so numerically == FALL; a dedicated EnderPearl/FALL-amount type is the clean
  follow-up that also captures the 1.8-vs-26 type delta).

### Entity tracking / position sync (1.8 `EntityTracker`/`EntityTrackerEntry`, 26.1 `ServerEntity`) - drives the FLIGHT SYNC model (status 3f)
The 1.8 CLIENT runs each projectile's full physics + raytrace locally every tick (`EntityArrow.t_` / `EntityProjectile.t_`); the server only CORRECTS it with packets. So how often/what the server broadcasts decides whether the client's local prediction (incl. its own block-hit raytrace) agrees with the server.
- **1.8 register intervals** (`EntityTracker.addEntity(entity, range, updateInterval, sendVelocity)`): arrow `(64, 20, false)`, snowball/egg/pearl `(64, 10, true)`, fishing hook `(64, 5, true)`, fireball `(64, 10, false)`.
- **1.8 `track()` per tick**: the position/velocity block runs every `updateInterval` ticks (`m % c == 0`). **Arrows are forced into a pos+rotation packet** (`!(tracker instanceof EntityArrow)` disables the pos-only / rot-only branches - the same `&& !AbstractArrow` exists in 26.1 `ServerEntity` line ~158). Relative move when the delta fits a byte (±4 blocks/update), else teleport (also on onGround change, `v > 400`, every 60t). **Velocity** is broadcast only when `sendVelocity` (`u`) is on AND motion changed `> 0.02` - so a vanilla **arrow sends ZERO velocity packets in flight** (the client predicts from the spawn velocity); throwables send it ~each 10t. An abrupt `velocityChanged` impulse (knockback) broadcasts velocity immediately, outside the interval - but an arrow deflect sets `motX *= -0.1` DIRECTLY (no `velocityChanged`), so it gets no special broadcast.
- **26.1 `ServerEntity.sendChanges`**: same shape - forced PosRot for `AbstractArrow`; position every `updateInterval` (or `needsSync`/dirty), `pos = positionChanged || tickCount%60==0`; teleport on `requiresPrecisePosition`/deltaTooBig/`teleportDelay>400`/onGround change; velocity (`SetEntityMotion`) only on change `> 1e-7` when `trackDelta`; `hurtMarked` broadcasts velocity immediately.
- **What we landed on** (status 3f): match the vanilla broadcast in BOTH dimensions.
  - **Position:** `synchronizePosition` sends the absolute teleport throttled to `syncInterval`, client predicts between (a per-tick teleport SHAKES - it fights the client's interpolation/prediction; reverted, along with `synchronizeNextTick()` and a `syncFlightNow()` deflect push).
  - **Velocity:** THE edge-slide fix. We replicate `sendVelocity=false` for arrows - velocity goes ONCE in the spawn packet (the `SpawnEntityPacket` velocity bytes), then the per-tick `EntityVelocityPacket` from Minestom's `Entity.tick` is DROPPED in `ProjectileEntity.sendPacketToViewers` (an `allowVelocityPacket` flag still lets the deliberate stuck-zero through). Per-tick velocity packets - lagged + quantized over Via - were overwriting the 1.8 client's clean local prediction, so its own raytrace mispredicted edges (slide) and its bounce prediction drifted (deflect "wrong to the target"). This is why RAYTRACE made no difference (the CLIENT decides via its prediction). Stripping them = vanilla lockstep.
  - The deflect "invisible to OTHER viewers" stays (vanilla); the opt-in `deflectParticles` knob renders a server-spawned crit trail at the authoritative position for visibility. The stuck-arrow `resyncStuck` (every `syncInterval`) mirrors vanilla `ServerEntity`'s re-assert for a stopped arrow. A future modern preset keeps the same cadence (modern physics match better).
