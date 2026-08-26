package net.swofty.type.skywarsgame.luckyblock.items.armor;

import net.minestom.server.color.Color;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockArmor;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class FrogHelmet implements LuckyBlockArmor {

    public static final String ID = "frog_helmet";
    private static final int BUFF_REFRESH_TICKS = 40;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Frog Helmet";
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
        return ItemStackCreator.raw(Material.LEATHER_HELMET, """
                        <a>Frog Helmet

                        <7>Protection I
                        <7>Feather Falling X

                        <7>Grants <a>Jump Boost III

                        <6><l>LUCKY BLOCK ITEM
                        """)
                .set(DataComponents.DYED_COLOR, new Color(0, 128, 0))
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }

    @Override
    public void onEquip(SkywarsPlayer player) {
        player.sendMessage("<a>Ribbit! You feel light on your feet!");
        applyJumpBoost(player);
    }

    @Override
    public void onUnequip(SkywarsPlayer player) {
        player.removeEffect(PotionEffect.JUMP_BOOST);
    }

    @Override
    public void onWornTick(SkywarsPlayer player) {
        if (player.getAliveTicks() % BUFF_REFRESH_TICKS == 0) {
            applyJumpBoost(player);
        }
    }

    private void applyJumpBoost(SkywarsPlayer player) {
        player.addEffect(new Potion(PotionEffect.JUMP_BOOST, (byte) 2, 100));
    }

    @Override
    public boolean hasPermanentBuff() {
        return true;
    }
}
