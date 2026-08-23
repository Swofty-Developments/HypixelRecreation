package net.swofty.type.dwarvenmines.gui.fragilis;

import lombok.Getter;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GUIBlocks extends HypixelInventoryGUI {
    private final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};

    public GUIBlocks() {
        super("Blocks", InventoryType.CHEST_6_ROW);
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
                        <9>Blocks
                        <8>Block Classification

                        <7>These blocks are affected by:\s
                        <8> - <6>☘ Block Fortune
                        <8> - <6>☘ Mining Fortune
                        <8> - <e>▚ Mining Spread""");
            }
        });

        List<BlockData> blocks = Arrays.asList(BlockData.values());

        for (int i = 0; i < blocks.size() && i < SLOTS.length; i++) {
            final BlockData block = blocks.get(i);
            final int slot = SLOTS[i];

            set(new GUIItem(slot) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    List<Text> lore = new ArrayList<>();

                    lore.add(Text.of("<8>Block"));

                    if (block.getBreakingPower() > 0 || block.getBlockStrength() > 0) {
                        lore.add(Text.empty());
                        lore.add(Text.of("<7>Properties"));
                        if (block.getBreakingPower() > 0) {
                            lore.add(Text.of("<7> Breaking Power: <2>{}", block.getBreakingPower()));
                        }
                        if (block.getBlockStrength() > 0) {
                            lore.add(Text.of("<7> Block Strength: <e>{}", block.getBlockStrength()));
                        }
                    }

                    return ItemStacks.item(
                            block.getMaterial(),
                            1,
                            Text.of("<9>{}", block.getDisplayName()),
                            lore
                    );
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
    public enum BlockData {
        COBBLESTONE("Cobblestone", Material.COBBLESTONE, 0, 0),
        STONE("Stone", Material.STONE, 0, 0),
        SAND("Sand", Material.SAND, 0, 0),
        GRAVEL("Gravel", Material.GRAVEL, 0, 0),
        ICE("Ice", Material.ICE, 0, 0),
        END_STONE("End Stone", Material.END_STONE, 0, 0),
        OBSIDIAN("Obsidian", Material.OBSIDIAN, 0, 0),
        NETHERRACK("Netherrack", Material.NETHERRACK, 0, 0),
        GLOWSTONE("Glowstone", Material.GLOWSTONE, 0, 0),
        RED_SAND("Red Sand", Material.RED_SAND, 0, 0),
        MYCELIUM("Mycelium", Material.MYCELIUM, 0, 0),
        HARD_STONE("Hard Stone", Material.STONE, 4, 50);

        private final String displayName;
        private final Material material;
        private final int breakingPower;
        private final int blockStrength;

        BlockData(String displayName, Material material, int breakingPower, int blockStrength) {
            this.displayName = displayName;
            this.material = material;
            this.breakingPower = breakingPower;
            this.blockStrength = blockStrength;
        }
    }
}
