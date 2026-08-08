package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bestiary;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIBestiary extends StatelessView {

    private static final int[] DISPLAY_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            29, 30, 32, 33
    };

    private enum BestiaryRegions {
        YOUR_ISLAND("§aYour Island", new GUIMaterial("c9c8881e42915a9d29bb61a16fb26d059913204d265df5b439b3d792acd56"), BestiaryCategories.YOUR_ISLAND, "§7View all of the mobs that you've", "§7found and killed on §aYour Island§7."),
        HUB("§aHub", new GUIMaterial("d7cc6687423d0570d556ac53e0676cb563bbdd9717cd8269bdebed6f6d4e7bf8"), BestiaryCategories.HUB, "§7View all of the mobs that you've", "§7found and killed in the §aHub§7."),
        THE_FARMING_ISLANDS("§aThe Farming Islands", new GUIMaterial("4d3a6bd98ac1833c664c4909ff8d2dc62ce887bdcf3cc5b3848651ae5af6b"), null, "§7View all of the mobs that you've", "§7found and killed in §aThe Farming", "§aIslands§7."),
        GARDEN("§bGarden", new GUIMaterial("f4880d2c1e7b86e87522e20882656f45bafd42f94932b2c5e0d6ecaa490cb4c"), null, "§7View all of the §6Pests §7that you've", "§7killed on the §bGarden§7."),
        SPIDERS_DEN("§cSpider's Den", new GUIMaterial("c754318a3376f470e481dfcd6c83a59aa690ad4b4dd7577fdad1c2ef08d8aee6"), null, "§7View all of the mobs that you've", "§7found and killed in the §cSpider's Den§7."),
        THE_END("§dThe End", new GUIMaterial("7840b87d52271d2a755dedc82877e0ed3df67dcc42ea479ec146176b02779a5"), BestiaryCategories.THE_END, "§7View all of the mobs that you've", "§7found and killed in §dThe End§7."),
        CRIMSON_ISLE("§cCrimson Isle", new GUIMaterial("c3687e25c632bce8aa61e0d64c24e694c3eea629ea944f4cf30dcfb4fbce071"), null, "§7View all of the mobs that you've", "§7found and killed in the §cCrimson Isle§7."),
        DEEP_CAVERNS("§bDeep Caverns", new GUIMaterial("569a1f114151b4521373f34bc14c2963a5011cdc25a6554c48c708cd96ebfc"), BestiaryCategories.DEEP_CAVERNS, "§7View all of the mobs that you've", "§7found and killed in the §bDeep Caverns§7."),
        DWARVEN_MINES("§2Dwarven Mines", new GUIMaterial("6b20b23c1aa2be0270f016b4c90d6ee6b8330a17cfef87869d6ad60b2ffbf3b5"), null, "§7View all of the mobs that you've", "§7found and killed in the §2Dwarven Mines§7."),
        CRYSTAL_HALLOWS("§5Crystal Hallows", new GUIMaterial("21dbe30b027acbceb612563bd877cd7ebb719ea6ed1399027dcee58bb9049d4a"), null, "§7View all of the mobs that you've", "§7found and killed in the §5Crystal", "§5Hollows§7."),
        THE_PARK("§3The Park", new GUIMaterial("a221f813dacee0fef8c59f76894dbb26415478d9ddfc44c2e708a6d3b7549b"), null, "§7View all of the mobs that you've", "§7found and killed in §3The Park§7."),
        GALATEA("§2Galatea", new GUIMaterial("9336d7cc95cbf6689f5e8c954294ec8d1efc494a4031325bb427bc81d56a484d"), null, "§7View all of the mobs that you've", "§7found and killed in §2Galatea§7."),
        SPOOKY_FESTIVAL("§6Spooky Festival", new GUIMaterial(Material.JACK_O_LANTERN), null, "§7View all of the mobs that you've", "§7found and killed during the §6Spooky", "§6Festival§7."),
        CATACOMBS("§cCatacombs", new GUIMaterial("964e1c3e315c8d8fffc37985b6681c5bd16a6f97ffd07199e8a05efbef103793"), null, "§7View all of the mobs that you've", "§7found and killed in §cThe Catacombs§7."),
        FISHING("§3Fishing", new GUIMaterial(Material.FISHING_ROD), null, "§7View all of the §3Sea Creatures §7that", "§7you've killed while fishing."),
        MYTHOLOGICAL_CREATURES("§bMythological Creatures", new GUIMaterial("83cc1cf672a4b2540be346ead79ac2d9ed19d95b6075bf95be0b6d0da61377be"), null, "§7View all of the §bMythological", "§bCreatures §7that you've killed."),
        JERRY("§6Jerry", new GUIMaterial("45f729736996a38e186fe9fe7f5a04b387ed03f3871ecc82fa78d8a2bdd31109"), null, "§7View all of the mobs that you've", "§7found and killed while fighting §6Jerry§7."),
        KUUDRA("§cKuudra", new GUIMaterial("5051c83d9ebf69013f1ec8c9efc979ec2d925a921cc877ff64abe09aadd2f6cc"), null, "§7View all of the mobs that you've", "§7found and killed while fighting §cKuudra§7.");

        private final String regionName;
        private final GUIMaterial guiMaterial;
        private final BestiaryCategories category;
        private final String[] lore;

        BestiaryRegions(String regionName, GUIMaterial guiMaterial, BestiaryCategories category, String... lore) {
            this.regionName = regionName;
            this.guiMaterial = guiMaterial;
            this.category = category;
            this.lore = lore;
        }
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Bestiary", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        // Title item
        layout.slot(4, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            List<String> lore = new ArrayList<>();
            player.getBestiaryData().getTotalDisplay(lore);
            return ItemStackCreator.getStack("§3Bestiary", Material.WRITTEN_BOOK, 1, lore);
        });

        // Display all regions
        BestiaryRegions[] allRegions = BestiaryRegions.values();
        for (int i = 0; i < DISPLAY_SLOTS.length && i < allRegions.length; i++) {
            BestiaryRegions region = allRegions[i];
            int slot = DISPLAY_SLOTS[i];

            layout.slot(slot, (s, c) -> ItemStackCreator.getUsingGUIMaterial(region.regionName, region.guiMaterial, 1, region.lore),
                    (click, c) -> {
                        if (region.category != null) {
                            c.player().openView(new GUIBestiaryIsland(region.category));
                        }
                    });
        }
    }
}
