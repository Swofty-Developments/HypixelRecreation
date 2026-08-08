package net.swofty.type.theend.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.theend.gui.GUIShopTyzzo;
import net.swofty.type.theend.service.EndProgress;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCTyzzo extends EndNpc {
    public NPCTyzzo() {
        super("Tyzzo", new Pos(-597, 5, -272, 0, 0));
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = player(event);
        if (isInDialogue(player)) return;

        if (!EndProgress.hasSpoken(player, DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_TYZZO)) {
            setDialogue(player, "hello").thenRun(() -> EndProgress.markSpoken(player,
                    DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_TYZZO));
            return;
        }

        if (!EndProgress.hasSpoken(player, DatapointToggles.Toggles.ToggleType.HAS_ASKED_TYZZO_FOR_SCALE)) {
            setDialogue(player, "prove").thenRun(() -> EndProgress.markSpoken(player,
                    DatapointToggles.Toggles.ToggleType.HAS_ASKED_TYZZO_FOR_SCALE));
            return;
        }

        if (!EndProgress.hasSpoken(player, DatapointToggles.Toggles.ToggleType.HAS_COMPLETED_TYZZO)) {
            if (player.getAmountInInventory(ItemType.DRAGON_SCALE) == 0) {
                setDialogue(player, "missing");
                return;
            }

            player.takeItem(ItemType.DRAGON_SCALE, 1);
            setDialogue(player, "worthy").thenRun(() -> EndProgress.markSpoken(player,
                    DatapointToggles.Toggles.ToggleType.HAS_COMPLETED_TYZZO));
            return;
        }

        player.openView(new GUIShopTyzzo());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("hello").lines(new String[]{
                        "You wish for my Abiphone Contact?",
                        "Are you worthy of it, though?"
                }).build(),
                DialogueSet.builder().key("prove").lines(new String[]{
                        "Prove it.",
                        "Bring me a Dragon Scale."
                }).build(),
                DialogueSet.builder().key("missing").lines(new String[]{
                        "You do not possess a Dragon Scale.",
                        "You'll sometimes get one from defeating a Young Dragon, though that's no easy task.",
                        "Come back once you've got one."
                }).build(),
                DialogueSet.builder().key("worthy").lines(new String[]{
                        "You actually managed to get a Dragon Scale.",
                        "I guess you are worthy of the title of Dragon Slayer after all.",
                        "Tyzzo has been added to your Abiphone's contacts!",
                        "Call me whenever you wish to use the Dragon Essence Shop."
                }).build()
        };
    }
}
