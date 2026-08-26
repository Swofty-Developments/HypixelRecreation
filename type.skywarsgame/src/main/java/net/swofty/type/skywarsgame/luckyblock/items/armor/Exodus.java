package net.swofty.type.skywarsgame.luckyblock.items.armor;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockArmor;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

import java.util.List;

public class Exodus implements LuckyBlockArmor {

    public static final String ID = "exodus";
    private static final int REGEN_DURATION_TICKS = 100;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Exodus";
    }

    @Override
    public EquipmentSlot getSlot() {
        return EquipmentSlot.HELMET;
    }

    @Override
    public Material getBaseMaterial() {
        return Material.DIAMOND_HELMET;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.raw(Material.DIAMOND_HELMET,
                        Text.of("<b>Exodus"),
                        List.of(
                                Text.empty(),
                                Text.of("<7>Grants <c>Regeneration II<7> on hit!"),
                                Text.empty(),
                                Text.of("<8><o>The departure from"),
                                Text.of("<8><o>death itself."),
                                Text.empty(),
                                Text.of("<6><l>LUCKY BLOCK ITEM")))
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }

    @Override
    public void onEquip(SkywarsPlayer player) {
        player.sendMessage("<b>You feel the power of Exodus!");
    }

    @Override
    public void onHit(SkywarsPlayer holder, Entity target) {
        holder.addEffect(new Potion(PotionEffect.REGENERATION, (byte) 1, REGEN_DURATION_TICKS));
    }

    @Override
    public boolean hasHitEffect() {
        return true;
    }
}
