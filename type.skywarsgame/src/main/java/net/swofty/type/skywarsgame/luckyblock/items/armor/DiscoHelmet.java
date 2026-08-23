package net.swofty.type.skywarsgame.luckyblock.items.armor;

import net.minestom.server.color.Color;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockArmor;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class DiscoHelmet implements LuckyBlockArmor {

    public static final String ID = "disco_helmet";
    private static final int COLOR_CHANGE_TICKS = 5;

    private static final Color[] RAINBOW_COLORS = {
            new Color(255, 0, 0),
            new Color(255, 127, 0),
            new Color(255, 255, 0),
            new Color(0, 255, 0),
            new Color(0, 0, 255),
            new Color(75, 0, 130),
            new Color(148, 0, 211)
    };

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Disco Helmet";
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
                        <d>Disco Helmet

                        <7>Protection III

                        <d>Changes colors while worn!

                        <6><l>LUCKY BLOCK ITEM
                        """)
                .set(DataComponents.DYED_COLOR, RAINBOW_COLORS[0])
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }

    @Override
    public void onEquip(SkywarsPlayer player) {
        player.sendMessage("<d>Disco time!");
    }

    @Override
    public void onWornTick(SkywarsPlayer player) {
        if (player.getAliveTicks() % COLOR_CHANGE_TICKS != 0) {
            return;
        }

        int colorIndex = (int) ((player.getAliveTicks() / COLOR_CHANGE_TICKS) % RAINBOW_COLORS.length);
        Color newColor = RAINBOW_COLORS[colorIndex];

        ItemStack currentHelmet = player.getHelmet();
        if (currentHelmet.isAir()) {
            return;
        }

        ItemStack updatedHelmet = currentHelmet.with(DataComponents.DYED_COLOR, newColor);
        player.setHelmet(updatedHelmet);
    }

    @Override
    public boolean hasVisualEffect() {
        return true;
    }
}
