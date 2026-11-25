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

public final class StandardMobDefinitions {
    public static final MobDefinition ZOMBIE_VILLAGER = hostile("ZOMBIE_VILLAGER", "Zombie Villager",
            EntityType.ZOMBIE_VILLAGER, 1, 120, 24, 7, 1, 2, RegionType.GRAVEYARD,
            Material.ZOMBIE_VILLAGER_SPAWN_EGG, 15, 4, new MobType[]{MobType.UNDEAD},
            drop(ItemType.ROTTEN_FLESH, 1, 100, COMMON),
            drop(ItemType.POISONOUS_POTATO, 1, 2, UNCOMMON));

    public static final MobDefinition CRYPT_GHOUL = hostile("CRYPT_GHOUL", "Crypt Ghoul", EntityType.ZOMBIE,
            30, 2_000, 350, 36, 13, 30, RegionType.CRYPTS, Material.ZOMBIE_HEAD, 15, 1,
            new MobType[]{MobType.UNDEAD}, drop(ItemType.ROTTEN_FLESH, 1, 100, COMMON),
            drop(ItemType.ROTTEN_FLESH, 1, 50, COMMON));

    public static final MobDefinition GOLDEN_GHOUL = hostile("GOLDEN_GHOUL", "Golden Ghoul", EntityType.ZOMBIE,
            60, 45_000, 800, 50, 100, 30, RegionType.CRYPTS, Material.ZOMBIE_HEAD, 15, 3,
            new MobType[]{MobType.UNDEAD}, drop(ItemType.ROTTEN_FLESH, 2, 100, COMMON),
            drop(ItemType.GOLD_INGOT, 1, 100, COMMON), drop(ItemType.GOLD_INGOT, 9, 35, COMMON),
            drop(ItemType.GOLDEN_POWDER, 1, 0.05, RNGESUS));

    public static final MobDefinition GRAVEL_SKELETON = hostile("RESPAWNING_SKELETON", "Gravel Skeleton",
            EntityType.SKELETON, 2, 100, 35, 0, 1, 1, RegionType.SPIDERS_DEN,
            Material.SKELETON_SKULL, 15, 3, new MobType[]{MobType.SKELETAL},
            drop(ItemType.BONE, 3, 100, COMMON), drop(ItemType.BONE, 1, 50, COMMON),
            drop(ItemType.BONE, 1, 50, COMMON), drop(ItemType.BONE, 1, 50, COMMON),
            drop(ItemType.BONE_DYE, 1, 0.000033, RNGESUS));

    public static final MobDefinition SPLITTER_SPIDER = hostile("SPLITTER_SPIDER_02", "Splitter Spider",
            EntityType.SPIDER, 2, 180, 30, 9, 2, 3, RegionType.SPIDERS_DEN, Material.SPIDER_EYE, 15, 2,
            new MobType[]{MobType.ARTHROPOD}, drop(ItemType.STRING, 1, 100, COMMON),
            drop(ItemType.SPIDER_EYE, 1, 10, COMMON));

    public static final MobDefinition DASHER_SPIDER = hostile("DASHER_SPIDER_04", "Dasher Spider",
            EntityType.SPIDER, 4, 170, 55, 10, 2, 8, RegionType.SPIDERS_DEN, Material.SPIDER_EYE, 15, 2,
            new MobType[]{MobType.ARTHROPOD}, drop(ItemType.STRING, 1, 100, COMMON),
            drop(ItemType.SPIDER_EYE, 1, 50, COMMON));

    public static final MobDefinition WEAVER_SPIDER = hostile("WEAVER_SPIDER_03", "Weaver Spider",
            EntityType.SPIDER, 3, 160, 35, 9, 2, 6, RegionType.SPIDERS_DEN, Material.SPIDER_EYE, 15, 2,
            new MobType[]{MobType.ARTHROPOD}, drop(ItemType.STRING, 1, 100, COMMON),
            drop(ItemType.SPIDER_EYE, 1, 50, COMMON));

    public static final MobDefinition SILVERFISH = hostile("SILVERFISH", "Silverfish", EntityType.SILVERFISH,
            2, 50, 20, 5, 0, 1, RegionType.SPIDERS_DEN, Material.STRING, 15, 2,
            new MobType[]{MobType.ARTHROPOD}, drop(ItemType.STRING, 1, 80, COMMON));

