package io.github.term4.polyp.mechanics.item;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.presets.vanilla18.Items;
import io.github.term4.polyp.mechanics.explosion.ExplosionSystem;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Item health 5, subtract-per-hit, and the pricing seam a custom server tunes. */
class ItemDamageTest extends HeadlessServerTest {

    @BeforeAll
    static void installItems() {
        ItemDamageSystem.install(polyp);
        ExplosionSystem.install(polyp);
    }

    private static ExplosionSystem explosions() {
        return polyp.module(ExplosionSystem.class);
    }

    private static ItemEntity drop(Instance inst, Material material, Pos at) {
        ItemEntity item = new ItemEntity(ItemStack.of(material));
        item.setInstance(inst, at).join();
        return item;
    }

    private static Instance vanillaInstance() {
        return flatInstance(MechanicsProfile.builder().set(MechanicsKeys.ITEM_DAMAGE, Items.damage()).build());
    }

    @Test
    void oneVanillaBlastClearsTheGround() {
        ItemDamageSystem items = polyp.module(ItemDamageSystem.class);
        ItemEntity item = drop(vanillaInstance(), Material.DIAMOND, new Pos(20.5, 66, 20.5));
        // the vanilla curve near a TNT is far above an item's 5 health
        assertTrue(items.hurt(item, ItemDamageSystem.EXPLOSION, 27f), "one blast destroys it");
        assertTrue(item.isRemoved());
    }

    /** The captured MineMen counts, driven through the per-blast price: fireball 2 -> 3 blasts. */
    @Test
    void pricedBlastsStackToTheCapturedCount() {
        ItemDamageSystem items = polyp.module(ItemDamageSystem.class);
        Instance inst = flatInstance(MechanicsProfile.builder()
                .set(MechanicsKeys.ITEM_DAMAGE, io.github.term4.polyp.presets.mmc18.Items.damage()).build());
        ItemEntity item = drop(inst, Material.DIAMOND, new Pos(24.5, 66, 24.5));
        assertFalse(items.hurt(item, ItemDamageSystem.EXPLOSION, 2f), "survives the first fireball");
        assertFalse(items.hurt(item, ItemDamageSystem.EXPLOSION, 2f), "and the second");
        assertTrue(items.hurt(item, ItemDamageSystem.EXPLOSION, 2f), "5 health - 2*3 -> dies on the third");

        ItemEntity tnt = drop(inst, Material.DIAMOND, new Pos(26.5, 66, 26.5));
        assertFalse(items.hurt(tnt, ItemDamageSystem.EXPLOSION, 3f), "TNT prices 3: survives the first");
        assertTrue(items.hurt(tnt, ItemDamageSystem.EXPLOSION, 3f), "and dies on the second");
    }

    /** 1.8 hardcodes it, 26.1 reaches the same call through the item's own resistance. */
    @Test
    void netherStarShrugsOffExplosions() {
        ItemDamageSystem items = polyp.module(ItemDamageSystem.class);
        ItemEntity star = drop(vanillaInstance(), Material.NETHER_STAR, new Pos(28.5, 66, 28.5));
        assertFalse(items.hurt(star, ItemDamageSystem.EXPLOSION, 99f), "explosion-immune");
        assertFalse(star.isRemoved());
        assertTrue(items.hurt(star, ItemDamageSystem.LAVA, 99f), "but lava still takes it");
    }

    /** Vanilla: flame charges 1 EVERY tick, so a 5-health item dies in 5 ticks of contact. */
    @Test
    void flameChargesEveryTick() {
        ItemDamageSystem items = polyp.module(ItemDamageSystem.class);
        ItemEntity item = drop(vanillaInstance(), Material.DIAMOND, new Pos(30.5, 66, 30.5));
        for (int i = 0; i < 4; i++) assertFalse(items.hurt(item, ItemDamageSystem.FIRE, 1f), "tick " + i);
        assertTrue(items.hurt(item, ItemDamageSystem.FIRE, 1f), "5th contact tick destroys it");
    }

    /** The fire STOCK is what kills loot flung out of a flame: it keeps draining and charging BURN. */
    @Test
    void ignitionOutlivesTheFlame() {
        ItemDamageSystem items = polyp.module(ItemDamageSystem.class);
        ItemEntity item = drop(vanillaInstance(), Material.DIAMOND, new Pos(32.5, 66, 32.5));
        assertFalse(item.getEntityMeta().isOnFire(), "not lit yet");
        items.ignite(item, 160); // vanilla setOnFire(8)
        assertEquals(160, items.fireTicks(item));
        assertTrue(item.getEntityMeta().isOnFire(), "the entity's own fire flag drives the visual");
        // 160 ticks at one charge per 20 = 8 damage, well past a 5-health item
        items.extinguish(item);
        assertEquals(-1, items.fireTicks(item), "water parks the stock");
        assertFalse(item.getEntityMeta().isOnFire());
    }

