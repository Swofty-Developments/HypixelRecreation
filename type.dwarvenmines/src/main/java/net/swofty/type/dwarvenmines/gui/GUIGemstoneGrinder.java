package net.swofty.type.dwarvenmines.gui;

import net.minestom.server.component.DataComponents;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributeGemData;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.gems.Gemstone;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.GemstoneComponent;
import net.swofty.type.skyblockgeneric.item.components.GemstoneImplComponent;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUIGemstoneGrinder extends HypixelInventoryGUI {
    private final static int[] PRE_SLOTS = {28, 29, 30, 31, 32, 33, 34};
    private static final Map<Integer, int[]> SLOTS = new HashMap<>(Map.of(
            0, new int[]{},
            1, new int[]{31},
            2, new int[]{30, 32},
            3, new int[]{30, 31, 32},
            4, new int[]{29, 30, 32, 33},
            5, new int[]{29, 30, 31, 32, 33}
    ));
    private SkyBlockItem item = null;

    public GUIGemstoneGrinder() {
        super("Gemstone Grinder", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(FILLER_ITEM);
        set(GUIClickableItem.getCloseItem(49));

        set(new GUIClickableItem(50) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIGemstoneGuide().open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.REDSTONE_TORCH, """
                        <a>Gemstone Guide
                        <7>Many items can have <d>Gemstones
                        <7>applied to them. Gemstones increase
                        <7>the stats of an item based on the
                        <7>type of Gemstone used.

                        <7>There are several <a>qualities <7>of
                        <7>Gemstones, ranging from <f>Rough <7>to
                        <6>Perfect<7>. The higher the quality, the
                        <7>better the stat!

                        <7>This guide shows the items that can
                        <7>have Gemstones applied to them.

                        <e>Click to view!""");
            }
        });

        updateFromItem(null);
    }

    public void updateFromItem(SkyBlockItem item) {
        this.item = item;

        if (item == null) {
            set(new GUIClickableItem(13) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    ItemStack stack = player.getInventory().getCursorItem();
                    SkyBlockItem item = new SkyBlockItem(stack);

                    if (stack.get(DataComponents.CUSTOM_NAME) == null) return;
                    if (!item.hasComponent(GemstoneComponent.class)) {
                        player.sendMessage("<c>Only items that can have Gemstones applied to them can be put in the Grinder!");
                        return;
                    }

                    e.setCancelled(true);
                    player.getInventory().setCursorItem(ItemStack.AIR);
                    updateFromItem(item);
                }

                @Override
                public boolean canPickup() {
                    return true;
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStack.builder(Material.AIR);
                }
            });

            for (int slot : PRE_SLOTS) {
                set(new GUIItem(slot) {
                    @Override
                    public ItemStack.Builder getItem(HypixelPlayer p) {
                        return ItemStacks.item(Material.GRAY_STAINED_GLASS_PANE, """
                                <d>Gemstone Slot
                                <7>Place an item above to apply
                                <7>Gemstones to it!""");
                    }
                });
            }

            updateItemStacks(getInventory(), getPlayer());
            return;
        }

        for (int slot : PRE_SLOTS) {
            set(new GUIItem(slot) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return FILLER_ITEM;
                }
            });
        }

        set(new GUIClickableItem(13) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                ItemStack stack = player.getInventory().getCursorItem();

                if (stack == ItemStack.AIR) {
                    e.setCancelled(true);
                    player.getInventory().setCursorItem(PlayerItemUpdater.playerUpdate(player, item.getItemStack()).build());
                    updateFromItem(null);
                }
            }

            @Override
            public boolean canPickup() {
                return true;
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return PlayerItemUpdater.playerUpdate(player, item.getItemStack());
            }
        });

        List<GemstoneComponent.GemstoneSlot> gemstoneSlots = item.getComponent(GemstoneComponent.class).getSlots();
        int[] slotsToPlaceGems = SLOTS.get(gemstoneSlots.size());
        int index = 0;
        for (GemstoneComponent.GemstoneSlot gemstoneSlot : gemstoneSlots) {
            int slot = slotsToPlaceGems[index];
            ItemAttributeGemData.GemData gemData = item.getAttributeHandler().getGemData();
            Gemstone.Slots gemSlot = gemstoneSlot.slot();

            if (gemData.isSlotUnlocked(index)) {
                if (gemData.getGem(index).filledWith != null) {
                    SkyBlockItem appliedGem = new SkyBlockItem(gemData.getGem(index).filledWith);

                    int finalIndex = index;
                    set(new GUIClickableItem(slot) {
                        @Override
                        public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                            SkyBlockPlayer player = (SkyBlockPlayer) p;
                            int costToRemove = appliedGem.getComponent(GemstoneImplComponent.class).getGemRarity().costToRemove;
                            if (player.getCoins() >= costToRemove) {
                                player.removeCoins(costToRemove);
                                player.addAndUpdateItem(appliedGem);
                                gemData.removeGem(finalIndex);

                                updateFromItem(item);
                            } else {
                                player.sendMessage("<c>You don't have enough coins to remove this!");
                            }
                        }

                        @Override
                        public ItemStack.Builder getItem(HypixelPlayer p) {
                            SkyBlockPlayer player = (SkyBlockPlayer) p;

                            ItemStack.Builder itemStack = PlayerItemUpdater.playerUpdate(
                                    player, appliedGem.getItemStack()
                            );
                            List<Text> lore = appliedGem.getLoreText();

                            lore.add(Text.empty());
                            lore.add(Text.of("<7>Cost to Remove"));
                            lore.add(Text.of("<6>{:,} Coins",
                                    appliedGem.getComponent(GemstoneImplComponent.class).getGemRarity().costToRemove));
                            lore.add(Text.empty());
                            lore.add(Text.of("<e>Click to remove!"));

                            return ItemStacks.lore(itemStack, lore);
                        }
                    });
                } else {
                    set(new GUIItem(slot) {
                        @Override
                        public ItemStack.Builder getItem(HypixelPlayer player) {
                            Text title = Text.of("<color:{}>{} {} Gemstone Slot",
                                    gemSlot.getColor(), gemSlot.getSymbol(), gemSlot.getName());
                            List<Text> lore = new ArrayList<>();

                            if (gemSlot.getValidGemstones().size() > 1) { // Universal Slot
                                lore.add(Text.of("<7>Click <a>any Gemstone <7>of any quality in"));
                                lore.add(Text.of("<7>your inventory to apply it to this item!"));
                                lore.add(Text.empty());
                                lore.add(Text.of("<7>Applicable Gemstones"));
                                for (Gemstone gemstone : gemSlot.getValidGemstones()) {
                                    lore.add(Text.of("<color:{}>{} Gemstone", gemstone.getColor(),
                                            StringUtility.toNormalCase(gemstone.name())));
                                }
                            } else { // Specific Gem Slot
                                Gemstone gemstone = gemSlot.getValidGemstones().getFirst();
                                lore.add(Text.of("<7>Click a <color:{}>{} Gemstone <7>of any",
                                        gemstone.getColor(), StringUtility.toNormalCase(gemstone.name())));
                                lore.add(Text.of("<7>quality in your inventory to apply it"));
                                lore.add(Text.of("<7>to this item!"));
                            }

                            return ItemStacks.item(gemSlot.paneColor, 1, title, lore);
                        }
                    });
                }
            } else {
                int finalI = index;
                set(new GUIClickableItem(slot) {
                    @Override
                    public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                        SkyBlockPlayer player = (SkyBlockPlayer) p;

                        int coins = Math.max(gemstoneSlot.unlockPrice(), 0);

                        List<GemstoneComponent.ItemRequirement> requirements = gemstoneSlot.itemRequirements();
                        Map<ItemType, Integer> itemRequirements = new HashMap<>();

                        for (GemstoneComponent.ItemRequirement requirement : requirements) {
                            itemRequirements.put(requirement.itemId(), requirement.amount());
                        }

                        if (player.getCoins() < coins) {
                            player.sendMessage("<c>You don't have the required items!");
                            return;
                        }

                        if (!player.removeItemsFromPlayer(itemRequirements)) {
                            player.sendMessage("<c>You don't have the required items!");
                            return;
                        }

                        player.removeCoins(coins);
                        gemData.unlockSlot(finalI);

                        updateFromItem(item);
                    }

                    @Override
                    public ItemStack.Builder getItem(HypixelPlayer player) {
                        Text title = Text.of("<color:{}>{} {} Gemstone Slot",
                                gemSlot.getColor(), gemSlot.getSymbol(), gemSlot.getName());

                        List<Text> lore = new ArrayList<>();
                        lore.add(Text.of("<7>This slot is locked! Purchasing this"));
                        lore.add(Text.of("<7>slot allows you to apply a"));
                        lore.add(Text.of("<color:{}>{} {} Gemstone <7>to it!",
                                gemSlot.getColor(), gemSlot.getSymbol(), gemSlot.getName()));
                        lore.add(Text.empty());

                        if (gemSlot.getValidGemstones().size() > 1) {
                            for (Gemstone gemstone : gemSlot.getValidGemstones()) {
                                lore.add(Text.of("<color:{}>{} Gemstone", gemstone.getColor(),
                                        StringUtility.toNormalCase(gemstone.name())));
                            }
                            lore.add(Text.empty());
                        }

                        if (!gemstoneSlot.itemRequirements().isEmpty() || gemstoneSlot.unlockPrice() > 0) {
                            lore.add(Text.of("<7>Cost"));
                            if (gemstoneSlot.unlockPrice() > 0) {
                                lore.add(Text.of("<6>{:,} Coins", gemstoneSlot.unlockPrice()));
                            }
                            if (!gemstoneSlot.itemRequirements().isEmpty()) {
                                for (GemstoneComponent.ItemRequirement requirement : gemstoneSlot.itemRequirements()) {
                                    Gemstone.Slots slots = Gemstone.Slots.getFromGemstone(Gemstone.getFromItemType(requirement.itemId()));
                                    SkyBlockItem skyBlockItem = new SkyBlockItem(requirement.itemId());
                                    lore.add(Text.of("<color:{}>{} {} <8>x{}",
                                            skyBlockItem.getComponent(GemstoneImplComponent.class).getGemRarity()
                                                    .getRarity().getColor().asHexString(),
                                            slots.getSymbol(),
                                            skyBlockItem.getDisplayName(),
                                            requirement.amount()));
                                }
                            }
                        }

                        lore.add(Text.empty());
                        lore.add(Text.of("<e>Click to unlock!"));

                        return ItemStacks.item(Material.GRAY_STAINED_GLASS_PANE, 1, title, lore);
                    }
                });
            }

            index++;
        }

        updateItemStacks(getInventory(), getPlayer());
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.getPlayer();
        SkyBlockItem clickedItem = new SkyBlockItem(e.getClickedItem());
        if (item == null) return;

        if (!clickedItem.hasComponent(GemstoneImplComponent.class)) {
            player.sendMessage("<c>You cannot apply that to this item!");
            e.setCancelled(true);
            return;
        }
        ItemAttributeGemData.GemData gemData = item.getAttributeHandler().getGemData();

        List<GemstoneComponent.GemstoneSlot> itemSlots = item.getComponent(GemstoneComponent.class).getSlots();
        int index = 0;
        for (GemstoneComponent.GemstoneSlot slot : itemSlots) {
            ItemAttributeGemData.GemData.GemSlots gemSlot = gemData.getGem(index);
            index++;
            if (gemSlot.filledWith != null) continue;

            List<ItemType> allowedGems = new ArrayList<>();
            for (Gemstone gemstone : slot.slot().getValidGemstones()) {
                allowedGems.addAll(gemstone.item);
            }

            if (gemSlot.isUnlocked() && allowedGems.contains(clickedItem.getAttributeHandler().getPotentialType())) {
                gemSlot.setFilledWith(clickedItem.getAttributeHandler().getPotentialType());
                player.getInventory().setItemStack(e.getSlot(), ItemStack.AIR);
                updateFromItem(item);
                break;
            }
        }
        e.setCancelled(true);
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.getPlayer();
        if (item != null) player.addAndUpdateItem(item);
    }
}
