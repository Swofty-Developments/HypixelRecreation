package io.github.term4.polyp.mechanics.consumable;

import io.github.term4.polyp.mechanics.consumable.ConsumableConfigResolver.ConsumableContext;
import io.github.term4.polyp.mechanics.hunger.HungerSystem;
import net.kyori.adventure.key.Key;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.Food;

/**
 * The unregistered-item floor: an item with a {@code food} component eats with its registry values (value-identical
 * to the 1.8 table, diffed against the pristine ItemFood ctors). Per-scope config rides {@link #KEY} like any type,
 * {@link ConsumableConfig#componentFoods()} turns it off.
 */
public final class ComponentFood {

    public static final Key KEY = Key.key("polyp:component-food");

    private ComponentFood() {}

    private static final ConsumableBehavior RESTORE = new ConsumableBehavior() {
        @Override public void onFinish(ConsumableContext ctx) {
            Food food = ctx.item().get(DataComponents.FOOD);
            HungerSystem hunger = ctx.services() != null ? ctx.services().hunger() : null;
            // saturationModifier is a misnomer: the codec maps the component's FINAL "saturation" value onto it
            if (food != null && hunger != null) hunger.restore(ctx.user(), food.nutrition(), food.saturationModifier());
        }
    };

    static final Consumable TYPE = Consumable.builder(KEY)
            .defaultConfig(ConsumableTypeConfig.builder(KEY)
                    .consumeTicks(ComponentFood::componentTicks)
                    .behavior(RESTORE)
                    .build())
            .build();

    private static int componentTicks(ConsumableContext ctx) {
        net.minestom.server.item.component.Consumable c = ctx.item().get(DataComponents.CONSUMABLE);
        return c != null ? c.consumeTicks() : Consumable.VANILLA_CONSUME_TICKS;
    }

    /** The {@code alwaysEdible} input for the canConsume gates. */
    public static boolean alwaysEdible(ItemStack item) {
        Food f = item.get(DataComponents.FOOD);
        return f != null && f.canAlwaysEat();
    }
}
