package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.GUISeymour;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCSeymour extends HypixelNPC {

    public NPCSeymour() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Seymour", "§e§lCLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "sotvNUxqLU3jo0VNPB7W4LhOGzNg1zTftkI9ApzQxXVzcxi7jzCZbAmDgUsHwgsLBag2L20Ma1JCCwL7mC5nbZxqbnF77FY8+PP/WhaFteivTV+p2NEg/LTBtRihKfNlP2Xj+mypFv+QskjsZ02e5t5F5z7AkXEQ+jZe2UnPbashtCg6yrxcdvnWXEi7AeCppfcwFxBubmOS+gnqQiKJ1DjED4s8Ok4L18M/xcvjhicHypghhls7+GUrCrDz+gzghQO9/fiFd4dsrajWEET03afDNr9/o7V1R7PMmESZ/GR98CaJQ1Oezpl1v9Qqtg9UapIj49bFsLjGD/AcjnuzjOBa63Jj+3TwC2mU7VnkFGw3Ez0CemTKJJEGX+qDllqSSAVV5upBhzH6sd8BdxcyjhGKPEATbAXZVKKYGPciU4F/b/XkTdVr8egfg3bkSrI6eTK/juIeCVxcr5u4UGX5j+T9Ug8DisdT6SerVn9yEjttyKDILYn6Rmx+syS82fQ/JWJr881ZlD2lb1FWAx88X6ZOX2uQ7SLHTjc1qI0VGeOttokJpdEFf17FV6rX18kY6yAIHDZ908gQ3XSx4uEchL2bdZrJZYAi6xHnh3wP3rjkLIvooYxJ2AACkMWYoU+vHV3dXvsctlTaJaNJLO+zv+SWuXY0SpQYQ8REnI1f/Ug=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NzMwNTQ2Njg5NzUsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2M4YWFjNGU2MzEzOThjMGY0N2ZlODU2MTVlN2Y0OTIxNTUwMDA0MGJiOTA4MTlkNTY4ZTgyNGFjZjk0YmZhNGUifX19";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(29, 66, -40, -21, 0);
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
        boolean hasSpokenBefore = player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_SEYMOUR);

        if (!hasSpokenBefore) {
            setGardenDialogue(player, "hello").thenRun(() -> {
                player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_SEYMOUR, true);
            });
            return;
        }

        new GUISeymour().open(player);
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[] {
                DialogueSet.builder()
                        .key("hello").lines(new String[]{
                                "Looking to buy something fancy?",
                        }).build(),
        };
    }

    private java.util.concurrent.CompletableFuture<Void> setGardenDialogue(HypixelPlayer player, String key) {
        return setDialogue(player, key).thenRun(() -> {
            if (player instanceof net.swofty.type.skyblockgeneric.user.SkyBlockPlayer skyBlockPlayer) {
                net.swofty.type.skyblockgeneric.garden.progression.GardenProgressionSupport.apply(
                    skyBlockPlayer,
                    net.swofty.type.skyblockgeneric.garden.progression.GardenProgressionReward.spokenNpc("SEYMOUR")
                );
            }
        });
    }
}
