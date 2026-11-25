package io.github.term4.polyp;

import io.github.term4.polyp.mechanics.attack.AttackSystem;
import io.github.term4.polyp.mechanics.attribute.AttributeSystem;
import io.github.term4.polyp.mechanics.blocking.BlockingSystem;
import io.github.term4.polyp.mechanics.consumable.ConsumableSystem;
import io.github.term4.polyp.mechanics.damage.DamageSystem;
import io.github.term4.polyp.mechanics.durability.DurabilitySystem;
import io.github.term4.polyp.mechanics.explosion.ExplosionSystem;
import io.github.term4.polyp.mechanics.hunger.HungerSystem;
import io.github.term4.polyp.mechanics.knockback.KnockbackSystem;
import io.github.term4.polyp.mechanics.projectile.ProjectileSystem;
import io.github.term4.polyp.presets.vanilla18.Attributes;
import io.github.term4.polyp.presets.vanilla18.Damage;
import io.github.term4.polyp.presets.vanilla18.Knockback;
import io.github.term4.polyp.platform.fixes.FixesSystem;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Install is once-only: a duplicate throws instead of stacking listeners; unregister allows an explicit swap. */
class ModuleReinstallTest extends HeadlessServerTest {

    private record Case(String name, Class<? extends MechanicsModule> type, Supplier<MechanicsModule> install) {}

    @Test
    void duplicateInstallThrowsAndUnregisterAllowsAFreshOne() {
        List<Case> cases = List.of(
                new Case("damage", DamageSystem.class, () -> DamageSystem.install(polyp, Damage.config())),
                new Case("knockback", KnockbackSystem.class, () -> KnockbackSystem.install(polyp, Knockback.melee())),
                new Case("attributes", AttributeSystem.class, () -> AttributeSystem.install(polyp, Attributes.config())),
                new Case("attack", AttackSystem.class, () -> AttackSystem.install(polyp)),
                new Case("hunger", HungerSystem.class, () -> HungerSystem.install(polyp)),
                new Case("consumable", ConsumableSystem.class, () -> ConsumableSystem.install(polyp)),
                new Case("blocking", BlockingSystem.class, () -> BlockingSystem.install(polyp)),
                new Case("projectile", ProjectileSystem.class, () -> ProjectileSystem.install(polyp)),
                new Case("explosion", ExplosionSystem.class, () -> ExplosionSystem.install(polyp)),
                new Case("durability", DurabilitySystem.class, () -> DurabilitySystem.install(polyp)),
                new Case("fixes", FixesSystem.class, () -> FixesSystem.install(polyp)));
        for (Case c : cases) {
            polyp.unregister(c.type()); // the harness pre-installs damage/knockback/attributes
            MechanicsModule first = c.install().get();
            assertNotNull(first.node().getParent(), c.name() + ": install attaches the node");
            assertThrows(IllegalStateException.class, () -> c.install().get(), c.name() + ": duplicate install throws");
            assertSame(first, polyp.module(c.type()), c.name() + ": the registry keeps the live install");
            polyp.unregister(c.type());
            assertNull(first.node().getParent(), c.name() + ": unregister detaches the node");
            assertNull(polyp.module(c.type()), c.name() + ": unregister empties the registry slot");
            MechanicsModule second = c.install().get();
            assertNotNull(second.node().getParent(), c.name() + ": a fresh install works after unregister");
        }
    }
}
