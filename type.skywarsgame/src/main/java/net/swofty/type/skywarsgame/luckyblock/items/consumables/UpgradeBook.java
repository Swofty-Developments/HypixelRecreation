package net.swofty.type.skywarsgame.luckyblock.items.consumables;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockConsumable;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class UpgradeBook implements LuckyBlockConsumable {

    private static final int DURATION_TICKS = 5 * 60 * 20;

    @Override
    public String getId() {
        return "upgrade_book";
    }

    @Override
    public String getDisplayName() {
        return "Upgrade Book";
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStacks.item(Material.ENCHANTED_BOOK, """
                <b><l>Upgrade Book</l>
                <7>Grants you enhanced protection
                <7>for 5 minutes!

                <e>Right-click to use!""")
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, getId())
                .build();
    }

    @Override
    public void onConsume(SkywarsPlayer player) {
        player.addEffect(new Potion(PotionEffect.RESISTANCE, (byte) 1, DURATION_TICKS));

        boolean hasArmor = !player.getEquipment(EquipmentSlot.HELMET).isAir() ||
                !player.getEquipment(EquipmentSlot.CHESTPLATE).isAir() ||
                !player.getEquipment(EquipmentSlot.LEGGINGS).isAir() ||
                !player.getEquipment(EquipmentSlot.BOOTS).isAir();

        if (hasArmor) {
            player.sendMessage("<b>Your armor has been magically enhanced!");
        } else {
            player.sendMessage("<b>You feel protected by ancient magic!");
        }
    }
}
