package net.swofty.type.skywarsgame.luckyblock.items.usables;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.TypeSkywarsGameLoader;
import net.swofty.type.skywarsgame.game.SkywarsGame;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItem;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

import java.util.List;

public class Jackhammer implements LuckyBlockItem {

    public static final String ID = "jackhammer";
    public static final Tag<Integer> USES_TAG = Tag.Integer("jackhammer_uses");
    private static final int MAX_USES = 10;
    private static final double MAX_REACH = 5.0;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Jackhammer";
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.item(Material.DIAMOND_PICKAXE, """
                <b><l>Jackhammer</l>
                <7>Destroys a 3x3x3 cube
                <7>of blocks on use!

                <7>Uses: <a>{}

                <e>Right-click a block to use!""", MAX_USES)
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .set(USES_TAG, MAX_USES)
                .build();
    }

    @Override
    public boolean onUse(SkywarsPlayer holder) {
        Instance instance = holder.getInstance();
        if (instance == null) return false;

        Point targetBlock = getTargetBlock(holder, instance);
        if (targetBlock == null) {
            holder.sendMessage("<c>No block in range!");
            return false;
        }

        SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(holder);
        int blocksDestroyed = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    Point blockPos = targetBlock.add(dx, dy, dz);
                    Block block = instance.getBlock(blockPos);
                    boolean isChest = game != null && game.getChestManager().isChestPosition(new net.minestom.server.coordinate.Pos(blockPos));

                    if (!block.air() && !block.compare(Block.BEDROCK) && !isChest) {
                        instance.setBlock(blockPos, Block.AIR);
                        blocksDestroyed++;
                    }
                }
            }
        }

        if (blocksDestroyed > 0) {
            holder.sendMessage("<b>Destroyed {} blocks!", blocksDestroyed);

            ItemStack currentItem = holder.getItemInMainHand();
            Integer uses = currentItem.getTag(USES_TAG);
            int remainingUses = (uses != null ? uses : MAX_USES) - 1;

            if (remainingUses <= 0) {
                holder.setItemInMainHand(ItemStack.AIR);
                holder.sendMessage("<c>Your Jackhammer broke!");
            } else {
                List<Text> updatedLore = Text.of("""
                        <7>Destroys a 3x3x3 cube
                        <7>of blocks on use!

                        <7>Uses: <a>{}

                        <e>Right-click a block to use!""", remainingUses).lines();
                ItemStack updatedItem = currentItem.with(builder ->
                        ItemStackCreator.lore(builder.set(USES_TAG, remainingUses), updatedLore));
                holder.setItemInMainHand(updatedItem);
            }
            return true;
        }

        return false;
    }

    private Point getTargetBlock(SkywarsPlayer player, Instance instance) {
        Vec eyePos = player.getPosition().add(0, player.getEyeHeight(), 0).asVec();
        Vec direction = player.getPosition().direction();

        for (double d = 0; d <= MAX_REACH; d += 0.25) {
            Vec checkPos = eyePos.add(direction.mul(d));
            Block block = instance.getBlock(checkPos);
            if (!block.air()) {
                return checkPos;
            }
        }
        return null;
    }

    @Override
    public boolean hasUseEffect() {
        return true;
    }
}
