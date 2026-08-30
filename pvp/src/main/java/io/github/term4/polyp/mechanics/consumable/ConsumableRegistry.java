package io.github.term4.polyp.mechanics.consumable;

import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** {@link Material} -&gt; {@link Consumable} lookup for the {@link ConsumableSystem}. */
public final class ConsumableRegistry {

    private final Map<Material, Consumable> byMaterial = new ConcurrentHashMap<>();

    /** Indexes under each of the consumable's materials; last registration wins per material. */
    public void register(Consumable consumable) {
        for (Material m : consumable.materials()) byMaterial.put(m, consumable);
    }

    public @Nullable Consumable forMaterial(@Nullable Material material) {
        return material == null ? null : byMaterial.get(material);
    }
}
