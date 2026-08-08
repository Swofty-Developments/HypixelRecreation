package io.github.term4.polyp.presets.vanilla;

import io.github.term4.polyp.item.ItemDef;
import io.github.term4.polyp.item.ItemRegistry;
import io.github.term4.polyp.item.VanillaItems;

/** Modern (26) item registry: weapon attack damage derives from Minestom's {@code ATTACK_DAMAGE}. */
public final class Items {

    private Items() {}

    public static ItemRegistry registry() {
        return new ItemRegistry(ItemDef.Version.MODERN, VanillaItems.weapons());
    }
}
