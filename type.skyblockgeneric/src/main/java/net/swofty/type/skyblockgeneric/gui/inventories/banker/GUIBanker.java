package net.swofty.type.skyblockgeneric.gui.inventories.banker;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.RefreshingGUI;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointBankData;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIBanker extends HypixelInventoryGUI implements RefreshingGUI {
    public GUIBanker() {
        super(Text.key("gui_banker.main.title"), InventoryType.CHEST_4_ROW);
    }

    @Override
    public void setItems(InventoryGUIOpenEvent e) {
        if (((SkyBlockPlayer) e.player()).isBankDelayed) {
            e.player().sendMessage(Text.key("gui_banker.main.processing_transactions"));
            e.player().sendMessage(Text.key("gui_banker.main.processing_wait"));
            e.player().closeInventory();
            return;
        }

        setTitle(Text.key(((SkyBlockPlayer) e.player()).isCoop()
            ? "gui_banker.main.title_coop"
            : "gui_banker.main.title_personal"));

        refreshItems(e.player());
    }

    @Override
    public void refreshItems(HypixelPlayer player) {
        fill(Material.BLACK_STAINED_GLASS_PANE, "");
        set(GUIClickableItem.getCloseItem(31));

        DatapointBankData.BankData bankData = (((SkyBlockPlayer) player).getSkyblockDataHandler())
            .get(SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class)
            .getValue();

        set(new GUIItem(32) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.REDSTONE_TORCH, 1,
                    Text.key("gui_banker.main.information"),
                    Text.keyLines("gui_banker.main.information.lore",
                        StringUtility.commaify(bankData.getBalanceLimit()),
                        SkyBlockCalendar.getHoursUntilNextInterest()));
            }
        });

        set(new GUIClickableItem(11) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                new GUIBankerDeposit().open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.CHEST, 1,
                    Text.key("gui_banker.main.deposit"),
                    Text.keyLines("gui_banker.main.deposit.lore",
                        StringUtility.decimalify(bankData.getAmount(), 1),
                        SkyBlockCalendar.getHoursUntilNextInterest()));
            }
        });

        set(new GUIClickableItem(13) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                new GUIBankerWithdraw().open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.DISPENSER, 1,
                    Text.key("gui_banker.main.withdraw"),
                    Text.keyLines("gui_banker.main.withdraw.lore",
                        StringUtility.decimalify(bankData.getAmount(), 1)));
            }
        });

        set(new GUIItem(15) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                List<Text> lore = new ArrayList<>();
                List<DatapointBankData.Transaction> transactions = bankData.getTransactions();

                if (transactions.isEmpty()) lore.add(Text.key("gui_banker.main.no_transactions"));
                else {
                    for (int i = Math.min(transactions.size() - 1, 10); i >= 0; i--) {
                        DatapointBankData.Transaction transaction = transactions.get(i);

                        boolean isNegative = transaction.amount < 0;

                        lore.add(Text.key("gui_banker.main.transaction_entry",
                            Text.of(isNegative ? "<c>-" : "<a>+"),
                            StringUtility.decimalify(Math.abs(transaction.amount), 1),
                            StringUtility.formatTimeAsAgo(transaction.timestamp),
                            transaction.originator));
                    }
                }

                return ItemStacks.item(Material.FILLED_MAP, 1,
                    Text.key("gui_banker.main.recent_transactions"), lore);
            }
        });

        set(new GUIClickableItem(35) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                new GUIBankUpgrades().open(p);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.GOLD_BLOCK, 1,
                    Text.key("gui_banker.main.account_upgrades"),
                    Text.keyLines("gui_banker.main.account_upgrades.lore",
                        Text.of("<color:{0}>{1}", bankData.getAccountTier().getColor(),
                            bankData.getAccountTier().getDisplayName()),
                        StringUtility.commaify(bankData.getBalanceLimit())));
            }
        });
    }

    @Override
    public int refreshRate() {
        return 20;
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
