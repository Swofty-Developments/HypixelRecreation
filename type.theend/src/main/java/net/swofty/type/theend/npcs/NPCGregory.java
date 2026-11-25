package net.swofty.type.theend.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.theend.dragon.EndDragonManager;
import net.swofty.type.theend.gui.GUIShopGregory;
import net.swofty.type.theend.service.EndProgress;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCGregory extends EndNpc {
    public NPCGregory() {
        super("Gregory", new Pos(-607.5, 22, -284.5, 0, 0), ignored -> EndDragonManager.isDragonAlive());
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = player(event);
        if (isInDialogue(player)) return;

        if (!EndProgress.hasSpoken(player, DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_GREGORY)) {
            setDialogue(player, "hello").thenRun(() -> EndProgress.markSpoken(player,
                    DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_GREGORY));
            return;
        }

        if (!EndDragonManager.isDragonAlive()) {
            sendNPCMessage(player, "There is no active dragon right now. Come back when one is summoned!");
            return;
        }

        player.openView(new GUIShopGregory());
    }

    @Override
    public DialogueSet[] dialogues(net.swofty.type.generic.user.HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("hello").lines(new String[]{
                        "Hey, I'm an opportunist!",
                        "I sell Arrows and Bows for a truly overpriced amount!"
                }).build()
        };
    }
}
