package net.swofty.type.theend.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.theend.gui.GUIShopLoneAdventurer;
import net.swofty.type.theend.service.EndProgress;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.commons.skyblock.item.ItemType;

public class NPCLoneAdventurer extends EndNpc {
    private static final Pos FIRST_POSITION = new Pos(-524.5, 101, -275.5, 0, 0);
    private static final Pos NEST_POSITION = new Pos(-588.5, 22, -270.5, 0, 0);

    public NPCLoneAdventurer() {
        super("Lone Adventurer", player -> EndProgress.hasEightEnderArmorPieces((SkyBlockPlayer) player)
                ? NEST_POSITION : FIRST_POSITION);
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = player(event);
        if (isInDialogue(player)) return;

        if (EndProgress.hasEightEnderArmorPieces(player)) {
            if (!EndProgress.hasSpoken(player, DatapointToggles.Toggles.ToggleType.HAS_COMPLETED_LONE_ADVENTURER)) {
                setDialogue(player, "armor").thenRun(() -> {
                    EndProgress.markSpoken(player, DatapointToggles.Toggles.ToggleType.HAS_COMPLETED_LONE_ADVENTURER);
                    if (!player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_RECEIVED_DRAGON_SHORTBOW)) {
                        player.addAndUpdateItem(ItemType.DRAGON_SHORTBOW);
                        player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_RECEIVED_DRAGON_SHORTBOW, true);
                    }
                });
            } else {
                setDialogue(player, "finished");
            }
            return;
        }

        if (!EndProgress.hasSpoken(player, DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_LONE_ADVENTURER)) {
            setDialogue(player, "intro").thenRun(() -> EndProgress.markSpoken(player,
                    DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_LONE_ADVENTURER));
            return;
        }

        if (!EndProgress.hasKilledFiveEndermen(player)) {
            setDialogue(player, "progress");
            return;
        }

        setDialogue(player, "offer").thenRun(() -> player.openView(new GUIShopLoneAdventurer()));
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("intro").lines(new String[]{
                        "Exhausted? me? No no no.",
                        "I'm just taking a break.",
                        "The End is a creepy place, but you get used to it!",
                        "If you want a piece of advice, you should start by killing the Endermen up here.",
                        "They sometimes drop important gear like the armor I'm wearing.",
                        "Are you strong enough though?",
                        "Try killing 5 of them!"
                }).build(),
                DialogueSet.builder().key("progress").lines(new String[]{
                        "Keep killing Endermen until you've defeated 5 of them!"
                }).build(),
                DialogueSet.builder().key("offer").lines(new String[]{
                        "Alright, not bad, not bad!",
                        "It took me a while to get that strong.",
                        "I use a Void Sword, it's a very powerful weapon.",
                        "It gets stronger with each piece of Ender Armor you are wearing.",
                        "I have an extremely strong emotional attachment to this item so...",
                        "I'm willing to sell it to you for the modest sum of 200,000 coins.",
                        "What do you say?"
                }).build(),
                DialogueSet.builder().key("armor").lines(new String[]{
                        "You collected all 8 pieces of Ender Armor!",
                        "You are ready for the Dragon's Nest.",
                        "Take this Dragon Shortbow and seek out the dragons!"
                }).build(),
                DialogueSet.builder().key("finished").lines(new String[]{
                        "The dragons are waiting in the Dragon's Nest. Good luck!"
                }).build()
        };
    }
}
