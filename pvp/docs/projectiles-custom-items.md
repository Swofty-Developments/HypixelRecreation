# Custom projectiles & items in a minigame

How to give specific items custom projectile behavior (damage, knockback, gravity, effects, trails, on-land logic)
**without** stacking per-item config lambdas — the lib stays oblivious to your items; your minigame owns a small
registry and one or two event listeners look things up in it.

## TL;DR
1. Put a small **id** on the item (a persistent tag/component you control — never the whole config object).
2. Keep an `id → handler` **map in your minigame**.
3. One `ProjectileHitEvent` listener (impact) + optionally one `ProjectileLaunchEvent` listener (spawn/flight) look
   up the id and apply your handler. Done — adding the 50th custom item touches only your map, never the lib.

## 1. Mark the item
```java
static final Tag<String> PROJECTILE_ID = Tag.String("mygame:projectile_id");

ItemStack boomSnowball = ItemStack.of(Material.SNOWBALL).withTag(PROJECTILE_ID, "boom");
```
(`Tag.String`/components persist across copy + serialization, unlike a `Tag.Transient`; a tiny id keeps the item light.)

## 2. A handler interface + registry (your code, not the lib)
```java
interface CustomProjectile {
    default void onLaunch(ProjectileLaunchEvent e) {}   // spawn/flight: velocity, gravity, behavior, trail
    default void onHit(ProjectileHitEvent e) {}         // impact: damage, knockback, response, effects
}

static final Map<String, CustomProjectile> REGISTRY = Map.of(
    "boom", new BoomSnowball(),
    "grapple", new GrappleArrow()
    // ... add items here only
);

static @Nullable CustomProjectile lookup(@Nullable ItemStack item) {
    String id = item == null ? null : item.getTag(PROJECTILE_ID);
    return id == null ? null : REGISTRY.get(id);
}
```

## 3. Two listeners delegate to the registry
```java
var node = MinecraftServer.getGlobalEventHandler();
node.addListener(ProjectileLaunchEvent.class, e -> {
    CustomProjectile p = lookup(e.snapshot().item());
    if (p != null) p.onLaunch(e);
});
node.addListener(ProjectileHitEvent.class, e -> {
    CustomProjectile p = lookup(e.snapshot().item());
    if (p != null) p.onHit(e);
});
```

## 4. What each handler can do (the full flight + hit surface)

`ProjectileLaunchEvent` — fired once, before the projectile enters the world (`e.projectile()` is a typed
`ProjectileEntity`):

| Want to… | Call |
|---|---|
| change launch position | `e.setSpawnPos(pos)` |
| change speed / direction | `e.setVelocity(vec)` *(blocks/tick)* |
| change gravity / drag | `e.projectile().setAerodynamics(new Aerodynamics(gravity, vDrag, hDrag))` |
| change physics order / immunity / stick | `e.projectile().setPhysicsOrder(...)`, `setLeftOwnerImmunity(...)`, `setStickPullback(...)` |
| attach per-tick / on-impact behavior | `e.behavior(new ProjectileBehavior(){ onTick/onImpact/onSpawn/... })` |
| add a trail / metadata directly | use `e.projectile()` (schedule a task, set meta) |
| cancel the launch | `e.setCancelled(true)` |
| preview the resolved physics | `e.resolvedFlight()` |

`ProjectileHitEvent` — fired on every entity/block hit, before effects apply:

| Want to… | Call |
|---|---|
| change damage | `e.damage(amount)` |
| change / disable knockback | `e.knockback(cfg)` / `e.knockbackSource(...)` |
| force the outcome | `e.response(HIT / PASS_THROUGH / DEFLECT / DESTROY)` |
| keep it flying / force removal | `e.removeOnHit(false / true)` |
| suppress everything (keep flying) | `e.setCancelled(true)` |
| read what hit / where | `e.target()`, `e.hitPoint()`, `e.isBlockHit()`, `e.projectile().getStuckBlock()` |
| preview the resolved hit | `e.resolvedHit()` |

So a "boom snowball" is just: `onHit` → `e.damage(6); spawnParticles(e.hitPoint());` — no config, no subclass.

## 5. When to use config instead of events
Events cover spawn/flight/impact for **logic**. The one thing they can't change *before the projectile is built* is
the resolved flight config — but you rarely need that, since `ProjectileLaunchEvent` already lets you set every physics
knob on the entity directly. If you do want a custom item to resolve a whole different config (e.g. for many items
sharing physics presets), use the same registry from a single config overlay instead of a listener:
```java
ProjectileTypeConfig.builder(Snowball.KEY)
    .subConfig(ctx -> { CustomProjectile p = lookup(ctx.item()); return p == null ? null : p.flightConfig(); })
    .build();
```
Either way it's **one** hook into the lib delegating to **your** registry — you never write N lambdas.

## Notes
- Per-shooter/instance/global config still applies underneath via `MechanicsProfile.projectiles(...)`; your event
  handlers run on top of whatever config resolved.
- The lib's built-in throwables (snowball/egg/pearl) and the bow fire these same events, so a custom item reusing a
  vanilla material (a tagged snowball) Just Works — no new `ProjectileType` needed unless you want a new entity.