    public static final MobDefinition VORACIOUS_SPIDER = hostile("VORACIOUS_SPIDER_10", "Voracious Spider",
            EntityType.SPIDER, 10, 300, 80, 10, 2, 3, RegionType.SPIDERS_DEN, Material.SPIDER_EYE, 15, 2,
            new MobType[]{MobType.ARTHROPOD}, drop(ItemType.STRING, 1, 100, COMMON),
            drop(ItemType.SPIDER_EYE, 1, 10, COMMON));

    public static final MobDefinition RAIN_SLIME = hostile("RAIN_SLIME", "Rain Slime", EntityType.SLIME,
            8, 400, 100, 12, 5, 5, RegionType.SPIDERS_DEN, Material.SLIME_BALL, 15, 4,
            new MobType[]{MobType.CUBIC}, drop(ItemType.SLIME_BALL, 3, 100, COMMON),
            drop(ItemType.SLIME_BALL, 1, 50, COMMON), drop(ItemType.SLIME_BALL, 1, 50, COMMON));

    public static final MobDefinition ENDERMAN_42 = enderman(42, 4_500, 500, 40, 10, 12);
    public static final MobDefinition ENDERMAN_45 = enderman(45, 6_000, 600, 42, 12, 12);
    public static final MobDefinition ENDERMAN_50 = enderman(50, 9_000, 700, 44, 15, 12);

    public static final MobDefinition ENDERMITE_37 = endermite(37, 2_000, 400, 10, 8,
            drop(ItemType.END_STONE, 1, 100, COMMON), drop(ItemType.END_STONE, 1, 50, COMMON),
            drop(ItemType.PEARLESCENT_DYE, 1, 0.00001, RNGESUS));
    public static final MobDefinition ENDERMITE_40 = endermite(40, 2_300, 475, 11, 12,
            drop(ItemType.END_STONE, 1, 100, COMMON), drop(ItemType.END_STONE, 1, 80, COMMON),
            drop(ItemType.PEARLESCENT_DYE, 1, 0.00001, RNGESUS));

    public static final MobDefinition NEST_ENDERMITE = hostile("NEST_ENDERMITE", "Nest Endermite",
            EntityType.ENDERMITE, 50, 4_500, 750, 38, 22, 24, RegionType.THE_END,
            Material.ENDERMITE_SPAWN_EGG, 25, 5, new MobType[]{MobType.ARTHROPOD, MobType.ENDER},
            drop(ItemType.ENCHANTED_END_STONE, 1, 100, UNCOMMON), drop(ItemType.MITE_GEL, 1, 100, UNCOMMON),
            drop(ItemType.PEARLESCENT_DYE, 1, 0.00002, RNGESUS));

    public static final MobDefinition WITHER_SKELETON = hostile("WITHER_SKELETON", "Wither Skeleton",
            EntityType.WITHER_SKELETON, 70, 600_000, 3_000, 120, 20, 100, RegionType.STRONGHOLD,
            Material.WITHER_SKELETON_SKULL, 20, 4, new MobType[]{MobType.WITHER, MobType.SKELETAL},
            drop(ItemType.BONE, 3, 100, COMMON), drop(ItemType.SWORD_OF_BAD_HEALTH, 1, 0.05, RNGESUS),
            drop(ItemType.BONE_DYE, 1, 0.000033, RNGESUS),
            drop(ItemType.CYCLAMEN_DYE, 1, 0.00001, RNGESUS));

    public static final MobDefinition MUSHROOM_BULL = hostile("CHARGING_MUSHROOM_COW", "Mushroom Bull",
            EntityType.MOOSHROOM, 80, 2_500_000, 5_000, 120, 20, 80, RegionType.MYSTIC_MARSH,
            Material.RED_MUSHROOM, 20, 3, new MobType[]{MobType.ANIMAL},
            drop(ItemType.RAW_BEEF, 1, 100, COMMON), drop(ItemType.RED_MUSHROOM, 4, 100, COMMON),
            drop(ItemType.LEATHER, 1, 100, COMMON), drop(ItemType.DIGESTED_MUSHROOMS, 1, 20, UNCOMMON),
            drop(ItemType.CYCLAMEN_DYE, 1, 0.00001, RNGESUS));

