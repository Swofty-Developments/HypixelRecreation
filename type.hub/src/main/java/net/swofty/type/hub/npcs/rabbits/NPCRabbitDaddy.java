package net.swofty.type.hub.npcs.rabbits;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory;

import java.util.stream.Stream;

public class NPCRabbitDaddy extends HypixelNPC {

    public NPCRabbitDaddy() {
        super(new RabbitConfiguration(
                DatapointChocolateFactory.EmployeeType.RABBIT_DADDY,
                "ewogICJ0aW1lc3RhbXAiIDogMTcxMjg0NzA0NzAwNSwKICAicHJvZmlsZUlkIiA6ICIzOThiZGM3NWVhYzQ0ZjMzYWEyMDBiMTYyNTRmMDhlOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJJa2h3YW4wNTEwIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzU3Y2FiMGMzNGQ3ZGRjZjcyZGI1NmZmMzZmMjg4M2Y1NTRjZmY3NmViNWQzYjNlMDU2MjMzODAzNmM5NzYwNDMiCiAgICB9CiAgfQp9",
                "IZ3NIeEVd4wX+2jebNRoHZ3hOGqGSfB8qXv8mBpwR2p9tSjQy/JAWsky2OKlqEufN6ImlAe3uLQUkLt180dfmOFyLJ9EE6zu2uOPqbxfpxBuDUV0TuND5TjmlGAXfNIlWqiFfgw5sCruxxzdD8VUcHh180nzbCbFOJoT1Jxc59UKITu2k69ZfZE/35I/Es2L2i1/2ivLXD7f1EfN/OZzjjN6dZB98IjXh5fCwwnqUFvj9eh9Jkm0qhXADF93PmW/wIU1+W9ggcREnGX4rapyDdJUxeZy/OamE9nvY8w+3SYv8/oPabF4xfRt23sYsCrazrlOyZjtDV1lMmQMj+pN1ocmppY20kJDCWSxQoPOIh5LYltgDWAumQr7cBUooK0dpRcEMgh862Iwe9xr3xMEi7DE7PQoX831IYl6Xl9LLOsngjDEpVE3BowkfGVipuvTsyCKJwEbwbEWL7d37VrNLoIeF4H0w2ut0w5+y/kRHVMF2Ed7KB69Lz8+KzA4sIB5DBLFRxaOE32up6Xd8UppF56Jq6u7R7cv8ErpKbc3rwlFNvuFLCmLo5+IFAcsNPqy4OgL+FEgn7RkPi1f4HmHdWE8NYUcTOHhi2WPcoAmc0b9I8gFLcYzxTSbavJYSRAgl4Mny0l2hJdAbSk2GXns2OJdRV91Vtv9ISLUw59+VkU=",
                new Pos(76.5, 65, 13.5, -181, 0)
        ));
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        if (isInDialogue(e.player())) return;

        setDialogue(e.player(), "dialogue-" + (int) (Math.random() * 12 + 1));
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder()
                        .key("dialogue-1").lines(
                                "In life there are <c>sharks<f> and there are <a>sheep<f>.",
                                "I'm a <c>shark<f>.",
                                "Well, I'm a rabbit. But you get the idea."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-2").lines(
                                "I am the heir to the <6>chocolate throne<f>.",
                                "The only thing in my way? You. Please hire me, boss."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-3").lines(
                                "Heir to the <6>chocolate throne<f>, they say.",
                                "More like heir to a bunch of headaches, courtesy of my own daughter's protests."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-4").lines(
                                "Every day, it's a new challenge. If it's not the market, it's Sis with her picket signs.",
                                "Still, we're making the world sweeter, one chocolate bar at a time."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-5").lines(
                                "Every family has its ups and downs, but ours?",
                                "We've got a whole soap opera thanks to Sis."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-6").lines(
                                "Between Bro's grindset, Cousin's relaxed approach, and Granny's wisdom, we've got all the ingredients for success.",
                                "Now, if only Sis would see that."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-7").lines(
                                "Bro's dedication is what this company needs more of. And Cousin, well, he brings...<d>creativity<f>.",
                                "Granny? She's the glue holding us all together."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-8").lines(
                                "<a>Hoppity <f>wants us to 'think outside the box.' Last time I did that, we ended up with chocolate-covered grass.",
                                "Sold out in a week. Shows what I know."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-9").lines(
                                "In life there are <c>sharks<f> and there are <a>sheep<f>.",
                                "I'm a <c>shark<f>.",
                                "Well, I'm a rabbit. But you get the idea."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-10").lines(
                                "I am the heir to the <6>chocolate throne<f>.",
                                "The only thing in my way? You. Please hire me, boss."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-11").lines(
                                "Heir to the <6>chocolate throne<f>, they say.",
                                "More like heir to a bunch of headaches, courtesy of my own daughter's protests."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-12").lines(
                                "Every day, it's a new challenge. If it's not the market, it's Sis with her picket signs.",
                                "Still, we're making the world sweeter, one chocolate bar at a time."
                        ).build()
        ).toArray(DialogueSet[]::new);
    }

}