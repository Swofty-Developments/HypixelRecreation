package net.swofty.type.skyblockgeneric.gui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointDouble;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulPaginatedView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.gui.inventories.shop.TradingOptionsView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.SellableComponent;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.shop.ShopPrice;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

public abstract class ShopView extends StatefulPaginatedView<ShopView.ShopItem, ShopView.State> {

    public static final int[] DEFAULT = new int[]{
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    public static final int[] WOOLWEAVER_VIBRANT = new int[]{
            1, 2, 3, 4, 5, 6, 7, 8,
            10, 11, 12, 13, 14, 15, 16, 17,
            19, 20, 21, 22, 23, 24, 25, 26,
            28, 29, 30, 31, 32, 33, 34, 35,
            37, 38, 39, 40, 41, 42, 43, 44
    };
    public static final int[] WOOLWEAVER_COOL = new int[]{
            0, 1, 2, 3, 4, 5, 6, 7,
            9, 10, 11, 12, 13, 14, 15, 16,
            18, 19, 20, 21, 22, 23, 24, 25,
            17, 28, 29, 30, 31, 32, 33, 34,
            26, 37, 38, 39, 40, 41, 42, 43,
    };
    public static final int[] UPPER5ROWS = new int[]{
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    };
    public static final int[] GREENTHUMB = new int[]{
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            38, 39, 40, 41, 42
    };
    public static final int[] VARIETY = new int[]{
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42
    };
    public static final int[] SINGLE_SLOT = new int[]{22};

    private final Text title;
    private final int[] interiorSlots;

    public ShopView(Text title, int[] interiorSlots) {
        this.title = title;
        this.interiorSlots = interiorSlots;
    }

    public ShopView(String title, int[] interiorSlots, Object... arguments) {
        this.title = Text.of(title, arguments);
        this.interiorSlots = interiorSlots;
    }

    public final void attachItem(ShopItem item) {
        itemsToAttach.add(item);
    }

    private final List<ShopItem> itemsToAttach = new ArrayList<>();

    public abstract void initializeShopItems();

    @Override
    public State initialState() {
        if (itemsToAttach.isEmpty()) {
            initializeShopItems();
        }
        return new State(new ArrayList<>(itemsToAttach), 0);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withText((state, _) -> {
            int totalPages = Math.max(1, (int) Math.ceil((double) getFilteredItems(state).size() / interiorSlots.length));
            if (totalPages == 1) {
                return title;
            }
            return title.append("<f> | Page {}/{}", state.page() + 1, totalPages);
        }, InventoryType.CHEST_6_ROW);
    }

    @Override
    protected int[] getPaginatedSlots() {
        return interiorSlots;
    }

    @Override
    protected int getNextPageSlot() {
        return 53;
    }

    @Override
    protected int getPreviousPageSlot() {
        return 45;
    }

    @Override
    protected void layoutBackground(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);
        for (int slot : interiorSlots) {
            layout.slot(slot, ItemStack.AIR.builder());
        }
    }

    @Override
    protected void layoutCustom(ViewLayout<State> layout, State state, ViewContext ctx) {
        layoutBuyback(layout, ctx);
    }

