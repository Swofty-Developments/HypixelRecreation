package io.github.term4.polyp.mechanics.knockback;

import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.Nullable;

/**
 * A pluggable knockback transform applied after the base/extra/friction/bounds pipeline. Each component runs in order
 * on the final vector (b/t) and may return a replacement (add/scale/snap); {@code null} leaves it unchanged. Self-gates
 * from the {@link KnockbackConfigResolver.KnockbackContext}, and can apply non-linear logic the base pipeline can't.
 */
@FunctionalInterface
public interface KnockbackComponent {

    /** The transformed knockback (blocks/tick) given the current vector, or {@code null} to leave it unchanged. */
    @Nullable Vec apply(KnockbackConfigResolver.KnockbackContext ctx, Vec kb);
}
