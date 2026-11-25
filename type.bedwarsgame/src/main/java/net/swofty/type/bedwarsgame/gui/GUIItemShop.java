package net.swofty.type.bedwarsgame.gui;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.bedwarsgame.TypeBedWarsGameLoader;
import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.bedwarsgame.shop.ShopItem;
import net.swofty.type.bedwarsgame.shop.ShopManager;
import net.swofty.type.bedwarsgame.shop.UpgradeableItemTier;
import net.swofty.type.bedwarsgame.shop.UpgradeableShopItem;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.user.HypixelPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class GUIItemShop implements StatefulView<GUIItemShop.State> {

    private static final ItemStack QUICK_BUY = ItemStacks.name(ItemStack.builder(Material.NETHER_STAR), "<b>Quick Buy").build();

    private static final ItemStack BLOCKS = ItemStacks.name(ItemStack.builder(Material.TERRACOTTA), "<a>Blocks").build();

    private static final ItemStack WEAPONS = ItemStacks.name(ItemStack.builder(Material.GOLDEN_SWORD), "<a>Weapons").build();

    private static final ItemStack ARMOR = ItemStacks.name(ItemStack.builder(Material.CHAINMAIL_BOOTS), "<a>Armor").build();

    private static final ItemStack TOOLS = ItemStacks.name(ItemStack.builder(Material.STONE_PICKAXE), "<a>Tools").build();

    private static final ItemStack BOWS = ItemStacks.name(ItemStack.builder(Material.BOW), "<a>Bows & Arrows").build();

    private static final ItemStack POTIONS = ItemStacks.name(ItemStack.builder(Material.BREWING_STAND), "<a>Potions").build();

    private static final ItemStack UTILITY = ItemStacks.name(ItemStack.builder(Material.TNT), "<a>Utility").build();

    private static final ItemStack ROTATING_ITEMS = ItemStacks.name(ItemStack.builder(Material.REDSTONE_TORCH), "<a>Rotating Items").build();

    private static final List<List<Material>> TIERED_ITEM_GROUPS = List.of(
            List.of(Material.LEATHER_BOOTS, Material.GOLDEN_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS)
    );

    private final ShopManager shopService = TypeBedWarsGameLoader.shopManager;
    private final BedWarsGame game;

    public GUIItemShop(BedWarsGame game) {
        this.game = game;
    }

    @Override
    public State initialState() {
        return new State(0);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Item Shop", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        layout.allowHotkey(false);

        for (int slot = 9; slot <= 17; slot++) {
            layout.slot(slot, ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
        }
        layout.slot(state.currentPage() + 9, ItemStacks.named(Material.GREEN_STAINED_GLASS_PANE, ""));

        addCategoryButton(layout, 0, QUICK_BUY, state.currentPage(), 0);
        addCategoryButton(layout, 1, BLOCKS, state.currentPage(), 1);
        addCategoryButton(layout, 2, WEAPONS, state.currentPage(), 2);
        addCategoryButton(layout, 3, ARMOR, state.currentPage(), 3);
        addCategoryButton(layout, 4, TOOLS, state.currentPage(), 4);
        addCategoryButton(layout, 5, BOWS, state.currentPage(), 5);
        addCategoryButton(layout, 6, POTIONS, state.currentPage(), 6);
        addCategoryButton(layout, 7, UTILITY, state.currentPage(), 7);
        addCategoryButton(layout, 8, ROTATING_ITEMS, state.currentPage(), 8);

        populateShopItems(layout, shopService, game, state.currentPage(), null, c -> c.session(State.class).refresh());
    }

    private void addCategoryButton(ViewLayout<State> layout, int slot, ItemStack icon, int currentPage, int targetPage) {
        layout.slot(slot,
                (s, c) -> convertToClickToView(icon, currentPage, targetPage),
                (click, c) -> {
                    c.session(State.class).update(prev -> prev.withCurrentPage(targetPage));
                    playClickSound(click.player());
                }
        );
    }

    private ItemStack.Builder convertToClickToView(ItemStack itemStack, int currentPage, int index) {
        ItemStack.Builder builder = itemStack.builder();
        if (currentPage != index) {
            return ItemStacks.lines(builder, "<e>Click to view!");
        }
        return builder;
    }

    public static <S> void populateShopItems(ViewLayout<S> layout,
                                             ShopManager shopService,
                                             BedWarsGame game,
                                             @Nullable Integer currentPage,
                                             @Nullable ShopItem quickBuyEditor,
                                             Consumer<ViewContext> update) {
        int[] shopSlots = {
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        if (currentPage != null && currentPage == 8) {
            layout.slot(49, (s, c) -> ItemStacks.item(Material.PAPER, """
                    <a>What are Rotating Items?
                    <7>Rotating Items are items that are
                    <7>only available for a limited amount of
                    <7>time. They may disappear and be
                    <7>replaced with another temporary
                    <7>item at any time."""));
        }

        if (currentPage != null && currentPage == 0) {
            layout.slot(45, (_, _) -> ItemStacks.item(Material.COMPASS, """
                <a>Tracker Shop
                <7>Purchase tracking upgrade for your
                <7>compass which will track each player
                <7>on a specific team until you die."""), (_, context) -> {
                context.push(new TrackerShopView());
            });
            layout.slot(53, (_, _) -> ItemStacks.item(Material.BLAZE_POWDER, """
                <a>Hotbar Manager
                <7>Edit preferred slots for your items
                <7>per category.

                <e>Click to edit!"""), (_, context) -> {
                context.push(new net.swofty.type.generic.gui.impl.HotbarManagerView());
            });
        }

        for (int i = 0; i < shopSlots.length; i++) {
            int slot = shopSlots[i];
            int index = i;

            layout.slot(slot,
                    (s, c) -> renderShopItem(c, shopService, game, currentPage, quickBuyEditor, index),
                    (click, c) -> handleShopItemClick(click.player(), click.click(), c, shopService, game, currentPage, quickBuyEditor, index, update)
            );
        }
    }

    private static ItemStack.Builder renderShopItem(ViewContext context,
                                                    ShopManager shopService,
                                                    BedWarsGame game,
                                                    @Nullable Integer currentPage,
                                                    @Nullable ShopItem quickBuyEditor,
                                                    int index) {
        BedWarsPlayer player = (BedWarsPlayer) context.player();
        ShopItem shopItem;
        if ((currentPage != null && currentPage == 0) || quickBuyEditor != null) {
            shopItem = shopService.getQuickShopItem(player, index);
        } else if (currentPage != null) {
            shopItem = shopService.getShopItem(currentPage, index);
        } else {
            throw new IllegalStateException("Current page cannot be null when getting shop items!");
        }

        if (shopItem == null) {
            if (quickBuyEditor != null) {
                return ItemStacks.item(Material.RED_STAINED_GLASS_PANE, """
                        <c>Empty slot!
                        <e>Click to set!""");
            }
            if (currentPage != 0) return ItemStack.builder(Material.AIR);
            return ItemStacks.item(Material.RED_STAINED_GLASS_PANE, """
                    <c>Empty slot!
                    <7>This is a Quick Buy Slot! <b>Shift Click
                    <7>any item in the shop to add it here.""");
        }

        if (shopItem instanceof UpgradeableShopItem upgradeableShopItem) {
            int nextLevel = upgradeableShopItem.getNextLevel(player);
            UpgradeableItemTier nextTier = upgradeableShopItem.getNextTier(player);
            boolean hasEnough = hasPlayerEnoughCurrencyForTier(game, player, nextTier);

            List<Text> lore = new ArrayList<>();
            if (quickBuyEditor != null) {
                lore.add(Text.of("<e>Click to replace!"));
            } else {
                lore.add(Text.of("<7>Cost: {}", Text.of("<color:{}>{} {}", nextTier.currency().getColor(),
                        nextTier.price().apply(game.getGameType()), nextTier.currency().getName())));
                lore.add(Text.literal(" "));
                if (upgradeableShopItem.getDescription() != null && !upgradeableShopItem.getDescription().isEmpty()) {
                    for (String line : StringUtility.splitByNewLine(upgradeableShopItem.getDescription())) {
                        lore.add(Text.of("<7>{}", Text.parse(line)));
                    }
                    lore.add(Text.literal(" "));
                }

                boolean isItemInQuickBuy = shopService.isItemIDinQuickBuy(player, upgradeableShopItem.getId());
                if (currentPage != 0 && !isItemInQuickBuy) {
                    lore.add(Text.of("<b>Shift Click to add to Quick Buy"));
                } else if (currentPage == 0 && isItemInQuickBuy) {
                    lore.add(Text.of("<b>Shift Click to remove from Quick Buy"));
                }
                if (nextLevel >= upgradeableShopItem.getTiers().size()) {
                    lore.add(Text.of("<c>You have already purchased the maximum tier of this item!"));
                } else if (hasEnough) {
                    lore.add(Text.of("<e>Click to buy!"));
                } else {
                    lore.add(Text.of("<c>You don't have enough {}!", nextTier.currency().getName()));
                }
            }

            Text name = Text.of(quickBuyEditor != null || hasEnough ? "<a>{}" : "<c>{}", nextTier.name());

            return ItemStacks.item(
                    nextTier.material(),
                    1,
                    name,
                    lore
            );
        }

        boolean hasEnough = hasPlayerEnoughCurrency(game, player, shopItem);
        ItemStack displayItem = shopItem.getDisplay(player);
        List<Text> lore = new ArrayList<>();
        if (quickBuyEditor != null) {
            lore.add(Text.of("<e>Click to replace!"));
        } else {
            lore.add(Text.of("<7>Cost: {}", Text.of("<color:{}>{} {}", shopItem.getCurrency().getColor(),
                    shopItem.getPrice().apply(game.getGameType()), shopItem.getCurrency().getName())));
            lore.add(Text.literal(" "));
            if (shopItem.getDescription() != null && !shopItem.getDescription().isEmpty()) {
                for (String line : StringUtility.splitByNewLine(shopItem.getDescription())) {
                    lore.add(Text.of("<7>{}", Text.parse(line)));
                }
                lore.add(Text.literal(" "));
            }

            boolean isItemInQuickBuy = shopService.isItemIDinQuickBuy(player, shopItem.getId());
            if (currentPage != 0 && !isItemInQuickBuy) {
                lore.add(Text.of("<b>Shift Click to add to Quick Buy"));
            } else if (currentPage == 0 && isItemInQuickBuy) {
                lore.add(Text.of("<b>Shift Click to remove from Quick Buy"));
            }
            if (!hasEnough) {
                lore.add(Text.of("<c>You don't have enough {}!", shopItem.getCurrency().getName()));
            } else if (!shopItem.isOwned(player)) {
                lore.add(Text.of("<a>UNLOCKED"));
            } else if (hasBetterItem(player, displayItem.material())) {
                lore.add(Text.of("<c>You already have a better item!"));
            } else {
                lore.add(Text.of("<e>Click to buy!"));
            }
        }

        Text name = Text.of(quickBuyEditor != null || (hasEnough && shopItem.isOwned(player)) ? "<a>{}" : "<c>{}",
                shopItem.getName());

        return ItemStacks.lore(
            ItemStacks.customName(displayItem.builder(), name),
                lore
        );
    }

    private static void handleShopItemClick(HypixelPlayer p,
                                            Click click,
                                            ViewContext ctx,
                                            ShopManager shopService,
                                            BedWarsGame game,
                                            @Nullable Integer currentPage,
                                            @Nullable ShopItem quickBuyEditor,
                                            int index,
                                            Consumer<ViewContext> update) {
        BedWarsPlayer player = (BedWarsPlayer) p;
        ShopItem shopItem;
        if ((currentPage != null && currentPage == 0) || quickBuyEditor != null) {
            shopItem = shopService.getQuickShopItem(player, index);
        } else if (currentPage != null) {
            shopItem = shopService.getShopItem(currentPage, index);
        } else {
            throw new IllegalStateException("Current page cannot be null when clicking shop items!");
        }

        if (quickBuyEditor != null) {
            shopService.setQuickBuyItem(player, index, quickBuyEditor);
            player.sendMessage("<a>Added {} to Quick Buy!", quickBuyEditor.getName());
            ctx.replace(new GUIItemShop(game));
            return;
        }

        if (shopItem == null) return;

        if (click instanceof Click.LeftShift || click instanceof Click.RightShift) {
            boolean isInQuickBuy = shopService.isItemIDinQuickBuy(player, shopItem.getId());
            if (isInQuickBuy) {
                if (currentPage != 0) return;
                shopService.removeQuickBuyItem(player, shopItem);
                player.sendMessage("<c>Removed {} from Quick Buy!", shopItem.getName());
            } else {
                ctx.replace(new GUIQuickBuyEditor(game, shopItem));
            }
            playClickSound(player);
            update.accept(ctx);
            return;
        }

        if (shopItem instanceof UpgradeableShopItem upgradeableShopItem) {
            int nextLevel = upgradeableShopItem.getNextLevel(player);
            if (nextLevel >= upgradeableShopItem.getTiers().size()) {
                player.sendMessage("<c>You have already purchased the maximum tier of this item!");
                return;
            }

            UpgradeableItemTier nextTier = upgradeableShopItem.getNextTier(player);
            if (!hasPlayerEnoughCurrencyForTier(game, player, nextTier)) {
                int owned = Arrays.stream(player.getInventory().getItemStacks())
                        .filter(s -> s.material() == nextTier.currency().getMaterial())
                        .mapToInt(ItemStack::amount)
                        .sum();
                int needed = nextTier.price().apply(game.getGameType()) - owned;
                player.sendMessage("<c>You don't have enough {}! Need {} more!", nextTier.currency().getName(), needed);
                return;
            }

            upgradeableShopItem.handlePurchase(player, game.getGameType());
            player.sendMessage("<a>You purchased {}!", nextTier.name());
            playBuySound(player);
            update.accept(ctx);
            return;
        }

        if (!hasPlayerEnoughCurrency(game, player, shopItem)) {
            player.sendMessage("<c>You don't have enough {}!", shopItem.getCurrency().getName());
            return;
        }
        if (!shopItem.isOwned(player)) {
            player.sendMessage("<c>You already have the highest tier available!");
            return;
        }

        if (hasBetterItem(player, shopItem.getDisplay(player).material())) {
            player.sendMessage("<c>You already have a better item!");
            return;
        }

        shopItem.handlePurchase(player, game.getGameType());
        playBuySound(player);
        update.accept(ctx);
    }

    private static boolean hasPlayerEnoughCurrency(BedWarsGame game, HypixelPlayer player, ShopItem shopItem) {
        int requiredAmount = shopItem.getPrice().apply(game.getGameType());
        Material currencyMaterial = shopItem.getCurrency().getMaterial();

        int playerAmount = 0;
        for (ItemStack item : player.getInventory().getItemStacks()) {
            if (item.material() == currencyMaterial) {
                playerAmount += item.amount();
            }
        }

        return playerAmount >= requiredAmount;
    }

    private static boolean hasPlayerEnoughCurrencyForTier(BedWarsGame game, HypixelPlayer player, UpgradeableItemTier tier) {
        int required = tier.price().apply(game.getGameType());
        Material cur = tier.currency().getMaterial();
        int have = 0;
        for (ItemStack it : player.getInventory().getItemStacks()) {
            if (it.material() == cur) have += it.amount();
        }
        return have >= required;
    }

    private static boolean hasBetterItem(Player player, Material materialToBuy) {
        for (List<Material> group : TIERED_ITEM_GROUPS) {
            if (!group.contains(materialToBuy)) {
                continue;
            }

            int tierToBuy = group.indexOf(materialToBuy);
            for (ItemStack stack : player.getInventory().getItemStacks()) {
                if (group.contains(stack.material()) && group.indexOf(stack.material()) > tierToBuy) {
                    return true;
                }
            }
            for (ItemStack stack : List.of(
                    player.getEquipment(EquipmentSlot.BOOTS),
                    player.getEquipment(EquipmentSlot.LEGGINGS),
                    player.getEquipment(EquipmentSlot.CHESTPLATE),
                    player.getEquipment(EquipmentSlot.HELMET))) {
                if (group.contains(stack.material()) && group.indexOf(stack.material()) > tierToBuy) {
                    return true;
                }
            }
            return false;
        }

        return false;
    }

    private static void playClickSound(HypixelPlayer player) {
        player.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1.0f, 1.0f));
    }

    private static void playBuySound(HypixelPlayer player) {
        player.playSound(Sound.sound(Key.key("minecraft:entity.experience_orb.pickup"), Sound.Source.MASTER, 1.0f, 1.0f));
    }

    public record State(int currentPage) {
        public State withCurrentPage(int page) {
            return new State(page);
        }
    }
}