    /** Health is settable per item AND at runtime, and immunity strips both ways. */
    @Test
    void healthAndImmunityAreFullyOverridable() {
        ItemDamageSystem items = polyp.module(ItemDamageSystem.class);
        Instance inst = flatInstance(MechanicsProfile.builder()
                .set(MechanicsKeys.ITEM_DAMAGE, ItemDamageConfig.builder()
                        .health(5)
                        .health(Material.DIAMOND, 12)                                  // per item
                        .vulnerable(Material.NETHER_STAR, ItemDamageSystem.EXPLOSION)  // strip the vanilla immunity
                        .immune(Material.STICK, ItemDamageSystem.LAVA)                 // grant a new one
                        .build()).build());

        ItemEntity tough = drop(inst, Material.DIAMOND, new Pos(34.5, 66, 34.5));
        assertEquals(12, items.maxHealth(tough), "per-item health");
        assertFalse(items.hurt(tough, ItemDamageSystem.EXPLOSION, 11f), "12 health survives 11");
        items.setHealth(tough, 2);                                                     // runtime override
        assertTrue(items.hurt(tough, ItemDamageSystem.EXPLOSION, 2f), "and dies once set low");

        ItemEntity star = drop(inst, Material.NETHER_STAR, new Pos(36.5, 66, 36.5));
        assertTrue(items.hurt(star, ItemDamageSystem.EXPLOSION, 99f), "vanilla immunity stripped");

        ItemEntity stick = drop(inst, Material.STICK, new Pos(38.5, 66, 38.5));
        assertFalse(items.hurt(stick, ItemDamageSystem.LAVA, 99f), "granted lava immunity");
    }

    /** End to end through the explosion system: a blast on the mmc profile really clears ground loot. */
    @Test
    void explosionSystemDestroysGroundLoot() {
        ExplosionSystem explosions = explosions();
        Instance inst = flatInstance(MechanicsProfile.builder()
                .set(MechanicsKeys.ITEM_DAMAGE, io.github.term4.polyp.presets.mmc18.Items.damage())
                .set(MechanicsKeys.EXPLOSION, io.github.term4.polyp.presets.mmc18.Explosion.config())
                .build());
        ItemEntity item = drop(inst, Material.DIAMOND, new Pos(50.5, 66, 50.5));
        // the explosion config resolves off the SOURCE's scope chain, so a sourceless blast would miss
        // the instance profile entirely and fall back to the install config
        Entity source = new Entity(EntityType.TNT);
        source.setInstance(inst, new Pos(50.5, 66, 54.5)).join();
        try {
            // TNT prices 3 against health 5: survives one blast, dies on the second
            explosions.explode(inst, new Pos(50.5, 66, 50.5), 4.0f, source);
            assertFalse(item.isRemoved(), "one TNT blast leaves it at 2 health");
            explosions.explode(inst, new Pos(50.5, 66, 50.5), 4.0f, source);
            assertTrue(item.isRemoved(), "the second clears it");
        } finally {
            if (!item.isRemoved()) item.remove();
            source.remove();
        }
    }

    /** A SOURCELESS blast still belongs to a world, so it must resolve that world's preset - not silently
     *  fall back to the install config and one-shot the loot the preset says survives. */
    @Test
    void sourcelessBlastStillUsesTheWorldsPreset() {
        ExplosionSystem explosions = explosions();
        Instance inst = flatInstance(MechanicsProfile.builder()
                .set(MechanicsKeys.ITEM_DAMAGE, io.github.term4.polyp.presets.mmc18.Items.damage())
                .set(MechanicsKeys.EXPLOSION, io.github.term4.polyp.presets.mmc18.Explosion.config())
                .build());
        ItemEntity item = drop(inst, Material.DIAMOND, new Pos(60.5, 66, 60.5));
        try {
            explosions.explode(inst, new Pos(60.5, 66, 60.5), 4.0f, null); // no source at all
            assertFalse(item.isRemoved(), "TNT-power blast prices 3, so one leaves it alive");
            explosions.explode(inst, new Pos(60.5, 66, 60.5), 4.0f, null);
            assertTrue(item.isRemoved(), "and the second clears it");
        } finally {
            if (!item.isRemoved()) item.remove();
        }
    }

    /** Pricing keys off the blast POWER, so a fireball-scale blast costs 2 even with no fireball entity. */
    @Test
    void fireballScalePowerPricesTwoWithoutASourceEntity() {
        ExplosionSystem explosions = explosions();
        Instance inst = flatInstance(MechanicsProfile.builder()
                .set(MechanicsKeys.ITEM_DAMAGE, io.github.term4.polyp.presets.mmc18.Items.damage())
                .set(MechanicsKeys.EXPLOSION, io.github.term4.polyp.presets.mmc18.Explosion.config())
                .build());
        ItemEntity item = drop(inst, Material.DIAMOND, new Pos(64.5, 66, 64.5));
        try {
            explosions.explode(inst, new Pos(64.5, 66, 64.5), 2.0f, null);
            assertFalse(item.isRemoved(), "fireball power: 2 damage, survives");
            explosions.explode(inst, new Pos(64.5, 66, 64.5), 2.0f, null);
            assertFalse(item.isRemoved(), "and survives the second");
            explosions.explode(inst, new Pos(64.5, 66, 64.5), 2.0f, null);
            assertTrue(item.isRemoved(), "dies on the third, like the capture");
        } finally {
            if (!item.isRemoved()) item.remove();
        }
    }
}
