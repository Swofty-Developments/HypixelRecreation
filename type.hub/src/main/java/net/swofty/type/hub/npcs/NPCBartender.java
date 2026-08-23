package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.GUIShopBartender;
import net.swofty.type.skyblockgeneric.mission.missions.MissionKillZombies;
import net.swofty.type.skyblockgeneric.mission.missions.MissionTalkToBartender;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCBartender extends HypixelNPC {

    public NPCBartender() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Bartender", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "BzV+QBeWNTuShsKXgjlncos8dIDbogV/LUCOSGt6yU+hc+aAhtdo68Z0fQYo53MEj2rIJSeAe+oqKvmWpp7PCbualsJ2YoU/RAM/E1n+3bsWZFfiYAtQNZZA/tSiJgHQ1pLW2GLD7xcOvMYtam5cP82PtGgbTk4TZVnTSaPI6syHWXz8jDEFY0jg2CVr6v6zxNf7uP2vXI1U2Xf8//rEhNXYmyBDWC3BBh0I5hYjYiioV5C/ibwe6yShaI/Qdbl9/NASZqFfPisqvo96wHePPPfSyZETsz9oY92jYFG8wXaq44JENEYfl3iehwnM2MVhIvEoZUADTgyJcnBmM63x18ldNqKqf5sj8YkWQOeCMes/F8HDn8VRqdXLqBC+P/FMVmnDKWuC2BAUij2762TFJ0G1+814rUbz3poCM5cpJwHN1JjXENWQqZgyPBcRDha67oJdDixrkzCLQ6UNJ+I2D6fyaTvCEXQC0S+CMMd7fIoDrBABnOJmoxn3wTBfHqquqLooTL4Vvnt/vh5miLEmDf/SkfdPRHZMRvfgPMTGdAC0FjaoWB+RmKANh2ePh85XhFplI5lI22kf+Jn1SxwLhP+5qv/3+0m2KY6czPrGau3HkCoDKurd/oimQgaqF8WvpKrBT3ciGiz9i6fJH/m0eNL/cdPWAit7xCPyuj5MiLU=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1Mzc2NTgyNzQ0NDcsInByb2ZpbGVJZCI6ImVmYWY1NzU3NzgxZTQ3YWViYzE0Y2Q4MmM5MWM3ZjgyIiwicHJvZmlsZU5hbWUiOiJNaW5lU2tpbiIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWVkZDhmMTZiMjAyYzQyYjNiNWFkNDU3ZDQzNmNjNjM5OTUyMTA3ZTM2MWFiMWU2MWI5MTY4OWU0MGM3OGJlNyJ9fX0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-83.5, 74, -135.5, 0, 0);
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
        if (!player.getMissionData().hasCompleted(MissionKillZombies.class)) {
            if (isInDialogue(player)) return;
            if (player.getMissionData().isCurrentlyActive(MissionKillZombies.class))
                setDialogue(player, "quest-talk");
            else
                setDialogue(player, "quest-hello").thenRun(() -> {
                    player.getMissionData().startMission(MissionKillZombies.class);
                });
            return;
        }
        if (!player.getMissionData().hasCompleted(MissionTalkToBartender.class)) {
            if (isInDialogue(player)) return;
            setDialogue(player, "quest-complete").thenRun(() -> {
                player.getMissionData().endMission(MissionTalkToBartender.class);
            });
        }

        player.openView(new GUIShopBartender());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[] {
            DialogueSet.ofTranslation("quest-hello", "npcs_hub.bartender.dialogue.quest_hello"),
            DialogueSet.ofTranslation("quest-talk", "npcs_hub.bartender.dialogue.quest_talk"),
            DialogueSet.ofTranslation("quest-complete", "npcs_hub.bartender.dialogue.quest_complete")
        };
    }
}