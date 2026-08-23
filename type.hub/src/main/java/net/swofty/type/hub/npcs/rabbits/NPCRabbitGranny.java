package net.swofty.type.hub.npcs.rabbits;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory;

import java.util.stream.Stream;

public class NPCRabbitGranny extends HypixelNPC {

    public NPCRabbitGranny() {
        super(new RabbitConfiguration(
                DatapointChocolateFactory.EmployeeType.RABBIT_GRANNY,
                "ewogICJ0aW1lc3RhbXAiIDogMTcxMjU5NDIyNDA2NCwKICAicHJvZmlsZUlkIiA6ICI2OGVmMmM5NTc5NjM0MjE4YjYwNTM5YWVlOTU3NWJiNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGVNdWx0aUFjb3VudCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kNmViMmQ4NWVlOGUzYWYxYzJlYzkzNGJlYjcwYTM5YzVlNzY2YjIzYmRhYjYzMjEwYmQyYWFjZDczY2JiZmM4IgogICAgfQogIH0KfQ==",
                "wSwFiREKbmU3npbnVXCeWQWBkfrSnwagmCJVheQaIn7qb1oxeEmDYU/rPqLaefhdtenSAHzrhzqWoC22p6Zrn2XhegJt5IvFlp7kKhlGBXNUH/Ly9SsDue246VtJsGdPwQ/Vp4utV7unqpbJ9/lqiLN4mc0Y2WtYB45yKfJ4qlusJ2JILWA2U5qNwIwrfExERMm/B6+jP6Y9DLfMZTzb+4vadkMvhNrDRraLaga1QIR/typvTrjHCgYvBrqly39OrCia9MdJ13vcBi21ZKF+IkbYylka5C2ojW9P38v9u02qh8zEPEkU8P9AHGVMiu+k7BkmPwau8yCX8D3NNfWDrpIxosWxaQs0pNY3PInceu6WuhAXq0uvkdohVDFHwwESYTlW6YBHOviKfrmBOx7Z6wZpoVfY4XSscnxRJwPFVY2Ja74L7bGMLpGgYxehdO+KNk2A/4kBqrq35kOM4ykS/pYk4hHUnKTA8KeFxCbCBNLRBBT+EMF4+BOz7Busi4Bn4dsy+QA6w6bhix2kujVUds3IjAKnn2HACavG5zsAgkaMuANM9YdVOF3BhD/fEBtPVCB3Y6vvFMWeuBAAin7HSMhkC6t4fxjZ7F3iHtWkPcxL6FQxD21lnvXOdxgkSLEZbTQ95drNAZEPwOM1cYIvvmZTJRjBAJL4vBHEp/25g/Q=",
                new Pos(58.5, 68, 10.5, 249, 0)
        ));
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        if (isInDialogue(e.player())) return;

        setDialogue(e.player(), "dialogue-" + (int) (Math.random() * 14 + 1));
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder()
                        .key("dialogue-1").lines(
                                "I'm looking to come out of retirement for <d>one last job<f>.",
                                "Plus, I decided I don't think I could ever give up chocolate."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-2").lines(
                                "The <6>Chocolate Factory <f>used to be different before <a>Hoppity<f> arrived.",
                                "You never know that you're in the glory days until it's too late."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-3").lines(
                                "In my day, we made sweets from whatever was in the garden. <a>Hoppity<f> just added cocoa.",
                                "Kids these days think they've invented sugar."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-4").lines(
                                "Every time <a>Hoppity<f> talks about 'expanding the business', I remind him: 'Don't forget to expand your heart too.'",
                                "He's a good boy, he listens."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-5").lines(
                                "<a>Hoppity<f> wants to automate the chocolate wrapping. I told him, 'Nothing beats the personal touch.'",
                                "He gave me a computerized knitting machine."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-6").lines(
                                "I've watched this family business grow from a single carrot patch to rows of cocoa trees.",
                                "Each bar we produce carries that legacy. Makes an old bunny proud."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-7").lines(
                                "They say you can't choose your family. But if I could, I'd choose this chocolate-crazed bunch every time.",
                                "From Dust Bowl to chocolate empire, we've come a long way."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-8").lines(
                                "In my days, we settled disputes over a hot cup of cocoa.",
                                "Maybe that's what Sis and my son need - a reminder of what binds us."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-9").lines(
                                "I'm looking to come out of retirement for <d>one last job<f>.",
                                "Plus, I decided I don't think I could ever give up chocolate."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-10").lines(
                                "The <6>Chocolate Factory <f>used to be different before <a>Hoppity<f> arrived.",
                                "You never know that you're in the glory days until it's too late."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-11").lines(
                                "In my day, we made sweets from whatever was in the garden. <a>Hoppity<f> just added cocoa.",
                                "Kids these days think they've invented sugar."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-12").lines(
                                "Every time <a>Hoppity<f> talks about 'expanding the business', I remind him: 'Don't forget to expand your heart too.'",
                                "He's a good boy, he listens."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-13").lines(
                                "<a>Hoppity<f> wants to automate the chocolate wrapping. I told him, 'Nothing beats the personal touch.'",
                                "He gave me a computerized knitting machine."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-14").lines(
                                "I've watched this family business grow from a single carrot patch to rows of cocoa trees.",
                                "Each bar we produce carries that legacy. Makes an old bunny proud."
                        ).build()
        ).toArray(DialogueSet[]::new);
    }
}