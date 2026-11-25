package io.github.term4.polyp.mechanics.attribute;

import io.github.term4.polyp.mechanics.attribute.defense.ArmorConfig;
import io.github.term4.polyp.mechanics.attribute.defense.Bypass;
import io.github.term4.polyp.mechanics.attribute.defense.MitigationRequest;
import io.github.term4.polyp.mechanics.attribute.defense.ProtectionConfig;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The staged mitigation pipeline (vanilla order: armor -&gt; resistance -&gt; EPF/protection; absorption is Minestom's,
 * applied later by {@code living.damage()}). Stages are replaceable via {@code AttributeConfig.mitigationStages}; each
 * built-in one self-gates on its {@link Bypass} flag and config.
 */
public final class MitigationPipeline {

    private MitigationPipeline() {}

    /** Reads and mutates {@link State#damage}. */
    @FunctionalInterface
    public interface Stage {
        void apply(State state);
    }

    /** Mutable state threaded through the stages. */
    public static final class State {
        public final AttributeSystem system;
        public final LivingEntity victim;
        public final MitigationRequest request;
        public final AttributeConfig cfg;
        public float damage;

        public State(AttributeSystem system, LivingEntity victim, MitigationRequest request, AttributeConfig cfg, float damage) {
            this.system = system;
            this.victim = victim;
            this.request = request;
            this.cfg = cfg;
            this.damage = damage;
        }
    }

    /** {@link ArmorConfig#damageAfterArmor} */
    public static final Stage ARMOR = s -> {
        Bypass bypass = s.request.bypass();
        ArmorConfig armor = s.cfg.armor;
        if (!bypass.armorStage() && !bypass.attribute(AttributeSystem.ARMOR_ATTRIBUTE_KEY) && armor != null && armor.enabled()) {
            s.damage = armor.damageAfterArmor(s.victim, s.damage);
        }
    };

    /** The vanilla integer curve ({@code (25 - 5*level) / 25}) or {@code resistancePerLevel}. */
    public static final Stage RESISTANCE = s -> {
        Bypass bypass = s.request.bypass();
        if (bypass.effectStage() || bypass.effect(AttributeSystem.RESISTANCE_KEY)) return;
        int level = s.system.effectLevel(s.victim, AttributeSystem.RESISTANCE_KEY);
        if (level <= 0) return;
        Double perLevel = s.cfg.resistancePerLevel;
        if (perLevel == null) {
            int reduced = 25 - 5 * level; // integer math, kept exact
            s.damage = reduced <= 0 ? 0f : s.damage * reduced / 25f;
        } else {
            double frac = 1.0 - perLevel * level;
            s.damage = frac <= 0 ? 0f : (float) (s.damage * frac);
        }
    };

    /** {@link ProtectionConfig#damageAfterProtection} */
    public static final Stage PROTECTION = s -> {
        Bypass bypass = s.request.bypass();
        ProtectionConfig protection = s.cfg.protection;
        if (!bypass.enchantStage() && protection != null && protection.enabled()) {
            s.damage = protection.damageAfterProtection(s.victim, s.request.categories(), s.damage, s.request.random(), bypass);
        }
    };

    /** The vanilla stage order, as a fresh mutable list. */
    public static List<Stage> vanilla() {
        return new ArrayList<>(VANILLA);
    }

    /** Runs {@code stages}, or the vanilla order when {@code null}; stops early once the damage reaches 0. */
    public static float run(@Nullable List<Stage> stages, State state) {
        for (Stage stage : stages != null ? stages : VANILLA) {
            stage.apply(state);
            if (state.damage <= 0) return state.damage;
        }
        return state.damage;
    }

    private static final List<Stage> VANILLA = List.of(ARMOR, RESISTANCE, PROTECTION);
}
