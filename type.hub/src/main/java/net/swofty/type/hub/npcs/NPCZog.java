package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.GUIShopZog;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCZog extends HypixelNPC {

    public NPCZog() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Zog", "§e§lCLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "MXHug6jxD2YJgbL1B1+cY+TZ+dwWdb489Y08Zpl3OzQX9sLr7b+FJFoTsdSp9DMNRDcgc0gm++LGk8Gb19Q9foL5wyCZYalPNGaW0XjufIhCvgTbUzhz0AGY9xTSxaaasRU/kwpBrVroB7fYvHOeWtXmVjjAMbras6x+b0+1b4EENaVvgN8fZbM7WvxQofHwPMywwWuNKLqcMQboQD1YbQyHry2fBb3hASv62CwfOJ+naPX4lFUd9OW4Z39GuLvs4d0+f1427OrbqxFOPnzz6baYXb24MLgIU//J8PFTa9cFUwxik8nkPWLl8tI05E5uXxod9jaR5i63zhCAehAE1BbqsCMSgYYsAsKjqlFQqJoXcs6cR4WhI1uAszU8LeaRX1+lM6wKaEYFrn69wiuj1o5KeplKC36rry7tAl4mJpb+QEulPSdbHtwsRxl1JV1wUcrQYqimKjAWn268X7yuL1gPD/umf6Lgn89YqazsruG5MG8szdf6ipr3z2+ZhzjTKsP9Gv6i1K9LVQjh0Wmxzle8yHrcs7m6dNQ4FBcg41/NnB0Qdb5ckNlcO5kbn+ywO9eoOIRHfmMbXcBzbbYzYSUubdY4hRDD6th1sDf/krodpvm7YJ7bnhcquQk5BoVCiETxAw2ZAu+pPIiUm1HmSShpj+ucToPKBcpxUnYIxzw=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1ODU1NDUxNzU2NjgsInByb2ZpbGVJZCI6IjllOGI4NzQ3YTMxNzQ0ZTY4YTQ4NzEzMzQwM2I0ZDM1IiwicHJvZmlsZU5hbWUiOiJFdmlsRGN0ciIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmJiY2Y3M2E0NjAzYjVhYTU0OTFjZWZjMTU1Zjc3MGRlMWE4MjQ2NmE1ODUwNjA0YmE1ZWEyNDk4MGNiNTQ3OSJ9fX0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(10, 69, -71, 90, 0);
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
        boolean hasSpokenBefore = player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_ZOG);

        if (!hasSpokenBefore) {
            setGardenDialogue(player, "hello").thenRun(() -> {
                player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_ZOG, true);
            });
            return;
        }

        player.openView(new GUIShopZog());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[] {
                DialogueSet.builder()
                        .key("hello").lines(new String[]{
                                "Hello! Have you heard of pet items?",
                                "Pet items give your companions bonuses!",
                                "I sell all kinds of them if you'd like to try one out!"
                        }).build(),
        };
    }

    private java.util.concurrent.CompletableFuture<Void> setGardenDialogue(HypixelPlayer player, String key) {
        return setDialogue(player, key).thenRun(() -> {
            if (player instanceof net.swofty.type.skyblockgeneric.user.SkyBlockPlayer skyBlockPlayer) {
                net.swofty.type.skyblockgeneric.garden.progression.GardenProgressionSupport.apply(
                    skyBlockPlayer,
                    net.swofty.type.skyblockgeneric.garden.progression.GardenProgressionReward.spokenNpc("ZOG")
                );
            }
        });
    }
}
