package net.swofty.type.generic.utility;

import net.minestom.server.entity.Entity;
import net.swofty.commons.text.Text;

/**
 * Markup-first helpers for raw Minestom entities, whose {@code setCustomName} only speaks
 * {@code Component}.
 */
public final class EntityUtility {

    private EntityUtility() {
    }

    public static void nameEntity(Entity entity, Text name) {
        entity.setCustomName(name.asComponent());
    }

    public static void nameEntity(Entity entity, String markup, Object... arguments) {
        nameEntity(entity, Text.of(markup, arguments));
    }

    public static void nameEntityVisible(Entity entity, Text name) {
        nameEntity(entity, name);
        entity.setCustomNameVisible(true);
    }

    public static void nameEntityVisible(Entity entity, String markup, Object... arguments) {
        nameEntityVisible(entity, Text.of(markup, arguments));
    }
}
