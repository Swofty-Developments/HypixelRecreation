package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.stream.Stream;

public class NPCDusk extends HypixelNPC {

    public NPCDusk() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"§5Dusk", "§e§lCLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "dRKIQEk2uly8QEPNSrKsJm+e9qN4+XUL149+U3jlpWClh/H5YdZAjHDazK6WkqRSHHU4UgD0zcf4tCcHDPXr4T/mV2iOYVGEQPUJAcjlIJZVy/Yl0UjmQl3aCpyejljvNkcE2JQotALQGb/Lce2H9ILqU4iae7JBZbHCA/8kO3AXmaeaYucxuee8VQU66V9G1m5xH+E3370K5X2UgNj+gkdxzhlxf64SK6dtDXJF/1L5pnhqQziYRbRHw+Wi90+WS+LME760fAThCDJU+cL3ANRekFSiKV62RaIYtLGvTj9e1TMmVFFg5J+pY0Qa62vipW+v453diS15XmzHsxAG/QxudjmqooCbLVgkrMslTsd4nb4xXPDaHv3tGh9D8RKlr9jTT3T59aAqtxFAlA0gJAILI+LhfJZi7Pzm39S0e9VMbEoMOWfMMfwbKT3DlLSokjtVMAnzjehd1jHtp/FsWBNDcA9PwbJl5ap+/UFCVgTakyIUKu7Nje7F+1426xz1+FpAlaDhirJMsxtFuhOdoZq91QvdO+75o+PcBWc0PU+BOLgqCljBwgp7CBnExpif+PCBaAPJai6Gfpzpw0RnFsfnbDTHkKFIff/Us6l6dCt/vixvJpdIgPnuqLMt4SzmOO1qErzz8a3mXVLo7a8ShDbMhNkqtj0EyeXQDL9WD6I=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NjMzMTc0ODQ5NzQsInByb2ZpbGVJZCI6IjllOGI4NzQ3YTMxNzQ0ZTY4YTQ4NzEzMzQwM2I0ZDM1IiwicHJvZmlsZU5hbWUiOiJFdmlsRGN0ciIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2NhODVjMTU2M2JmNTY5ZDg4YmY3YmMzNzVjYmM4MjBkN2Q0YTczYzkyYWFmN2Q5NDc5YWY1ZWY1MjU1ZDA0MCJ9fX0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(20.5, 63, -135.5, 45, 0);
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
        setGardenDialogue(e.player(), "hello");
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder()
                        .key("hello").lines(new String[]{
                                "You can apply §drunes §fto weapons and armor with the §dRune Pedestal §fbehind me.",
                                "You can also combine two runes for a chance to create a higher level rune with a better effect!"
                        }).build()
        ).toArray(DialogueSet[]::new);
    }

    private java.util.concurrent.CompletableFuture<Void> setGardenDialogue(HypixelPlayer player, String key) {
        return setDialogue(player, key).thenRun(() -> {
            if (player instanceof net.swofty.type.skyblockgeneric.user.SkyBlockPlayer skyBlockPlayer) {
                net.swofty.type.skyblockgeneric.garden.progression.GardenProgressionSupport.apply(
                    skyBlockPlayer,
                    net.swofty.type.skyblockgeneric.garden.progression.GardenProgressionReward.spokenNpc("DUSK")
                );
            }
        });
    }
}
