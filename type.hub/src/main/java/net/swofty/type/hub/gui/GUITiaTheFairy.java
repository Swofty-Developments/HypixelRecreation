package net.swofty.type.hub.gui;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointBackpacks;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointFairySouls;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelCause;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.fairysouls.FairySoulExchangeLevels;

import java.util.ArrayList;
import java.util.List;

public class GUITiaTheFairy extends HypixelInventoryGUI {
    public GUITiaTheFairy() {
        super("Fairy", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));
        set(GUIClickableItem.getCloseItem(49));

        int collectedAmount = ((SkyBlockPlayer) getPlayer()).getFairySouls().getCollectedFairySouls().size();
        boolean canExchange = collectedAmount >= 5;
        FairySoulExchangeLevels nextLevel = ((SkyBlockPlayer) getPlayer()).getFairySouls().getNextExchangeLevel();

        set(new GUIClickableItem(22) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                if (!canExchange) {
                    player.sendMessage("<c>You don't have enough Fairy Souls!");
                    return;
                }

                player.closeInventory();
                player.getFairySouls().exchange();
                player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.FAIRY_SOULS, DatapointFairySouls.class)
                                .setValue(player.getFairySouls());
                player.sendMessage("<a>You have exchanged your Fairy Souls for rewards!");
                nextLevel.getDisplay().forEach(line -> player.sendMessage(line));

                player.getSkyBlockExperience().addExperience(
                        SkyBlockLevelCause.getFairySoulExchangeCause(nextLevel.ordinal())
                );

                DatapointBackpacks.PlayerBackpacks backpacks = ((SkyBlockPlayer) getPlayer()).getSkyblockDataHandler().get(
                        SkyBlockDataHandler.Data.BACKPACKS, DatapointBackpacks.class
                ).getValue();
                backpacks.setUnlockedSlots(backpacks.getUnlockedSlots() +
                        nextLevel.getBackpackSlots());
                ((SkyBlockPlayer) getPlayer()).getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BACKPACKS, DatapointBackpacks.class)
                        .setValue(backpacks);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                List<Text> lore = new ArrayList<>(List.of(
                        Text.of("<7>Find <d>Fairy Souls </d>around the"),
                        Text.of("<7>world and bring them back to me"),
                        Text.of("<7>and I will reward you with"),
                        Text.of("<7>SkyBlock XP and Backpack Slots!"),
                        Text.empty(),
                        Text.of("<7>Fairy Souls: " + (canExchange ? "<a>" : "<e>") + "{}<7>/<d>5", collectedAmount),
                        Text.empty(),
                        Text.of("<7>Next Reward:")
                ));

                nextLevel.getDisplay().forEach(line -> lore.add(Text.of("<7>{}", Text.parse(line))));

                lore.addAll(List.of(
                        Text.empty(),
                        Text.of(canExchange ? "<e>Click to exchange!" : "<c>You don't have enough Fairy Souls!")
                ));

                return ItemStacks.head("b96923ad247310007f6ae5d326d847ad53864cf16c3565a181dc8e6b20be2387",
                        Text.of("<a>Exchange Fairy Souls"), lore);
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
        e.setCancelled(true);
    }
}
