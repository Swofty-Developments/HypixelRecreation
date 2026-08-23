package net.swofty.type.hub.npcs.rabbits;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory;

import java.util.stream.Stream;

public class NPCRabbitSis extends HypixelNPC {
    private static final String NPC_NAME = "Rabbit Sis";

    public NPCRabbitSis() {
        super(new RabbitConfiguration(
                DatapointChocolateFactory.EmployeeType.RABBIT_SIS,
                "ewogICJ0aW1lc3RhbXAiIDogMTcxMjg0NzA5MzAxMSwKICAicHJvZmlsZUlkIiA6ICIyMWNjMzkxZmNkMjc0NzY5OTg5Y2M3M2VjYWRiNTE3YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJHT1NUTFk5NyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9mZDA3NmUwZTNkNDA3MmQwZmZmZWUwYTg3YTVkNzI2ZmMzNGIyYmNlYzM4YzI2NGZiOWI2Nzg3MWE4ZWFkNjMzIgogICAgfQogIH0KfQ==",
                "JaWKU06wQ7XZG8N37+3CTfj4vIm5j+8UsNX0gdwmmsijKiDi8e1qt37RI5DswBBe9PgZMJoJ2VPpZBMHeA9SqtawP9JS06pG3SHmi22jXNNtcmE2/SBjukBYPa6hZmmCerligQxzjo4/4Q/ca2Sdx8hSvwYln5dJCLKtxp2uVc1ywfgthwJJTatpEGjuhSROyBKkaB2WOIP15r+u+PdFqDtR5lCsxEXDHh2mK9twL6Cb7ubFBHhItnyhc6tJxoM2c8kj2XNwCMNQeUs72w2/St+8sqWhU6PwY2JhvP57A2Tqy23Im46b5wFkIwmX0o0f0TRki1LYFaGi6LQAYT4tg8BpStFzEYPUaArlGkAO9PCBYdCL4jeCcfJfKTpMbMDvYrqv6tC/DvZWlWHLgeXz2DmUwMYoDFP3emNtKxl9g4KYxLwtVCHquPzlIVy9qCQfDuC4Yid395Bex/AvMHNluQMLeibU62InR4IxXTiERa7MjXPAWZoTvQZfNXbNRK+9NiySMRSEIY8cgKNvsdhI+O6kOITrlROK8Hs949YbDkkPumnMPXFCHkwTfFz0B2Z53wUjlhFn1TTcz/zjLSFWFry3Tm7k+q2eU28PwEj8Ky8pILPYf4GTgBlFMV0MI6TMccgUY4srcH49SxdDLnZ3caKtGhVs+lPvlQzHJioJDmg=",
                new Pos(68.5, 68, 12.5, -225, 0)
        ));
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        if (isInDialogue(e.player())) return;

        setDialogue(e.player(), "dialogue-" + (int) (Math.random() * 19 + 1));
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder()
                        .key("dialogue-1").lines(
                                "Hire me, " + player.getUsername() + "!",
                                "Together we can abolish the patriarchy, and ensure clean chocolate for all rabbits!"
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-2").lines(
                                "Down with Big Chocolate!"
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-3").lines(
                                "Regulate! Regulate! Regulate!"
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-4").lines(
                                "Chocolate is love, chocolate is life. But at what cost?",
                                "It's time this family faces the music and listens to the cocoa beans' side of the story!"
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-5").lines(
                                "They call me a troublemaker, a rebel. I say, I'm the only one talking sense!",
                                "Wake up and smell the exploitation, family!"
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-6").lines(
                                "A protest a day keeps the unethical practices away.",
                                "Dad might not see it now, but I'm doing this for the future of all chocolate bunnies."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-7").lines(
                                "They say I'm disrupting the peace, but I'm just trying to sprinkle a little truth on our chocolate-covered lies.",
                                "The factory needs a new recipe...for justice."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-8").lines(
                                "Bro's always talking about his 'sigma grindset'. Tried to get me to read a book on it.",
                                "I told him I'd start my own movement: the 'chocolate mindfulness mindset'."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-9").lines(
                                "Cousin might be the laziest bunny I know, but he's onto something.",
                                "Why protest when you can just 'be the change'? By napping, apparently."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-10").lines(
                                "I told <a>Hoppity<f> we should have a line of eco-friendly chocolates.",
                                "He asked if green food coloring counted.",
                                "We're...working on it."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-11").lines(
                                "<a>Hoppity<f> actually listens to my protests.",
                                "Well, more like he can't avoid them since I do it in the lobby. But it's a start, right?"
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-12").lines(
                                "Hire me, " + player.getUsername() + "!",
                                "Together we can abolish the patriarchy, and ensure clean chocolate for all rabbits!"
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-13").lines(
                                "Down with Big Chocolate!"
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-14").lines(
                                "Regulate! Regulate! Regulate!"
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-15").lines(
                                "Chocolate is love, chocolate is life. But at what cost?",
                                "It's time this family faces the music and listens to the cocoa beans' side of the story!"
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-16").lines(
                                "They call me a troublemaker, a rebel. I say, I'm the only one talking sense!",
                                "Wake up and smell the exploitation, family!"
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-17").lines(
                                "A protest a day keeps the unethical practices away.",
                                "Dad might not see it now, but I'm doing this for the future of all chocolate bunnies."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-18").lines(
                                "They say I'm disrupting the peace, but I'm just trying to sprinkle a little truth on our chocolate-covered lies.",
                                "The factory needs a new recipe...for justice."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-19").lines(
                                "Bro's always talking about his 'sigma grindset'. Tried to get me to read a book on it.",
                                "I told him I'd start my own movement: the 'chocolate mindfulness mindset'."
                        ).build()
        ).toArray(DialogueSet[]::new);
    }
}
