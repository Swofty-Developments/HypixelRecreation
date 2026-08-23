package net.swofty.type.ravengardgeneric.queue;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.ServerType;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.ravengardgeneric.user.RavengardPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The fight queue as the 0.2 playtest presents it: joining answers with the gray then green
 * queue messages and the ominous drums, the menu button flips to leaving, and a found server
 * transfers the player to a dungeon. Matchmaking wait times are not observable from captures,
 * so a short fixed delay stands in for them.
 */
public final class RavengardQueue {
    public static final String FIGHT_QUEUE = "Ravengard Dungeon Trios";
    private static final TaskSchedule MATCH_DELAY = TaskSchedule.seconds(3);
    private static final Sound DRUMS = Sound.sound()
            .type(Key.key("hypixel_ravengard", "action.drums"))
            .volume(1.0f)
            .pitch(1.0f)
            .build();

    private static final Map<UUID, Task> QUEUED = new ConcurrentHashMap<>();

    private RavengardQueue() {
    }

    public static boolean isQueued(HypixelPlayer player) {
        return QUEUED.containsKey(player.getUuid());
    }

    public static void join(RavengardPlayer player) {
        if (isQueued(player)) {
            player.sendMessage("<c>{} is already queued for a game!", player.getUsername());
            return;
        }
        player.sendMessage("<7>Joining fight...");
        player.sendMessage("<a>Successfully queued for {}!", FIGHT_QUEUE);
        player.playSound(DRUMS);

        Task task = MinecraftServer.getSchedulerManager().buildTask(() -> {
            QUEUED.remove(player.getUuid());
            if (!player.isOnline()) {
                return;
            }
            player.sendMessage("<7>Found server, initiating transfer to {}...",
                    ServerType.RAVENGARD_DUNGEON.name().toLowerCase());
            player.sendTo(ServerType.RAVENGARD_DUNGEON);
        }).delay(MATCH_DELAY).schedule();
        QUEUED.put(player.getUuid(), task);
    }

    public static void leave(RavengardPlayer player) {
        Task task = QUEUED.remove(player.getUuid());
        if (task == null) {
            return;
        }
        task.cancel();
        player.sendMessage("<c>You have left the queue.");
    }

    public static void forget(UUID uuid) {
        Task task = QUEUED.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }
}
