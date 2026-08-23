package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;

import java.util.stream.Stream;

import net.swofty.type.generic.event.custom.NPCInteractEvent;

public class NPCBillyJoe extends HypixelNPC {

    public NPCBillyJoe() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Billy Joe", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "OfOl4uROq84Rq7ig5Uy+P5N0J66V0Sc6BW+5iwOs659y4LtK7MlFbX917io1Cp8OUIuizXYeEXz1xPsJJUfpokDVISOcuv2a/CsK4hSWIHs4KiFd2GS9EEPYwoibWSBZuKy7c9QXOW1+6MRSXQYGstI4wta+ra4pTnyvkNuhlSKgvhGTejJWh/Rl6xS5dKS/alGg3okdNY+fXJBWUdZ5jBcZaZ4pYdneVRZKk3zdArOfm8xlpvMJecuS07kS8bVRcl+WDydwDEiG2uZu76lBV9uWrKz9N+Z/r9qBUeuhdGJ12hUpR6xeNUKwGN9bdVFjjddbozzkC++J+WwX4MRlUjYSGasThWzUkJrjVr9PKKTkwoaShyIRyuOYXotaAYmnkxbcYiAnKoUk2dgDcIJ0d+A2r1StFXAa72Gx5RZY5ZYOIBCVnyZX3oQygqBxov9l3Xtk/Tw3tRpU901QUWILgxFXrDlgbGHl8SgLsLUXUHvTysno9unQBA8175dFqKTOeWi/gi4mxxfS7A2Ve/DT9E5drCyKVTpBo+PT1rOVHh50qgKNkvHTmJgE6vCOnf8ubmdmzl6Z7pK8uLSgfGMxYSp7XTuEQmdDEFR1mhliKbBmLVFj03jJ8/2bxUNZAWGTjBUO+MN5YcVHJo6CQuk2yPIiEaY1JwOgWotoynV7l5E=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYxMjExNzExMTcxOCwKICAicHJvZmlsZUlkIiA6ICI3ZGEyYWIzYTkzY2E0OGVlODMwNDhhZmMzYjgwZTY4ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJHb2xkYXBmZWwiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjBmYzRhMDNkYmYwMGZlYWI5YjQyMzU1OTNjMjkwNDkxOTZjN2U3ZjA3NTRkOGU3ZjBiNjYzMmQ3ZTI5NWNmNyIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-78, 70, -70, -115, 0);
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
        setDialogue(e.player(), "hello");
    }
    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder()
                        .key("hello").lines(
                                "You can hold as many <a>Accessories <f>as you want in your inventory. They will always work.",
                                "If you have more then one of the same type of the Accessory in your inventory, <c>only one <f>will work."
                        ).build()
        ).toArray(DialogueSet[]::new);
    }
}
