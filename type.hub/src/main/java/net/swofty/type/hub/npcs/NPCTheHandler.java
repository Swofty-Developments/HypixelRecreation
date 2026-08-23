package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class NPCTheHandler extends HypixelNPC {

    public NPCTheHandler() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<5>The Handler", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "WoxEznJjYEa8/EQR/hLwVMgP4yAz6n12GZ7ZEOLw7WOV0CHMJ+ipoNvERz9fTWg3X8X28hfv5mFKuK7XQ7WqKyEj0OchfHGdyVeAvgCmIkL5o4g0Ah8WCKNbCfBvdKHiKLDsjJv91DdVo3ZZao9UVEKg2r7E/Gsk/mdqGrXUwv73P1vA+PprdsL5AQnLPFUxUZN15pl5LkNMdLoQq2i/yPiFRIqDaqNuNzyvadXFO91ET+NhkLnNN5BssIE98G+xMgoeFLQspbubO5NImZkQjzrvXVJzScWAusFvW0x+BPGGmX6yCOGqHdEc8NdoOobZJ8NfAxxktaldFnacbqfWJu+sEXDHxk/gJhgr0EDE5/InLgKZXErDE+oCW/WzLg05F6zyAKHKfrj6eDlKwEVyqMZxEW4A7iMHzTdyfdkuvWt5LaHsPinVvjv2toZzT4Nx4nXARI3bIch9Sw42xtIRFnrFlXT9OtiJCyXUv6u9tDI2cC1Dd3TQYjDvKJxjwxsqpaPgixsRsXHb5pMHugfgccyYXpy4fpM6xdcIh2mqlZwXSvrsdTsqMirkkDZeSz71HhPmI2pwokghlH812kMWM/V4N1jfOguzdAcVb+M/eoP3WCBUCumjimlGtsd7egCrc/3mhHx2ViSeQ/n8yh9y0OcEqnqbu/j0maAwT8DbFBM=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYyMTcxNjcyOTQ1MSwKICAicHJvZmlsZUlkIiA6ICJiNjM2OWQ0MzMwNTU0NGIzOWE5OTBhODYyNWY5MmEwNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJCb2JpbmhvXyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80YTNmZjg5N2QzMjg3YWY4MjBhMWYyNDAwNDM0MGUzYWY2MjU3ZGM1ZjhkYzBmOGQyZjlkYmViZGM1M2UzNjZiIgogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(40.5, 72, 1.5, 90, 45);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.getPlayer();
        if (isInDialogue(player)) return;

        setDialogue(player, "not-prosperous-" + (int) (Math.random() * 5 + 1));
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return List.of(
            DialogueSet.builder().key("not-prosperous-1").lines("<d>The Hex <f>will not see you now.").build(),
            DialogueSet.builder().key("not-prosperous-2").lines("he Hex is troubled by your presence.").build(),
            DialogueSet.builder().key("not-prosperous-3").lines("There is an absence of <d>prosperity <f>within you.").build(),
            DialogueSet.builder().key("not-prosperous-4").lines("<d>The Hex <f>does not wish to be tainted with <d>useless <f>items.").build(),
            DialogueSet.builder().key("not-prosperous-5").lines("<f>The Hex <f>does not have time for you.").build()
        ).toArray(DialogueSet[]::new);
    }
}
