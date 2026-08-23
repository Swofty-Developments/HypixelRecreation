package net.swofty.type.skyblockgeneric.user;

import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointStash;

public class StashReminder {

    /**
     * Start the stash reminder loop.
     * Sends clickable reminders every 60 seconds if players have items in their stash.
     */
    public static void start(Scheduler scheduler) {
        scheduler.submitTask(() -> {
            for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
                if (player.getSkyblockDataHandler() == null) continue;

                DatapointStash.PlayerStash stash = player.getStash();

                // Send item stash reminder
                if (stash.getItemStashCount() > 0) {
                    sendItemStashReminder(player, stash);
                }

                // Send material stash reminder
                if (stash.getMaterialStashCount() > 0) {
                    sendMaterialStashReminder(player, stash);
                }
            }
            return TaskSchedule.tick(1200); // 60 seconds = 1200 ticks
        });
    }

    private static void sendItemStashReminder(SkyBlockPlayer player, DatapointStash.PlayerStash stash) {
        int count = stash.getItemStashCount();

        // Check for near-full warning (90% = 648 items)
        if (stash.isItemStashNearFull()) {
            player.sendMessage("<c>YOUR STASH IS ALMOST AT MAX CAPACITY!");
        }

        String itemWord = count == 1 ? "item" : "items";
        String itWord = count == 1 ? "it" : "them";

        player.sendMessage("");
        player.sendMessage("<click:run:'/pickupstash item'><center><7>You have <a>{} {} <7>stashed away!</center></click>",
                count, itemWord);
        player.sendMessage("<click:run:'/pickupstash item'><center><6>>>> CLICK HERE <e>to pick {} up! <6>\\<\\<\\<</center></click>",
                itWord);
        player.sendMessage("");
    }

    private static void sendMaterialStashReminder(SkyBlockPlayer player, DatapointStash.PlayerStash stash) {
        int count = stash.getMaterialStashCount();
        int types = stash.getMaterialTypeCount();

        String materialWord = count == 1 ? "material" : "materials";
        String typeWord = types == 1 ? "type" : "types";
        String itWord = count == 1 ? "it" : "them";

        player.sendMessage("");
        player.sendMessage("<click:run:'/pickupstash material'><center><7>You have <b>{} {} <7>stashed away!</center></click>",
                count, materialWord);
        player.sendMessage("<click:run:'/pickupstash material'><center><8>(This totals {} {} of materials stashed!)</center></click>",
                types, typeWord);
        player.sendMessage("<click:run:'/pickupstash material'><center><2>>>> CLICK HERE <b>to pick {} up! <2>\\<\\<\\<</center></click>",
                itWord);

        player.sendMessage("");
    }
}
