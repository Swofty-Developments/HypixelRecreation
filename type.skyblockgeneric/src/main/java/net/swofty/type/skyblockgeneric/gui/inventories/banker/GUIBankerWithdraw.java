package net.swofty.type.skyblockgeneric.gui.inventories.banker;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIQueryItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.DataMutexService;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointBankData;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class GUIBankerWithdraw extends HypixelInventoryGUI {

    public GUIBankerWithdraw() {
        super(Text.key("gui_banker.withdraw.title"), InventoryType.CHEST_4_ROW);
    }

    @Override
    public void setItems(InventoryGUIOpenEvent e) {
        fill(ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
        set(GUIClickableItem.getGoBackItem(31, new GUIBanker()));

        double bankBalance = ((SkyBlockPlayer) e.player()).getSkyblockDataHandler().get(net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class).getValue().getAmount();

        set(new GUIClickableItem(10) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.DISPENSER, 64,
                        Text.key("gui_banker.withdraw.everything"),
                        List.of(
                                Text.key("gui_banker.withdraw.everything_subtitle"),
                                Text.literal(" "),
                            Text.key("gui_banker.withdraw.current_balance", StringUtility.decimalify(bankBalance, 1)),
                            Text.key("gui_banker.withdraw.amount_to_withdraw", StringUtility.decimalify(bankBalance, 1)),
                                Text.literal(" "),
                                Text.key("gui_banker.withdraw.click")
                        ));
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.closeInventory();
                attemptWithdrawal(player, bankBalance);
            }
        });

        set(new GUIClickableItem(12) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.DISPENSER, 32,
                        Text.key("gui_banker.withdraw.half_account"),
                        List.of(
                                Text.key("gui_banker.withdraw.everything_subtitle"),
                                Text.literal(" "),
                            Text.key("gui_banker.withdraw.current_balance", StringUtility.decimalify(bankBalance, 1)),
                            Text.key("gui_banker.withdraw.amount_to_withdraw", StringUtility.decimalify(bankBalance / 2, 1)),
                                Text.literal(" "),
                                Text.key("gui_banker.withdraw.click")
                        ));
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.closeInventory();
                attemptWithdrawal(player, bankBalance / 2);
            }
        });

        set(new GUIClickableItem(14) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.closeInventory();
                attemptWithdrawal(player, bankBalance / 5);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.DISPENSER, 1,
                        Text.key("gui_banker.withdraw.twenty_percent"),
                        List.of(
                                Text.key("gui_banker.withdraw.everything_subtitle"),
                                Text.literal(" "),
                            Text.key("gui_banker.withdraw.current_balance", StringUtility.decimalify(bankBalance, 1)),
                            Text.key("gui_banker.withdraw.amount_to_withdraw", StringUtility.decimalify(bankBalance / 5, 1)),
                                Text.literal(" "),
                                Text.key("gui_banker.withdraw.click")
                        ));
            }
        });

        set(new GUIQueryItem(16) {
            @Override
            public HypixelInventoryGUI onQueryFinish(String query, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                try {
                    double amount = Double.parseDouble(query);
                    if (amount > bankBalance) {
                        player.sendMessage(Text.key("gui_banker.withdraw.not_enough_coins"));
                        return null;
                    }
                    if (amount <= 0) {
                        player.sendMessage(Text.key("gui_banker.withdraw.invalid_amount"));
                        return null;
                    }

                    player.closeInventory();
                    attemptWithdrawal(player, amount);
                } catch (NumberFormatException ex) {
                    player.sendMessage(Text.key("gui_banker.withdraw.invalid_number"));
                }
                return null;
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return ItemStacks.item(Material.OAK_SIGN, 1,
                        Text.key("gui_banker.withdraw.custom_amount"),
                        List.of(
                                Text.key("gui_banker.withdraw.everything_subtitle"),
                                Text.literal(" "),
                            Text.key("gui_banker.withdraw.current_balance", StringUtility.decimalify(
                                player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class).getValue().getAmount(), 1)),
                                Text.literal(" "),
                                Text.key("gui_banker.withdraw.click")
                        ));
            }
        });
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {
        if (e == null) return;
        SkyBlockPlayer player = (SkyBlockPlayer) e.getPlayer();
        player.setBankDelayed(false);
    }

    @Override
    public void suddenlyQuit(Inventory inventory, HypixelPlayer player) {
        ((SkyBlockPlayer) player).setBankDelayed(false);
    }

    private void attemptWithdrawal(SkyBlockPlayer player, double amount) {
        player.sendMessage(Text.key("gui_banker.withdraw.withdrawing"));

        if (!player.isCoop()) {
            DatapointBankData datapoint = player.getSkyblockDataHandler()
                    .get(SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class);
            DatapointBankData.BankData bankData = datapoint.getValue();
            if (amount > bankData.getAmount()) {
                player.sendMessage(Text.key("gui_banker.withdraw.not_enough_coins"));
                return;
            }

            bankData.removeAmount(amount);
            bankData.addTransaction(new DatapointBankData.Transaction(
                    System.currentTimeMillis(), -amount, player.getUsername()));
            datapoint.setValue(bankData);

            player.addCoins(amount);
            player.sendMessage(Text.key("gui_banker.withdraw.success", StringUtility.decimalify(amount, 1), StringUtility.decimalify(bankData.getAmount(), 1)));
            return;
        }

        double[] newBalance = new double[1];
        DataMutexService.Outcome outcome = DataMutexService.withSynchronizedData(
                player.getSkyblockDataHandler().getCurrentProfileId(),
                SkyBlockDataHandler.Data.BANK_DATA,
                (DatapointBankData.BankData latestBankData) -> {
                    if (amount > latestBankData.getAmount()) return null;

                    latestBankData.removeAmount(amount);
                    latestBankData.addTransaction(new DatapointBankData.Transaction(
                            System.currentTimeMillis(), -amount, player.getUsername()));
                    newBalance[0] = latestBankData.getAmount();

                    return latestBankData;
                });

        switch (outcome) {
            case APPLIED -> {
                player.addCoins(amount);
                player.sendMessage(Text.key("gui_banker.withdraw.success",
                        StringUtility.decimalify(amount, 1), StringUtility.decimalify(newBalance[0], 1)));
            }
            case UNCHANGED -> player.sendMessage(Text.key("gui_banker.withdraw.not_enough_coins"));
            default -> player.sendMessage(Text.key("gui_banker.withdraw.coop_busy"));
        }
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
