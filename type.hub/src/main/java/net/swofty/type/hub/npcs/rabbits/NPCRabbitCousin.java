package net.swofty.type.hub.npcs.rabbits;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory;

import java.util.stream.Stream;

public class NPCRabbitCousin extends HypixelNPC {
    private static final String NPC_NAME = "Rabbit Cousin";

    public NPCRabbitCousin() {
        super(new RabbitConfiguration(
                DatapointChocolateFactory.EmployeeType.RABBIT_COUSIN,
                "ewogICJ0aW1lc3RhbXAiIDogMTcxMjU5NDI2ODkxNCwKICAicHJvZmlsZUlkIiA6ICJlMjc5NjliODYyNWY0NDg1YjkyNmM5NTBhMDljMWMwMSIsCiAgInByb2ZpbGVOYW1lIiA6ICJLRVZJTktFTE9LRSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9hOTgyODI1YzAxYjY1OGYzNDhhMDk5YjQ1NzkwMjlhMTgwZDJlNDE1MTgzOTUxYjJlNmU1ZTI3MjU3ZGY0MjU0IgogICAgfQogIH0KfQ==",
                "UUEKIv2viCvMq6ZqV3PGKR7tuIW4LWTSj/6xxKQBLoZUFYkXU1K4Jqh+olqTd5HIHj7iztW1/nqbkRKZm0JnSXDPuSJtxx3iGCBQAVX2Rj50lsCm4gNuW73XR5Jm3O391EDCiqPXOuIdmN6PeEQFQgHAavbPr7z9ZSswwbs+bSY4IGmAc9rtaHEw+4RG+HOVbXx6fRzalShnyfMwUAiN2EQcBgLL0ecwoHGdb87aduHExO8VjxJamiexwfN2fbXfkGK23f5ZT0xQF3JFhADGyeDGSIGftCEkSOKWSpOAfZiaW2RU9s1jupzOc1vHaVcuT59mdW6Kfw1pWULnywdjGTCDNf/GepjpB3oESSAx8EynuOLrluoU+mEg4QHJvKC45QIlAMAkKhkvIM7Ir891lZjuGOiFiupmk+XZ+Tv0x6t0QYBcdQSLEckgPQgZh5sF2ppRfDzQKkS606SAl24DbZrrJg1S1qXGAVlhOqp1AdQcHE7uansLwqnvAhzsXmnSMxhk7sDGGAwFOc1MUe3g2iu5cz8YBqcZe+B+z2DN/3ycw3bN0INYmfW+TTlZOqGVHG1DaM3Mq3mWX4mzaMh5VU53ORPMYXZXbWlmFLRacAKT2tI/t10ya8WkJni3FswO5bFbsEmmaphTNpxutyQtMe2lbE4Id3hURS+vf4cTTeA=",
                new Pos(69.594, 68, 4.5, -276, 0)
        ));
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        if (isInDialogue(e.player())) return;

        setDialogue(e.player(), "dialogue-" + (int) (Math.random() * 8 + 1));
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder()
                        .key("dialogue-1").lines(
                                "My parents have been hounding me to get a job! Please hire me!"
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-2").lines(
                                "I need a job soon, or my parents will kick me out of the warren."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-3").lines(
                                "I tell ya, family gatherings got a lot more interesting when <a>Hoppity<f> started bringing those experimental chocolates.",
                                "Remember the carrot crunch debacle?"
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-4").lines(
                                "Working hard or hardly working? With chocolate, it's both. I might drift in past noon, but when I'm on, I'm on fire.",
                                "They say genius often looks like laziness. Guess I'm living proof."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-5").lines(
                                "When I finally get to it, even <a>Granny's<f> impressed with the flavors I whip up."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-6").lines(
                                "I suggested chocolate-covered carrots to <a>Hoppity<f> once. He laughed until he tried it. Now, who's laughing?",
                                "Still him, because it was a terrible idea."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-7").lines(
                                "My parents have been hounding me to get a job! Please hire me!"
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-8").lines(
                                "I need a job soon, or my parents will kick me out of the warren."
                        ).build()
        ).toArray(DialogueSet[]::new);
    }
}
