package net.swofty.type.skywarsgame.luckyblock.items.usables;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItem;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;
import net.swofty.type.skywarsgame.util.PolypTnt;

public class ThrowableTNT implements LuckyBlockItem {

    private static final int FUSE_TICKS = 60;

    @Override
    public String getId() {
        return "throwable_tnt";
    }

    @Override
    public String getDisplayName() {
        return "Throwable TNT";
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.item(Material.TNT, """
                <c><l>Throwable TNT</l>
                <7>Throw explosive TNT at
                <7>your enemies!

                <c>Explodes in 3 seconds!

                <e>Right-click to throw!""")
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, getId())
                .build();
    }

    @Override
    public boolean onUse(SkywarsPlayer holder) {
        Vec direction = holder.getPosition().direction();
        Pos spawnPos = holder.getPosition().add(direction.mul(2)).add(0, 1.5, 0);

        var tnt = PolypTnt.spawn(holder, spawnPos, FUSE_TICKS);
        if (tnt == null) return false;

        Vec velocity = direction.mul(20).add(0, 10, 0);
        tnt.setVelocity(velocity);

        holder.sendMessage("<c>TNT thrown!");
        return true;
    }

    @Override
    public boolean hasUseEffect() {
        return true;
    }
}