    private void layoutBuyback(ViewLayout<State> layout, ViewContext ctx) {
        layout.slot(49,
                (s, c) -> {
                    SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                    if (!player.getShoppingData().hasAnythingToBuyback()) {
                        return ItemStacks.item(Material.HOPPER, 1, """
                                <a>Sell Item
                                <7>Click items in your inventory to
                                <7>sell them to this Shop!""");
                    }

                    SkyBlockItem last = new SkyBlockItem(player.getShoppingData().lastBuyback().getKey());
                    int amountOfLast = player.getShoppingData().lastBuyback().getValue();
                    ItemStack.Builder itemStack = PlayerItemUpdater.playerUpdate(player, last.getItemStackBuilder().build());

                    double buyBackPrice = last.getComponent(SellableComponent.class).getSellValue() * amountOfLast;

                    List<Text> lore = ItemStacks.loreText(itemStack);
                    lore.add(Text.empty());
                    lore.add(Text.of("<7>Cost"));
                    lore.add(Text.of("<6>{:,} Coin{}", buyBackPrice, buyBackPrice != 1 ? "s" : ""));
                    lore.add(Text.empty());
                    lore.add(Text.of("<e>Click to buyback!"));

                    itemStack.amount(amountOfLast);
                    return ItemStacks.lore(itemStack, lore);
                },
                (click, c) -> {
                    SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                    if (!player.getShoppingData().hasAnythingToBuyback()) return;

                    SkyBlockItem last = new SkyBlockItem(player.getShoppingData().lastBuyback().getKey());
                    int amountOfLast = player.getShoppingData().lastBuyback().getValue();

                    ItemStack.Builder itemStack = PlayerItemUpdater.playerUpdate(player, last.getItemStackBuilder().build());
                    itemStack.amount(amountOfLast);

                    double value = last.getComponent(SellableComponent.class).getSellValue() * amountOfLast;
                    double playerCoins = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.COINS, DatapointDouble.class).getValue();

                    if (playerCoins < value) {
                        player.sendMessage("<c>You don't have enough coins!");
                        return;
                    }

                    player.addAndUpdateItem(new SkyBlockItem(itemStack.build()));
                    player.playSuccessSound();
                    player.getShoppingData().popBuyback();
                    player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.COINS, DatapointDouble.class).setValue(playerCoins - value);

                    c.session(Object.class).refresh();
                }
        );
    }

    @Override
    protected ItemStack.Builder renderItem(ShopItem item, int index, HypixelPlayer p) {
        SkyBlockPlayer player = (SkyBlockPlayer) p;

        try {
            SkyBlockItem sbItem = item.item;
            ShopPrice price = item.price;

            ItemStack.Builder itemStack = PlayerItemUpdater.playerUpdate(player, sbItem.getItemStackBuilder().build());
            itemStack.amount(item.amount);

            if (item.getDisplayName() != null) {
                ItemStacks.name(itemStack, Text.read(item.getDisplayName()));
            }

            if (item.getLore() != null) {
                ItemStacks.lore(itemStack, item.getLore());
            } else {
                ItemStacks.lore(itemStack, ItemStacks.loreText(itemStack));
            }

            List<Text> lore = new ArrayList<>();
            lore.add(Text.empty());
            lore.add(Text.of("<7>Cost"));
            lore.addAll(price.getGUIDisplay());
            lore.add(Text.empty());

            if (item.hasStock) {
                lore.add(Text.of("<7>Stock"));
                lore.add(Text.of("<6>{} <7>remaining", player.getShoppingData().getStock(item.getItem().toUnderstandable())));
                lore.add(Text.empty());
            }

            lore.add(Text.of("<e>Click to trade!"));
            if (item.stackable) {
                lore.add(Text.of("<e>Right-click for more trading options!"));
            }

            return ItemStacks.appendLore(itemStack, lore);
        } catch (Exception e) {
            p.sendMessage("<c>There was an error processing item {}!", item.getItem().getDisplayName());
            Logger.error(e, "Error loading shop view");
            return ItemStacks.item(Material.BARRIER, 1, "<c>Error");
        }
    }

    @Override
    protected void onItemClick(ClickContext<State> click, ViewContext ctx, ShopItem item, int index) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();

        if (item.isHasStock() && !player.getShoppingData().canPurchase(item.item.toUnderstandable(), item.amount)) {
            player.sendMessage("<c>You have reached the maximum amount of items you can buy!");
            return;
        }

        ShopPrice price = item.price;
        ShopPrice stackPrice = item.price.divide(item.amount);

        if (item.isStackable() && click.click() instanceof Click.Right) {
            ctx.navigator().push(new TradingOptionsView(), new TradingOptionsView.State(item, stackPrice));
            return;
        }

        if (!price.canAfford(player)) {
            player.sendMessage("<c>You don't have enough {}!", price.getNamePlural());
            return;
        }

        price.processPurchase(player);

        SkyBlockItem toGive = item.item;
        toGive.setAmount(item.amount);
        player.addAndUpdateItem(toGive);
        player.playSound(Sound.sound(Key.key("block.note_block.pling"), Sound.Source.PLAYER, 1.0f, 2.0f));

        if (item.hasStock) {
            player.getShoppingData().documentPurchase(item.getItem().toUnderstandable(), item.amount);
        }

        ctx.session(Object.class).refresh();
    }

    @Override
    protected boolean shouldFilterFromSearch(State state, ShopItem item) {
        return false;
    }

    @Override
    public void onClose(State state, ViewContext ctx, net.swofty.type.generic.gui.v2.ViewSession.CloseReason reason) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        SkyBlockDataHandler.Data.INVENTORY.onLoad.accept(
                player, SkyBlockDataHandler.Data.INVENTORY.onQuit.apply(player)
        );
    }

    @Override
    public boolean onBottomClick(ClickContext<State> click, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();

        ItemStack stack = player.getInventory().getItemStack(click.slot());
        if (stack.material().equals(Material.AIR)) return true;

        SkyBlockItem item = new SkyBlockItem(stack);
        if (!item.hasComponent(SellableComponent.class)) {
            player.sendMessage("<c>You can't sell this item!");
            return true;
        }

        SellableComponent sellable = item.getComponent(SellableComponent.class);
        double sellPrice = sellable.getSellValue() * stack.amount();

        player.getShoppingData().pushBuyback(item.toUnderstandable(), stack.amount());
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.COINS, DatapointDouble.class).setValue(
                player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.COINS, DatapointDouble.class).getValue() + sellPrice
        );

        player.sendMessage("<a>You sold <f>{} <a>for <6>{:,} Coin{}<a>!",
                ItemStacks.nameText(stack, Text.literal(item.getDisplayName())),
                sellPrice, sellPrice != 1 ? "s" : "");

        player.getInventory().setItemStack(click.slot(), ItemStack.AIR);
        ctx.session(Object.class).refresh();
        return true;
    }

    public record State(List<ShopItem> items, int page) implements PaginatedState<ShopItem> {
        @Override
        public PaginatedState<ShopItem> withPage(int page) {
            return new State(items, page);
        }

        @Override
        public PaginatedState<ShopItem> withItems(List<ShopItem> items) {
            return new State(items, page);
        }
    }

    @Getter
    @AllArgsConstructor
    @RequiredArgsConstructor
    public static class ShopItem {
        private final SkyBlockItem item;
        private final int amount;
        private final ShopPrice price;
        private final boolean stackable;
        private List<Text> lore = null;
        @Setter
        private String displayName = null;
        private boolean hasStock = true;

        public ShopItem(SkyBlockItem item, int amount, ShopPrice price, boolean stackable, boolean hasStock) {
            this.item = item;
            this.amount = amount;
            this.price = price;
            this.stackable = stackable;
            this.hasStock = hasStock;
        }

        public static ShopItem Stackable(SkyBlockItem item, int amount, ShopPrice price) {
            return new ShopItem(item, amount, price, true);
        }

        public static ShopItem Single(SkyBlockItem item, int amount, ShopPrice price) {
            return new ShopItem(item, amount, price, false);
        }

        public void setLore(List<Text> lore) {
            this.lore = lore;
        }

        public void setLore(String textBlock, Object... arguments) {
            this.lore = Text.of(textBlock, arguments).lines();
        }

    }

}
