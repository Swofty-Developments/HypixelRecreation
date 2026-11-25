package net.swofty.type.skywarsgame.luckyblock.items.weapons;

import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockWeapon;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

import java.util.List;

public class SwordOfJustice implements LuckyBlockWeapon {

    public static final String ID = "sword_of_justice";
    private static final float HEAL_AMOUNT = 2.0f;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Sword of Justice";
    }

    @Override
    public Material getBaseMaterial() {
        return Material.IRON_SWORD;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.raw(Material.IRON_SWORD,
                        Text.of("<f>Sword of Justice"),
                        List.of(
                                Text.empty(),
                                Text.of("<7>Sharpness II"),
                                Text.empty(),
                                Text.of("<7>Heals <c>1❤</c> on hit."),
                                Text.empty(),
                                Text.of("<8><o>\"Justice is served.\""),
                                Text.empty(),
                                Text.of("<6><l>LUCKY BLOCK ITEM")))
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }

    @Override
    public float onWeaponHit(SkywarsPlayer holder, Entity target, float damage) {
        float currentHealth = holder.getHealth();
        float maxHealth = 20.0f;

        if (currentHealth < maxHealth) {
            float newHealth = Math.min(currentHealth + HEAL_AMOUNT, maxHealth);
            holder.setHealth(newHealth);

            holder.sendMessage("<c>+1\u2764");
        }

        return damage;
    }

    @Override
    public double getAttackDamage() {
        return 6.0 + 2.5;
    }

    @Override
    public boolean hasOnHitEffect() {
        return true;
    }
}
