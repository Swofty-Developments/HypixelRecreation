package io.github.term4.polyp.mechanics.damage.types.melee;

import io.github.term4.polyp.Services;
import io.github.term4.polyp.mechanics.attribute.Attribute;
import io.github.term4.polyp.mechanics.attribute.AttributeConfigResolver.AttributeContext;
import io.github.term4.polyp.mechanics.attribute.AttributeSystem;
import io.github.term4.polyp.mechanics.attribute.combat.CombatFacts;
import io.github.term4.polyp.mechanics.damage.DamageSnapshot;
import io.github.term4.polyp.mechanics.damage.DamageSystem;
import io.github.term4.polyp.mechanics.damage.DamageConfigResolver.DamageContext;
import io.github.term4.polyp.mechanics.damage.types.DamageType;
import io.github.term4.polyp.mechanics.damage.types.VanillaTypes;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The melee ({@code minecraft:player_attack}) damage type. Crit is determined in the attack layer; this type owns the
 * crit multiplier and builds the {@link DamageSnapshot} the caller applies. Tunables come from {@link MeleeDamageConfig}.
 */
public final class MeleeDamage extends DamageType {

    public static final Key KEY = Key.key("minecraft:player_attack");
    private static final MeleeDamageConfig DEFAULT = MeleeDamageConfig.builder().critMultiplier(1.5).build();
    public static final MeleeDamage INSTANCE = new MeleeDamage();

    private MeleeDamage() {
        super(KEY, "Melee", VanillaTypes.PLAYER_ATTACK, DEFAULT);
    }

    /**
     * Builds a melee snapshot for a hit with {@code item}, in vanilla order:
     * <pre>attackDamage (1.8 weapon table + modifiers) → crit (×multiplier) → + melee flat-add (Sharpness)</pre>
     * The attack-damage and flat-add terms read attribute <em>values</em> through the {@link AttributeSystem}, so adding a
     * contributor is a registration, not an edit here; with none active (or the system absent) it reduces to table×crit.
     */
    public DamageSnapshot snapshot(Entity attacker, Entity target, boolean critical, @Nullable ItemStack item,
                                   Services services) {
        DamageSnapshot prelim = DamageSnapshot.of(target, this).withSource(attacker).withItem(item);
        // victim-scoped chain so per-profile melee tables resolve
        DamageSystem dmg = services != null ? services.damage() : null;
        DamageContext ctx = dmg != null ? dmg.contextFor(prelim) : DamageContext.of(prelim, services);
        MeleeDamageConfig cfg = ctx.typeConfig() instanceof MeleeDamageConfig p ? p : DEFAULT;

        Double tableBase = cfg.baseAmount(ctx);
        float amount = tableBase != null ? tableBase.floatValue() : 0f;

        AttributeSystem attrs = services != null ? services.attributes() : null;
        AttributeContext actx = (attrs != null && attacker instanceof LivingEntity le)
                ? attrs.context(le, item != null ? item : le.getItemInMainHand()).with(CombatFacts.TARGET, target)
                : null;
        if (actx != null) amount = (float) actx.value(Attribute.ATTACK_DAMAGE, amount);

        if (critical) {
            Double crit = cfg.critMultiplier(ctx);
            if (crit != null) amount *= crit.floatValue();
        }

        if (actx != null) amount += (float) actx.value(Attribute.MELEE_FLAT_ADD, 0);

        return prelim.withAmount(amount);
    }

    /**
     * Whether the hit carries enchantment ("magic") damage - Sharpness, or Smite/Bane against an applicable target - the
     * vanilla magic-crit gate ({@code i1 > 0}: {@link Attribute#MELEE_FLAT_ADD} resolved with the target creature fact).
     */
    public static boolean enchantCritical(Entity attacker, Entity target, @Nullable ItemStack item, Services services) {
        AttributeSystem attrs = services != null ? services.attributes() : null;
        if (attrs == null || !(attacker instanceof LivingEntity le)) return false;
        ItemStack weapon = item != null ? item : le.getItemInMainHand();
        return attrs.context(le, weapon).with(CombatFacts.TARGET, target).value(Attribute.MELEE_FLAT_ADD, 0) > 0;
    }
}
