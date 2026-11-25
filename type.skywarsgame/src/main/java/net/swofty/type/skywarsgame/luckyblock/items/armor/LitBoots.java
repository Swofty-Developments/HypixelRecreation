package net.swofty.type.skywarsgame.luckyblock.items.armor;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockArmor;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

import java.time.Duration;

public class LitBoots implements LuckyBlockArmor {

    public static final String ID = "lit_boots";
    private static final int TRAIL_TICK_INTERVAL = 5;
    private static final Duration FIRE_DURATION = Duration.ofSeconds(3);
    private static final int FIRE_RESISTANCE_REFRESH_TICKS = 40;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Lit Boots";
    }

    @Override
    public EquipmentSlot getSlot() {
        return EquipmentSlot.BOOTS;
    }

    @Override
    public Material getBaseMaterial() {
        return Material.LEATHER_BOOTS;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.raw(Material.LEATHER_BOOTS, """
                        <c>Lit Boots

                        <7>Leave a <c>fire trail<7> behind you.
                        <7>Grants <6>Fire Resistance<7>.

                        <6><l>LUCKY BLOCK ITEM
                        """)
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }

    @Override
    public void onEquip(SkywarsPlayer player) {
        player.sendMessage("<c>Your feet are on fire!");
        applyFireResistance(player);
    }

    @Override
    public void onUnequip(SkywarsPlayer player) {
        player.removeEffect(PotionEffect.FIRE_RESISTANCE);
    }

    @Override
    public void onWornTick(SkywarsPlayer player) {
        if (player.getAliveTicks() % FIRE_RESISTANCE_REFRESH_TICKS == 0) {
            applyFireResistance(player);
        }

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
        Pos blockAbove = new Pos(
                playerPos.blockX(),
                playerPos.blockY(),
                playerPos.blockZ()
        );

        Block currentBlock = instance.getBlock(blockAbove);

        if (!currentBlock.air()) {
            return;
        }

        Block blockBelow = instance.getBlock(blockAbove.add(0, -1, 0));
        if (!blockBelow.solid()) {
            return;
        }

        instance.setBlock(blockAbove, Block.FIRE);

        player.scheduler().buildTask(() -> {
            Block currentAtPos = instance.getBlock(blockAbove);
            if (currentAtPos.compare(Block.FIRE)) {
                instance.setBlock(blockAbove, Block.AIR);
            }
        }).delay(FIRE_DURATION).schedule();
    }

    private void applyFireResistance(SkywarsPlayer player) {
        player.addEffect(new Potion(PotionEffect.FIRE_RESISTANCE, (byte) 0, 100));
    }

    @Override
    public boolean hasTrailEffect() {
        return true;
    }

    @Override
    public boolean hasPermanentBuff() {
        return true;
    }
}
