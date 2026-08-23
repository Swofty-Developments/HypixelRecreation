package net.swofty.type.hub.gui.elizabeth;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIAccountAndProfileUpgrades extends HypixelInventoryGUI {

    public GUIAccountAndProfileUpgrades() {
        super("Community Shop", InventoryType.CHEST_6_ROW);
    }

    private final int[] categoriesItemsSlots = {
            10, 11, 12, 13, 14, 16
    };
    private final int[] tabSlots = {
            1, 2, 3, 4, 5, 7
    };
    public enum ShopCategorys {
        CITY_PROJECTS(new GUICityProjects(), Material.GOLDEN_HORSE_ARMOR, false, """
                <a>City Projects
                <7>Participate with the whole SkyBlock
                <7>community to upgrade the village
                <7>and more.

                <b>Contribute <7>to various projects to
                <7>obtain unique perks!

                """),
        ACCOUNT_AND_PROFILE_UPGRADES(new GUIAccountAndProfileUpgrades(), Material.HOPPER, false, """
                <d>Account & Profile Upgrades
                <7>Upgrade your current profile and your
                <7>SkyBlock account with permanent
                <7>upgrades.

                <7>Profile: <8>Nothing going on!
                <7>Account: <8>None underway!

                """),
        BOOSTER_COOKIE(new GUIBuyBoosterCookies(), Material.COOKIE, true, """
                <6>Booster Cookie
                <7>Obtain a temporary buff letting
                <7>you earn <b>bits</b>, as well as <d>tons of
                <d>perks<7>.

                """),
        BITS_SHOP(new GUIBitsShop(), Material.DIAMOND, true, """
                <b>Bits Shop
                <7>Spend <b>bits </b>on a variety of
                <7>powerful items.

                <7>Earn bits from <6>Booster Cookies</6>.

                """),
        FIRE_SALES(new GUIFIRESales(), Material.BLAZE_POWDER, true, """
                <6>♨ <c><l>FIRE </l>Sales <6>♨
                <7>Acquire <6>exclusive <7>cosmetics which are
                <7>only available in <c>limited quantity
                <7>across all of SkyBlock.

                <6><l>UPCOMING SALE</l>
                <c><l>0 </l><7>Fire Sales are starting soon.

                """),
        HYPIXEL_RANKS(new GUIHypixelRanks(), Material.EMERALD, true, """
                <e>Hypixel Ranks
                <7>Browse the SkyBlock perks of our
                <e>server-wide <7>ranks such as the
                <6>[MVP<2>++<6>] <7>rank.

                """),
        ;
        public final HypixelInventoryGUI gui;
        private final Material material;
        private final boolean enchanted;
        private final String textBlock;

        ShopCategorys(HypixelInventoryGUI gui, Material material, boolean enchanted, String textBlock) {
            this.gui = gui;
            this.material = material;
            this.enchanted = enchanted;
            this.textBlock = textBlock;
        }

        public ItemStack.Builder buildStack(boolean selected) {
            ItemStack.Builder builder = ItemStacks.item(material,
                    textBlock + (selected ? "<a>Currently selected!" : "<e>Click to view!"));
            return enchanted ? ItemStacks.enchanted(builder) : builder;
        }
    }

    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));

        ShopCategorys[] allShopCategorys = ShopCategorys.values();
        int index = 0;
        for (int slot : tabSlots) {
            ShopCategorys shopCategorys = allShopCategorys[index];
            set(new GUIClickableItem(slot) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    if (slot != 2) {
                        shopCategorys.gui.open(p);
                    }
                }
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return shopCategorys.buildStack(slot == 2);
                }
            });
            index++;
        }

        for (int slot : categoriesItemsSlots) {
            set(new GUIItem(slot) {
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStacks.item(slot != 11
                            ? Material.GRAY_STAINED_GLASS_PANE
                            : Material.GREEN_STAINED_GLASS_PANE, """
                            <8>▲ <7>Categories
                            <8>▼ <7>Items""");
                }
            });
        }
        set(new GUIClickableItem(49) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                p.openBook(Book.builder()
                        .addPage(Component.text("Purchase ranks, gems and more on our webstore!")
                                .appendNewline()
                                .appendNewline()
                                .append(Component.text("      "))
                                .append(Component.text("VISIT STORE").clickEvent(ClickEvent.openUrl("http://bit.ly/4aG54lt")).color(TextColor.fromHexString("#00AAAA"))))
                        .build()
                );
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
    public void onClose(InventoryCloseEvent e, CloseReason reason) {
    }

    @Override
    public void suddenlyQuit(Inventory inventory, HypixelPlayer player) {
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
    }
}
