package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.entity.npc.trait.NPCAbiphoneTrait;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.gui.inventories.builder.GUIBuilder;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCBuilder extends HypixelNPC implements NPCAbiphoneTrait {

    public NPCBuilder() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Builder", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "EzyAHb6TAVKVuO3R6cTt6eNJYXdU6C1fpPByuOEL/FUIIHqW5QpUnQLP7s3EjLhhzRagDi/eU/xGe09Ucsb7s6tSavn1jzfqwnmVG7C2FJ30ELl35y3pYbNKwmBl8I2fDY9pQrmfJbWRVhv9Gw8W4h8YRZARnW5PfVdsL1ddbTTsssaxapU8YTfUc88h2egnTD/bEHaqYEgfLBzjyMAyK9pDUIqe0NDmBJLbjPZXIVImRbMKanwgLRxmUkjGLONerb0HE8Kx6QoJEumoLOBrOLA5BJF7Jwghrv2d1W9S6hr89Ul6R8CnxQwHFfBMejccm0hLZein4DrKbiFHC8c/hs4jCoC4JT4rvOd/Yp8zNr3Y/dtUk5uTOguk/gYExI+p+1xc8HwTK3sK75LiFl+Ryu4LlKv5GBEznsnRHv1Ufeia3NeuVXDLi/W3zR8VG95Hf0lmKHdwJ/R9E56TxNYRh7wpma37ZTfEpUpKE1o7Z2m5c3jmDxLRdQg8dK1ZYMjlul36Qa8SXYTM4T+bdB1577M/44Vyde1NFVepYK0vRXDDNRal2LoDRM9buoTuN2taeP3pmt5C+pL554r7tWgOdCHUz51E9hwsOA9VCVxIA5eS+bgzSBLkWbXZYNo+zi/0bVr9OGdP2hCTJDsd0x7YEL2P7qribidVjnRLWWP8a2k=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NTA2Nzg1OTkwMDQsInByb2ZpbGVJZCI6ImEyZjgzNDU5NWM4OTRhMjdhZGQzMDQ5NzE2Y2E5MTBjIiwicHJvZmlsZU5hbWUiOiJiUHVuY2giLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2M4Y2NkNGZkZjU4YjMwYWE4MzAxN2NmYTVmZWQ5NzcxOTZjMDI0YzhkMWEyNzYwMDRlOTA2OGU4ZWNiYjBiNzkifX19";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-8.5, 71, -61.5, 45, 0);
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
        boolean hasSpokenBefore = player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_BUILDER);

        if (!hasSpokenBefore) {
            setDialogue(player, "hello").thenRun(() -> {
                player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_BUILDER, true);
            });
            return;
        }

        new GUIBuilder().open(player);
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[] {
                DialogueSet.builder()
                        .key("hello").lines(
                                "If you build, they will come!",
                                "Click me again to open the Builder Shop!"
                        ).build(),
        };
    }

    @Override
    public String getAbiphoneKey() {
        return "builder";
    }
}
