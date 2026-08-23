package net.swofty.commons.skyblock.auctions;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.item.Material;

import java.util.List;

@Getter
public enum AuctionCategories {
    WEAPONS(Material.GOLDEN_SWORD, Material.ORANGE_STAINED_GLASS_PANE, NamedTextColor.GOLD, List.of("Swords", "Bows", "Axes", "Magic Weapons")),
    ARMOR(Material.DIAMOND_CHESTPLATE, Material.BLUE_STAINED_GLASS_PANE, NamedTextColor.AQUA, List.of("Helmets", "Chestplates", "Leggings", "Boots")),
    ACCESSORIES(Material.SKELETON_SKULL, Material.GREEN_STAINED_GLASS_PANE, NamedTextColor.GREEN, List.of("Rings", "Necklaces", "Talismans", "Artifacts")),
    CONSUMABLES(Material.APPLE, Material.RED_STAINED_GLASS_PANE, NamedTextColor.RED, List.of("Potions", "Food", "Enchantments", "Books")),
    BLOCKS(Material.COBBLESTONE, Material.BROWN_STAINED_GLASS_PANE, NamedTextColor.YELLOW, List.of("Building Blocks", "Decoration Blocks", "Redstone", "Transportation")),
    TOOLS(Material.STICK, Material.PURPLE_STAINED_GLASS_PANE, NamedTextColor.LIGHT_PURPLE, List.of("Tools", "Specials", "Magic")),
    ;

    private final Material displayMaterial;
    private final Material material;
    private final TextColor color;
    private final List<String> examples;

    AuctionCategories(Material displayMaterial, Material material, TextColor color, List<String> examples) {
        this.displayMaterial = displayMaterial;
        this.material = material;
        this.color = color;
        this.examples = examples;
    }
}
