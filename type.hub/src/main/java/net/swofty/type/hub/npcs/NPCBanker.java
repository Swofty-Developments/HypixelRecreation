package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.gui.inventories.banker.GUIBanker;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCBanker extends HypixelNPC {
    public NPCBanker() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Banker", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "SkhUNSUjtfjFXHhfKO/Wsr0KYV96DzBjBlnzHbyvzHrY/xtHypc6qM8KB2TDPhNGlT3gNdjAyruf3rRaIeXZ9mpN1WdidPL4nYmGIDZRyxdMoEFuK20vHCg95gdg5sjVQyJmYjLzjAtOqBeZHfHiax8jTmuZjUEq94WiSzO5TkPNDwT9yu2hF51U4kvJKNIsdTsn6Y9Kkefx+mVSpd7UcsggmJ6uTEoP9aR9DeUwvaRA++1Ee5UyCURVFdIkGZrN52Ch63fbk9Gfr1XLThm6TYnUaIGatfrklW42KCkKhTuBNUeApAHiTd4lAApQJdqwRSMU4Z/L4THz0Kp64aHWOzqeY4ieW7PWxAS1f9grNRmM4wwlAKQEoyYW6YPpOhYCvHyxh9KlIix4g36sPj1xinmFuPKJMWwFSfMUZNQ/6D6QCejZcoY88ZL2bT3Q70jAl0vIqeS72dtlTjO33alTnkUIpxL7VWnRQSMWH1Q/LpcnLUkXTeJw07gX7C6oOH7nqmL6PTTrV+I5bZdgBYi9PDVj75iUBpWviODVIfQBr/Mzsbvv9KoDOttFjnXVX1l526whTbwnPyewq4rokqAuD5WXx22Rx6wAzQ/Z4SSNyV6oNm9RZWrcYIyvYXoj7sSgb3UsA9Qn+bmAoBMax0e43+Hy8QAn+vyzlqVgYTYruZM=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NTA2ODA4Mjg5MjMsInByb2ZpbGVJZCI6IjIzZjFhNTlmNDY5YjQzZGRiZGI1MzdiZmVjMTA0NzFmIiwicHJvZmlsZU5hbWUiOiIyODA3Iiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8zZjFlOGY5ZjVjZWE3YmFmNTZkOWUwNzkxMDU3YTdiMjNlNzkzNTZlNzY4M2VkM2Y0NzYwZWFhNmZjNWRjNGIxIn19fQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-29.5, 72, -38, -90, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        if (isInDialogue(e.player())) return;

        MissionData missionData = ((SkyBlockPlayer) e.player()).getMissionData();

        if (missionData.isCurrentlyActive("talk_to_banker")) {
            setDialogue(e.player(), "quest-hello").thenRun(() -> {
                missionData.endMission("talk_to_banker");
            });
        } else {
            new GUIBanker().open(e.player());
        }
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[] {
            DialogueSet.ofTranslation("quest-hello", "npcs_hub.banker.dialogue.quest_hello")
        };
    }
}
