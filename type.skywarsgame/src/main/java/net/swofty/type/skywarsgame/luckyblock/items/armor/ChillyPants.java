package net.swofty.type.skywarsgame.luckyblock.items.armor;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockArmor;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

import java.time.Duration;

public class ChillyPants implements LuckyBlockArmor {

    public static final String ID = "chilly_pants";
    private static final int TRAIL_TICK_INTERVAL = 4;
    private static final Duration ICE_DURATION = Duration.ofSeconds(5);

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Chilly Pants";
    }

    @Override
    public EquipmentSlot getSlot() {
        return EquipmentSlot.LEGGINGS;
    }

    @Override
    public Material getBaseMaterial() {
        return Material.LEATHER_LEGGINGS;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.raw(Material.LEATHER_LEGGINGS, """
                        <b>Chilly Pants

                        <7>The ground beneath you 
                        <7>turns to <b>ice<7> as you walk.

                        <6><l>LUCKY BLOCK ITEM
                        """)
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }

    @Override
    public void onEquip(SkywarsPlayer player) {
        player.sendMessage("<b>You feel a chill...");
    }

    @Override
    public void onWornTick(SkywarsPlayer player) {
        if (player.getAliveTicks() % TRAIL_TICK_INTERVAL != 0) {
            return;
        }

        if (!player.isOnGround()) {
            return;
        }

        Instance instance = player.getInstance();
        if (instance == null) {
            return;
        }

        Pos playerPos = player.getPosition();
        Pos blockBelow = new Pos(
                playerPos.blockX(),
                playerPos.blockY() - 1,
                playerPos.blockZ()
        );

        Block currentBlock = instance.getBlock(blockBelow);

        if (!currentBlock.solid() || currentBlock.compare(Block.PACKED_ICE) || currentBlock.compare(Block.BEDROCK)) {
            return;
        }

        instance.setBlock(blockBelow, Block.PACKED_ICE);

        player.scheduler().buildTask(() -> {
            Block currentAtPos = instance.getBlock(blockBelow);
            if (currentAtPos.compare(Block.PACKED_ICE)) {
                instance.setBlock(blockBelow, currentBlock);
            }
        }).delay(ICE_DURATION).schedule();
    }

    @Override
    public boolean hasTrailEffect() {
        return true;
    }
}
