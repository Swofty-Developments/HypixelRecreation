package net.swofty.type.skyblockgeneric.abiphone;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AbiphoneDialogueController {
    private final HashMap<HypixelPlayer, Map.Entry<AbiphoneNPC.DialogueSet, CompletableFuture<String>>> activeDialogues = new HashMap<>();
    private final AbiphoneNPC npc;

    public AbiphoneDialogueController(AbiphoneNPC npc) {
        this.npc = npc;
    }

    /**
     * Checks if the player is currently in a dialogue with this NPC.
     *
     * @param player The player to check.
     * @return True if the player is in dialogue, false otherwise.
     */
    public boolean isInDialogue(HypixelPlayer player) {
        return activeDialogues.containsKey(player);
    }

    /**
     * Starts a dialogue with the player using the specified dialogue key.
     *
     * @param player The player to start the dialogue with.
     * @param key    The key of the dialogue set to play.
     * @return A CompletableFuture that completes when the dialogue finishes, with the dialogue key.
     */
    public CompletableFuture<String> setDialogue(HypixelPlayer player, String key) {
        CompletableFuture<String> future = new CompletableFuture<>();

        AbiphoneNPC.DialogueSet[] dialogueSets = npc.dialogues(player);
        for (AbiphoneNPC.DialogueSet dialogueSet : dialogueSets) {
            if (dialogueSet.key().equals(key)) {
                activeDialogues.put(player, Map.entry(dialogueSet, future));
                handleLineSendingLoop(player, dialogueSet);
                return future;
            }
        }

        future.completeExceptionally(new NullPointerException("Dialogue set with key '" + key + "' not found."));
        return future;
    }

    /**
     * Removes the player from the dialogue, completing the future with null.
     *
     * @param player The player to remove from dialogue.
     */
    public void cancelDialogue(HypixelPlayer player) {
        Map.Entry<AbiphoneNPC.DialogueSet, CompletableFuture<String>> entry = activeDialogues.get(player);
        if (entry != null) {
            entry.getValue().complete(null);
            activeDialogues.remove(player);
        }
    }

    private void handleLineSendingLoop(HypixelPlayer player, AbiphoneNPC.DialogueSet dialogueSet) {
        npc.sendNPCMessage(player, dialogueSet.lines()[0]);

        Text[] newLines = new Text[dialogueSet.lines().length - 1];
        System.arraycopy(dialogueSet.lines(), 1, newLines, 0, dialogueSet.lines().length - 1);

        if (newLines.length == 0) {
            Map.Entry<AbiphoneNPC.DialogueSet, CompletableFuture<String>> entry = activeDialogues.get(player);
            if (entry != null) {
                entry.getValue().complete(dialogueSet.key());
            }
            activeDialogues.remove(player);
            return;
        }

        Scheduler scheduler = MinecraftServer.getSchedulerManager();
        scheduler.buildTask(() -> {
            // Check if the player is still in dialogue (might have been canceled)
            if (activeDialogues.containsKey(player)) {
                handleLineSendingLoop(player, new AbiphoneNPC.DialogueSet(
                        dialogueSet.key(), newLines, false));
            }
        }).delay(TaskSchedule.seconds(2)).schedule();
    }

    public static void removeAllDialogues(HypixelPlayer player) {
        for (AbiphoneNPC npc : AbiphoneRegistry.getRegisteredContactNPCs()) {
            npc.dialogue().cancelDialogue(player);
        }
    }
}
