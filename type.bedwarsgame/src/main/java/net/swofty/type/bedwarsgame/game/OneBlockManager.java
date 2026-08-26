package net.swofty.type.bedwarsgame.game;

import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.text.Text;
import net.swofty.type.bedwarsgame.TypeBedWarsGameLoader;
import net.swofty.type.bedwarsgame.shop.ShopItem;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.game.game.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class OneBlockManager {
    private static final int GAME_LENGTH_SECONDS = 5 * 60;

    private final BedWarsGame game;
    private final List<ItemStack> itemPool = new ArrayList<>();
    private Task ticker;
    private int elapsedSeconds;
    private int itemIntervalSeconds = 5;

    public OneBlockManager(BedWarsGame game) {
        this.game = game;
        Material.values().stream()
            .filter(material -> material != Material.AIR)
            .map(ItemStack::of)
            .forEach(itemPool::add);
        for (ShopItem item : TypeBedWarsGameLoader.getShopManager().getAllItems()) {
            itemPool.add(item.getDisplay().withAmount(item.getAmount()));
        }
    }

    public void start() {
        if (!game.getGameType().isOneBlock() || ticker != null) return;

        ticker = MinecraftServer.getSchedulerManager().buildTask(this::tick)
            .delay(TaskSchedule.seconds(1))
            .repeat(TaskSchedule.seconds(1))
            .schedule();
    }

    public void stop() {
        if (ticker != null) {
            ticker.cancel();
            ticker = null;
        }
    }

    private void tick() {
        if (game.getState() != GameState.IN_PROGRESS) {
            stop();
            return;
        }

        elapsedSeconds++;
        if (elapsedSeconds == 60) {
            itemIntervalSeconds = 4;
            game.broadcastMessage(Text.key("bedwars.new_item_in_4"));
        } else if (elapsedSeconds == 150) {
            itemIntervalSeconds = 3;
            game.broadcastMessage(Text.key("bedwars.new_item_in_3"));
        } else if (elapsedSeconds == 240) {
            itemIntervalSeconds = 1;
            game.broadcastMessage(Text.key("bedwars.new_item_in_1"));
        }

        if (elapsedSeconds % itemIntervalSeconds == 0) {
            giveRandomItems();
        }
        if (elapsedSeconds >= GAME_LENGTH_SECONDS) {
            game.getGameEventManager().endOneBlockGame();
            stop();
        }
    }

    private void giveRandomItems() {
        for (BedWarsPlayer player : game.getPlayers()) {
            if (!game.isPlayerCurrentlyPlaying(player.getUuid())) continue;
            ItemStack item = itemPool.get(ThreadLocalRandom.current().nextInt(itemPool.size()));
            player.getInventory().addItemStack(item);
        }
    }
}
