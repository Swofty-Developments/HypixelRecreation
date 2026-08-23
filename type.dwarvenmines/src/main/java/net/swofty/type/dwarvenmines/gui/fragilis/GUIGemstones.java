package net.swofty.type.dwarvenmines.gui.fragilis;

import lombok.Getter;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.Arrays;
import java.util.List;

public class GUIGemstones extends HypixelInventoryGUI {
    private final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};

    public GUIGemstones() {
        super("Gemstones", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        border(FILLER_ITEM);
        set(GUIClickableItem.getGoBackItem(48, new GUIHandyBlockGuide()));
        set(GUIClickableItem.getCloseItem(49));

        set(new GUIItem(4) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.BOOK, """
                        <d>Gemstones
                        <8>Block Classification

                        <7>These blocks are affected by:\s
                        <8> - <6>☘ Gemstone Fortune
                        <8> - <6>☘ Mining Fortune
                        <8> - <e>▚ Gemstone Spread
                        <8> - <5>✧ Pristine""");
            }
        });

        List<GemstoneData> gemstones = Arrays.asList(GemstoneData.values());

        for (int i = 0; i < gemstones.size() && i < SLOTS.length; i++) {
            final GemstoneData gemstone = gemstones.get(i);
            final int slot = SLOTS[i];

            set(new GUIItem(slot) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    return ItemStacks.item(gemstone.getMaterial(), 1, """
                            <d>{} Gemstone
                            <8>Gemstone

                            <7>Properties
                            <7> Breaking Power: <2>{}
                            <7> Block Strength: <e>{:,}""",
                            gemstone.getDisplayName(), gemstone.getBreakingPower(), gemstone.getBlockStrength());
                }
            });
        }

        updateItemStacks(getInventory(), getPlayer());
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }

    @Getter
    public enum GemstoneData {
        RUBY("Ruby", Material.RED_STAINED_GLASS, 6, 2300),
        AMBER("Amber", Material.ORANGE_STAINED_GLASS, 7, 3000),
        SAPPHIRE("Sapphire", Material.LIGHT_BLUE_STAINED_GLASS, 7, 3000),
        JADE("Jade", Material.LIME_STAINED_GLASS, 7, 3000),
        AMETHYST("Amethyst", Material.PURPLE_STAINED_GLASS, 7, 3000),
        OPAL("Opal", Material.WHITE_STAINED_GLASS, 7, 3000),
        TOPAZ("Topaz", Material.YELLOW_STAINED_GLASS, 8, 3800),
        JASPER("Jasper", Material.MAGENTA_STAINED_GLASS, 9, 4800),
        ONYX("Onyx", Material.BLACK_STAINED_GLASS, 9, 5200),
        AQUAMARINE("Aquamarine", Material.BLUE_STAINED_GLASS, 9, 5200),
        CITRINE("Citrine", Material.BROWN_STAINED_GLASS, 9, 5200),
        PERIDOT("Peridot", Material.GREEN_STAINED_GLASS, 9, 5200);

        private final String displayName;
        private final Material material;
        private final int breakingPower;
        private final int blockStrength;

        GemstoneData(String displayName, Material material, int breakingPower, int blockStrength) {
            this.displayName = displayName;
            this.material = material;
            this.breakingPower = breakingPower;
            this.blockStrength = blockStrength;
        }
    }
}
