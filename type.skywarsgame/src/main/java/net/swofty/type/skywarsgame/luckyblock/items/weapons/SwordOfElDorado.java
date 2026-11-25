package net.swofty.type.skywarsgame.luckyblock.items.weapons;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockWeapon;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

import java.util.List;

public class SwordOfElDorado implements LuckyBlockWeapon {

    public static final String ID = "sword_of_el_dorado";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Sword of El Dorado";
    }

    @Override
    public Material getBaseMaterial() {
        return Material.GOLDEN_SWORD;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.raw(Material.GOLDEN_SWORD,
                        Text.of("<6>Sword of El Dorado"),
                        List.of(
                                Text.empty(),
                                Text.of("<7>Sharpness IV"),
                                Text.of("<7>Fire Aspect I"),
                                Text.empty(),
                                Text.of("<c>Sets enemies ablaze."),
                                Text.empty(),
                                Text.of("<8><o>\"The lost city's treasure.\""),
                                Text.empty(),
                                Text.of("<6><l>LUCKY BLOCK ITEM")))
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }

    @Override
    public float onWeaponHit(SkywarsPlayer holder, Entity target, float damage) {
        if (target instanceof LivingEntity living) {
            living.setFireTicks(80);
        }

        return damage;
    }

    @Override
    public double getAttackDamage() {
        return 4.0 + 5.0;
    }

    @Override
    public boolean hasOnHitEffect() {
        return true;
    }
}
