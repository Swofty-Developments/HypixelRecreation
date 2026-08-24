package io.github.term4.polyp.mechanics.damage;

import io.github.term4.polyp.mechanics.damage.types.fall.FallDamage;
import io.github.term4.polyp.mechanics.damage.types.fall.FallDetail;
import io.github.term4.polyp.mechanics.damage.types.melee.MeleeDamage;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** {@code setInvulnerable(true)} is vanilla {@code abilities.isInvulnerable}: no damage, any gamemode. */
class InvulnerableFlagTest extends HeadlessServerTest {

    @Test
    void theFlagBlocksDamageInAnyGameMode() {
        LivingEntity attacker = zombie(new Pos(0, 64, 800));
        LivingEntity victim = zombie(new Pos(0, 64, 801));
        victim.setHealth(20f);
        victim.setInvulnerable(true);

        var melee = services.damage().apply(MeleeDamage.INSTANCE.snapshot(
                attacker, victim, false, ItemStack.of(Material.DIAMOND_SWORD), services));
        assertEquals(DamageSystem.DamageOutcome.IMMUNE, melee, "melee bounces off the flag");

        var fall = services.damage().apply(DamageSnapshot.of(victim, FallDamage.INSTANCE)
                .withDetail(FallDetail.of(30f)));
        assertEquals(DamageSystem.DamageOutcome.IMMUNE, fall, "a 30-block landing bounces off the flag");
        assertEquals(20f, victim.getHealth(), 1e-6);

        victim.setInvulnerable(false);
        var landed = services.damage().apply(MeleeDamage.INSTANCE.snapshot(
                attacker, victim, false, ItemStack.of(Material.DIAMOND_SWORD), services));
        assertEquals(DamageSystem.DamageOutcome.FRESH_DAMAGE, landed, "clearing the flag restores damage");
    }
}
