package net.swofty.type.skywarsgame.luckyblock.items.weapons;

import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockWeapon;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class ExplosiveBow implements LuckyBlockWeapon {

    public static final String ID = "explosive_bow";
    public static final double EXPLOSION_RADIUS = 4.0;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Explosive Bow";
    }

    @Override
    public Material getBaseMaterial() {
        return Material.BOW;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.raw(Material.BOW, """
                        <c>Explosive Bow

                        <7>Arrows explode on impact!
                        <7>Launches nearby players
                        <7>without dealing damage.

                        <6><l>LUCKY BLOCK ITEM
                        """)
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }

    @Override
    public float onWeaponHit(SkywarsPlayer holder, Entity target, float damage) {
        return damage;
    }

    @Override
    public boolean hasOnHitEffect() {
        return false;
    }
}
