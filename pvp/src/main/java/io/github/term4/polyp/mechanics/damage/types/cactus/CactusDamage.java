package io.github.term4.polyp.mechanics.damage.types.cactus;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.damage.DamageProducers;
import io.github.term4.polyp.mechanics.damage.DamageSystem;
import io.github.term4.polyp.mechanics.damage.EnvironmentalDamageTicker;
import io.github.term4.polyp.mechanics.damage.EnvironmentalTickProducer;
import io.github.term4.polyp.util.BlockContact;
import io.github.term4.polyp.mechanics.damage.types.DamageType;
import io.github.term4.polyp.mechanics.damage.types.DamageTypeConfig;
import io.github.term4.polyp.mechanics.damage.types.VanillaTypes;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.block.Block;

// onTouch rejected: it fits cactus's inset shape, but only fires for blocks carrying a BlockHandler - which a library
//  can't guarantee on every cactus in every world. The entity-side scan below is world-agnostic. Revisit only if the lib owns block placement.
/**
 * Cactus contact damage ({@code minecraft:cactus}). Vanilla 1.8: 1.0 damage attempted every tick while overlapping a
 * cactus collision shape (inset 1/16, not the full cell); the invul window gates the cadence. Self-driven via
 * {@link EnvironmentalDamageTicker}; tunables come from the type's {@link DamageTypeConfig}.
 */
public final class CactusDamage extends DamageType implements EnvironmentalTickProducer {

    public static final Key KEY = Key.key("minecraft:cactus");
    public static final CactusDamage INSTANCE = new CactusDamage();

    private boolean registered;

    private CactusDamage() {
        super(KEY, "Cactus", VanillaTypes.CACTUS, DamageTypeConfig.builder(KEY).build());
    }

    @Override
    public void enable(DamageSystem system, Polyp polyp) {
        EnvironmentalDamageTicker.instance().bind(system);
        if (!registered) {
            EnvironmentalDamageTicker.instance().register(this);
            registered = true;
        }
    }

    @Override
    public void disable() {
        if (registered) {
            EnvironmentalDamageTicker.instance().unregister(this);
            registered = false;
        }
    }

    @Override
    public void tick(LivingEntity living, DamageSystem sys) {
        // cell contact, not the 1/16-inset shape: 1.8 doBlockCollisions walks CELLS (bounds only gate movement)
        if (!BlockContact.touching(living, b -> b.compare(Block.CACTUS))) return;
        DamageProducers.emit(sys, living, this);
    }
}
