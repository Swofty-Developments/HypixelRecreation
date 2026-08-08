package net.swofty.type.theend.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.theend.service.EndProgress;
import net.swofty.type.theend.service.EndRaceService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCGuber extends EndNpc {
    public NPCGuber() {
        super("Guber", new Pos(-494.5, 121, -241.5, 0, 0));
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = player(event);
        if (isInDialogue(player)) return;

        if (!EndProgress.hasSpoken(player, DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_GUBER)) {
            setDialogue(player, "hello").thenRun(() -> EndProgress.markSpoken(player,
                    DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_GUBER));
            return;
        }

        if (EndRaceService.isRacing(player)) {
            setDialogue(player, "racing");
        } else if (EndRaceService.hasCompleted(player)) {
            setDialogue(player, "complete");
        } else {
            setDialogue(player, "idle");
        }
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        double bestTime = player instanceof SkyBlockPlayer skyBlockPlayer
                ? EndRaceService.bestTime(skyBlockPlayer) / 1000.0
                : 0;
        return new DialogueSet[]{
                DialogueSet.builder().key("hello").lines(new String[]{
                        "Hey you! Over here! Come talk to me! I'll teach you about racing!",
                        "The End is a large island... my favorite hobby is racing across it!",
                        "My best time is pretty fast, you probably will never beat me. But you can try!",
                        "To complete the race, you'll need to reach the opposite side of the island and come all the way back.",
                        "Start the race by walking over the pressure plate. If you finish in under 3 minutes, I'll reward you for it! Good luck!"
                }).build(),
                DialogueSet.builder().key("idle").lines(new String[]{
                        "Walk over the pressure plate when you're ready to race!"
                }).build(),
                DialogueSet.builder().key("racing").lines(new String[]{
                        "You're already racing! Reach the opposite side and come back!"
                }).build(),
                DialogueSet.builder().key("complete").lines(new String[]{
                        "Your best time is " + bestTime + " seconds. Keep practicing!"
                }).build()
        };
    }
}
