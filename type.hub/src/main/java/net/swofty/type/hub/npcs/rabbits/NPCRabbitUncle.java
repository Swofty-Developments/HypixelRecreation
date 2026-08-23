package net.swofty.type.hub.npcs.rabbits;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory;

import java.util.stream.Stream;

public class NPCRabbitUncle extends HypixelNPC {
    private static final String NPC_NAME = "Rabbit Uncle";

    public NPCRabbitUncle() {
        super(new RabbitConfiguration(
                DatapointChocolateFactory.EmployeeType.RABBIT_UNCLE,
                "ewogICJ0aW1lc3RhbXAiIDogMTcxNjkwOTAxMDg2NywKICAicHJvZmlsZUlkIiA6ICJmYjZkM2E5Zjk3MWY0ZTdlYmQ0MjE2Yjk0MjE5NDA3NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJtYXJjaXhkZCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9hODY1MTc2NzIzYTBiOWVlMjkxNjE4MGE1NWEwNGNjY2I3NzA0YWQxZjMxZmRmM2U5ZDg5Yzc5OGY2ODAyZTZiIgogICAgfQogIH0KfQ==",
                "eXW6yZoOB+xQNMaO/ZSRCiB84jeguuQUC5V6TNz3oxH3zNOXHbbl+xhzBiskUwIkwpaVoz1OKFIrIpbHSHNYkSvaLPOBNxl5XrC4Ma9veZt4HKXY7cRyytV5UYx5kXmz1ZbE6ZBn/KWPn1aaI1JIzt4SLObPI4y8LUaJBCceu4HN69TIStSfudW6HnHZCGCi4XLH9KrVcW8PlQd0Xh4ozUpd6uzTS2HlbroLpUDNLj25ZewC7g6owtBFZmBtYwOUzzpvvbT7tIZ50O07ZeuhrsU0+P+fmENumntkDrQxFy8ha2eJRTAZ2FzrjgFyVkBhX/y+zzwr7F5Lqh4zp3+ZqIM4Fp2tItX5dz2IszRl0PwAWvgVLXPpWgahNjPnWm0U5nQD+Ma7IHb3H4Hki1p3eWRCuG4Vk06+3qAIPeEE+2NHOKlkV7SkaZzhjtsicmOx/nW2Fl4I0i3SxTnbaPK6wpGaSAa1WACg/U/LfhCn7Svzm6NukUMD4J09swJTaiEen8//WEYHe5gjUCKatfrQSnjNd0tui80A2Nkv9n5c65jY9NBZBbhfvg9ORXoiJNqZGH244SdCjxNxtfHMfJJNyC3Gce65jk5F8+DA/7GSvADkBjYi7j6l3fkLhTrfCM3cIyZTes6tqHaj3wTUqg4I/i54XLX9BcyS63LNUq0GJaE=",
                new Pos(77.5, 65, 9.5, 45, 0)
        ));
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        if (isInDialogue(e.player())) return;

        setDialogue(e.player(), "dialogue-" + (int) (Math.random() * 25 + 1));
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder()
                        .key("dialogue-1").lines(
                                "Grandma always said I could be somebody if I put my mind to it.",
                                "She was wrong."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-2").lines(
                                player.getUsername() + "...you have to hire me. Please."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-3").lines(
                                "I should be in the Hall of Fame by now.",
                                "Instead, I am here. Please, hire me."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-4").lines(
                                "Back in high school, I was just one play away from that state championship.",
                                "If only I had zigged instead of zagged..."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-5").lines(
                                "I used to chuck that ball like nobody's business. Those were the days, eh?",
                                "Still got some of those moves, you know."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-6").lines(
                                "Every time I see a football, I can't help but wonder 'What if?'",
                                "Was so close to grabbing that championship ring."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-7").lines(
                                "Sometimes, late at night, I replay that final drive in my head.",
                                "I could have been a legend, you know?"
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-8").lines(
                                "Man."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-9").lines(
                                "I've got the entire high school trophy case memorized.",
                                "Sometimes, I give tours. You know, just to keep the legacy going."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-10").lines(
                                "Cuz might not have the typical athlete's discipline, but he's clutch.",
                                "Just like my old teammate who didn't show up to every practice, but could score tuddies when it mattered most."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-11").lines(
                                "<d>Hoppity<f>'s like the star quarterback of this chocolate game. Always looking for that next big play.",
                                "He's got that championship mindset, just like I had."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-12").lines(
                                "Sis has that fire, like the team captains back in my day.",
                                "She doesn't just play - she changes the game.",
                                "Always rallying the troops for her cause, much like a good quarterback does in the fourth quarter."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-13").lines(
                                "Sure, Rabbit Bro's up at dawn doing push-ups and planning his day.",
                                "I used to be like him, then I tore my ACL.",
                                "My knee has never been the same since, man."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-14").lines(
                                "Cuz showing up to work is like a trick play.",
                                "You never see it coming, and when it happens you can't help but wonder if it was a fluke."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-15").lines(
                                "Grandma always said I could be somebody if I put my mind to it.",
                                "She was wrong."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-16").lines(
                                player.getUsername() + "...you have to hire me. Please."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-17").lines(
                                "I should be in the Hall of Fame by now.",
                                "Instead, I am here. Please, hire me."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-18").lines(
                                "Back in high school, I was just one play away from that state championship.",
                                "If only I had zigged instead of zagged..."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-19").lines(
                                "I used to chuck that ball like nobody's business. Those were the days, eh?",
                                "Still got some of those moves, you know."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-20").lines(
                                "Every time I see a football, I can't help but wonder 'What if?'",
                                "Was so close to grabbing that championship ring."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-21").lines(
                                "Sometimes, late at night, I replay that final drive in my head.",
                                "I could have been a legend, you know?"
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-22").lines(
                                "Man."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-23").lines(
                                "I've got the entire high school trophy case memorized.",
                                "Sometimes, I give tours. You know, just to keep the legacy going."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-24").lines(
                                "Cuz might not have the typical athlete's discipline, but he's clutch.",
                                "Just like my old teammate who didn't show up to every practice, but could score tuddies when it mattered most."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-25").lines(
                                "<d>Hoppity<f>'s like the star quarterback of this chocolate game. Always looking for that next big play.",
                                "He's got that championship mindset, just like I had."
                        ).build()
        ).toArray(DialogueSet[]::new);
    }
}
