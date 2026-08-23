package net.swofty.type.skyblockgeneric.bazaar;

import lombok.Getter;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

@Getter
public enum BazaarCategories {
    FARMING(Material.GOLDEN_HOE, Material.YELLOW_STAINED_GLASS_PANE, "<e>",
            new BazaarItemSet(ItemType.WHEAT, "Wheat & Seeds",
                    ItemType.WHEAT, ItemType.ENCHANTED_BREAD, ItemType.HAY_BALE, ItemType.ENCHANTED_HAY_BALE, ItemType.SEEDS, ItemType.ENCHANTED_SEEDS),
            new BazaarItemSet(ItemType.CARROT, "Carrot",
                    ItemType.CARROT, ItemType.ENCHANTED_CARROT, ItemType.ENCHANTED_GOLDEN_CARROT),
            new BazaarItemSet(ItemType.POTATO, "Potato",
                    ItemType.POTATO, ItemType.POISONOUS_POTATO, ItemType.ENCHANTED_POTATO, ItemType.ENCHANTED_POISONOUS_POTATO, ItemType.ENCHANTED_BAKED_POTATO),
            new BazaarItemSet(ItemType.PUMPKIN, "Pumpkin",
                    ItemType.PUMPKIN, ItemType.ENCHANTED_PUMPKIN, ItemType.POLISHED_PUMPKIN),
            new BazaarItemSet(ItemType.MELON_SLICE, "Melon",
                    ItemType.MELON_SLICE, ItemType.ENCHANTED_GLISTERING_MELON, ItemType.ENCHANTED_MELON, ItemType.ENCHANTED_MELON_BLOCK),
            new BazaarItemSet(ItemType.MUSHROOM, "Mushrooms",
                    ItemType.RED_MUSHROOM, ItemType.BROWN_MUSHROOM, ItemType.RED_MUSHROOM_BLOCK, ItemType.BROWN_MUSHROOM_BLOCK, ItemType.ENCHANTED_RED_MUSHROOM_BLOCK, ItemType.ENCHANTED_BROWN_MUSHROOM_BLOCK),
            new BazaarItemSet(ItemType.COCOA_BEANS, "Cocoa Beans",
                    ItemType.COCOA_BEANS, ItemType.ENCHANTED_COCOA_BEANS, ItemType.ENCHANTED_COOKIE),
            new BazaarItemSet(ItemType.CACTUS_GREEN, "Cactus",
                    ItemType.CACTUS, ItemType.ENCHANTED_CACTUS_GREEN, ItemType.ENCHANTED_CACTUS),
            new BazaarItemSet(ItemType.SUGAR_CANE, "Sugar Cane",
                    ItemType.SUGAR_CANE, ItemType.ENCHANTED_SUGAR, ItemType.ENCHANTED_PAPER, ItemType.ENCHANTED_SUGAR_CANE),
            new BazaarItemSet(ItemType.LEATHER, "Leather & Beef",
                    ItemType.LEATHER, ItemType.ENCHANTED_LEATHER, ItemType.RAW_BEEF, ItemType.ENCHANTED_RAW_BEEF),
            new BazaarItemSet(ItemType.RAW_PORKCHOP, "Pork",
                    ItemType.RAW_PORKCHOP, ItemType.ENCHANTED_PORK, ItemType.ENCHANTED_GRILLED_PORK),
            new BazaarItemSet(ItemType.RAW_CHICKEN, "Chicken & Feather",
                    ItemType.RAW_CHICKEN, ItemType.ENCHANTED_RAW_CHICKEN, ItemType.FEATHER, ItemType.ENCHANTED_FEATHER, ItemType.ENCHANTED_EGG, ItemType.SUPER_ENCHANTED_EGG, ItemType.OMEGA_ENCHANTED_EGG),
            new BazaarItemSet(ItemType.MUTTON, "Mutton",
                    ItemType.MUTTON, ItemType.ENCHANTED_MUTTON, ItemType.ENCHANTED_COOKED_MUTTON),
            new BazaarItemSet(ItemType.RAW_RABBIT, "Rabbit",
                    ItemType.RAW_RABBIT, ItemType.RABBIT_FOOT, ItemType.RABBIT_HIDE, ItemType.ENCHANTED_RAW_RABBIT, ItemType.ENCHANTED_RABBIT_FOOT, ItemType.ENCHANTED_RABBIT_HIDE),
            new BazaarItemSet(ItemType.NETHER_WART, "Nether Warts",
                    ItemType.NETHER_WART, ItemType.ENCHANTED_NETHER_WART)

    ),
    MINING(Material.DIAMOND_PICKAXE, Material.BLUE_STAINED_GLASS_PANE, "<b>",
            new BazaarItemSet(ItemType.COBBLESTONE, "Cobblestone",
                    ItemType.COBBLESTONE, ItemType.ENCHANTED_COBBLESTONE),
            new BazaarItemSet(ItemType.COAL, "Coal",
                    ItemType.COAL, ItemType.ENCHANTED_COAL, ItemType.ENCHANTED_CHARCOAL, ItemType.ENCHANTED_COAL_BLOCK),
            new BazaarItemSet(ItemType.IRON_INGOT, "Iron",
                    ItemType.IRON_INGOT, ItemType.ENCHANTED_IRON_INGOT, ItemType.ENCHANTED_IRON_BLOCK),
            new BazaarItemSet(ItemType.GOLD_INGOT, "Gold",
                    ItemType.GOLD_INGOT, ItemType.ENCHANTED_GOLD_INGOT, ItemType.ENCHANTED_GOLD_BLOCK),
            new BazaarItemSet(ItemType.DIAMOND, "Diamond",
                    ItemType.DIAMOND, ItemType.ENCHANTED_DIAMOND, ItemType.ENCHANTED_DIAMOND_BLOCK),
            new BazaarItemSet(ItemType.LAPIS_LAZULI, "Lapis",
                    ItemType.LAPIS_LAZULI, ItemType.ENCHANTED_LAPIS_LAZULI, ItemType.ENCHANTED_LAPIS_LAZULI_BLOCK),
            new BazaarItemSet(ItemType.EMERALD, "Emerald",
                    ItemType.EMERALD, ItemType.ENCHANTED_EMERALD, ItemType.ENCHANTED_EMERALD_BLOCK),
            new BazaarItemSet(ItemType.REDSTONE, "Redstone",
                    ItemType.REDSTONE, ItemType.ENCHANTED_REDSTONE, ItemType.ENCHANTED_REDSTONE_BLOCK),
            new BazaarItemSet(ItemType.QUARTZ, "Quartz",
                    ItemType.QUARTZ, ItemType.ENCHANTED_QUARTZ, ItemType.ENCHANTED_QUARTZ_BLOCK),
            new BazaarItemSet(ItemType.OBSIDIAN, "Obsidian",
                    ItemType.OBSIDIAN, ItemType.ENCHANTED_OBSIDIAN),
            new BazaarItemSet(ItemType.GLOWSTONE_DUST, "Glowstone",
                    ItemType.GLOWSTONE_DUST, ItemType.ENCHANTED_GLOWSTONE_DUST, ItemType.ENCHANTED_GLOWSTONE),
            new BazaarItemSet(ItemType.FLINT, "Flint & Gravel",
                    ItemType.FLINT, ItemType.ENCHANTED_FLINT, ItemType.GRAVEL),
            new BazaarItemSet(ItemType.HARD_STONE, "Hard Stone",
                    ItemType.HARD_STONE, ItemType.ENCHANTED_HARD_STONE, ItemType.CONCENTRATED_STONE),
            new BazaarItemSet(ItemType.ICE, "Ice",
                    ItemType.ICE, ItemType.PACKED_ICE, ItemType.ENCHANTED_ICE, ItemType.ENCHANTED_PACKED_ICE, ItemType.GLACIAL_FRAGMENT),
            new BazaarItemSet(ItemType.NETHERRACK, "Netherrack",
                    ItemType.NETHERRACK, ItemType.ENCHANTED_NETHERRACK),
            new BazaarItemSet(ItemType.SAND, "Sand",
                    ItemType.SAND, ItemType.ENCHANTED_SAND, ItemType.RED_SAND, ItemType.ENCHANTED_RED_SAND, ItemType.ENCHANTED_RED_SAND_CUBE),
            new BazaarItemSet(ItemType.END_STONE, "End Stone",
                    ItemType.END_STONE, ItemType.ENCHANTED_END_STONE),
            new BazaarItemSet(ItemType.SNOW_BLOCK, "Snow",
                    ItemType.SNOWBALL, ItemType.SNOW_BLOCK, ItemType.ENCHANTED_SNOW_BLOCK),
            new BazaarItemSet(ItemType.SULPHUR, "Sulphur",
                    ItemType.SULPHUR, ItemType.ENCHANTED_SULPHUR, ItemType.ENCHANTED_SULPHUR_CUBE),
            new BazaarItemSet(ItemType.MYCELIUM, "Mycelium",
                    ItemType.MYCELIUM, ItemType.ENCHANTED_MYCELIUM, ItemType.ENCHANTED_MYCELIUM_CUBE),
            new BazaarItemSet(ItemType.MITHRIL, "Dwarven Mines",
                    ItemType.MITHRIL, ItemType.ENCHANTED_MITHRIL, ItemType.REFINED_MITHRIL, ItemType.TITANIUM, ItemType.ENCHANTED_TITANIUM, ItemType.REFINED_TITANIUM, ItemType.STARFALL, ItemType.TREASURITE, ItemType.SORROW)
    ),
    COMBAT(Material.IRON_SWORD, Material.RED_STAINED_GLASS_PANE, "<c>",
            new BazaarItemSet(ItemType.ROTTEN_FLESH, "Rotten Flesh",
                    ItemType.ROTTEN_FLESH, ItemType.ENCHANTED_ROTTEN_FLESH),
            new BazaarItemSet(ItemType.BONE, "Bone",
                    ItemType.BONE, ItemType.ENCHANTED_BONE, ItemType.ENCHANTED_BONE_BLOCK, ItemType.ENCHANTED_BONE_MEAL),
            new BazaarItemSet(ItemType.STRING, "Arachnids",
                    ItemType.STRING, ItemType.ENCHANTED_STRING, ItemType.SPIDER_EYE, ItemType.ENCHANTED_SPIDER_EYE, ItemType.ENCHANTED_FERMENTED_SPIDER_EYE, ItemType.SOUL_STRING),
            new BazaarItemSet(ItemType.GUNPOWDER, "Gunpowder",
                    ItemType.GUNPOWDER, ItemType.ENCHANTED_GUNPOWDER, ItemType.ENCHANTED_FIREWORK_ROCKET),
            new BazaarItemSet(ItemType.ENDER_PEARL, "Ender Pearl",
                    ItemType.ENDER_PEARL, ItemType.ENCHANTED_ENDER_PEARL, ItemType.ENCHANTED_EYE_OF_ENDER),
            new BazaarItemSet(ItemType.GHAST_TEAR, "Ghast Tear",
                    ItemType.GHAST_TEAR, ItemType.ENCHANTED_GHAST_TEAR),
            new BazaarItemSet(ItemType.SLIME_BALL, "Slime Drops",
                    ItemType.SLIME_BALL, ItemType.ENCHANTED_SLIME_BALL, ItemType.ENCHANTED_SLIME_BLOCK, ItemType.MAGMA_CREAM, ItemType.ENCHANTED_MAGMA_CREAM, ItemType.WHIPPED_MAGMA_CREAM),
            new BazaarItemSet(ItemType.BLAZE_ROD, "Blaze Rod",
                    ItemType.BLAZE_ROD, ItemType.ENCHANTED_BLAZE_POWDER, ItemType.ENCHANTED_BLAZE_ROD),
            new BazaarItemSet(ItemType.FEATHER, "Mythological",
                    ItemType.GRIFFIN_FEATHER, ItemType.DAEDALUS_STICK, ItemType.ANCIENT_CLAW, ItemType.ENCHANTED_ANCIENT_CLAW)
    ),
    WOODS_AND_FISHES(Material.FISHING_ROD, Material.ORANGE_STAINED_GLASS_PANE, "<6>"),
    ODDITIES(Material.ENCHANTING_TABLE, Material.PINK_STAINED_GLASS_PANE, "<d>",
            new BazaarItemSet(ItemType.ATTRIBUTE_SHARD, "Attribute Shards", AttributeBazaarItems.values())),
    ;

    private final Material displayItem;
    private final Material glassItem;
    private final Set<BazaarItemSet> items;
    private final String color;

    BazaarCategories(Material displayItem, Material glassItem, String color, BazaarItemSet... items) {
        this.displayItem = displayItem;
        this.glassItem = glassItem;
        this.color = color;
        this.items = Set.of(items);
    }

    BazaarCategories(Material displayItem, Material glassItem, String color) {
        this.displayItem = displayItem;
        this.glassItem = glassItem;
        this.color = color;
        this.items = Set.of();
    }

    @Override
    public String toString() {
        return StringUtility.toNormalCase(name());
    }

    public static Map.Entry<BazaarCategories, BazaarItemSet> getFromItem(ItemType itemType) {
        for (BazaarCategories category : BazaarCategories.values()) {
            for (BazaarItemSet itemSet : category.items) {
                if (itemSet.items.contains(itemType)) {
                    return new AbstractMap.SimpleEntry<>(category, itemSet);
                }
            }
        }
        return null;
    }
}
