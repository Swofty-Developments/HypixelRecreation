package io.github.term4.polyp.mechanics.attribute.catalog.enchant;

import io.github.term4.polyp.mechanics.attribute.Attribute;
import io.github.term4.polyp.mechanics.attribute.AttributeConfigResolver.AttributeContext;
import io.github.term4.polyp.mechanics.attribute.source.ItemSource;
import io.github.term4.polyp.mechanics.attribute.source.Source;
import io.github.term4.polyp.mechanics.attribute.combat.CombatFacts;
import io.github.term4.polyp.mechanics.attribute.combat.HitContext;
import io.github.term4.polyp.mechanics.attribute.combat.MonsterTypes;
import io.github.term4.polyp.mechanics.attribute.combat.OnHit;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Bane of Arthropods - {@code 2.5 × level} melee bonus vs arthropods (like {@link Smite}, gated on the combat
 * {@link CombatFacts#TARGET target}), plus {@code 20 + rand(10×level)} ticks of Slowness IV as an {@link OnHit} effect.
 * The slowness needs {@link io.github.term4.polyp.mechanics.attribute.catalog.effect.Slowness} registered to
 * move the victim.
 */
public final class Bane {

    public static final Key KEY = Key.key("minecraft:bane_of_arthropods");
    private static final double PER_LEVEL = 2.5;
    private static final byte SLOWNESS_AMPLIFIER = 3; // Slowness IV

    private Bane() {}

    public static final Source INSTANCE = new BaneSource();

    private static final class BaneSource extends ItemSource implements OnHit {
        private BaneSource() { super(KEY); }

        @Override public List<Mod> modifiers(int level, AttributeContext ctx) {
            Entity target = ctx.fact(CombatFacts.TARGET);
            if (level <= 0 || !MonsterTypes.isArthropod(target)) return List.of();
            return List.of(new Mod(Attribute.MELEE_FLAT_ADD, AttributeOperation.ADD_VALUE, PER_LEVEL * level));
        }

        @Override public void onHit(HitContext ctx) {
            if (ctx.level() <= 0 || !MonsterTypes.isArthropod(ctx.victim())) return;
            int ticks = 20 + ThreadLocalRandom.current().nextInt(10 * ctx.level()); // 1.8 EnchantmentWeaponDamage.a
            // flags 0 would hide the swirl + icon vanilla shows
            ctx.victim().addEffect(new Potion(PotionEffect.SLOWNESS, SLOWNESS_AMPLIFIER, ticks,
                    (byte) (Potion.PARTICLES_FLAG | Potion.ICON_FLAG)));
        }
    }
}
