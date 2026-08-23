package net.swofty.type.ravengardgeneric.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.ViewNavigator;
import net.swofty.type.generic.gui.v2.ViewSession;
import net.swofty.type.generic.gui.v2.context.ClickContext;
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
 * The sell side of a shop. Clicking a sellable item in the inventory stages it into the grid at
 * the top, clicking a staged item takes it back, and the sell items button sells everything
 * staged for its crown value. Items still staged when the menu closes go back to the inventory.
 */
public class GUIShopSell extends RavengardView {
    private static final int PANEL_ICON = 0xE23C;
    private static final int SLOT_TEXT_SELL = 50;
    private static final int[] GRID = {4, 5, 6, 7, 8, 13, 14, 15, 16, 17,
            22, 23, 24, 25, 26, 31, 32, 33, 34, 35};

    private final RavengardShop shop;
    private final List<ItemStack> staged;
    private boolean handedOver;

    public GUIShopSell(RavengardShop shop) {
        this(shop, new ArrayList<>());
    }

    private GUIShopSell(RavengardShop shop, List<ItemStack> staged) {
        this.shop = shop;
        this.staged = staged;
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
    protected boolean stagesFromInventory() {
        return true;
    }

    @Override
    protected void content(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        place(layout, GUIShop.SLOT_BANNER, RavengardItems.button(GUIShop.banner(shop))
                .label(Text.literal(shop.title()))
                .lore("<7>Buy and sell items to help you on.",
                        "<7>your adventures!"));

        interactive(layout, GUIShop.SLOT_BUY, RavengardItems.button(RavengardButton.BUY)
                        .label("Buy")
                        .lore("<7>Purchase items from this shop to",
                                "<7>help you on your adventures!")
                        .blankLine()
                        .lore("<e>Click to buy!"),
                (click, viewContext) -> {
                    returnStaged(viewContext);
                    ViewNavigator.get(viewContext.player()).push(new GUIShop(shop));
                });

        place(layout, GUIShop.SLOT_SELL, RavengardItems.button(RavengardButton.SELL)
                .label("Sell")
                .lore("<7>Sell items from your inventory for",
                        "<7>some extra cash!")
                .blankLine()
                .lore("<a>You are here!"));

        for (int index = 0; index < GRID.length && index < staged.size(); index++) {
            placeStaged(layout, GRID[index], index);
        }

        interactive(layout, SLOT_TEXT_SELL, RavengardItems.button(RavengardButton.TEXT_SELL)
                        .label("Sell Items")
                        .blankLine()
                        .lore("<e>Click to sell everything above!"),
                (click, viewContext) -> sellStaged(viewContext));
    }

    @Override
    public boolean onBottomClick(ClickContext<DefaultState> click, ViewContext ctx) {
        if (!(ctx.player() instanceof RavengardPlayer player) || staged.size() >= GRID.length) {
            return false;
        }
        ItemStack stack = player.getInventory().getItemStack(click.slot());
        RavengardItemType type = typeOf(stack);
        if (type == null || type.getValue() <= 0) {
            return false;
        }
        player.getInventory().setItemStack(click.slot(), ItemStack.AIR);
        List<ItemStack> next = new ArrayList<>(staged);
        next.add(stack);
        handedOver = true;
        ViewNavigator.get(player).push(new GUIShopSell(shop, next));
        return false;
    }

    @Override
    public void onClose(DefaultState state, ViewContext ctx, ViewSession.CloseReason reason) {
        if (reason != ViewSession.CloseReason.REPLACED || !handedOver) {
            returnStaged(ctx);
        }
        super.onClose(state, ctx, reason);
    }

    private void placeStaged(ViewLayout<DefaultState> layout, int gridSlot, int index) {
        ItemStack stack = staged.get(index);
        RavengardItemType type = typeOf(stack);
        if (type == null) {
            return;
        }
        int value = type.getValue();

        List<Text> lore = new ArrayList<>(RavengardItem.loreOf(type, true));
        lore.add(Text.empty());
        lore.add(Text.of("<e>Selling for <f>\uD83D\uDC51<#FFCE47>{}<e>! Click to take back.", value));

        ItemStack.Builder display = RavengardItem.displayBuilder(type);
        ItemStacks.lines(display, lore);

        layout.slot(gridSlot, display, (click, viewContext) -> {
            if (!(viewContext.player() instanceof RavengardPlayer player)) {
                return;
            }
            List<ItemStack> next = new ArrayList<>(staged);
            ItemStack removed = next.remove(index);
            player.getInventory().addItemStack(removed);
            handedOver = true;
            ViewNavigator.get(player).push(new GUIShopSell(shop, next));
        });
    }

    private void sellStaged(ViewContext ctx) {
        if (!(ctx.player() instanceof RavengardPlayer player) || staged.isEmpty()) {
            return;
        }
        int total = 0;
        for (ItemStack stack : staged) {
            RavengardItemType type = typeOf(stack);
            if (type != null) {
                total += type.getValue();
            }
        }
        staged.clear();
        if (total > 0) {
            RavengardProfiles.addCrowns(player, total);
            player.sendMessage(Component.text("You sold your items for ").color(NamedTextColor.GREEN)
                    .append(RavengardItem.crowns(total, ""))
                    .append(Component.text("!").color(NamedTextColor.GREEN)));
        }
        handedOver = true;
        ViewNavigator.get(player).push(new GUIShopSell(shop));
    }

    private void returnStaged(ViewContext ctx) {
        if (staged.isEmpty() || !(ctx.player() instanceof RavengardPlayer player)) {
            return;
        }
        staged.forEach(stack -> player.getInventory().addItemStack(stack));
        staged.clear();
    }

    private static RavengardItemType typeOf(ItemStack stack) {
        if (stack.isAir()) {
            return null;
        }
        return new net.swofty.type.ravengardgeneric.item.attribute.RavengardItemAttributeHandler(stack).getType();
    }
}
