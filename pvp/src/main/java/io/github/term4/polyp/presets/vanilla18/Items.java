package io.github.term4.polyp.presets.vanilla18;

import io.github.term4.polyp.item.ItemDef;
import io.github.term4.polyp.item.ItemRegistry;
import io.github.term4.polyp.item.VanillaItems;
import io.github.term4.polyp.mechanics.item.ItemDamageConfig;

/** Vanilla 1.8 item registry: the LEGACY weapon table; armor rides Minestom's {@code ARMOR} attribute. */
public final class Items {

    private Items() {}

    public static ItemRegistry registry() {
        return new ItemRegistry(ItemDef.Version.LEGACY, VanillaItems.weapons());
    }

    /** Dropped-item destruction. Health 5 and every source at its vanilla amount, identical in 1.8 and 26.1 -
     *  a blast's own curve is always well past 5, so one explosion clears the ground. */
    public static ItemDamageConfig damage() {
        return ItemDamageConfig.builder().health(5).voidDestroys(true).build();
    }
}
