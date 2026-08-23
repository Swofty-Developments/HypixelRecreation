package net.swofty.type.hub.npcs.rabbits;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory;

import java.util.stream.Stream;

public class NPCRabbitBro extends HypixelNPC {

    public NPCRabbitBro() {
        super(new RabbitConfiguration(
                DatapointChocolateFactory.EmployeeType.RABBIT_BRO,
                "ewogICJ0aW1lc3RhbXAiIDogMTcxMjU5NDI0NjM2MywKICAicHJvZmlsZUlkIiA6ICJjZjc4YzFkZjE3ZTI0Y2Q5YTIxYmU4NWQ0NDk5ZWE4ZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNYXR0c0FybW9yU3RhbmRzIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzI4NzkzNGJkZDlkZjI3MDViMjUxYmI5OTdlMDI5YjE4YzFlOTRkZjEyOTkyYjgxMDdlNzQ0OTdiMjA1Y2E3ZTgiCiAgICB9CiAgfQp9",
                "GvFhGqpWtF4xgX080rEgliRB1DevBr41QoCDINCI/4TDEQz0TJsn0yrZnSn2A8UuEPJCwP+wdBKqG0vvdOXsjD2Qo9FN1n8q85hs1Ew+K+Q/tKCI4cBzOJYbtgPykM75Sx4+NtDnEvrIwMQ+CttKuqaSFV4Wi81YZXHxqNgNp7A7I8h7Bm96okpSHwo72c3Au2HUBOtQAOvGmvMeijegLO2C6Uzi9ZKgWzObTS8qKtTui/dfR/O7TRnG0GBlLFJ0HUWlIoidhig9ayKp8FE5n3CmUUEyLshbXepk5QGZEYz0JhKa94s4JOblSy2xOUQx6DYjuAgMUVRGt/giUFk7QmsUUmgw0BeI6GEf7KU8QIe2RXY+FxjYTwC8bJzHleG80P+tgnwa+zSvjSehVnBubgOHErk1KuOGcdfLBNbsUg3DbNtJwnexeEblYQp0Mx3JCoROU5aeHfWObP1LOiVCPoEDpL/62oB0wcF0uTqJTAiKVjpYi7V7cup/W0n6vsPJDUeyQwp6b5xywzfNjHwY4Gmk+8GEMkaL9og3+dEM1Gffgdf7mEfSHfBGnE8u/tljibY56UZh/nsCmDrj6lF7JRrI84D6pHajzD54/YNwmV4lQ1AKOl+g47pNNGBMeOgyIe/1LQO7mDbFO3F5P7js1ji97bs2MrlJkpdwPFUnt8w=",
                new Pos(63.5, 68, 3.5, 0, 0)
        ));
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        if (isInDialogue(event.player())) return;

        setDialogue(event.player(), "dialogue-" + (int) (Math.random() * 12 + 1));
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder()
                        .key("dialogue-1").lines(
                                "You should hire me! I can help you boost your <6>Chocolate Factory <f>production <d>tenfold <f>by next quarter!"
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-2").lines(
                                "Hire me, bro!"
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-3").lines(
                                "Get me a job at your <6>Chocolate Factory<f>!"
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-4").lines(
                                "<a>Hoppity <f>thinks he's all that with his chocolate empire.",
                                "But ask him this...who can hop the highest?"
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-5").lines(
                                "Rise and grind, as I say. The early rabbit gets the cocoa beans."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-6").lines(
                                "<a>Hoppity <f>may run the factory, but who do you think inspires the work ethic?"
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-7").lines(
                                "My morning routine? A quick hop around the fields, a bit of carrot juice, and then straight to work on the next big chocolate innovation."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-8").lines(
                                "Sleep is for the weak!"
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-9").lines(
                                "Working out before dawn has its perks; you get to see <a>Hoppity<f>'s 'inspirational' morning pep talks.",
                                "It's like a shot of espresso, but with more hopping and less coffee."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-10").lines(
                                "<a>Hoppity's <f>living the sweet life now, but who's the one who taught him to dodge those garden gnomes? Bro knows best."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-11").lines(
                                "It's like a shot of espresso, but with more hopping and less coffee."
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-12").lines(
                                "<a>Hoppity's <f>living the sweet life now, but who's the one who taught him to dodge those garden gnomes? Bro knows best."
                        ).build()
        ).toArray(DialogueSet[]::new);
    }
}