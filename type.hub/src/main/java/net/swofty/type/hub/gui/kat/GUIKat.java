package net.swofty.type.hub.gui.kat;

import net.minestom.server.component.DataComponents;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.KatUpgrade;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIKat extends HypixelInventoryGUI {

    boolean pricePaid = false;

    public GUIKat() {
        super("Pet Sitter", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));
        set(GUIClickableItem.getCloseItem(40));

        updateFromItem(null);
    }

    public void updateFromItem(SkyBlockItem item) {

        if (item == null) {
            set(new GUIClickableItem(13) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    ItemStack stack = p.getInventory().getCursorItem();

                    if (stack.get(DataComponents.CUSTOM_NAME) == null) {
                        updateFromItem(null);
                        return;
                    }

                    SkyBlockItem item = new SkyBlockItem(stack);
                    updateFromItem(item);
                }

                @Override
                public boolean canPickup() {
                    return true;
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                    return ItemStack.builder(Material.AIR);
                }
            });
            set(new GUIClickableItem(22) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                    player.sendMessage("<c>Place a pet in the empty slot for Kat to take care of!");
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                    return ItemStacks.item(Material.RED_TERRACOTTA, """
                            <e>Pet Sitter
                            <7>Place a pet above for Kat to take
                            <7>care of!

                            <7>After some time, your pet <9>Rarity <7>will
                            <7>be upgraded!""");
                }
            });
            updateItemStacks(getInventory(), getPlayer());
            return;
        }

        set(new GUIClickableItem(13) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return PlayerItemUpdater.playerUpdate(player, item.getItemStack());
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                ItemStack stack = e.getClickedItem();
                if (stack.isAir()) return;

                updateFromItem(null);

                player.addAndUpdateItem(stack);
            }
        });

        if (item.getAmount() > 1 || !(item.hasComponent(PetComponent.class))) {
            set(new GUIItem(22) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                    return ItemStacks.item(Material.BARRIER, "<c>Error!\n<c>Kat only takes care of pets!");
                }
            });
            updateItemStacks(getInventory(), getPlayer());
            return;
        }

        set(new GUIClickableItem(22) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                if (item.getComponent(PetComponent.class).getKatUpgrades().getForRarity(item.getAttributeHandler().getRarity().upgrade()) == null) return;
                KatUpgrade katUpgrade = item.getComponent(PetComponent.class).getKatUpgrades().getForRarity(item.getAttributeHandler().getRarity().upgrade());

                if (player.getCoins() < katUpgrade.getCoins() || !hasRequiredItems(player, katUpgrade)) return;

                new GUIConfirmKat(item).open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                if (item.getComponent(PetComponent.class).getKatUpgrades().getForRarity(item.getAttributeHandler().getRarity().upgrade()) == null) {
                    return ItemStacks.item(Material.RED_TERRACOTTA, "<a>Something went wrong!");
                }
                KatUpgrade katUpgrade = item.getComponent(PetComponent.class).getKatUpgrades().getForRarity(item.getAttributeHandler().getRarity().upgrade());
                int coins = katUpgrade.getCoins();
                long time = katUpgrade.getTime();
                ItemType upgradeItem = katUpgrade.getItem();
                Integer itemAmount = katUpgrade.getAmount();
                List<Text> lore = new ArrayList<>();
                Material material = Material.RED_TERRACOTTA;
                if (player.getCoins() >= coins && hasRequiredItems(player, katUpgrade)) {
                    material = Material.GREEN_TERRACOTTA;
                }
                lore.add(Text.of("<7>Kat will take care of your <5>{}", item.getDisplayName()));
                lore.add(Text.of("<7>for <9>{} <7>then its <9>rarity<7> will be", StringUtility.formatTimeLeftWrittenOut(time)));
                lore.add(Text.of("<7>upgraded!"));
                lore.add(Text.empty());
                lore.add(Text.of("<7>Cost"));
                if (upgradeItem != null) {
                    lore.add(Text.of("<9>{} <8>x{}", StringUtility.toNormalCase(upgradeItem.name()), itemAmount));
                }
                if (coins != 0) {
                    lore.add(Text.of("<6>{:,} Coins", coins));
                }
                if (player.getCoins() >= coins && hasRequiredItems(player, katUpgrade)) {
                    lore.add(Text.empty());
                    lore.add(Text.of("<e>Click to hire Kat!"));
                } else if (player.getCoins() < coins) {
                    lore.add(Text.empty());
                    lore.add(Text.of("<c>You don't have enough Coins!"));
                } else {
                    lore.add(Text.empty());
                    lore.add(Text.of("<c>You don't have the required items!"));
                }
                return ItemStacks.item(material, 1, Text.of("<a>Hire Kat"), lore);
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
        if (reason == CloseReason.SERVER_EXITED && pricePaid) return;
        ((SkyBlockPlayer) e.getPlayer()).addAndUpdateItem(new SkyBlockItem(e.getInventory().getItemStack(13)));
    }

    @Override
    public void suddenlyQuit(Inventory inventory, HypixelPlayer player) {
        ((SkyBlockPlayer) player).addAndUpdateItem(new SkyBlockItem(inventory.getItemStack(13)));
    }

    private static boolean hasRequiredItems(SkyBlockPlayer player, KatUpgrade upgrade) {
        if (upgrade.getItem() == null) return true;
        return upgrade.getAmount() != null
                && player.getAmountInInventory(upgrade.getItem()) >= upgrade.getAmount();
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {

    }
}