    public static final MobDefinition PACK_SPIRIT = hostile("PACK_SPIRIT", "Pack Spirit", EntityType.WOLF,
            30, 6_000, 300, 22, 11, 10, RegionType.HOWLING_CAVE, Material.BONE, 15, 2,
            new MobType[]{MobType.ANIMAL, MobType.SPOOKY}, drop(ItemType.BONE, 1, 50, COMMON),
            drop(ItemType.BIRCH_LOG, 1, 15, UNCOMMON), drop(ItemType.OAK_LOG, 1, 15, UNCOMMON));

    public static final MobDefinition HOWLING_SPIRIT = hostile("HOWLING_SPIRIT", "Howling Spirit",
            EntityType.WOLF, 35, 7_000, 500, 22, 11, 10, RegionType.HOWLING_CAVE, Material.BONE, 15, 2,
            new MobType[]{MobType.ANIMAL, MobType.SPOOKY}, drop(ItemType.ACACIA_LOG, 1, 15, UNCOMMON),
            drop(ItemType.SPRUCE_LOG, 1, 15, UNCOMMON), drop(ItemType.DARK_OAK_LOG, 1, 15, UNCOMMON));

    public static final MobDefinition BARBARIAN = MobDefinition.builder("BARBARIAN", "Barbarian", EntityType.PIGLIN)
            .level(75).stats(2_000_000, 3_500, 100).rewards(120, 20, 100)
            .types(MobType.HUMANOID, MobType.INFERNAL).gui(new GUIMaterial(Material.PIGLIN_SPAWN_EGG))
            .loot(new MobLootTable("BARBARIAN", drop(ItemType.CYCLAMEN_DYE, 1, 0.00004, RNGESUS)))
            .behaviour(RegionType.CRIMSON_ISLE, true, false).build();

    private StandardMobDefinitions() {
    }

    private static MobDefinition enderman(int level, double health, double damage, long xp, int coins, int orbs) {
        String id = "ENDERMAN_" + level;
        return MobDefinition.builder(id, "Enderman", EntityType.ENDERMAN).level(level).stats(health, damage, 100)
                .rewards(xp, coins, orbs).types(MobType.ENDER)
                .loot(new MobLootTable(id,
                        drop(ItemType.ENDER_PEARL, 1, 100, COMMON),
                        drop(ItemType.ENDER_PEARL, 1, 75, COMMON),
                        drop(ItemType.ENDER_PEARL, 1, 25, COMMON),
                        drop(ItemType.ENCHANTED_ENDER_PEARL, 1, 1, UNCOMMON),
                        drop(ItemType.PEARLESCENT_DYE, 1, 0.00001, RNGESUS)))
                .gui(new GUIMaterial(Material.ENDER_PEARL)).bestiary(25, 4)
                .behaviour(RegionType.THE_END, true, false).build();
    }

    private static MobDefinition endermite(int level, double health, double damage, int coins, int orbs,
                                            MobLootTable.Drop... drops) {
        return hostile("ENDERMITE_" + level, "Endermite", EntityType.ENDERMITE, level, health, damage,
                40, coins, orbs, RegionType.THE_END, Material.ENDERMITE_SPAWN_EGG, 25, 5,
                new MobType[]{MobType.ARTHROPOD, MobType.ENDER}, drops);
    }

    private static MobDefinition hostile(String id, String name, EntityType entityType, int level,
                                          double health, double damage, long xp, int coins, int orbs,
                                          RegionType region, Material material, int tier, int bracket,
                                          MobType[] types, MobLootTable.Drop... drops) {
        return MobDefinition.builder(id, name, entityType).level(level).stats(health, damage, 100)
                .rewards(xp, coins, orbs).types(types).loot(new MobLootTable(id, drops))
                .gui(new GUIMaterial(material)).bestiary(tier, bracket).behaviour(region, true, true).build();
    }

    private static MobLootTable.Drop drop(ItemType item, int amount, double chance, BestiaryDropRarity rarity) {
        return new MobLootTable.Drop(item, amount, chance, rarity);
    }

    private static MobLootTable.Drop drop(ItemType item, int minimum, int maximum, double chance,
                                           BestiaryDropRarity rarity) {
        return new MobLootTable.Drop(item, minimum, maximum, chance, rarity);
    }
}
