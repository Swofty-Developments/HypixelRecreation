package io.github.term4.polyp.mechanics.damage.types;

import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.registry.RegistryKey;

/**
 * Cache of the vanilla Minecraft damage-type registry keys we reference (Minestom {@link DamageType} keys, not our
 * {@code damage.types.DamageType}); each of our types passes one as its {@code minecraftType}. Valid without server init.
 */
public final class VanillaTypes {

    private VanillaTypes() {}

    public static final RegistryKey<DamageType> PLAYER_ATTACK = DamageType.PLAYER_ATTACK;
    public static final RegistryKey<DamageType> GENERIC = DamageType.GENERIC;
    public static final RegistryKey<DamageType> FALL = DamageType.FALL;
    public static final RegistryKey<DamageType> IN_FIRE = DamageType.IN_FIRE;
    public static final RegistryKey<DamageType> ON_FIRE = DamageType.ON_FIRE;
    public static final RegistryKey<DamageType> LAVA = DamageType.LAVA;
    public static final RegistryKey<DamageType> CACTUS = DamageType.CACTUS;
    public static final RegistryKey<DamageType> DROWN = DamageType.DROWN;
    public static final RegistryKey<DamageType> IN_WALL = DamageType.IN_WALL;
    public static final RegistryKey<DamageType> STARVE = DamageType.STARVE;
    public static final RegistryKey<DamageType> EXPLOSION = DamageType.EXPLOSION;
    public static final RegistryKey<DamageType> MAGIC = DamageType.MAGIC;

}
