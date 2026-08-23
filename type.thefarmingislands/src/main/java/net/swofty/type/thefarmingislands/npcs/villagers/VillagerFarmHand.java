package net.swofty.type.thefarmingislands.npcs.villagers;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.VillagerProfession;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.VillagerConfiguration;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.missions.barn.MissionCraftWheatMinion;
import net.swofty.type.skyblockgeneric.mission.missions.barn.MissionTalkToFarmHand;
import net.swofty.type.skyblockgeneric.mission.missions.barn.MissionTalkToFarmhandAgain;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

import net.swofty.type.generic.event.custom.NPCInteractEvent;

public class VillagerFarmHand extends HypixelNPC {
    public VillagerFarmHand() {
        super(new VillagerConfiguration(){
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Farmhand", "<e><l>CLICK"};
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(144.5, 73, -240.5, -35f, 0f);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public VillagerProfession profession() {
                return VillagerProfession.NONE;
            }
        });
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return List.of(
                DialogueSet.builder()
                        .key("quest-hello").lines(
                                "Welcome to the Barn!",
                                "Feel free to harvest any crops on the island, I'm taking a break.",
                                "Did you craft a <a>What Minion <f>yet? Here's the recipe!"
                        ).build(),
                DialogueSet.builder()
                        .key("spoke-again").lines(
                                "Nice! I have a Tier X Wheat Minion of my own, maybe one day you can be like me!",
                                "For now here's some <e>Enchanted Bread <f>to get you started.",
                                "Enchanted Bread can be used in Farming Minion as fuel! It makes them temporarily faster."
                        ).build(),
                DialogueSet.builder()
                        .key("initial-hello").lines(
                                "The farm animals here provide a variety or resources.",
                                "Did you know that chicken drops eggs and cows be milked?",
                                "Collect wheat, carrots, potatoes, pumpkins and melon slices to add to your Collection and unlock cool rewards!",
                                "Those <d>Fairy Souls scare off the animals."
                        ).build()
        ).stream().toArray(DialogueSet[]::new);
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        if (isInDialogue(e.player())) return;
        MissionData data = ((SkyBlockPlayer) e.player()).getMissionData();

        if (data.isCurrentlyActive(MissionTalkToFarmHand.class)) {
            setDialogue(e.player(), "quest-hello").thenRun(() -> {
                data.endMission(MissionTalkToFarmHand.class);
            });
            return;
        }
        if (!data.hasCompleted(MissionCraftWheatMinion.class)) {
            e.player().sendMessage("<e>[NPC] Farmhand<f>: Craft a <a>Wheat Minion<f>!");
            return;
        }
        if (!data.hasCompleted(MissionTalkToFarmhandAgain.class)) {
            setDialogue(e.player(), "spoke-again").thenRun(() -> {
                data.endMission(MissionTalkToFarmhandAgain.class);
            });
            return;
        }
        setDialogue(e.player(), "initial-hello");
    }
}
