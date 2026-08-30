package net.swofty.type.skywarsgame.luckyblock.items.usables;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItem;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

import java.util.Map;
import java.util.Random;

public class SuperDie implements LuckyBlockItem {

    private static final Random RANDOM = new Random();

    @Override
    public String getId() {
        return "super_die";
    }

    @Override
    public String getDisplayName() {
        return "Super Die";
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.item(Material.MAGMA_CREAM, """
                <c><l>Super Die</l>
                <7>Roll the die and test
                <7>your luck!

                <4>1: Instant death
                <c>2: Half health
                <6>3: Absorption II
                <b>4: Diamond Sword (Sharp I)
                <d>5: Diamond Sword (Fire II)
                <a>6: Full Diamond Armor!

                <e>Right-click to roll!""")
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, getId())
                .build();
    }

    @Override
    public boolean onUse(SkywarsPlayer holder) {
        int roll = RANDOM.nextInt(6) + 1;

        holder.sendMessage("<6>You rolled a <e><l>{}</l><6>!", roll);

        switch (roll) {
            case 1 -> {
                holder.sendMessage("<4>UNLUCKY! You die instantly!");
                holder.damage(Damage.fromEntity(null, 1000f));
            }
            case 2 -> {
                holder.sendMessage("<c>Your health is halved!");
                float currentHealth = holder.getHealth();
                holder.setHealth(Math.max(1, currentHealth / 2));
            }
            case 3 -> {
                holder.sendMessage("<6>You gained Absorption II!");
                holder.addEffect(new Potion(PotionEffect.ABSORPTION, (byte) 1, Integer.MAX_VALUE));
            }
            case 4 -> {
                holder.sendMessage("<b>You received a Diamond Sword with Sharpness I!");
                ItemStack sword = ItemStack.builder(Material.DIAMOND_SWORD)
                        .set(DataComponents.ENCHANTMENTS, new EnchantmentList(Map.of(Enchantment.SHARPNESS, 1)))
                        .build();
                holder.getInventory().addItemStack(sword);
            }
            case 5 -> {
                holder.sendMessage("<d>You received a Diamond Sword with Fire Aspect II!");
                ItemStack sword = ItemStack.builder(Material.DIAMOND_SWORD)
                        .set(DataComponents.ENCHANTMENTS, new EnchantmentList(Map.of(Enchantment.FIRE_ASPECT, 2)))
                        .build();
                holder.getInventory().addItemStack(sword);
            }
            case 6 -> {
                holder.sendMessage("<a>JACKPOT! Full Diamond Armor!");
                holder.setEquipment(EquipmentSlot.HELMET, ItemStack.of(Material.DIAMOND_HELMET));
                holder.setEquipment(EquipmentSlot.CHESTPLATE, ItemStack.of(Material.DIAMOND_CHESTPLATE));
                holder.setEquipment(EquipmentSlot.LEGGINGS, ItemStack.of(Material.DIAMOND_LEGGINGS));
                holder.setEquipment(EquipmentSlot.BOOTS, ItemStack.of(Material.DIAMOND_BOOTS));
            }
        }

        return true;
    }

    @Override
    public boolean hasUseEffect() {
        return true;
    }
}
