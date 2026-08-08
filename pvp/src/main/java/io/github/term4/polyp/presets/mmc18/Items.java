package io.github.term4.polyp.presets.mmc18;

import io.github.term4.polyp.mechanics.item.ItemDamageConfig;

/** MineMen dropped-item rules. */
public final class Items {

    private Items() {}

    /**
     * Vanilla health 5 and vanilla environment; the explosion price is per BLAST TYPE and lives on the
     * explosion config ({@link Explosion#config()}: fireball 2, TNT 3), since MineMen's two differ.
     *
     * <p>Captured: mmcfbitemdestroy 10/10 controlled items died on the 3rd fireball, mmctntdestroy 12/12 on
     * the 2nd TNT, both flat across 0.04-0.89 blocks - not vanilla, which one-shots loot at any of these ranges.
     */
    public static ItemDamageConfig damage() {
        return ItemDamageConfig.builder()
                .health(5)
                .voidDestroys(true)
                .build();
    }
}
