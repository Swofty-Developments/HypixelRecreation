package net.swofty.type.ravengardgeneric.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.ViewNavigator;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.ravengardgeneric.item.RavengardItem;
import net.swofty.type.ravengardgeneric.item.RavengardItemRegistry;
import net.swofty.type.ravengardgeneric.item.RavengardItemType;
import net.swofty.type.ravengardgeneric.profile.RavengardProfiles;
import net.swofty.type.ravengardgeneric.shop.RavengardShop;
import net.swofty.type.ravengardgeneric.user.RavengardPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * The buy side of a shop, rebuilt from captures: banner top left, stock in the grid, the stock
 * refresh hourglass, and the buy and sell tabs down the left edge.
 */
public class GUIShop extends RavengardView {
    private static final int PANEL_ICON = 0xE23B;
    static final int SLOT_BANNER = 0;
    static final int SLOT_HOURGLASS = 19;
    static final int SLOT_BUY = 27;
    static final int SLOT_SELL = 36;
    private final RavengardShop shop;

    public GUIShop(RavengardShop shop) {
        this.shop = shop;
    }

    @Override
    protected String title() {
        return shop.title();
    }

    @Override
    protected int panelIcon() {
        return PANEL_ICON;
    }

    @Override
    protected void content(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        place(layout, SLOT_BANNER, RavengardItems.button(banner(shop))
                .label(Text.literal(shop.title()))
                .lore("<7>Buy and sell items to help you on.",
                        "<7>your adventures!"));

        for (int slot : shop.shelfSlots()) {
            placeStock(layout, slot);
        }

        layout.autoUpdating(SLOT_HOURGLASS,
                (state2, ctx2) -> RavengardItems.button(RavengardButton.HOURGLASS)
                        .label("Stock Refresh")
                        .lore("<7>This shop's stock will refresh in:")
                        .lore(Text.of("<e>{}", net.swofty.type.ravengardgeneric.shop.RavengardShopStock.refreshText()))
                        .origin(SLOT_HOURGLASS)
                        .toBuilder(),
                java.time.Duration.ofSeconds(1));

        place(layout, SLOT_BUY, RavengardItems.button(RavengardButton.BUY)
                .label("Buy")
                .lore("<7>Purchase items from this shop to",
                        "<7>help you on your adventures!")
                .blankLine()
                .lore("<a>You are here!"));

        interactive(layout, SLOT_SELL, RavengardItems.button(RavengardButton.SELL)
                        .label("Sell")
                        .lore("<7>Sell items from your inventory for",
                                "<7>some extra cash!")
                        .blankLine()
                        .lore("<e>Click to sell!"),
                (click, viewContext) -> ViewNavigator.get(viewContext.player()).push(new GUIShopSell(shop)));
    }

    private void placeStock(ViewLayout<DefaultState> layout, int slot) {
        layout.autoUpdating(slot,
                (state2, ctx2) -> renderStock(slot, ctx2),
                (click, viewContext) -> buy(slot, viewContext),
                java.time.Duration.ofSeconds(1));
    }

    private ItemStack.Builder renderStock(int slot, ViewContext ctx) {
        var entry = net.swofty.type.ravengardgeneric.shop.RavengardShopStock.shelf(shop).at(slot);
        RavengardItemType type = entry == null ? null : RavengardItemRegistry.get(entry.item());
        if (type == null) {
            return ItemStack.builder(net.minestom.server.item.Material.AIR);
        }

        boolean wrongClass = ctx.player() instanceof RavengardPlayer viewer
                && !type.usableBy(viewer.getRavengardClass());

        ItemStack.Builder display = RavengardItem.displayBuilder(type);
        List<Text> lore = new ArrayList<>(RavengardItem.loreOf(type, true));
        lore.add(Text.empty());
        if (!entry.inStock()) {
            display.set(DataComponents.ITEM_MODEL, type.getItemModel() + "_greyed");
            lore.add(Text.of("<c>Out of stock!"));
        } else if (wrongClass) {
            display.set(DataComponents.ITEM_MODEL, type.getItemModel() + "_greyed");
            lore.add(Text.of("<c>Your class cannot use this item!"));
        } else if (entry.price() == 0) {
            lore.add(Text.of("<e>Click to get for FREE!"));
        } else {
            lore.add(Text.of("<e>Click to buy for <f>\uD83D\uDC51<#FFCE47>{}<e>!", entry.price()));
        }
        ItemStacks.lines(display, lore);
        return display;
    }

    private void buy(int slot, ViewContext viewContext) {
        if (!(viewContext.player() instanceof RavengardPlayer player)) {
            return;
        }
        var entry = net.swofty.type.ravengardgeneric.shop.RavengardShopStock.shelf(shop).at(slot);
        RavengardItemType type = entry == null ? null : RavengardItemRegistry.get(entry.item());
        if (type == null) {
            return;
        }
        if (!entry.inStock()) {
            player.sendMessage("<c>That item is out of stock!");
            return;
        }
        if (!RavengardProfiles.tryPurchase(player, entry.price())) {
            player.sendMessage("<c>You don't have enough Crowns!");
            return;
        }
        if (!entry.take()) {
            RavengardProfiles.addCrowns(player, entry.price());
            player.sendMessage("<c>That item is out of stock!");
            return;
        }
        player.getInventory().addItemStack(RavengardItem.of(type, player, entry.boost()));
        player.sendMessage(Component.text("You bought ").color(NamedTextColor.GREEN)
                .append(Component.text(type.getDisplayName() == null ? entry.item() : type.getDisplayName())
                        .color(NamedTextColor.WHITE))
                .append(Component.text(" for ").color(NamedTextColor.GREEN))
                .append(RavengardItem.crowns(entry.price(), ""))
                .append(Component.text("!").color(NamedTextColor.GREEN)));
    }

    static RavengardButton banner(RavengardShop shop) {
        return switch (shop.banner()) {
            case "armorer" -> RavengardButton.BANNER_ARMORER;
            case "alchemist" -> RavengardButton.BANNER_ALCHEMIST;
            case "free_gear" -> RavengardButton.BANNER_FREE_GEAR;
            default -> RavengardButton.BANNER_BLACKSMITH;
        };
    }
}
