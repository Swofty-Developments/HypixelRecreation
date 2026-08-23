package net.swofty.type.hub.npcs.rabbits;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.GUIShopAdventurer;

public class NPCCoachJackrabbit extends HypixelNPC {
    public NPCCoachJackrabbit() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<d>Coach Jackrabbit", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "GvFhGqpWtF4xgX080rEgliRB1DevBr41QoCDINCI/4TDEQz0TJsn0yrZnSn2A8UuEPJCwP+wdBKqG0vvdOXsjD2Qo9FN1n8q85hs1Ew+K+Q/tKCI4cBzOJYbtgPykM75Sx4+NtDnEvrIwMQ+CttKuqaSFV4Wi81YZXHxqNgNp7A7I8h7Bm96okpSHwo72c3Au2HUBOtQAOvGmvMeijegLO2C6Uzi9ZKgWzObTS8qKtTui/dfR/O7TRnG0GBlLFJ0HUWlIoidhig9ayKp8FE5n3CmUUEyLshbXepk5QGZEYz0JhKa94s4JOblSy2xOUQx6DYjuAgMUVRGt/giUFk7QmsUUmgw0BeI6GEf7KU8QIe2RXY+FxjYTwC8bJzHleG80P+tgnwa+zSvjSehVnBubgOHErk1KuOGcdfLBNbsUg3DbNtJwnexeEblYQp0Mx3JCoROU5aeHfWObP1LOiVCPoEDpL/62oB0wcF0uTqJTAiKVjpYi7V7cup/W0n6vsPJDUeyQwp6b5xywzfNjHwY4Gmk+8GEMkaL9og3+dEM1Gffgdf7mEfSHfBGnE8u/tljibY56UZh/nsCmDrj6lF7JRrI84D6pHajzD54/YNwmV4lQ1AKOl+g47pNNGBMeOgyIe/1LQO7mDbFO3F5P7js1ji97bs2MrlJkpdwPFUnt8w=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTcxMzAyMjkyOTYwNCwKICAicHJvZmlsZUlkIiA6ICI2NGY0MGFiNzFmM2E0NGZiYjg0N2I5ZWFhOWZjNDRlNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJvZGF2aWRjZXNhciIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iYzBjYzY3ZTc5YzIyOGU1NDFlNjhhZWIxZDgxZWQ3YWY1MTE2NjYyMmFkNGRiOTQxN2Q3YTI5ZDFiODlhZjk1IgogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(63.5, 68, 3.5, 0, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        HypixelPlayer player = e.player();
        if (isInDialogue(player)) return;
        boolean hasSpokenBefore = player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_ADVENTURER);

        if (!hasSpokenBefore) {
            setDialogue(player, "hello").thenRun(() -> {
                player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_ADVENTURER, true);
            });
            return;
        }

        player.openView(new GUIShopAdventurer());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.ofTranslation("hello", "npcs_hub.adventurer.dialogue.hello")
        };
    }
}
