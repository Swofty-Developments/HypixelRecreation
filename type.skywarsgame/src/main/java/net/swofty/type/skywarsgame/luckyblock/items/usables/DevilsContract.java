package net.swofty.type.skywarsgame.luckyblock.items.usables;

import net.minestom.server.entity.damage.Damage;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockConsumable;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

import java.util.Map;

public class DevilsContract implements LuckyBlockConsumable {

    @Override
    public String getId() {
        return "devils_contract";
    }

    @Override
    public String getDisplayName() {
        return "Devil's Contract";
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStacks.item(Material.PAPER, """
                <4><l>Devil's Contract</l>
                <7>Sign a contract with the devil...
                <7>Receive powerful items, but you
                <c>will die in 60 seconds!

                <e>Right-click to sign!""")
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, getId())
                .build();
    }

    @Override
    public void onConsume(SkywarsPlayer player) {
        ItemStack sword = ItemStacks.named(Material.DIAMOND_SWORD, "<4>Devil's Blade")
                .set(DataComponents.ENCHANTMENTS, new EnchantmentList(Map.of(Enchantment.SHARPNESS, 1)))
                .build();

        player.getInventory().addItemStack(sword);
        player.getInventory().addItemStack(ItemStack.of(Material.ENDER_PEARL, 16));

        player.sendMessage("<4>You signed the Devil's Contract!");
        player.sendMessage("<c>You will die in 60 seconds...");

        player.scheduler().buildTask(() -> {
            if (!player.isOnline() || player.isEliminated()) return;
            player.sendMessage("<c>30 seconds remaining!");
        }).delay(TaskSchedule.seconds(30)).schedule();

        player.scheduler().buildTask(() -> {
            if (!player.isOnline() || player.isEliminated()) return;
            player.sendMessage("<4>10 seconds remaining!");
        }).delay(TaskSchedule.seconds(50)).schedule();

        player.scheduler().buildTask(() -> {
            if (!player.isOnline() || player.isEliminated()) return;
            player.sendMessage("<4>5 seconds remaining!");
        }).delay(TaskSchedule.seconds(55)).schedule();

        player.scheduler().buildTask(() -> {
            if (!player.isOnline() || player.isEliminated()) return;
            player.sendMessage("<4>The Devil collects his due...");
            player.damage(Damage.fromPlayer(player, 1000));
        }).delay(TaskSchedule.seconds(60)).schedule();
    }
}
