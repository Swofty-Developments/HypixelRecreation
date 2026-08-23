package net.swofty.type.skywarsgame.luckyblock.items.armor;

import net.minestom.server.color.Color;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockArmor;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class HotHead implements LuckyBlockArmor {

    public static final String ID = "hot_head";
    private static final int FIRE_TICKS = 60;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Hot Head";
    }

    @Override
    public EquipmentSlot getSlot() {
        return EquipmentSlot.HELMET;
    }

    @Override
    public Material getBaseMaterial() {
        return Material.LEATHER_HELMET;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStacks.raw(Material.LEATHER_HELMET, """
                        <c>Hot Head

                        <7>Protection I
                        <7>Fire Protection X
                        <7>Unbreaking I

                        <7>Sets <c>enemies on fire<7> when you hit them!

                        <6><l>LUCKY BLOCK ITEM
                        """)
                .set(DataComponents.DYED_COLOR, new Color(255, 0, 0))
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }

    @Override
    public void onEquip(SkywarsPlayer player) {
        player.sendMessage("<c>Your head is on fire!");
    }

    @Override
    public void onHit(SkywarsPlayer holder, Entity target) {
        if (target instanceof LivingEntity living) {
            living.setFireTicks(FIRE_TICKS);
        }
    }

    @Override
    public boolean hasHitEffect() {
        return true;
    }
}
