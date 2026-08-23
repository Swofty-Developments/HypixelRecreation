package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.collection;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.collection.CollectionCategories;
import net.swofty.type.skyblockgeneric.collection.CollectionCategory;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCollection;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUICollectionItem extends StatelessView {
    private final ItemType item;
    private final CollectionCategory category;
    private final CollectionCategory.ItemCollection collection;

    public GUICollectionItem(ItemType item) {
        this.item = item;
        this.category = CollectionCategories.getCategory(item);
        this.collection = category.getCollection(item);
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.literal(item.getDisplayName() + " Collection"), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        layout.slot(4, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            return ItemStacks.item(item.material, 1, """
                    <e>{}
                    <7>View all your {} Collection
                    <7>progress and rewards!

                    <7>Total Collected: <e>{}""",
                    item.getDisplayName(), item.getDisplayName(), player.getCollection().get(item));
        });

        int slot = 17;
        for (CollectionCategory.ItemCollectionReward reward : collection.rewards()) {
            slot++;
            int finalSlot = slot;

            layout.slot(finalSlot, (s, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                DatapointCollection.PlayerCollection playerCollection = player.getCollection();

                List<String> rendered = new ArrayList<>();
                playerCollection.getDisplay(rendered, collection, reward);

                List<Text> lore = new ArrayList<>();
                lore.add(Text.empty());
                lore.addAll(rendered.stream().map(Text::parse).toList());
                lore.add(Text.empty());
                lore.add(Text.of("<e>Click to view rewards!"));

                if (playerCollection.getReward(collection) == null) {
                    return ItemStacks.item(Material.GREEN_STAINED_GLASS_PANE, 1,
                            Text.of("<7>{} {:roman}", item.getDisplayName(), collection.getPlacementOf(reward) + 1), lore);
                }

                Material material;
                String colourTag;
                if (playerCollection.getReward(collection) == reward) {
                    material = Material.YELLOW_STAINED_GLASS_PANE;
                    colourTag = "<e>";
                } else if (collection.getPlacementOf(playerCollection.getReward(collection)) > collection.getPlacementOf(reward)) {
                    material = Material.LIME_STAINED_GLASS_PANE;
                    colourTag = "<a>";
                } else {
                    material = Material.RED_STAINED_GLASS_PANE;
                    colourTag = "<c>";
                }

                return ItemStacks.item(material, 1,
                        Text.of(colourTag + "{} {:roman}", item.getDisplayName(), collection.getPlacementOf(reward) + 1), lore);
            }, (click, c) -> {
                ctx.push(new GUICollectionReward(item, reward));
            });
        }
    }
}
