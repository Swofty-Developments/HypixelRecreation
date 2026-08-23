package net.swofty.type.backwaterbayou.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.stream.Stream;

public class NPCHattie extends HypixelNPC {

    public NPCHattie() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{
                    "<d>Hattie",
                    "<e><l>CLICK"
                };
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "vQ5zgSiec5YR9aUfRwR+dZH29487jWT9Pr7PfdIx9pCuK07orJTg5/h8LQBsLFFiNNxQDnENw7/JeaRUP9JxZcxT/OjVLGnlR/P5fn+AET6tSok86mYS81JMDuFPFR2qhoEPBEmOnT0JDmMW4MCCVZtjBz5ENvHvVelZ3eDqy5KROz2u1qwjVBL6QToyT1pLravS8Y/juJDkmFEr4HW76cErnsXU73TOcJ8VeN0oV8KMeKyayLnFK6zZ0OMHZmBEDyQJ59tfoNo12jkdyEZ2rIo/Ix2inZ/VB4QYA+sODyFbTb+YGWSOBU50fkGngwLXsCsc51xm3nGNm3+fVvO5Y6MoVKsUk9G7pddppFdw0G/dOLuoyMTtcWVbuYPqvVhd+b47h5z3zG5767FmZbm2QwlR1VqKC6KqyZFSR5T8p6RdZxz5to+d6RGs1reK5tCGyyPwyVc0TW1hTXQ523uiMYmPKbRWUssjd2uypIAFSiytjVWRl9LI2K5QrRazyv4t8e4JIoDczELdgM8y2qxHrbb4rdMbkSS2/KQnYWDBZoR4xprZR0j4rOxbXVjwQ/lZsHhYV7SPbtun2sHyNd/nD+XvydM7V7Zjci1Zt48AvHpkHGn+4J/eTLpBHe4C4aKdDCnnfdwb1F3DPWyQKs5nsc/LSvTacfwYZwW7SQ6f+U0=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTczOTk4ODE2OTQyNCwKICAicHJvZmlsZUlkIiA6ICI0YWY1YmQ3NTdmZDE0MWEwOTczYmUxNTFkZWRjNmM5ZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJjcmFzaGludG95b3VybW9tIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2E4NzQxZTFiZTdjY2VlN2E3YjY2ZDE5YWMwZjZmNDM2ZmFlNjhkNGFkZjNlNzVkMjBmNTk1MjIxYzIyOTMwMSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(17.5, 71, 14.5, -135, 20);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        HypixelPlayer player = event.player();
        if (isInDialogue(player)) {
            return;
        }

        setDialogue(player, "hotspots-" + (1 + (int) (Math.random() * 2)));
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
            DialogueSet.builder()
                .key("hotspots-1").lines(
                    "Hotspots are temporary rings that appear above the water or lava.",
                    "Fish inside one and you'll get a bonus while your bobber stays in it.",
                    "The Bayou, Hub, Spider's Den, and Crimson Isle all have them. Keep your eyes open!"
                ).build(),
            DialogueSet.builder()
                .key("hotspots-2").lines(
                    "Hotspots can boost your <b>Fishing Speed<f>, <3>Sea Creature Chance<f>, <6>Treasure Chance<f>, or even <5>Trophy Fish Chance<f>.",
                    "Some creatures only show up when you're fishing inside one.",
                    "If the water starts glowing, don't waste the opportunity."
                ).build()
        ).toArray(DialogueSet[]::new);
    }
}
