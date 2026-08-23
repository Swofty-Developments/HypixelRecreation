package net.swofty.type.bedwarsgame.item.impl;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.bedwarsgame.game.v2.BedWarsGame;
import net.swofty.type.bedwarsgame.item.SimpleInteractableItem;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.game.game.GameState;
import net.swofty.type.generic.gui.inventory.ItemStacks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LeaveGameBed extends SimpleInteractableItem {

    private static Map<UUID, Task> leaveTasks = new ConcurrentHashMap<>();

    public LeaveGameBed() {
        super("leave_game");
    }

    @Override
    public ItemStack getBlandItem() {
        return ItemStacks.item(Material.RED_BED, """
                <c><l>Return to Lobby </l><7>(Right Click)
                <7>Right-click to leave to the lobby!""").build();
    }

    @Override
    public void onItemUse(PlayerUseItemEvent event) {
        BedWarsPlayer player = (BedWarsPlayer) event.getPlayer();
        BedWarsGame game = player.getGame();
        if (game != null) {
            if (leaveTasks.containsKey(player.getUuid())) {
                leaveTasks.get(player.getUuid()).cancel();
                leaveTasks.remove(player.getUuid());
                player.sendMessage("<c><l>Teleport cancelled!");
                return;
            }
            leaveTasks.put(player.getUuid(), MinecraftServer.getSchedulerManager().scheduleTask(() -> {
                if (game.getState() != GameState.IN_PROGRESS) {
                    leaveTasks.remove(player.getUuid());
                    game.leave(player);
                }
                return TaskSchedule.stop();
            }, TaskSchedule.seconds(3L)));
            player.sendMessage("<a><l>Teleporting you to the lobby in 3 seconds... Right-click again to cancel the teleport!");
        }
    }
}
