package net.swofty.type.hub.gui;

import net.minestom.server.color.Color;
import net.minestom.server.component.DataComponents;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class GUISeymour extends HypixelInventoryGUI {
    private final List<SkyBlockItem> cheapTuxedoSet = List.of(
            new SkyBlockItem(ItemType.CHEAP_TUXEDO_CHESTPLATE),
            new SkyBlockItem(ItemType.CHEAP_TUXEDO_LEGGINGS),
            new SkyBlockItem(ItemType.CHEAP_TUXEDO_BOOTS)
    );

    private final List<SkyBlockItem> fancyTuxedoSet = List.of(
            new SkyBlockItem(ItemType.FANCY_TUXEDO_CHESTPLATE),
            new SkyBlockItem(ItemType.FANCY_TUXEDO_LEGGINGS),
            new SkyBlockItem(ItemType.FANCY_TUXEDO_BOOTS)
    );

    private final List<SkyBlockItem> elegantTuxedoSet = List.of(
            new SkyBlockItem(ItemType.ELEGANT_TUXEDO_CHESTPLATE),
            new SkyBlockItem(ItemType.ELEGANT_TUXEDO_LEGGINGS),
            new SkyBlockItem(ItemType.ELEGANT_TUXEDO_BOOTS)
    );

    private final double cheapTuxedoPrice = 3_000_000;
    private final double fancyTuxedoPrice = 20_000_000;
    private final double elegantTuxedoPrice = 74_999_999;

    private final double cheapTuxedoCritDamage = cheapTuxedoSet.stream()
                    .mapToDouble(item -> item.getAttributeHandler().getStatistics().getOverall(ItemStatistic.CRITICAL_DAMAGE))
                    .sum();
    private final double fancyTuxedoCritDamage = cheapTuxedoSet.stream()
            .mapToDouble(item -> item.getAttributeHandler().getStatistics().getOverall(ItemStatistic.CRITICAL_DAMAGE))
            .sum();
    private final double elegantTuxedoCritDamage = cheapTuxedoSet.stream()
            .mapToDouble(item -> item.getAttributeHandler().getStatistics().getOverall(ItemStatistic.CRITICAL_DAMAGE))
            .sum();

    private final double cheapTuxedoIntelligence = cheapTuxedoSet.stream()
            .mapToDouble(item -> item.getAttributeHandler().getStatistics().getOverall(ItemStatistic.INTELLIGENCE))
            .sum();
    private final double fancyTuxedoIntelligence = cheapTuxedoSet.stream()
            .mapToDouble(item -> item.getAttributeHandler().getStatistics().getOverall(ItemStatistic.INTELLIGENCE))
            .sum();
    private final double elegantTuxedoIntelligence = cheapTuxedoSet.stream()
            .mapToDouble(item -> item.getAttributeHandler().getStatistics().getOverall(ItemStatistic.INTELLIGENCE))
            .sum();

    public GUISeymour() {
        super("Seymour's Fancy Suits", InventoryType.CHEST_4_ROW);
    }

    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));
        set(GUIClickableItem.getCloseItem(31));

        set(new GUIClickableItem(11) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                double coins = player.getCoins();
                if (coins < cheapTuxedoPrice) {
                    player.sendMessage("<c>You don't have enough coins!");
                    return;
                }
                cheapTuxedoSet.forEach(player::addAndUpdateItem);
                player.playSuccessSound();
                player.removeCoins(cheapTuxedoPrice);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                ItemStack.Builder builder = ItemStacks.item(Material.LEATHER_CHESTPLATE, """
                        <5>Cheap Tuxedo

                        <8>Complete suit
                        <7>Crit Damage: <c>+{}%
                        <7>Intelligence: <a>+{}

                        <6>Full Set Bonus: Dashing <7>(0/3)
                        <7>Max Health set to <c>75♥</c>.
                        <7>Deal <c>+50% </c>damage!
                        <8>Very stylish.

                        <7>Cost: <6>{:,} Coins

                        {}""",
                        (int) cheapTuxedoCritDamage, (int) cheapTuxedoIntelligence, cheapTuxedoPrice,
                        Text.of(player.getCoins() >= cheapTuxedoPrice ? "<e>Click to purchase" : "<c>Can't afford this!"));

                builder.set(DataComponents.DYED_COLOR, new Color(56, 56, 56));
                return builder;
            }
        });

        set(new GUIClickableItem(13) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                double coins = player.getCoins();
                if (coins < fancyTuxedoPrice) {
                    player.sendMessage("<c>You don't have enough coins!");
                    return;
                }
                fancyTuxedoSet.forEach(player::addAndUpdateItem);
                player.playSuccessSound();
                player.removeCoins(fancyTuxedoPrice);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                ItemStack.Builder builder = ItemStacks.item(Material.LEATHER_CHESTPLATE, """
                        <6>Fancy Tuxedo

                        <8>Complete suit
                        <7>Crit Damage: <c>+{}%
                        <7>Intelligence: <a>+{}

                        <6>Full Set Bonus: Dashing <7>(0/3)
                        <7>Max Health set to <c>150♥</c>.
                        <7>Deal <c>+100% </c>damage!
                        <8>Very stylish.

                        <7>Cost: <6>{:,} Coins

                        {}""",
                        (int) fancyTuxedoCritDamage, (int) fancyTuxedoIntelligence, fancyTuxedoPrice,
                        Text.of(player.getCoins() >= fancyTuxedoPrice ? "<e>Click to purchase" : "<c>Can't afford this!"));

                builder.set(DataComponents.DYED_COLOR, new Color(51, 42, 42));

                return builder;
            }
        });

        set(new GUIClickableItem(15) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                double coins = player.getCoins();
                if (coins < elegantTuxedoPrice) {
                    player.sendMessage("<c>You don't have enough coins!");
                    return;
                }
                elegantTuxedoSet.forEach(player::addAndUpdateItem);
                player.playSuccessSound();
                player.removeCoins(elegantTuxedoPrice);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                ItemStack.Builder builder = ItemStacks.item(Material.LEATHER_CHESTPLATE, """
                        <6>Elegant Tuxedo

                        <8>Complete suit
                        <7>Crit Damage: <c>+{}%
                        <7>Intelligence: <a>+{}

                        <6>Full Set Bonus: Dashing <7>(0/3)
                        <7>Max Health set to <c>1250♥</c>.
                        <7>Deal <c>+150% </c>damage!
                        <8>Very stylish.

                        <7>Cost: <6>{:,} Coins

                        {}""",
                        (int) elegantTuxedoCritDamage, (int) elegantTuxedoIntelligence, elegantTuxedoPrice,
                        Text.of(player.getCoins() >= elegantTuxedoPrice ? "<e>Click to purchase" : "<c>Can't afford this!"));

                builder.set(DataComponents.DYED_COLOR, new Color(25, 25, 25));
                return builder;
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
