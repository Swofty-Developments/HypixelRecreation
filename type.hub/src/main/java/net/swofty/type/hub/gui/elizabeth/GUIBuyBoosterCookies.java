package net.swofty.type.hub.gui.elizabeth;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import net.swofty.type.generic.user.HypixelPlayer;

public class GUIBuyBoosterCookies extends HypixelInventoryGUI {
    private static final String COOKIE_PERKS = """
            <7>Consume to gain the <d>Cookie Buff </d>for
            <b>4 <7>days:

            <7>▸ Ability to gain <b>Bits</b>!
            <7>▸ <3>+25☯ <7>on all <3>Wisdom stats
            <7>▸ <b>+15✯ <7>Magic Find
            <7>▸ Keep <6>coins <7>on death
            <7>▸ <e>Permafly on private islands
            <7>▸ Quick access to some menus using their respective commands:
            <6>/ah<7>, <6>/bazaar<7>, <a>/bank<7>, <f>/anvil<7>, <d>/etable <7>and <e>/quiver
            <7>▸ Sell items directly to the trades and cookie menu
            <7>▸ AFK <a>immunity <7>on your island
            <7>▸ Toggle specific <d>potion effects
            <8>‣ <7>Link your items in chat using <e>/show
            <8>‣ <7>Insta-sell your Material stash to the <6>Bazaar

            <6>Legendary

            <7>Cost
            <a>{} Skyblock Gems

            <7>You have: <a>{:,} Gems

            """;

    private static final String CAN_AFFORD = "<e>Click to purchase!";
    private static final String CANNOT_AFFORD = """
            <c>Cannot afford this!
            <e>Click here to get gems!""";

    public GUIBuyBoosterCookies() {
        super("Community Shop", InventoryType.CHEST_6_ROW);
    }

    private final int[] categoriesItemsSlots = {
            10, 11, 12, 13, 14, 16
    };
    private final int[] tabSlots = {
            1, 2, 3, 4, 5, 7
    };

    private final Integer cookieCost = 325;

    private final Book book = Book.builder()
            .addPage(Component.text("Purchase ranks, gems and more on our webstore!")
                    .appendNewline()
                    .appendNewline()
                    .append(Component.text("      "))
                    .append(Component.text("VISIT STORE").clickEvent(ClickEvent.openUrl("http://bit.ly/4aG54lt")).color(TextColor.fromHexString("#00AAAA"))))
            .build();

    private static ItemStack.Builder cookieStack(String name, String amountLine, int price, int gems) {
        String block = name + "\n\n" + amountLine + "\n" + COOKIE_PERKS
                + (gems >= price ? CAN_AFFORD : CANNOT_AFFORD);
        return ItemStacks.enchanted(ItemStacks.item(Material.COOKIE, block, price, gems));
    }

    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));

        GUIAccountAndProfileUpgrades.ShopCategorys[] allShopCategorys = GUIAccountAndProfileUpgrades.ShopCategorys.values();
        int index = 0;
        for (int slot : tabSlots) {
            GUIAccountAndProfileUpgrades.ShopCategorys shopCategorys = allShopCategorys[index];
            set(new GUIClickableItem(slot) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    if (slot != 3) {
                        shopCategorys.gui.open(p);
                    }
                }
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return shopCategorys.buildStack(slot == 3);
                }
            });
            index++;
        }

        for (int slot : categoriesItemsSlots) {
            set(new GUIItem(slot) {
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStacks.item(slot != 12
                            ? Material.GRAY_STAINED_GLASS_PANE
                            : Material.GREEN_STAINED_GLASS_PANE, """
                            <8>▲ <7>Categories
                            <8>▼ <7>Items""");
                }
            });
        }

        set(new GUIClickableItem(29) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                if (player.getGems() >= cookieCost) {
                    player.addAndUpdateItem(ItemType.BOOSTER_COOKIE);
                    player.removeGems(cookieCost);
                    new GUIBuyBoosterCookies().open(player);
                } else {
                    player.openBook(book);
                }
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return cookieStack("<6>Single Cookie", "<6>Booster Cookie <8>x1", cookieCost, player.getGems());
            }
        });
        set(new GUIClickableItem(31) {
            final int boosterCookieAmount = 6;
            final int totalCookiePrice = boosterCookieAmount*cookieCost;

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                if (player.getGems() >= totalCookiePrice) {
                    player.addAndUpdateItem(ItemType.BOOSTER_COOKIE, boosterCookieAmount);
                    player.removeGems(totalCookiePrice);
                    new GUIBuyBoosterCookies().open(player);
                } else {
                    player.openBook(book);
                }
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return cookieStack("<6>Half-Dozen Cookies", "<6>Booster Cookie <8>x6", totalCookiePrice, player.getGems());
            }
        });
        set(new GUIClickableItem(33) {
            final int boosterCookieAmount = 12;
            final int totalCookiePrice = boosterCookieAmount*cookieCost;

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                if (player.getGems() >= totalCookiePrice) {
                    player.addAndUpdateItem(ItemType.BOOSTER_COOKIE, boosterCookieAmount);
                    player.removeGems(totalCookiePrice);
                    new GUIBuyBoosterCookies().open(player);
                } else {
                    player.openBook(book);
                }
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return cookieStack("<6>A Dozen Cookies", "<6>Booster Cookie <8>x12", totalCookiePrice, player.getGems());
            }
        });
        set(new GUIClickableItem(49) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                p.openBook(book);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return ItemStacks.enchanted(ItemStacks.item(Material.EMERALD, """
                        <a>Community Shop
                        <8>Elizabeth

                        <7>Gems: <a>{:,}
                        <8>Purchase on store.hypixel.net!

                        <7>Bits: <b>{:,}
                        <8>Earn from Booster Cookies!

                        <7>Fame Rank: <e>
                        <8>Rank up by spending gems & bits!
                        <e>Click to get link!""", player.getGems(), player.getBits()));
            }
        });
        updateItemStacks(getInventory(), getPlayer());
    }
    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
    }
}
