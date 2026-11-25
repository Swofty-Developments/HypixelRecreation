package io.github.term4.polyp.mechanics.attribute.combat;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;

import java.util.Set;

/**
 * Vanilla monster-type classification for the conditional combat enchants ({@code sensitive_to_smite} = undead,
 * {@code sensitive_to_bane_of_arthropods} = arthropods). Players are neither, so these only fire vs mobs.
 */
public final class MonsterTypes {
    private MonsterTypes() {}

    private static final Set<EntityType> UNDEAD = Set.of(
            EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK, EntityType.DROWNED,
            EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON, EntityType.WITHER,
            EntityType.ZOMBIFIED_PIGLIN, EntityType.ZOGLIN, EntityType.PHANTOM,
            EntityType.SKELETON_HORSE, EntityType.ZOMBIE_HORSE);

    private static final Set<EntityType> ARTHROPODS = Set.of(
            EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.SILVERFISH, EntityType.ENDERMITE, EntityType.BEE);

    public static boolean isUndead(Entity e) { return e != null && UNDEAD.contains(e.getEntityType()); }

    public static boolean isArthropod(Entity e) { return e != null && ARTHROPODS.contains(e.getEntityType()); }
}
