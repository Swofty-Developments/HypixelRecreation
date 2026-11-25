package io.github.term4.polyp.mechanics.attribute.defense;

import java.util.Random;
import java.util.Set;

/**
 * The per-hit inputs the damage side hands {@link io.github.term4.polyp.mechanics.attribute.AttributeSystem#mitigate}:
 * the {@link ProtectionCategory categories} that gate EPF, the {@link Bypass} spec, and the {@code random} driving the
 * 1.8 EPF roll.
 */
public record MitigationRequest(Set<ProtectionCategory> categories, Bypass bypass, Random random) {

    public static MitigationRequest of(Set<ProtectionCategory> categories, Bypass bypass, Random random) {
        return new MitigationRequest(categories, bypass != null ? bypass : Bypass.NONE, random);
    }
}
