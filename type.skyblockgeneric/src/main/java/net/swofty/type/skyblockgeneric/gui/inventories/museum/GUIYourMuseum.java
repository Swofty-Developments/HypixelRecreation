package net.swofty.type.skyblockgeneric.gui.inventories.museum;

import lombok.SneakyThrows;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.ServiceType;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointMuseum;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.museum.MuseumableItemCategory;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class GUIYourMuseum extends HypixelInventoryGUI {
    private static final Map<MuseumableItemCategory, Integer> CATEGORY_SLOTS = Map.of(
            MuseumableItemCategory.WEAPONS, 20,
            MuseumableItemCategory.ARMOR_SETS, 22,
            MuseumableItemCategory.RARITIES, 24
    );
    private static final String MUSEUM_INFO_TEXTURE =
            "597e4e27a04afa5f06108265a9bfb797630391c7f3d880d244f610bb1ff393d8";

    public GUIYourMuseum() {
        super(Text.key("gui_museum.main.title"), InventoryType.CHEST_6_ROW);
    }

    @SneakyThrows
    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        if (!new ProxyService(ServiceType.ITEM_TRACKER).isOnline().join()) {
            e.player().sendMessage(Text.key("gui_museum.main.item_tracker_offline"));
            e.player().closeInventory();
            return;
        }

        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        DatapointMuseum.MuseumData data = player.getMuseumData();
        fill(ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
        set(GUIClickableItem.getCloseItem(49));

        set(new GUIClickableItem(40) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                new GUIMuseumRewards().open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.GOLD_BLOCK, 1, Text.key("gui_museum.main.rewards_button"),
                        Text.keyLines("gui_museum.main.rewards_button.lore"));
            }
        });
        set(new GUIItem(45) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.NAME_TAG, 1, Text.key("gui_museum.main.edit_npc_tags"),
                        Text.keyLines("gui_museum.main.edit_npc_tags.lore"));
            }
        });
        set(new GUIItem(4) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                List<Text> lore = new ArrayList<>(Text.keyLines("gui_museum.main.museum_info.lore"));

                int maxAmountOfItems = MuseumableItemCategory.getMuseumableItemCategorySize();
                int unlockedItems = data.getAllItems().size();

                double percentageUnlocked = (double) unlockedItems / (double) maxAmountOfItems * 100;
                double percentageUnlockedToTwoDecimalPlaces = Math.round(percentageUnlocked * 100) / 100.0;

                lore.add(Text.key("gui_museum.main.items_donated", percentageUnlockedToTwoDecimalPlaces));
                lore.add(getAsDisplay(unlockedItems, maxAmountOfItems));

                Map<UUID, Double> calculatedPrices = data.getCalculatedPrices();
                if (!calculatedPrices.isEmpty()) {
                    lore.add(Text.literal(" "));
                    lore.add(Text.key("gui_museum.main.top_items"));

                    AtomicInteger index = new AtomicInteger(1);
                    calculatedPrices.entrySet().stream()
                            .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                            .limit(5)
                            .forEach(entry -> {
                                SkyBlockItem item = data.getFromUUID(entry.getKey());
                                lore.add(Text.of("<8>{}. {}", index.getAndIncrement(),
                                        item.getDisplayName()));
                                lore.add(Text.of("<8>    {:,} Coins", entry.getValue()));
                            });
                }

                return ItemStacks.head(MUSEUM_INFO_TEXTURE, Text.key("gui_museum.main.museum_info"), lore);
            }
        });

        for (MuseumableItemCategory category : MuseumableItemCategory.values()) {
            Integer slot = CATEGORY_SLOTS.get(category);
            if (slot == null) {
                continue;
            }

            set(new GUIClickableItem(slot) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    if (category == MuseumableItemCategory.ARMOR_SETS)
                        new GUIMuseumArmorCategory().open(player);
                    else new GUIMuseumCategory(category).open(player);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    List<Text> lore = new ArrayList<>(Text.keyLines("gui_museum.main.category_button.lore_prefix",
                            Text.empty(), Text.parse(category.getColor() + category.getCategory())));

                    int maxAmountOfItems = MuseumableItemCategory.getMuseumableItemCategorySize(category);
                    int unlockedItems = data.getItemsByCategory(category).size();
                    double percentage = (double) unlockedItems / (double) maxAmountOfItems * 100;
                    double percentageToTwoDecimalPlaces = Math.round(percentage * 100) / 100.0;

                    lore.add(Text.key("gui_museum.main.category_items_donated", percentageToTwoDecimalPlaces));
                    lore.add(getAsDisplay(unlockedItems, maxAmountOfItems));
                    lore.add(Text.literal(" "));
                    lore.add(Text.key("gui_museum.main.category_click"));

                    return ItemStacks.item(category.getMaterial(), 1,
                            Text.of("<a>{}", category.getCategory()), lore);
                }
            });
        }

        updateItemStacks(getInventory(), getPlayer());
    }

    public static Text getAsDisplay(int unlocked, int total) {
        String baseLoadingBar = "─────────────────";
        int completedLength = Math.min((int) ((unlocked / (double) total) * baseLoadingBar.length()),
                baseLoadingBar.length());

        return Text.of("<b><m>{}</m><7><m>{}</m><r> <b>{}<9>/<b>{}",
                baseLoadingBar.substring(0, completedLength), baseLoadingBar.substring(completedLength),
                unlocked, total);
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }
}
