package net.swofty.type.skywarsgame.luckyblock.items.usables;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockConsumable;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class VoidCharm implements LuckyBlockConsumable {

    public static final Tag<Boolean> VOID_CHARM_TAG = Tag.Boolean("has_void_charm");

    @Override
    public String getId() {
        return "void_charm";
    }

    @Override
    public String getDisplayName() {
        return "Void Charm";
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStacks.item(Material.GHAST_TEAR, """
                <d><l>Void Charm</l>
                <7>Activates protection from
                <7>falling into the void!
                <8>(One-time use)

                <e>Right-click to activate!""")
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, getId())
                .build();
    }

    @Override
    public void onConsume(SkywarsPlayer player) {
        player.setTag(VOID_CHARM_TAG, true);
        player.sendMessage("<d>Void Charm activated!");
        player.sendMessage("<7>You will be saved from the void once!");
    }

    public static boolean hasVoidCharm(SkywarsPlayer player) {
        Boolean hasCharm = player.getTag(VOID_CHARM_TAG);
        return hasCharm != null && hasCharm;
    }

    public static void consumeVoidCharm(SkywarsPlayer player) {
        player.removeTag(VOID_CHARM_TAG);
    }
}
