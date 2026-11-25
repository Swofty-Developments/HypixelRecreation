package net.swofty.type.skywarsgame.luckyblock.items.weapons;

import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockWeapon;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class StickOfTruth implements LuckyBlockWeapon {

    public static final String ID = "stick_of_truth";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Stick of Truth";
    }

    @Override
    public Material getBaseMaterial() {
        return Material.STICK;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.raw(Material.STICK, """
                        <b>Stick of Truth

                        <7>Reveals the <b>true identity
                        <7>of disguised players!

                        <6><l>LUCKY BLOCK ITEM
                        """)
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .set(DataComponents.ENCHANTMENTS, EnchantmentList.EMPTY.with(Enchantment.SHARPNESS, 1))
                .build();
    }

    @Override
    public float onWeaponHit(SkywarsPlayer holder, Entity target, float damage) {
        if (target instanceof Player player) {
            String realName = player.getUsername();
            Component displayName = player.getDisplayName();

            if (displayName != null) {
                String displayString = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                        .plainText().serialize(displayName);

                if (!displayString.equals(realName)) {
                    holder.sendMessage("<b>That player's true identity is: <e>{}", realName);
                }
            }
        }

        return damage;
    }

    @Override
    public double getAttackDamage() {
        return 4.0;
    }

    @Override
    public boolean hasOnHitEffect() {
        return true;
    }
}
