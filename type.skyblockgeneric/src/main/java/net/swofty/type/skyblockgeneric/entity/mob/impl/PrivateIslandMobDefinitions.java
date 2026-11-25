package net.swofty.type.skyblockgeneric.entity.mob.impl;

import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.loottable.BestiaryDropRarity;
import net.swofty.type.skyblockgeneric.loottable.MobLootTable;
import net.swofty.type.skyblockgeneric.region.RegionType;

import static net.swofty.type.skyblockgeneric.loottable.BestiaryDropRarity.COMMON;
import static net.swofty.type.skyblockgeneric.loottable.BestiaryDropRarity.RNGESUS;
import static net.swofty.type.skyblockgeneric.loottable.BestiaryDropRarity.UNCOMMON;

public final class PrivateIslandMobDefinitions {
    public static final MobDefinition SKELETON = hostile("SKELETON", "Skeleton", EntityType.SKELETON,
            100, 15, 6, 1, 1, Material.SKELETON_SKULL, new MobType[]{MobType.SKELETAL},
            drop(ItemType.BONE, 1, 2, 100, COMMON), drop(ItemType.BONE_DYE, 1, 0.000033, RNGESUS));
    public static final MobDefinition SPIDER = hostile("SPIDER", "Spider", EntityType.SPIDER,
            120, 35, 8, 1, 1, Material.SPIDER_EYE, new MobType[]{MobType.ARTHROPOD},
            drop(ItemType.STRING, 1, 100, COMMON), drop(ItemType.SPIDER_EYE, 1, 50, COMMON));
    public static final MobDefinition CREEPER = hostile("CREEPER", "Creeper", EntityType.CREEPER,
            80, 20, 8, 2, 2, Material.CREEPER_HEAD, new MobType[]{MobType.CUBIC},
            drop(ItemType.GUNPOWDER, 1, 100, COMMON));
    public static final MobDefinition ENDERMAN = hostile("ENDERMAN", "Enderman", EntityType.ENDERMAN,
            160, 40, 15, 2, 4, Material.ENDER_PEARL, new MobType[]{MobType.ENDER},
            false, drop(ItemType.ENDER_PEARL, 1, 100, COMMON));
    public static final MobDefinition WITCH = hostile("WITCH", "Witch", EntityType.WITCH,
            150, 20, 15, 1, 4, Material.POTION, new MobType[]{MobType.HUMANOID, MobType.ARCANE},
            drop(ItemType.GUNPOWDER, 1, 50, UNCOMMON), drop(ItemType.GLOWSTONE_DUST, 1, 50, UNCOMMON),
            drop(ItemType.GLASS_BOTTLE, 2, 20, UNCOMMON));
    public static final MobDefinition SLIME = hostile("SLIME", "Slime", EntityType.SLIME,
            80, 15, 4, 1, 1, Material.SLIME_BALL, new MobType[]{MobType.CUBIC},
            drop(ItemType.SLIME_BALL, 1, 100, COMMON));
    public static final MobDefinition COW = passive("COW", "Cow", EntityType.COW, 50, 3,
            Material.COW_SPAWN_EGG, new MobType[]{MobType.ANIMAL}, drop(ItemType.RAW_BEEF, 1, 100, COMMON),
            drop(ItemType.LEATHER, 1, 100, COMMON));
    public static final MobDefinition PIG = passive("PIG", "Pig", EntityType.PIG, 50, 3,
            Material.PIG_SPAWN_EGG, new MobType[]{MobType.ANIMAL}, drop(ItemType.RAW_PORKCHOP, 1, 100, COMMON));
    public static final MobDefinition CHICKEN = passive("CHICKEN", "Chicken", EntityType.CHICKEN, 20, 2,
            Material.CHICKEN_SPAWN_EGG, new MobType[]{MobType.ANIMAL}, drop(ItemType.FEATHER, 1, 100, COMMON),
            drop(ItemType.RAW_CHICKEN, 1, 100, COMMON));
    public static final MobDefinition SHEEP = passive("SHEEP", "Sheep", EntityType.SHEEP, 50, 3,
            Material.SHEEP_SPAWN_EGG, new MobType[]{MobType.ANIMAL}, drop(ItemType.MUTTON, 1, 100, COMMON),
            drop(ItemType.WHITE_WOOL, 1, 100, COMMON));
    public static final MobDefinition HORSE = passive("HORSE", "Horse", EntityType.HORSE, 15, 0,
            Material.SADDLE, new MobType[]{MobType.ANIMAL});
    public static final MobDefinition BAT = MobDefinition.builder("BAT", "Bat", EntityType.BAT)
            .level(3).stats(100, 0, 100).rewards(33, 100, 100).types(MobType.ANIMAL, MobType.AIRBORNE)
            .loot(new MobLootTable("BAT", drop(ItemType.BAT_TALISMAN, 1, 1, RNGESUS)))
            .gui(new GUIMaterial(Material.BAT_SPAWN_EGG)).behaviour(RegionType.PRIVATE_ISLAND, false, false).build();

    private PrivateIslandMobDefinitions() {
    }

    public static MobDefinition zombie(int level, String id) {
        return hostile(id, "Zombie", EntityType.ZOMBIE, 100, 20, 6, 1, 1, Material.ZOMBIE_HEAD,
                new MobType[]{MobType.UNDEAD}, drop(ItemType.ROTTEN_FLESH, 1, 100, COMMON),
                drop(ItemType.POISONOUS_POTATO, 1, 2, UNCOMMON), drop(ItemType.POTATO, 1, 1, UNCOMMON),
                drop(ItemType.CARROT, 1, 1, UNCOMMON));
    }

    private static MobDefinition hostile(String id, String name, EntityType type, double health, double damage,
                                          long xp, int coins, int orbs, Material material, MobType[] mobTypes,
                                          MobLootTable.Drop... drops) {
        return hostile(id, name, type, health, damage, xp, coins, orbs, material, mobTypes, true, drops);
    }

    private static MobDefinition hostile(String id, String name, EntityType type, double health, double damage,
                                          long xp, int coins, int orbs, Material material, MobType[] mobTypes,
                                          boolean targetsPlayers, MobLootTable.Drop... drops) {
        return MobDefinition.builder(id, name, type).stats(health, damage, 100).rewards(xp, coins, orbs)
                .types(mobTypes).loot(new MobLootTable(id, drops)).gui(new GUIMaterial(material))
                .behaviour(RegionType.PRIVATE_ISLAND, true, targetsPlayers).build();
    }

    private static MobDefinition passive(String id, String name, EntityType type, double health, long xp,
                                          Material material, MobType[] mobTypes, MobLootTable.Drop... drops) {
        return MobDefinition.builder(id, name, type).stats(health, 0, 100).rewards(xp, 0, 1)
                .types(mobTypes).loot(new MobLootTable(id, drops)).gui(new GUIMaterial(material))
                .behaviour(RegionType.PRIVATE_ISLAND, false, false).build();
    }

    private static MobLootTable.Drop drop(ItemType item, int amount, double chance, BestiaryDropRarity rarity) {
        return new MobLootTable.Drop(item, amount, chance, rarity);
    }

    private static MobLootTable.Drop drop(ItemType item, int minimum, int maximum, double chance,
                                           BestiaryDropRarity rarity) {
        return new MobLootTable.Drop(item, minimum, maximum, chance, rarity);
    }
}
