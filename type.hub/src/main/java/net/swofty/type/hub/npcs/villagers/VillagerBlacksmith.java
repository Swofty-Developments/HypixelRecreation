package net.swofty.type.hub.npcs.villagers;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.VillagerProfession;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.VillagerConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.gui.inventories.GUIReforge;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.missions.blacksmith.MissionMineCoal;
import net.swofty.type.skyblockgeneric.mission.missions.blacksmith.MissionTalkToBlacksmith;
import net.swofty.type.skyblockgeneric.mission.missions.blacksmith.MissionTalkToBlacksmithAgain;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class VillagerBlacksmith extends HypixelNPC {
    public VillagerBlacksmith() {
        super(new VillagerConfiguration(){
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<6>Blacksmith", "<e><l>CLICK"};
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(10.5, 63, -126.5, 45f, 0f);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public VillagerProfession profession() {
                return VillagerProfession.WEAPONSMITH;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (isInDialogue(player)) return;
        MissionData data = player.getMissionData();

        if (data.isCurrentlyActive(MissionTalkToBlacksmith.class)) {
            setDialogue(player, "initial-hello").thenRun(() -> {
                data.endMission(MissionTalkToBlacksmith.class);
            });
            return;
        }
        if (!data.hasCompleted(MissionMineCoal.class)) {
            player.sendMessage("Retrieve 10 coal from the Coal Mines!");
            return;
        }
        if (!data.hasCompleted(MissionTalkToBlacksmithAgain.class)) {
            setDialogue(player, "spoke-again").thenRun(() -> {
                data.endMission(MissionTalkToBlacksmithAgain.class);
            });
            return;
        }

        new GUIReforge().open(player);
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return List.of(
                DialogueSet.builder()
                        .key("initial-hello").lines(
                                "I'm the town Blacksmith! I can <a>reforge <f>items for you, for a price.",
                                "Reforging usually costs Coins, but since I'm feeling friendly I can reforge your first item for Coal <8>x10.",
                                "Go into the Mine to collect Coal, then come back to learn how to reforge items!"
                        ).build(),
                DialogueSet.builder()
                        .key("spoke-again").lines(
                                "Ahh, excellent!",
                                "Reforging items allows you to get the most out of your weapons, armor, and other items by applying stat modifiers to them!",
                                "To reforge an item, place an item in my inventory. Reforging costs Coins - the more prestigious items cost more to reforge!",
                                "However, this time I will reforge any item for the low price of Coal <8>x10!"
                        ).build()
        ).stream().toArray(DialogueSet[]::new);
    }
}
