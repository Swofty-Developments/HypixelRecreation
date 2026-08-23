package net.swofty.type.skywarsgame.luckyblock.items.weapons;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockWeapon;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class VomitBagel implements LuckyBlockWeapon {

    public static final String ID = "vomit_bagel";
    private static final int NAUSEA_DURATION = 160;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Vomit Bagel";
    }

    @Override
    public Material getBaseMaterial() {
        return Material.PUMPKIN_PIE;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStacks.raw(Material.PUMPKIN_PIE, """
                        <a>Vomit Bagel

                        <7>Makes your target feel
                        <7>extremely <a>nauseous</a>!

                        <6><l>LUCKY BLOCK ITEM
                        """)
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .set(DataComponents.ENCHANTMENTS, EnchantmentList.EMPTY.with(Enchantment.SHARPNESS, 2))
                .build();
    }

    @Override
    public float onWeaponHit(SkywarsPlayer holder, Entity target, float damage) {
        if (target instanceof LivingEntity living) {
            living.addEffect(new Potion(PotionEffect.NAUSEA, (byte) 0, NAUSEA_DURATION));
            holder.sendMessage("<a>Your target feels sick!");
        }

        return damage;
    }

    @Override
    public double getAttackDamage() {
        return 5.0;
    }

    @Override
    public boolean hasOnHitEffect() {
        return true;
    }
}
