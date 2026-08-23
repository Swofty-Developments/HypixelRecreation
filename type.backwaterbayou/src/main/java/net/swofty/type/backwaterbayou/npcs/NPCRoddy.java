package net.swofty.type.backwaterbayou.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.backwaterbayou.gui.GUIFishingRodParts;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.Stream;

public class NPCRoddy extends HypixelNPC {

    public NPCRoddy() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{
                    "<6><l>ROD MECHANIC",
                    "<2>Roddy",
                    "<e><l>CLICK"
                };
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "m9YzKGb9O5Hs5mLECUJS6JmzLBDBQy9J768PK8lwNBEmF5a9J8Xdy93EWCAtCGCAFMar9CWRmyqt9Ees7u/3PHNtUVg9EkmvG27cPEIMUB2lqVir0U62pfNcR+5JkjjqaleDLhcOBgXdN9iVHJUcZq2tTY3t1eVX5OKI8K9SVjGvxgGMKxFmR1758eqwh9Krvb7AXI1/zRaRdFRsdXs2SgrvI/HcD4GN4vrjl6ZteOprTY4vB9oHZZSBLffkL28psxckC6JwvpiEahEtHKo1lS43qMpKtqKX9qwSs8WZ8/9A+/dXwB6g0+b1C/JSFWkyETFfFs1dUSdVsThkqGstRmt6c6Ko4xdvS61WQYLS4pbEbPN4kk0WapdqrWrrqNvjjcBUd94mnqNgvA0N+qPVOD/QkLGLvZDfXC5Iyv2BlpdeQHosTgn9ZPDjotESMwWrGwhm/uLLxNc2Z/rAGke5zTjz4v1JDSha7uyXT1yZ3oCckUqhbGFzmx5YOQoT/PbWr/mONQbYWTJ5GnSeWIIEs616Icwn+Rlw0qF+3EPUeR7QRO1bt5aqpmCUcZvrYoMyS1pvLDjS66MyuK5mEsQDEY5Uufux4iB3J0rrSGSgoC/XP1s6kGlz+jFRbDT9VPv/m72msPS35uAB6nCnI3o6uXKr0GrUGX9BiHB68Us0Pfs=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTc0MTI3MjI2NDk0NiwKICAicHJvZmlsZUlkIiA6ICIzOWEzOTMzZWE4MjU0OGU3ODQwNzQ1YzBjNGY3MjU2ZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJkZW1pbmVjcmFmdGVybG9sIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzE3Y2FkOWYyN2Q3NGRlM2U1OWYyYTUxN2QwNGEyODJlMmNmODZlNGM1ZTQwNjM5ZGFlY2E3YjFhYWU3ZjVhNzEiCiAgICB9CiAgfQp9";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(21.5, 78, -23.5, 135, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) {
            return;
        }

        if (!player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_RODDY)) {
            setDialogue(player, "first-interaction").thenRun(() -> {
                player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_RODDY, true);
                player.openView(new GUIFishingRodParts());
            });
            return;
        }

        player.openView(new GUIFishingRodParts());
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
            DialogueSet.builder()
                .key("first-interaction").lines(
                    "If your rod's missing some punch, you've come to the right mechanic.",
                    "Hooks, lines, and sinkers can change what your rod is best at catching.",
                    "Put a <a>Fishing Rod <f>in the slot and I'll show you what parts fit."
                ).build()
        ).toArray(DialogueSet[]::new);
    }
}
