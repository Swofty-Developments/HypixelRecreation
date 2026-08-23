package net.swofty.type.skywarsgame.luckyblock.items.armor;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockArmor;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class SpeedsterBoots implements LuckyBlockArmor {

    public static final String ID = "speedster_boots";
    private static final int SPEED_REFRESH_TICKS = 40;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Speedster Boots";
    }

    @Override
    public EquipmentSlot getSlot() {
        return EquipmentSlot.BOOTS;
    }

    @Override
    public Material getBaseMaterial() {
        return Material.LEATHER_BOOTS;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStacks.raw(Material.LEATHER_BOOTS, """
                        <b>Speedster Boots

                        <7>Grants <f>Speed II<7> while worn.

                        <6><l>LUCKY BLOCK ITEM
                        """)
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }

    @Override
    public void onEquip(SkywarsPlayer player) {
        player.sendMessage("<b>You feel faster!");
        applySpeedEffect(player);
    }

    @Override
    public void onUnequip(SkywarsPlayer player) {
        player.removeEffect(PotionEffect.SPEED);
    }

    @Override
    public void onWornTick(SkywarsPlayer player) {
        if (player.getAliveTicks() % SPEED_REFRESH_TICKS == 0) {
            applySpeedEffect(player);
        }
    }

    private void applySpeedEffect(SkywarsPlayer player) {
        player.addEffect(new Potion(PotionEffect.SPEED, (byte) 1, 100));
    }

    @Override
    public boolean hasPermanentBuff() {
        return true;
    }
}
