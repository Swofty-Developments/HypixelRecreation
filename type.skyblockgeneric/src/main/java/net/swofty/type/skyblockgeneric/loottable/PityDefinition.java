package net.swofty.type.skyblockgeneric.loottable;

import net.kyori.adventure.key.Key;

public record PityDefinition(Key id, long threshold) {
    public PityDefinition {
        if (id == null) throw new IllegalArgumentException("Pity id cannot be null");
        if (threshold <= 0) throw new IllegalArgumentException("Pity threshold must be positive");
    }
}
