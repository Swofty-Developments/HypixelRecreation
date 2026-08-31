package net.swofty.type.skywarsgame.luckyblock.items.armor;

import net.minestom.server.color.Color;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockArmor;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;
import net.swofty.type.skywarsgame.util.PolypTnt;

import java.util.Random;

public class GreaserJacket implements LuckyBlockArmor {

    public static final String ID = "greaser_jacket";
    private static final Random RANDOM = new Random();
    private static final double TNT_DROP_CHANCE = 0.3;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Greaser Jacket";
    }

    @Override
    public EquipmentSlot getSlot() {
        return EquipmentSlot.CHESTPLATE;
    }

    @Override
    public Material getBaseMaterial() {
        return Material.LEATHER_CHESTPLATE;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.raw(Material.LEATHER_CHESTPLATE, """
                        <8>Greaser Jacket

                        <7>Protection I
                        <7>Blast Protection II

                        <7>Drops <c>TNT<7> when you take damage!

                        <6><l>LUCKY BLOCK ITEM
                        """)
                .set(DataComponents.DYED_COLOR, new Color(30, 30, 30))
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }

    @Override
    public void onEquip(SkywarsPlayer player) {
        player.sendMessage("<8>Stay cool... but explosive!");
    }

    @Override
    public void onDamaged(SkywarsPlayer player, Entity attacker, float damage) {
        if (RANDOM.nextDouble() > TNT_DROP_CHANCE) {
            return;
        }

        Pos tntPos = player.getPosition().add(
                RANDOM.nextDouble() * 2 - 1,
                0.5,
                RANDOM.nextDouble() * 2 - 1
        );
        PolypTnt.spawn(player, tntPos, 60);
    }

    @Override
    public boolean hasDamageEffect() {
        return true;
    }
}
