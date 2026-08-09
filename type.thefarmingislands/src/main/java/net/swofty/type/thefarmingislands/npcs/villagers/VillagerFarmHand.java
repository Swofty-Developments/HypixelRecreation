package net.swofty.type.thefarmingislands.npcs.villagers;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.VillagerProfession;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.VillagerConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.missions.barn.MissionCraftWheatMinion;
import net.swofty.type.skyblockgeneric.mission.missions.barn.MissionTalkToFarmHand;
import net.swofty.type.skyblockgeneric.mission.missions.barn.MissionTalkToFarmhandAgain;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class VillagerFarmHand extends HypixelNPC {
    public VillagerFarmHand() {
        super(new VillagerConfiguration(){
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Farmhand", "§e§lCLICK"};
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
                        .key("quest-hello").lines(new String[]{
                                "Welcome to the Barn!",
                                "Feel free to harvest any crops on the island, I'm taking a break.",
                                "Did you craft a §aWhat Minion §fyet? Here's the recipe!"
                        }).build(),
                DialogueSet.builder()
                        .key("spoke-again").lines(new String[]{
                                "Nice! I have a Tier X Wheat Minion of my own, maybe one day you can be like me!",
                                "For now here's some §eEnchanted Bread §fto get you started.",
                                "Enchanted Bread can be used in Farming Minion as fuel! It makes them temporarily faster."
                        }).build(),
                DialogueSet.builder()
                        .key("initial-hello").lines(new String[]{
                                "The farm animals here provide a variety or resources.",
                                "Did you know that chicken drops eggs and cows be milked?",
                                "Collect wheat, carrots, potatoes, pumpkins and melon slices to add to your Collection and unlock cool rewards!",
                                "Those §dFairy Souls scare off the animals."
                        }).build()
        ).stream().toArray(DialogueSet[]::new);
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        if (isInDialogue(e.player())) return;
        MissionData data = ((SkyBlockPlayer) e.player()).getMissionData();

        if (data.isCurrentlyActive(MissionTalkToFarmHand.class)) {
            setGardenDialogue(e.player(), "quest-hello").thenRun(() -> {
                data.endMission(MissionTalkToFarmHand.class);
            });
            return;
        }
        if (!data.hasCompleted(MissionCraftWheatMinion.class)) {
            e.player().sendMessage("§e[NPC] Farmhand§f: Craft a §aWheat Minion§f!");
            return;
        }
        if (!data.hasCompleted(MissionTalkToFarmhandAgain.class)) {
            setGardenDialogue(e.player(), "spoke-again").thenRun(() -> {
                data.endMission(MissionTalkToFarmhandAgain.class);
            });
            return;
        }
        setGardenDialogue(e.player(), "initial-hello");
    }

    private java.util.concurrent.CompletableFuture<Void> setGardenDialogue(HypixelPlayer player, String key) {
        return setDialogue(player, key).thenRun(() -> {
            if (player instanceof net.swofty.type.skyblockgeneric.user.SkyBlockPlayer skyBlockPlayer) {
                net.swofty.type.skyblockgeneric.garden.progression.GardenProgressionSupport.apply(
                    skyBlockPlayer,
                    net.swofty.type.skyblockgeneric.garden.progression.GardenProgressionReward.spokenNpc("FARMHAND")
                );
            }
        });
    }
}
