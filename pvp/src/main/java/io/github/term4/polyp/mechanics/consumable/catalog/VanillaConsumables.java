package io.github.term4.polyp.mechanics.consumable.catalog;

import io.github.term4.polyp.mechanics.attribute.catalog.VanillaPotions;
import io.github.term4.polyp.mechanics.consumable.Consumable;
import io.github.term4.polyp.mechanics.consumable.ConsumableBehavior;
import io.github.term4.polyp.mechanics.consumable.ConsumableConfigResolver.ConsumableContext;
import io.github.term4.polyp.mechanics.damage.types.magic.HealOrHarm;
import io.github.term4.polyp.mechanics.hunger.HungerSystem;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;
import net.minestom.server.potion.CustomPotionEffect;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Vanilla consumable <em>type identities</em> + the building blocks presets use for their version-specific behavior.
 * Version-split effects (golden apples, pufferfish) live in the preset's per-type behavior, version-invariant ones on
 * the type. Plain foods need no entry at all - the {@code ComponentFood} floor eats them off the item registry.
 */
public final class VanillaConsumables {

    private VanillaConsumables() {}

    /** The preset supplies its effects/food. */
    public static final Consumable GOLDEN_APPLE = Consumable
            .builder(Key.key("minecraft:golden_apple"), Material.GOLDEN_APPLE).build();

    /** The preset supplies its effects/food. */
    public static final Consumable ENCHANTED_GOLDEN_APPLE = Consumable
            .builder(Key.key("minecraft:enchanted_golden_apple"), Material.ENCHANTED_GOLDEN_APPLE).build();

    public static final Consumable MILK_BUCKET = Consumable
            .builder(Key.key("minecraft:milk_bucket"), Material.MILK_BUCKET)
            .behavior(clearEffects())
            .build();

    /** Applies the item's {@code potion_contents} - the same payload tipped arrows resolve. */
    public static final Consumable POTION = Consumable
            .builder(Key.key("minecraft:potion"), Material.POTION)
            .behavior(drinkPotion())
            .remainder(Material.GLASS_BOTTLE)
            .build();

    // FxHandler-food values from the pristine 1.8 ItemFood table; all but the pufferfish are version-identical.
    /** Raw chicken: 30% Hunger I (30s). */
    public static final Consumable RAW_CHICKEN = Consumable
            .builder(Key.key("minecraft:chicken"), Material.CHICKEN)
            .behavior(effectFood(2, 1.2f, eff(PotionEffect.HUNGER, 1, 600, 0.3f)))
            .build();

    /** Rotten flesh: 80% Hunger I (30s). */
    public static final Consumable ROTTEN_FLESH = Consumable
            .builder(Key.key("minecraft:rotten_flesh"), Material.ROTTEN_FLESH)
            .behavior(effectFood(4, 0.8f, eff(PotionEffect.HUNGER, 1, 600, 0.8f)))
            .build();

    /** Spider eye: Poison I (5s), always. */
    public static final Consumable SPIDER_EYE = Consumable
            .builder(Key.key("minecraft:spider_eye"), Material.SPIDER_EYE)
            .behavior(effectFood(2, 3.2f, eff(PotionEffect.POISON, 1, 100)))
            .build();

    /** Poisonous potato: 60% Poison I (5s). */
    public static final Consumable POISONOUS_POTATO = Consumable
            .builder(Key.key("minecraft:poisonous_potato"), Material.POISONOUS_POTATO)
            .behavior(effectFood(2, 1.2f, eff(PotionEffect.POISON, 1, 100, 0.6f)))
            .build();

    /** VERSION-SPLIT effects (1.8: Poison IV + Nausea II; modern: Poison II + Nausea I), so the preset supplies them. */
    public static final Consumable PUFFERFISH = Consumable
            .builder(Key.key("minecraft:pufferfish"), Material.PUFFERFISH)
            .build();

