package io.github.term4.polyp.mechanics.item;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsModule;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.util.BlockContact;
import io.github.term4.polyp.util.tick.TickContext;
import io.github.term4.polyp.util.tick.TickPhase;
import io.github.term4.polyp.util.tick.TickScaler;
import io.github.term4.polyp.util.tick.TickSystem;
import io.github.term4.polyp.world.MechanicsWorld;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.item.Material;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.component.DamageResistant;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Destroys dropped items exactly the way vanilla does. The entity carries health (5) and every source subtracts;
 * fire additionally IGNITES it, and that burn keeps charging after it leaves the flame.
 *
 * <p>The 1.8 model, unchanged in 26.1 ({@code Entity.move} + {@code Entity.C_}):
 * <ul>
 *   <li>standing in flame: {@link #FIRE} 1 <em>every tick</em>, and once the ignition delay elapses it lights for
 *       {@code fireIgniteTicks};</li>
 *   <li>standing in lava: {@link #LAVA} 4 every tick, lit for {@code lavaIgniteTicks};</li>
 *   <li>lit at all: {@link #BURN} 1 every {@code burnInterval} fire-ticks as the stock drains - this is what kills
 *       loot that was only flung <em>through</em> a fire;</li>
 *   <li>water snuffs the stock out (the vanilla fizz);</li>
 *   <li>cactus: {@link #CACTUS} 1 every tick; the void removes outright, never damage.</li>
 * </ul>
 *
 * <p>Immunity resolves in one order: a scope {@code vulnerable} entry wins, then a scope {@code immune} entry, then
 * the stack's own {@code damage_resistant} component (26.1's rule - netherite shrugs off fire), then 1.8's hardcoded
 * nether-star-vs-explosion.
 */
public final class ItemDamageSystem implements MechanicsModule {

    public static final Key KEY = Key.key("polyp:item-damage");

    /** Explosion blast (vanilla: the blast's own curve, always far above an item's health). */
    public static final Key EXPLOSION = Key.key("polyp:explosion");
    /** Standing in flame: 1 per tick. */
    public static final Key FIRE = Key.key("polyp:fire");
    /** Lit and burning down, in or out of the flame: 1 per {@code burnInterval}. */
    public static final Key BURN = Key.key("polyp:burn");
    /** Standing in lava: 4 per tick. */
    public static final Key LAVA = Key.key("polyp:lava");
    /** Touching a cactus: 1 per tick. */
    public static final Key CACTUS = Key.key("polyp:cactus");

    private static final int VANILLA_HEALTH = 5;
    private static final float FIRE_TICK_DAMAGE = 1f, BURN_DAMAGE = 1f, LAVA_DAMAGE = 4f, CACTUS_DAMAGE = 1f;
    private static final int FIRE_IGNITE_TICKS = 8 * 20, LAVA_IGNITE_TICKS = 15 * 20, BURN_INTERVAL = 20;
    /** Vanilla {@code maxFireTicks}: the counter parks here out of flame, so ignition needs a tick of contact. */
    private static final int IGNITE_DELAY = 1;

    private static final Tag<Integer> HEALTH = Tag.Transient("polyp:item-health");
    /** Vanilla {@code fireTicks}: positive = lit and draining, negative = the out-of-flame park. */
    private static final Tag<Integer> FIRE_TICKS = Tag.Transient("polyp:item-fire-ticks");

    private static final AtomicBoolean TICK_HOOK = new AtomicBoolean();

    private final Polyp polyp;
    private final EventNode<@NotNull Event> node;

    public ItemDamageSystem(Polyp polyp) {
        this.polyp = polyp;
        this.node = EventNode.all("polyp:item-damage");
    }

    public static ItemDamageSystem install(Polyp polyp) {
        ItemDamageSystem system = new ItemDamageSystem(polyp);
        polyp.register(system);
        polyp.install(system.node);
        // registered once for the JVM (TickSystem has no removal); dispatches through the live registry
        if (TICK_HOOK.compareAndSet(false, true)) {
            TickSystem.register(TickPhase.DEFAULT, ctx -> {
                ItemDamageSystem live = polyp.module(ItemDamageSystem.class);
                if (live != null) live.tick(ctx);
            });
        }
        return system;
    }

    @Override public @NotNull EventNode<@NotNull Event> node() { return node; }

    private @Nullable ItemDamageConfig configFor(Entity item) {
        return polyp.profiles().resolve(item, MechanicsKeys.ITEM_DAMAGE);
    }

    private static boolean on(@Nullable ItemDamageConfig cfg) {
        return cfg != null && !Boolean.FALSE.equals(cfg.enabled());
    }

    private static <T> T pick(@Nullable T scoped, T fallback) { return scoped != null ? scoped : fallback; }

    /** Starting health for this stack: the scope's per-item override, else its default, else vanilla 5. */
    public int maxHealth(ItemEntity item) {
        ItemDamageConfig cfg = configFor(item);
        if (cfg == null) return VANILLA_HEALTH;
        Integer perItem = cfg.health(item.getItemStack().material());
        return perItem != null ? perItem : pick(cfg.health(), VANILLA_HEALTH);
    }

    /** Remaining health, seeded from {@link #maxHealth} on first read. */
    public int health(ItemEntity item) {
        Integer v = item.getTag(HEALTH);
        if (v != null) return v;
        int max = maxHealth(item);
        item.setTag(HEALTH, max);
        return max;
    }

    /** Sets remaining health outright; {@code <= 0} destroys it, like any lethal hit. */
    public void setHealth(ItemEntity item, int value) {
        item.setTag(HEALTH, value);
        if (value <= 0 && !item.isRemoved()) item.remove();
    }

    /** Drops the tracked health so the next read re-seeds from the (possibly changed) config. */
    public void resetHealth(ItemEntity item) { item.removeTag(HEALTH); }

    /** Fire-ticks left on the stock; {@code <= 0} = not lit. */
    public int fireTicks(ItemEntity item) {
        Integer v = item.getTag(FIRE_TICKS);
        return v != null ? v : -IGNITE_DELAY;
    }

    /** Lights the item for {@code ticks} (vanilla {@code setOnFire}: only ever extends). */
    public void ignite(ItemEntity item, int ticks) {
        if (fireTicks(item) < ticks) {
            item.setTag(FIRE_TICKS, ticks);
            item.getEntityMeta().setOnFire(true);
        }
    }

    /** Snuffs the stock out (water, or an app cancelling the burn). */
    public void extinguish(ItemEntity item) {
        item.setTag(FIRE_TICKS, -IGNITE_DELAY);
        item.getEntityMeta().setOnFire(false);
    }

    /**
     * Hurts {@code item} from {@code source}; the scope may price the source, else {@code amount} stands.
     * Returns true when this destroyed it.
     */
    public boolean hurt(ItemEntity item, Key source, float amount) {
        ItemDamageConfig cfg = configFor(item);
        if (!on(cfg) || item.isRemoved() || immune(item, source)) return false;
        Float priced = cfg.damage(source);
        float dealt = priced != null ? priced : amount;
        if (!(dealt > 0)) return false;
        int left = health(item) - (int) dealt;
        item.setTag(HEALTH, left);
        if (left > 0) return false;
        item.remove();
        return true;
    }

    /** Scope override (either way) &gt; the stack's resistance component (26.1) &gt; 1.8's nether star. */
    public boolean immune(ItemEntity item, Key source) {
        Material material = item.getItemStack().material();
        ItemDamageConfig cfg = configFor(item);
        if (cfg != null) {
            if (cfg.vulnerable(material, source)) return false;
            if (cfg.immune(material, source)) return true;
            if (!Boolean.FALSE.equals(cfg.itemResistance()) && resistsByComponent(item, source)) return true;
        }
        return EXPLOSION.equals(source) && material == Material.NETHER_STAR;
    }

    /** 26.1 {@code ItemStack.canBeHurtBy}: the stack's {@code damage_resistant} tag covers this source's type. */
    private static boolean resistsByComponent(ItemEntity item, Key source) {
        DamageResistant resistant = item.getItemStack().get(DataComponents.DAMAGE_RESISTANT);
        if (resistant == null) return false;
        for (RegistryKey<DamageType> type : resistant.types()) {
            if (sourceOf(type.key()).equals(source)) return true;
        }
        return false;
    }

    /** Vanilla damage type -> the source key it arrives as here. */
    private static Key sourceOf(Key damageType) {
        return switch (damageType.asString()) {
            case "minecraft:in_fire" -> FIRE;
            case "minecraft:on_fire" -> BURN;
            case "minecraft:lava" -> LAVA;
            case "minecraft:cactus" -> CACTUS;
            case "minecraft:explosion", "minecraft:player_explosion" -> EXPLOSION;
            default -> KEY; // never a source we charge
        };
    }

    private void tick(TickContext ctx) {
        MechanicsWorld world = ctx.world();
        for (Entity entity : world.entities()) {
            if (!(entity instanceof ItemEntity item) || item.isRemoved() || !ctx.owns(item)) continue;
            ItemDamageConfig cfg = configFor(item);
            if (!on(cfg)) continue;
            if (!Boolean.FALSE.equals(cfg.voidDestroys()) && world.isInVoid(item.getPosition())) {
                item.remove(); // vanilla removes outright below the floor - never a damage source
                continue;
            }
            if (!burnDown(item, cfg)) continue;
            contact(item, cfg, world);
        }
    }

    /** Vanilla {@code Entity.C_}: drain the stock, charging {@link #BURN} on the interval. False = destroyed. */
    private boolean burnDown(ItemEntity item, ItemDamageConfig cfg) {
        int ticks = fireTicks(item);
        if (ticks <= 0) return true;
        int interval = Math.max(1, TickScaler.duration(item, pick(cfg.burnInterval(), BURN_INTERVAL), KEY));
        boolean charge = ticks % interval == 0;
        int left = ticks - 1;
        item.setTag(FIRE_TICKS, left);
        if (left <= 0) item.getEntityMeta().setOnFire(false);
        return !(charge && hurt(item, BURN, BURN_DAMAGE));
    }

    /** The blocks the item is standing in. Lava/cactus/flame all charge per tick; flame and lava also ignite. */
    private void contact(ItemEntity item, ItemDamageConfig cfg, MechanicsWorld world) {
        boolean[] hot = new boolean[4]; // lava, flame, cactus, water
        BlockContact.scan(world, item.getPosition(), item.getBoundingBox(), block -> {
            switch (block.name()) {
                case "minecraft:lava" -> hot[0] = true;
                case "minecraft:fire", "minecraft:soul_fire" -> hot[1] = true;
                case "minecraft:cactus" -> hot[2] = true;
                case "minecraft:water" -> hot[3] = true;
                default -> { }
            }
            return false; // visit every touched cell
        });
        if (hot[3]) extinguish(item); // vanilla fizz: water parks the stock
        if (hot[0]) {
            if (!hot[3]) ignite(item, TickScaler.duration(item, pick(cfg.lavaIgniteTicks(), LAVA_IGNITE_TICKS), KEY));
            if (hurt(item, LAVA, LAVA_DAMAGE)) return;
        }
        if (hot[1]) {
            // vanilla counts contact ticks up from -maxFireTicks and lights the moment it reaches 0
            if (!hot[3]) {
                int ticks = fireTicks(item);
                if (ticks < 0) item.setTag(FIRE_TICKS, ticks + 1);
                if (ticks + 1 == 0) ignite(item, TickScaler.duration(item, pick(cfg.fireIgniteTicks(), FIRE_IGNITE_TICKS), KEY));
            }
            if (hurt(item, FIRE, FIRE_TICK_DAMAGE)) return;
        }
        if (hot[2]) hurt(item, CACTUS, CACTUS_DAMAGE);
    }
}
