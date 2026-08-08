package io.github.term4.polyp.mechanics.attribute.combat;

import io.github.term4.polyp.Services;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;

/** The context handed to an {@link OnHit} enchant when its holder lands a hit. */
public record HitContext(LivingEntity attacker, LivingEntity victim, int level, ItemStack item, Services services) {}
