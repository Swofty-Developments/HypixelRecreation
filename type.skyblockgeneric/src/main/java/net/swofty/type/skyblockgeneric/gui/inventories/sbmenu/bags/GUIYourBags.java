package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bags;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.collection.CustomCollectionAward;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIYourBags extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.translatable("gui_sbmenu.bags.main.title", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();

        // Sack of Sacks
        if (player.hasCustomCollectionAward(CustomCollectionAward.SACK_OF_SACKS)) {
            layout.slot(20, (s, c) -> ItemStacks.head(
                            "80a077e248d142772ea800864f8c578b9d36885b29daf836b64a706882b6ec10",
                            Text.key("gui_sbmenu.bags.sack_of_sacks.unlocked"),
                            Text.keyLines("gui_sbmenu.bags.sack_of_sacks.unlocked.lore")),
                    (click, c) -> c.player().openView(new GUISackOfSacks()));
        } else {
            layout.slot(20, (s, c) -> ItemStacks.item(Material.GRAY_DYE, 1,
                    Text.key("gui_sbmenu.bags.sack_of_sacks.locked"),
                    Text.keyLines("gui_sbmenu.bags.sack_of_sacks.locked.lore")));
        }

        // Fishing Bag
        if (player.hasCustomCollectionAward(CustomCollectionAward.FISHING_BAG)) {
            layout.slot(21, (s, c) -> ItemStacks.head(
                    "eb8e297df6b8dffcf135dba84ec792d420ad8ecb458d144288572a84603b1631",
                    Text.key("gui_sbmenu.bags.fishing_bag.unlocked"),
                    Text.keyLines("gui_sbmenu.bags.fishing_bag.unlocked.lore")));
        } else {
            layout.slot(21, (s, c) -> ItemStacks.item(Material.GRAY_DYE, 1,
                    Text.key("gui_sbmenu.bags.fishing_bag.locked"),
                    Text.keyLines("gui_sbmenu.bags.fishing_bag.locked.lore")));
        }

        // Potion Bag
        if (player.hasCustomCollectionAward(CustomCollectionAward.POTION_BAG)) {
            layout.slot(22, (s, c) -> ItemStacks.head(
                    "9f8b82427b260d0a61e6483fc3b2c35a585851e08a9a9df372548b4168cc817c",
                    Text.key("gui_sbmenu.bags.potion_bag.unlocked"),
                    Text.keyLines("gui_sbmenu.bags.potion_bag.unlocked.lore")));
        } else {
            layout.slot(22, (s, c) -> ItemStacks.item(Material.GRAY_DYE, 1,
                    Text.key("gui_sbmenu.bags.potion_bag.locked"),
                    Text.keyLines("gui_sbmenu.bags.potion_bag.locked.lore")));
        }

        // Quiver
        if (player.hasCustomCollectionAward(CustomCollectionAward.QUIVER)) {
            layout.slot(23, (s, c) -> ItemStacks.head(
                            "396ce13ff6155fdf3235d8d22174c5de4bf5512f1adeda1afa3fc28180f3f7",
                            Text.key("gui_sbmenu.bags.quiver.unlocked"),
                            Text.keyLines("gui_sbmenu.bags.quiver.unlocked.lore")),
                    (click, c) -> c.player().openView(new GUIQuiver()));
        } else {
            layout.slot(23, (s, c) -> ItemStacks.item(Material.GRAY_DYE, 1,
                    Text.key("gui_sbmenu.bags.quiver.locked"),
                    Text.keyLines("gui_sbmenu.bags.quiver.locked.lore")));
        }

        // Accessory Bag
        if (player.hasCustomCollectionAward(CustomCollectionAward.ACCESSORY_BAG)) {
            layout.slot(24, (s, c) -> {
                SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                return ItemStacks.head(
                        "396ce13ff6155fdf3235d8d22174c5de4bf5512f1adeda1afa3fc28180f3f7",
                        Text.key("gui_sbmenu.bags.accessory_bag.unlocked"),
                        Text.keyLines("gui_sbmenu.bags.accessory_bag.unlocked.lore",
                                StringUtility.commaify(p.getMagicalPower())));
            }, (click, c) -> c.player().openView(new GUIAccessoryBag()));
        } else {
            layout.slot(24, (s, c) -> ItemStacks.item(Material.GRAY_DYE, 1,
                    Text.key("gui_sbmenu.bags.accessory_bag.locked"),
                    Text.keyLines("gui_sbmenu.bags.accessory_bag.locked.lore")));
        }
    }
}
