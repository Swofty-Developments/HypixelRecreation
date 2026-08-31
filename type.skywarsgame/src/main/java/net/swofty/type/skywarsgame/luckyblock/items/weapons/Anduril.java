package net.swofty.type.skywarsgame.luckyblock.items.weapons;

import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockWeapon;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

import java.util.List;

public class Anduril implements LuckyBlockWeapon {

    public static final String ID = "anduril";
    private static final int BUFF_REFRESH_TICKS = 20;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Anduril";
    }

    @Override
    public Material getBaseMaterial() {
        return Material.IRON_SWORD;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.raw(Material.IRON_SWORD,
                        Text.of("<6>Anduril"),
                        List.of(
                                Text.empty(),
                                Text.of("<7>Sharpness II"),
                                Text.empty(),
                                Text.of("<6>While held:"),
                                Text.of("<b> • Speed I"),
                                Text.of("<f> • Resistance I"),
                                Text.empty(),
                                Text.of("<8><o>\"The Flame of the West\""),
                                Text.empty(),
                                Text.of("<6><l>LUCKY BLOCK ITEM")))
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }

    @Override
    public float onWeaponHit(SkywarsPlayer holder, Entity target, float damage) {
        return damage;
    }

    @Override
    public void onHeldTick(SkywarsPlayer holder) {
        if (holder.getAliveTicks() % BUFF_REFRESH_TICKS == 0) {
            applyBuffs(holder);
        }
    }

    private void applyBuffs(SkywarsPlayer holder) {
        holder.addEffect(new Potion(PotionEffect.SPEED, (byte) 0, 60));
        holder.addEffect(new Potion(PotionEffect.RESISTANCE, (byte) 0, 60));
    }

    @Override
    public double getAttackDamage() {
        return 6.0 + 2.5;
    }

    @Override
    public boolean hasOnHitEffect() {
        return false;
    }

    @Override
    public boolean hasPassiveBuff() {
        return true;
    }
}
