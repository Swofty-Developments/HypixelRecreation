package net.swofty.type.hub.npcs.rabbits;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory;

import java.util.stream.Stream;

public class NPCRabbitDog extends HypixelNPC {
    public NPCRabbitDog() {
        super(new RabbitConfiguration(
                DatapointChocolateFactory.EmployeeType.RABBIT_DOG,
                "ewogICJ0aW1lc3RhbXAiIDogMTcxNDk1OTAyNzAyNCwKICAicHJvZmlsZUlkIiA6ICJiZDNhNWRmY2ZkZjg0NDczOTViZDJiZmUwNGY0YzAzMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJwcmVja3Jhc25vIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzM1Y2E5OGJlZGUzODY1ZGQxMjA1ZTRkMDkxMDM2Y2Q5ZGMzNjc5MWI4M2VhNGUwZmY0YTk5YWQ2MWI3MWU4OTgiCiAgICB9CiAgfQp9",
                "GvFhGqpWtF4xgX080rEgliRB1DevBr41QoCDINCI/4TDEQz0TJsn0yrZnSn2A8UuEPJCwP+wdBKqG0vvdOXsjD2Qo9FN1n8q85hs1Ew+K+Q/tKCI4cBzOJYbtgPykM75Sx4+NtDnEvrIwMQ+CttKuqaSFV4Wi81YZXHxqNgNp7A7I8h7Bm96okpSHwo72c3Au2HUBOtQAOvGmvMeijegLO2C6Uzi9ZKgWzObTS8qKtTui/dfR/O7TRnG0GBlLFJ0HUWlIoidhig9ayKp8FE5n3CmUUEyLshbXepk5QGZEYz0JhKa94s4JOblSy2xOUQx6DYjuAgMUVRGt/giUFk7QmsUUmgw0BeI6GEf7KU8QIe2RXY+FxjYTwC8bJzHleG80P+tgnwa+zSvjSehVnBubgOHErk1KuOGcdfLBNbsUg3DbNtJwnexeEblYQp0Mx3JCoROU5aeHfWObP1LOiVCPoEDpL/62oB0wcF0uTqJTAiKVjpYi7V7cup/W0n6vsPJDUeyQwp6b5xywzfNjHwY4Gmk+8GEMkaL9og3+dEM1Gffgdf7mEfSHfBGnE8u/tljibY56UZh/nsCmDrj6lF7JRrI84D6pHajzD54/YNwmV4lQ1AKOl+g47pNNGBMeOgyIe/1LQO7mDbFO3F5P7js1ji97bs2MrlJkpdwPFUnt8w=",
                new Pos(63.5, 68, 3.5, 0, 0)
        ));
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        if (isInDialogue(e.player())) return;

        setDialogue(e.player(), "dialogue-" + (int) (Math.random() * 26 + 1));
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        Sound barkSound = Sound.sound(Key.key("entity.wolf.ambient"), Sound.Source.NEUTRAL, 1.0f, 1.0f);

        return Stream.of(
                DialogueSet.builder()
                        .key("dialogue-1").lines(
                                "In the pursuit of excellence, one must remember that the journey is as significant as the destination.",
                                "True success lies not in the accolades, but in the lessons learned along the way."
                        ).sound(barkSound).build(),
                DialogueSet.builder()
                        .key("dialogue-2").lines(
                                "Consider the possibility that limitations are merely figments of our imagination, crafted by our fears and insecurities.",
                                "To break free, one must first believe in the boundlessness of their own potential."
                        ).sound(barkSound).build(),
                DialogueSet.builder()
                        .key("dialogue-3").lines(
                                "Empathy is the silent language of the soul, spoken through actions rather than words.",
                                "To truly understand another, one must listen not only with ears but with the heart."
                        ).sound(barkSound).build(),
                DialogueSet.builder()
                        .key("dialogue-4").lines(
                                "The greatest paradox of life is that in seeking control, we often surrender it.",
                                "True power comes not from dominion over others, but from mastery over oneself."
                        ).sound(barkSound).build(),
                DialogueSet.builder()
                        .key("dialogue-5").lines(
                                "Wisdom often comes to us in whispers, through the leaves of the trees or the ripple of the waters.",
                                "It teaches us that every voice, no matter how small, has something of value to say."
                        ).sound(barkSound).build(),
                DialogueSet.builder()
                        .key("dialogue-6").lines(
                                "In every moment of decision, we stand at the crossroads of countless possibilities.",
                                "The paths we choose not only define our destiny but also reflect the essence of our being."
                        ).sound(barkSound).build(),
                DialogueSet.builder()
                        .key("dialogue-7").lines(
                                "Solitude is not the absence of company, but the moment when our soul is free to speak to us, unencumbered by the chaos of the world."
                        ).sound(barkSound).build(),
                DialogueSet.builder()
                        .key("dialogue-8").lines(
                                "Life, like a vast symphony, plays on the strings of our experiences, emotions, and encounters.",
                                "Each note, while fleeting, contributes to the eternal melody of our existence."
                        ).sound(barkSound).build(),
                DialogueSet.builder()
                        .key("dialogue-9").lines(
                                "Ah, Rabbit Bro, tirelessly toiling from the break of dawn, clinging to his routines with the desperation of a shipwreck survivor to a life raft.",
                                "One wonders if he runs from failure or merely jogs alongside it."
                        ).sound(barkSound).build(),
                DialogueSet.builder()
                        .key("dialogue-10").lines(
                                "And there strides Rabbit Sis, torchbearer of tumult, whose fervent protests might one day change the world, if they don't first incite her to single-handedly dismantle it, piece by bureaucratic piece."
                        ).sound(barkSound).build(),
                DialogueSet.builder()
                        .key("dialogue-11").lines(
                                "Ever observant Rabbit Daddy, guardian of the gold, manages finances with a paranoia that would make a conspiracy theorist blush.",
                                "His ledger is tighter than a drum, his brow perpetually furrowed in fiscal fear."
                        ).sound(barkSound).build(),
                DialogueSet.builder()
                        .key("dialogue-12").lines(
                                "Granny, with anecdotes as endless as eternity, dispenses wisdom like a vending machine stuck on dispense, whether you requested it or not.",
                                "Each story a subtle reminder that history repeats itself, especially at family gatherings."
                        ).sound(barkSound).build(),
                DialogueSet.builder()
                        .key("dialogue-13").lines(
                                "Observe the tranquil Rabbit Cuz, philosopher of sloth, whose profound punctuality issues suggest a man deeply at peace with life's ephemeral nature, or perhaps just deeply asleep."
                        ).sound(barkSound).build(),
                DialogueSet.builder()
                        .key("dialogue-14").lines(
                                "Bark!"
                        ).sound(barkSound).build(),
                DialogueSet.builder()
                        .key("dialogue-15").lines(
                                "*pants*"
                        ).sound(barkSound).build(),
                DialogueSet.builder()
                        .key("dialogue-16").lines(
                                "Woof!"
                        ).sound(barkSound).build(),
                DialogueSet.builder()
                        .key("dialogue-17").lines(
                                "In the pursuit of excellence, one must remember that the journey is as significant as the destination.",
                                "True success lies not in the accolades, but in the lessons learned along the way."
                        ).sound(barkSound).build(),
                DialogueSet.builder()
                        .key("dialogue-18").lines(
                                "Consider the possibility that limitations are merely figments of our imagination, crafted by our fears and insecurities.",
                                "To break free, one must first believe in the boundlessness of their own potential."
                        ).sound(barkSound).build(),
                DialogueSet.builder()
                        .key("dialogue-19").lines(
                                "Empathy is the silent language of the soul, spoken through actions rather than words.",
                                "To truly understand another, one must listen not only with ears but with the heart."
                        ).sound(barkSound).build(),
                DialogueSet.builder()
                        .key("dialogue-20").lines(
                                "The greatest paradox of life is that in seeking control, we often surrender it.",
                                "True power comes not from dominion over others, but from mastery over oneself."
                        ).sound(barkSound).build(),
                DialogueSet.builder()
                        .key("dialogue-21").lines(
                                "Wisdom often comes to us in whispers, through the leaves of the trees or the ripple of the waters.",
                                "It teaches us that every voice, no matter how small, has something of value to say."
                        ).sound(barkSound).build(),
                DialogueSet.builder()
                        .key("dialogue-22").lines(
                                "In every moment of decision, we stand at the crossroads of countless possibilities.",
                                "The paths we choose not only define our destiny but also reflect the essence of our being."
                        ).sound(barkSound).build(),
                DialogueSet.builder()
                        .key("dialogue-23").lines(
                                "Solitude is not the absence of company, but the moment when our soul is free to speak to us, unencumbered by the chaos of the world."
                        ).sound(barkSound).build(),
                DialogueSet.builder()
                        .key("dialogue-24").lines(
                                "Life, like a vast symphony, plays on the strings of our experiences, emotions, and encounters.",
                                "Each note, while fleeting, contributes to the eternal melody of our existence."
                        ).sound(barkSound).build(),
                DialogueSet.builder()
                        .key("dialogue-25").lines(
                                "Ah, Rabbit Bro, tirelessly toiling from the break of dawn, clinging to his routines with the desperation of a shipwreck survivor to a life raft.",
                                "One wonders if he runs from failure or merely jogs alongside it."
                        ).sound(barkSound).build(),
                DialogueSet.builder()
                        .key("dialogue-26").lines(
                                "And there strides Rabbit Sis, torchbearer of tumult, whose fervent protests might one day change the world, if they don't first incite her to single-handedly dismantle it, piece by bureaucratic piece."
                        ).sound(barkSound).build()
        ).toArray(DialogueSet[]::new);
    }
}