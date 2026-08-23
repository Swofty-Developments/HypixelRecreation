package net.swofty.type.hub.gui.kat;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.KatUpgrade;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.generic.user.HypixelPlayer;

public class GUIConfirmKat extends HypixelInventoryGUI {

    private SkyBlockItem pet;

    public GUIConfirmKat(SkyBlockItem pet) {
        super("Confirm Hiring Kat", InventoryType.CHEST_3_ROW);
        this.pet = pet;
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));

        set(new GUIClickableItem(11) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p; 
                if (pet.getComponent(PetComponent.class).getKatUpgrades().getForRarity(pet.getAttributeHandler().getRarity().upgrade()) == null) return;
                KatUpgrade katUpgrade = pet.getComponent(PetComponent.class).getKatUpgrades().getForRarity(pet.getAttributeHandler().getRarity().upgrade());
                int coins = katUpgrade.getCoins();
                long time = katUpgrade.getTime();

                Long timeWhenFinished = time + System.currentTimeMillis();
                player.getKatData().setKatMap(timeWhenFinished, pet);
                player.removeCoins(coins);
                player.closeInventory();
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p; 
                if (pet.getComponent(PetComponent.class).getKatUpgrades().getForRarity(pet.getAttributeHandler().getRarity().upgrade()) == null) {
                    return ItemStacks.item(Material.RED_TERRACOTTA, "<a>Something went wrong!");
                }
                KatUpgrade katUpgrade = pet.getComponent(PetComponent.class).getKatUpgrades().getForRarity(pet.getAttributeHandler().getRarity().upgrade());
                long time = katUpgrade.getTime();
                return ItemStacks.item(Material.GREEN_TERRACOTTA, """
                        <a>Confirm
                        <c>WARNING: You will not be able to
                        <c>retrieve your pet for {} and its
                        <c>level will change as a result of the
                        <c>rarity upgrade.""", StringUtility.formatTimeLeftWrittenOut(time));
            }
        });

        set(new GUIClickableItem(15) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p; 
                player.closeInventory();
                player.addAndUpdateItem(pet);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return ItemStacks.item(Material.RED_TERRACOTTA, "<c>Cancel");
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
