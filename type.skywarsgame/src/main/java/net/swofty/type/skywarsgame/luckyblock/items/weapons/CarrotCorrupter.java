package net.swofty.type.skywarsgame.luckyblock.items.weapons;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockWeapon;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CarrotCorrupter implements LuckyBlockWeapon {

    public static final String ID = "carrot_corrupter";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Carrot Corrupter";
    }

    @Override
    public Material getBaseMaterial() {
        return Material.CARROT_ON_A_STICK;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.raw(Material.CARROT_ON_A_STICK, """
                        <6>Carrot Corrupter

                        <7>Corrupts a random item in
                        <7>your target's hotbar into
                        <7>a <6>carrot</6>!

                        <6><l>LUCKY BLOCK ITEM
                        """)
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }

    @Override
    public float onWeaponHit(SkywarsPlayer holder, Entity target, float damage) {
        if (target instanceof Player player) {
            List<Integer> validSlots = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                ItemStack item = player.getInventory().getItemStack(i);
                if (!item.isAir()) {
                    validSlots.add(i);
                }
            }

            if (!validSlots.isEmpty()) {
                int slot = validSlots.get(ThreadLocalRandom.current().nextInt(validSlots.size()));
                player.getInventory().setItemStack(slot, ItemStack.of(Material.CARROT));
                holder.sendMessage("<6>You corrupted an item into a carrot!");
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
