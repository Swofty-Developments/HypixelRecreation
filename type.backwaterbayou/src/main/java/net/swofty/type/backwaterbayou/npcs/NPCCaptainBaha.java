package net.swofty.type.backwaterbayou.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.Stream;

public class NPCCaptainBaha extends HypixelNPC {

    public NPCCaptainBaha() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{
                    "<6>Captain Baha",
                    "<e><l>CLICK"
                };
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "A9Wh529RWV2HEMvVnPzQEPfvT7p8m2GU8IB5FowBVYRash8GUSC6OvO88v5eBXAsCJvAauOnCFkp0DrxNTHUTS6E8rcGpo5ieHTr+QYglXIlA8S+rgA5eGODgI3LEtOZucHJ6H64a23Bu41lNMpN2c+LzQbisqC9WBnfVBxYo6qrzgh5JBGsRDIg2h3UKmTnNgJPuhN2cwRDDlHG8/k+xES5ZqyEFvdjGn6O5HHL6xyMkCukjZN0E8s03NkpkKxZXEm1M/Eg8EWtwGqZIa3DHNmxchYok4mDPMst8iRy4pGRlJN+VBCmGLIV7pq4QZlGzuXWplrX/PAOb+B36Rg67SHvmIk23tpnu+7uvB3rw9NedWY1+xLp8W4gPKpynOobSCbKiJ6bX0mCQfURVh2svFT5nG/VnKCL0TE8CUiTxOuxJR8QWwWRI4BMRMJQfQxy0mofvPnR5g1XUnHzvGWr4m44dmooqyCgB4W9iysADAEgc9CVtizjroopAJLXCtfsxwIuioHaZsBKQU1NpvpH55bPqf//RI9FyJJwOXTgX7fbF49z0eAgjnRAyF9VE9VYI2hFZwa3BIFnvdGxlZhE63QPB+nmKQMT0WzTz15lm77lxvvpQsurkm2gKr6FlL9+SokbTUuQmisyzS84s2EocpRscgc9JF1Dv/NjK7T+3GU=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTc0MDQxOTU1NzI0NiwKICAicHJvZmlsZUlkIiA6ICIzY2I3YTA3YWY3ZjM0ZWZiYTlkNGI4ODQ3NDM4Mzc0ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJBUkJVWklLMTIwMTMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzcxYzQ0YWQ0MDdjMDIxNzM3YWQ3ODkwN2I1MDY4ZDdiY2MwYzY1OGIyYTJmYmFiZjAxNzA2NTYzYmQ5NDQ3ZCIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-22.5, 75, -8.5, -115, 0);
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

        if (!player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_VISITED_BACKWATER_BAYOU)) {
            setDialogue(player, "arrived").thenRun(() ->
                player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_VISITED_BACKWATER_BAYOU, true));
            return;
        }

        setDialogue(player, "idle");
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
            DialogueSet.builder()
                .key("arrived").lines(
                    "Land ho! We have arrived at the <2>Backwater Bayou<f>!",
                    "You go on and explore! Come back to the <6>Ship <f>and set sail when you're done!"
                ).build(),
            DialogueSet.builder()
                .key("idle").lines(
                    "The <6>Ship Navigator <f>is yours whenever you're ready to head back.",
                    "Don't keep the marsh waiting too long, though."
                ).build()
        ).toArray(DialogueSet[]::new);
    }
}
