package net.swofty.type.dwarvenmines.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelPaginatedGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.utility.PaginationList;
import net.swofty.type.skyblockgeneric.gems.Gemstone;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.GemstoneComponent;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.*;

public class GUIGemstoneGuide extends HypixelPaginatedGUI<SkyBlockItem> {
    protected GUIGemstoneGuide() {
        super(InventoryType.CHEST_6_ROW);
    }

    @Override
    public int[] getPaginatedSlots() {
        return new int[]{
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };
    }

    @Override
    protected boolean shouldFilterFromSearch(String query, SkyBlockItem item) {
        return false;
    }

    @Override
    protected PaginationList<SkyBlockItem> fillPaged(HypixelPlayer player, PaginationList<SkyBlockItem> paged) {
        paged.addAll(Arrays.stream(ItemType.values()).map(SkyBlockItem::new).toList());
        paged.removeIf((element) -> !element.hasComponent(GemstoneComponent.class) ||
                element.getComponent(GemstoneComponent.class).getSlots() == null);

        return paged;
    }

    @Override
    protected void performSearch(HypixelPlayer player, String query, int page, int maxPage) {
        border(FILLER_ITEM);
        set(GUIClickableItem.getGoBackItem(48, new GUIGemstoneGrinder()));
        set(GUIClickableItem.getCloseItem(49));

        set(new GUIItem(4) {
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
                        <7>have Gemstones applied to them.""");
            }
        });

        if (page > 1) {
            set(createNavigationButton(this, 45, query, page, false));
        }
        if (page < maxPage) {
            set(createNavigationButton(this, 53, query, page, true));
        }
    }

    @Override
    protected GUIClickableItem createItemFor(SkyBlockItem item, int slot, HypixelPlayer p) {
        SkyBlockPlayer player = (SkyBlockPlayer) p;
        ItemStack.Builder itemStack = PlayerItemUpdater.playerUpdate(
                player, item.getItemStack()
        );

        return new GUIClickableItem(slot) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                List<Text> lore = item.getLoreText();
                Map<Gemstone.Slots, Integer> slots = new HashMap<>();

                for (GemstoneComponent.GemstoneSlot slot : item.getComponent(GemstoneComponent.class).getSlots()) {
                    slots.put(slot.slot(), slots.getOrDefault(slot.slot(), 0) + 1);
                }

                lore.add(Text.empty());
                lore.add(Text.of("<7>Available Gemstone Slots"));
                for (Map.Entry<Gemstone.Slots, Integer> slot : slots.entrySet()) {
                    Gemstone.Slots key = slot.getKey();
                    lore.add(Text.of("  <color:{}>{} {} <8>x{}",
                            key.getColor(), key.getSymbol(), key.getName(), slot.getValue()));
                }

                return ItemStacks.lore(itemStack, lore);
            }
        };
    }

    @Override
    protected String getTitle(HypixelPlayer player, String query, int page, PaginationList<SkyBlockItem> paged) {
        return "(" + page + "/" + paged.getPageCount() + ") Gemstone Guide";
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {

    }
}
