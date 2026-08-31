package io.github.term4.polyp.mechanics.attribute;

import io.github.term4.polyp.mechanics.attribute.catalog.enchant.Bane;
import io.github.term4.polyp.mechanics.attribute.catalog.enchant.Smite;
import io.github.term4.polyp.mechanics.damage.types.melee.MeleeDamage;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Smite/Bane contribute to {@code MELEE_FLAT_ADD} only when {@code CombatFacts.TARGET} (dropped in by the melee path)
 * matches their monster type.
 */
class ConditionalEnchantTest extends HeadlessServerTest {

    private static final float EPS = 1e-3f;

    private static ItemStack swordWith(Key enchant, int level) {
        return enchanted(Material.DIAMOND_SWORD, enchant, level);
    }

    private float melee(LivingEntity attacker, Entity victim, ItemStack item) {
        return MeleeDamage.INSTANCE.snapshot(attacker, victim, false, item, services).amount();
    }

    private static LivingEntity spawn(EntityType type, double z) {
        LivingEntity e = new LivingEntity(type);
        e.setInstance(instance, new Pos(0, 64, z)).join();
        return e;
    }

    @Test
    void smiteAddsVsUndeadOnly() {
        LivingEntity atk = spawn(EntityType.ZOMBIE, 80);
        Entity undead = spawn(EntityType.SKELETON, 81);
        Entity living = spawn(EntityType.PIG, 82);
        ItemStack sword = swordWith(Smite.KEY, 3);
        assertEquals(14.5f, melee(atk, undead, sword), EPS); // 7 + 2.5×3
        assertEquals(7.0f, melee(atk, living, sword), EPS);
    }

    @Test
    void baneAddsVsArthropodsOnly() {
        LivingEntity atk = spawn(EntityType.ZOMBIE, 83);
        Entity arthropod = spawn(EntityType.SPIDER, 84);
        Entity undead = spawn(EntityType.ZOMBIE, 85);
        ItemStack sword = swordWith(Bane.KEY, 3);
        assertEquals(14.5f, melee(atk, arthropod, sword), EPS); // 7 + 2.5×3
        assertEquals(7.0f, melee(atk, undead, sword), EPS);     // undead is not an arthropod
    }
}
