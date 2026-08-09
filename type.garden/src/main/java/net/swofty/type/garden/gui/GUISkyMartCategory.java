package net.swofty.type.garden.gui;

import net.minestom.server.inventory.InventoryType;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.garden.config.GardenConfigRegistry;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GUISkyMartCategory extends StatelessView {
    private static final int[] ENTRY_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34
    };

    private final String category;
    private final String title;

    public GUISkyMartCategory(String category, String title) {
        this.category = category;
        this.title = title;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(title, InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, 49, ctx);

        List<Map<String, Object>> entries = net.swofty.type.garden.GardenServices.desk().getSkyMartEntries(category);
        for (int index = 0; index < Math.min(entries.size(), ENTRY_SLOTS.length); index++) {
            Map<String, Object> entry = entries.get(index);
            int slot = ENTRY_SLOTS[index];
            layout.slot(slot, (s, c) -> buildEntry((SkyBlockPlayer) c.player(), entry), (click, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                String id = GardenConfigRegistry.getString(entry, "id", "");
                long copper = GardenGuiSupport.core(player).getCopper();
                long price = GardenConfigRegistry.getLong(entry, "copper", 0L);
                boolean repeatable = isRepeatable(id);
                if (!repeatable && GardenGuiSupport.core(player).getSkyMartPurchases().contains(id)) {
                    player.sendMessage("§eYou already bought that SkyMart unlock.");
                    return;
                }
                if (copper < price) {
                    player.sendMessage("§cYou don't have enough Copper.");
                    return;
                }

                ItemType itemType = ItemType.get(id);
                if (id.endsWith("_BARN_SKIN")) {
                    GardenGuiSupport.core(player).setCopper(copper - price);
                    GardenGuiSupport.core(player).getSkyMartPurchases().add(id);
                    player.playSuccessSound();
                    player.sendMessage("§aPurchased " + GardenConfigRegistry.getString(entry, "display_name", id) + "§a.");
                    c.session(Object.class).refresh();
                    return;
                }
                if (itemType == null) {
                    player.sendMessage("§cThat item's runtime definition is not available yet.");
                    return;
                }

                GardenGuiSupport.core(player).setCopper(copper - price);
                player.addAndUpdateItem(itemType);
                if (!repeatable) {
                    GardenGuiSupport.core(player).getSkyMartPurchases().add(id);
                }
                player.playSuccessSound();
                c.session(Object.class).refresh();
            });
        }
    }

    private net.minestom.server.item.ItemStack.Builder buildEntry(SkyBlockPlayer player, Map<String, Object> entry) {
        String id = GardenConfigRegistry.getString(entry, "id", "");
        String name = GardenConfigRegistry.getString(entry, "display_name", StringUtility.toNormalCase(id));
        long price = GardenConfigRegistry.getLong(entry, "copper", 0L);
        boolean repeatable = isRepeatable(id);
        boolean owned = GardenGuiSupport.core(player).getSkyMartPurchases().contains(id);
        boolean available = ItemType.get(id) != null || id.endsWith("_BARN_SKIN");

        List<String> lore = new ArrayList<>(List.of(
            "§7Cost",
            "§c" + StringUtility.commaify(price) + " Copper",
            ""
        ));
        if (!available) {
            lore.add("§8Runtime item data pending");
            lore.add("");
            lore.add("§cUnavailable");
        } else if (!repeatable && owned) {
            lore.add("§aPURCHASED");
        } else {
            lore.add("§eClick to trade!");
        }

        return GardenGuiSupport.itemWithLore(id, "§a" + name, lore);
    }

    private boolean isRepeatable(String id) {
        return id.endsWith("_GARDEN_CHIP");
    }
}
