package net.swofty.type.theend.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.theend.gui.GUIShopPearlDealer;
import net.swofty.type.theend.service.EndProgress;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCPearlDealer extends EndNpc {
    public NPCPearlDealer() {
        super("Pearl Dealer", new Pos(-504.5, 101, -284.5, 0, 0));
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = player(event);
        if (isInDialogue(player)) return;

        if (!EndProgress.hasSpoken(player, DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_PEARL_DEALER)) {
            setDialogue(player, "hello").thenRun(() -> EndProgress.markSpoken(player,
                    DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_PEARL_DEALER));
            return;
        }

        setDialogue(player, "random-" + (1 + (int) (Math.random() * 6)))
                .thenRun(() -> player.openView(new GUIShopPearlDealer()));
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("hello").lines(new String[]{
                        "You have reached The End, though this is only the beginning.",
                        "I am the Pearl Dealer, and you are on dangerous ground.",
                        "Be careful when using Ender Pearls on this island, their energy attracts Endermen!",
                        "The End also has many resources, including End Stone and Obsidian.",
                        "The deeper you go, the stranger the things you'll find!"
                }).build(),
                DialogueSet.builder().key("random-1").lines(new String[]{
                        "Ender Pearls attract the attention of Endermen, but Silent Pearls don't! You can get them in my shop."
                }).build(),
                DialogueSet.builder().key("random-2").lines(new String[]{
                        "The End has endless End Stone and Obsidian. You may find a special type of these resources deep in the caves."
                }).build(),
                DialogueSet.builder().key("random-3").lines(new String[]{
                        "The items in my shop may be of help as you descend into the depths of The End."
                }).build(),
                DialogueSet.builder().key("random-4").lines(new String[]{
                        "Deep in these caves lays the nest of the Dragons, where you will find only peril.",
                        "But you should try to find it anyway."
                }).build(),
                DialogueSet.builder().key("random-5").lines(new String[]{
                        "Talk to Guber up the staircase, he is a master of traversing this troubling terrain."
                }).build(),
                DialogueSet.builder().key("random-6").lines(new String[]{
                        "Have you tried running The End Race yet?",
                        "Talk to Guber to learn more. He is just up that staircase over there!"
                }).build()
        };
    }
}