    /** The vanilla consumable types to register on the system. */
    public static Consumable[] types() {
        return new Consumable[]{GOLDEN_APPLE, ENCHANTED_GOLDEN_APPLE, MILK_BUCKET, POTION,
                RAW_CHICKEN, ROTTEN_FLESH, SPIDER_EYE, POISONOUS_POTATO, PUFFERFISH};
    }

    /**
     * 1.8 {@code EntityPlayer.canEat}: creative / spectator ({@code disableDamage}) can NEVER start eating. Drinks
     * (potion / milk) don't call this - {@code setItemInUse} is unconditional there.
     */
    public static boolean legacyCanEat(ConsumableContext ctx, boolean alwaysEdible) {
        Player u = ctx.user();
        return (alwaysEdible || u.getFood() < 20) && !invulnerable(u);
    }

    /**
     * 26.1 {@code Player.canEat}: creative / spectator ALWAYS eat (they just don't lose the item) - the difference
     * from {@link #legacyCanEat} that the preset split encodes.
     */
    public static boolean modernCanEat(ConsumableContext ctx, boolean alwaysEdible) {
        Player u = ctx.user();
        return invulnerable(u) || alwaysEdible || u.getFood() < 20;
    }

    /** Vanilla {@code abilities.invulnerable} / {@code disableDamage}: creative and spectator. */
    private static boolean invulnerable(Player u) {
        return u.getGameMode() == GameMode.CREATIVE || u.getGameMode() == GameMode.SPECTATOR;
    }

    /** {@code level} is 1-based (level I = amplifier 0). */
    public static FxHandler eff(PotionEffect id, int level, int ticks) { return new FxHandler(id, (byte) (level - 1), ticks, 1f); }

    /** {@link #eff} with a vanilla apply {@code chance}. */
    public static FxHandler eff(PotionEffect id, int level, int ticks, float chance) { return new FxHandler(id, (byte) (level - 1), ticks, chance); }

    /** A potion effect a consumable applies on finish, at {@code chance} (1 = always). */
    public record FxHandler(PotionEffect id, byte amplifier, int ticks, float chance) {}

    /**
     * On finish restores food + saturation through the {@link HungerSystem} and applies each {@link FxHandler}. The
     * building block for food/golden-apple behaviors.
     */
    public static ConsumableBehavior effectFood(int nutrition, float saturation, FxHandler... effects) {
        return new ConsumableBehavior() {
            @Override public void onFinish(ConsumableContext ctx) {
                Player u = ctx.user();
                byte flags = ctx.particles().potionFlags();
                for (FxHandler e : effects) {
                    if (e.chance() < 1f && ThreadLocalRandom.current().nextFloat() >= e.chance()) continue;
                    VanillaPotions.addEffect(u, new Potion(e.id(), e.amplifier(), e.ticks(), flags));
                }
                HungerSystem hunger = ctx.services() != null ? ctx.services().hunger() : null;
                if (hunger != null) hunger.restore(u, nutrition, saturation);
            }
        };
    }

    /** Milk: clears all of the user's active effects on finish. */
    public static ConsumableBehavior clearEffects() {
        return new ConsumableBehavior() {
            @Override public void onFinish(ConsumableContext ctx) { ctx.user().clearEffects(); }
        };
    }

    /**
     * Applies the item's {@code potion_contents} - base potion + any custom effects - at full duration. Instant
     * effects apply through {@link HealOrHarm} at intensity {@code 1.0} (vanilla drink); a water bottle is a no-op.
     */
    public static ConsumableBehavior drinkPotion() {
        return new ConsumableBehavior() {
            @Override public void onFinish(ConsumableContext ctx) {
                Player u = ctx.user();
                byte flags = ctx.particles().potionFlags();
                for (CustomPotionEffect e : VanillaPotions.payload(ctx.item())) {
                    if (HealOrHarm.apply(ctx.services(), u, u, null, e, 1.0)) continue;
                    VanillaPotions.addEffect(u, new Potion(e.id(), e.amplifier(), e.duration(), flags));
                }
            }
        };
    }
}
