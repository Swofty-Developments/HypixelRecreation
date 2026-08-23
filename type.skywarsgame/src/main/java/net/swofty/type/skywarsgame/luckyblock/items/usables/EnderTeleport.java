package net.swofty.type.skywarsgame.luckyblock.items.usables;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockConsumable;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class EnderTeleport implements LuckyBlockConsumable {

    @Override
    public String getId() {
        return "ender_teleport";
    }

    @Override
    public String getDisplayName() {
        return "Ender Teleport";
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStacks.item(Material.ENDER_EYE, 3, """
                <5><l>Ender Teleport</l>
                <7>Teleport to the block you're
                <7>looking at (up to 100 blocks)!

                <e>Right-click to use!""")
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, getId())
                .build();
    }

    @Override
    public void onConsume(SkywarsPlayer player) {
        Point targetBlock = player.getTargetBlockPosition(100);
        if (targetBlock == null) {
            player.sendMessage("<c>No block in range!");
            return;
        }

        if (!player.getInstance().getBlock(targetBlock.add(0, 1, 0)).air() ||
                !player.getInstance().getBlock(targetBlock.add(0, 2, 0)).air()) {
            player.sendMessage("<c>Not enough space to teleport!");
            return;
        }

        player.teleport(new Pos(
                targetBlock.x() + 0.5,
                targetBlock.y() + 1,
                targetBlock.z() + 0.5,
                player.getPosition().yaw(),
                player.getPosition().pitch()
        ));
        player.sendMessage("<5>Whoosh!");
    }
}
