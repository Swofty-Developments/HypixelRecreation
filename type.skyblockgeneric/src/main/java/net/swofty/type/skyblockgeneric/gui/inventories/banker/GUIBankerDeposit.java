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
import net.swofty.type.skyblockgeneric.mission.missions.MissionDepositCoinsInBank;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class GUIBankerDeposit extends HypixelInventoryGUI {

    public GUIBankerDeposit() {
        super(Text.key("gui_banker.deposit.title"), InventoryType.CHEST_4_ROW);
    }

    @Override
    public void setItems(InventoryGUIOpenEvent e) {
        fill(ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));
        set(GUIClickableItem.getGoBackItem(31, new GUIBanker()));

        set(new GUIClickableItem(11) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return ItemStacks.item(Material.CHEST, 64,
                        Text.key("gui_banker.deposit.whole_purse"),
                        List.of(
                                Text.key("gui_banker.deposit.whole_purse_subtitle"),
                                Text.literal(" "),
                            Text.key("gui_banker.deposit.current_balance", StringUtility.decimalify(
                                player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class).getValue().getAmount(), 1)),
                            Text.key("gui_banker.deposit.amount_to_deposit", StringUtility.decimalify(player.getCoins(), 1)),
                                Text.literal(" "),
                                Text.key("gui_banker.deposit.click")
                        ));
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.closeInventory();
                attemptDeposit(player, player.getCoins());
            }
        });

        set(new GUIClickableItem(13) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return ItemStacks.item(Material.CHEST, 32,
                        Text.key("gui_banker.deposit.half_purse"),
                        List.of(
                                Text.key("gui_banker.deposit.whole_purse_subtitle"),
                                Text.literal(" "),
                            Text.key("gui_banker.deposit.current_balance", StringUtility.decimalify(
                                player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class).getValue().getAmount(), 1)),
                            Text.key("gui_banker.deposit.amount_to_deposit", StringUtility.decimalify(player.getCoins() / 2, 1)),
                                Text.literal(" "),
                                Text.key("gui_banker.deposit.click")
                        ));
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                player.closeInventory();
                attemptDeposit(player, player.getCoins() / 2);
            }
        });

        set(new GUIQueryItem(15) {
            @Override
            public HypixelInventoryGUI onQueryFinish(String query, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                try {
                    double amount = Double.parseDouble(query);
                    if (amount > player.getCoins()) {
                        player.sendMessage(Text.key("gui_banker.deposit.not_enough_coins"));
                        return null;
                    }
                    if (amount <= 0) {
                        player.sendMessage(Text.key("gui_banker.deposit.invalid_amount"));
                        return null;
                    }

                    player.closeInventory();
                    attemptDeposit(player, amount);
                } catch (NumberFormatException ex) {
                    player.sendMessage(Text.key("gui_banker.deposit.invalid_number"));
                }
                return null;
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return ItemStacks.item(Material.OAK_SIGN, 1,
                        Text.key("gui_banker.deposit.custom_amount"),
                        List.of(
                                Text.key("gui_banker.deposit.whole_purse_subtitle"),
                                Text.literal(" "),
                            Text.key("gui_banker.deposit.current_balance", StringUtility.decimalify(
                                player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class).getValue().getAmount(), 1)),
                                Text.key("gui_banker.deposit.custom_amount_label"),
                                Text.literal(" "),
                                Text.key("gui_banker.deposit.click")
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

    private void attemptDeposit(SkyBlockPlayer player, double amount) {
        if (player.getMissionData().isCurrentlyActive(MissionDepositCoinsInBank.class)) {
            player.getMissionData().endMission(MissionDepositCoinsInBank.class);
        }
        DatapointBankData datapoint = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class);
        DatapointBankData.BankData bankData = datapoint.getValue();
        if (bankData.getAmount() + amount > bankData.getBalanceLimit()) {
            player.sendMessage(Text.key("gui_banker.deposit.exceed_limit", StringUtility.commaify(bankData.getBalanceLimit())));
            return;
        }

        player.sendMessage(Text.key("gui_banker.deposit.depositing"));

        if (!player.isCoop()) {
            bankData.addAmount(amount);
            bankData.addTransaction(new DatapointBankData.Transaction(
                    System.currentTimeMillis(),
                    amount,
                    player.getUsername()
            ));
            datapoint.setValue(bankData);
            player.removeCoins(amount);

            player.sendMessage(Text.key("gui_banker.deposit.success", StringUtility.decimalify(amount, 1), StringUtility.decimalify(bankData.getAmount(), 1)));
            return;
        }

        player.setBankDelayed(true);

        double[] newBalance = new double[1];
        double[] hitLimit = new double[1];
        DataMutexService.Outcome outcome = DataMutexService.withSynchronizedData(
                player.getSkyblockDataHandler().getCurrentProfileId(),
                SkyBlockDataHandler.Data.BANK_DATA,
                (DatapointBankData.BankData latestBankData) -> {
                    if (latestBankData.getAmount() + amount > latestBankData.getBalanceLimit()) {
                        hitLimit[0] = latestBankData.getBalanceLimit();
                        return null;
                    }

                    latestBankData.addAmount(amount);
                    latestBankData.addTransaction(new DatapointBankData.Transaction(
                            System.currentTimeMillis(), amount, player.getUsername()));
                    newBalance[0] = latestBankData.getAmount();

                    return latestBankData;
                });

        switch (outcome) {
            case APPLIED -> {
                player.removeCoins(amount);
                player.sendMessage(Text.key("gui_banker.deposit.success",
                        StringUtility.decimalify(amount, 1), StringUtility.decimalify(newBalance[0], 1)));
            }
            case UNCHANGED -> player.sendMessage(Text.key("gui_banker.deposit.exceed_limit",
                    StringUtility.commaify(hitLimit[0])));
            default -> player.sendMessage(Text.key("gui_banker.deposit.coop_busy"));
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
