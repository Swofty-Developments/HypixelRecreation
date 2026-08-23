package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.missions.farmer.MissionCollectWheat;
import net.swofty.type.skyblockgeneric.mission.missions.farmer.MissionTalkToFarmer;
import net.swofty.type.skyblockgeneric.mission.missions.farmer.MissionTalkToFarmerAgain;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCFarmerRigby extends HypixelNPC {

    public NPCFarmerRigby() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Farmer Rigby", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "vq4VZf30+S4JnGMAf4x5LiWe0tMtUsAAxWtfqd7ceEbDzg/JKihESAhTelDED93joK5tJxEWrE2reVxKmqolCKHKoLGrIkX5qn6zcmo+o9M4LHOcApOLxRqmfcbCtCB7KBObK+zwA1Z4P1wuol7AwEax//lssvBruhWaMpH4iOjM/0Ao+0E0to/QE3/oCrq0stgNtqB1ddbI/Vco5AVU26E2LEnyOgrDYTol9TeAyQc7uDqi/s51Z451GCnwuTWo3xPUPQ9quMi+1A7S10OnPOkKvNFyK4T7kqtmJFSCPcxHBXoeY2GGoqbwPvSt83Xl3hgLAlkYizYJBiHtN3ZrewxcVUkZbzxe96+0yHGDPT8oEFErRruHw6ZYY6rgujAeZHrQYAVGTz04XHbVSsi/9l0GwZTipbSpmFJepzAbExTGU6sAeCF3rwzphumhzY8rC+oIN5DBBr1LE4MbuZQaX0wlqW0dcIuDwZU+xeCxv9yJ5atYB6gndmDGOMZaf2pYWhuJNWUBssTj7JDM6VFSHsGR4BFP/KdDLGINlDQq2NZQaZC6T2BGTADFqMDM0JT2OE8PtmVxXhjJEK/rkGnMOqxvElmj+BuxcLlFNbW3CY01F5A9Zj92gcZXwswPORdfdq4/LGyKYd/Bh4nym5fzZvaIAZapVWk2bDhO9HPEkT8=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NTkxMzM3MTY3MTEsInByb2ZpbGVJZCI6IjkxZjA0ZmU5MGYzNjQzYjU4ZjIwZTMzNzVmODZkMzllIiwicHJvZmlsZU5hbWUiOiJTdG9ybVN0b3JteSIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDUyMzJhOGU4NWE2NzUwODM2YzVkZmJjMzQwNGMzYWIzMTQ1MzQ5NjIwZjBiYzUyMDhkZWRiMTZiYjRjNDgzMCJ9fX0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(61.5, 72, -147.5, -35, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (isInDialogue(player)) return;
        MissionData data = player.getMissionData();

        if (data.isCurrentlyActive(MissionTalkToFarmer.class)) {
            setDialogue(player, "initial-hello").thenRun(() -> {
                data.endMission(MissionTalkToFarmer.class);
            });
            return;
        }
        if (!data.hasCompleted(MissionCollectWheat.class)) {
            player.sendMessage("My cow is getting worse! Hurry with that Wheat!");
            return;
        }
        if (!data.hasCompleted(MissionTalkToFarmerAgain.class)) {
            setDialogue(player, "spoke-again").thenRun(() -> {
                data.endMission(MissionTalkToFarmerAgain.class);
            });
        }

    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[] {
            DialogueSet.ofTranslation("initial-hello", "npcs_hub.farmer_rigby.dialogue.initial_hello"),
            DialogueSet.ofTranslation("spoke-again", "npcs_hub.farmer_rigby.dialogue.spoke_again")
        };
    }

}
