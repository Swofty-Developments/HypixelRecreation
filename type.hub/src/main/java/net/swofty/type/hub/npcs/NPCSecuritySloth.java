package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.stream.Stream;

public class NPCSecuritySloth extends HypixelNPC {

    public NPCSecuritySloth() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<a><l>STAY SAFE!", "<c>Security Sloth", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "ESu1W+i5foGb/+/4hIcVR7ArPgTMZBO2PGjAagT+2jRYbWs0ZZN/5Hs8KxEiBxpU7zaJh1gpt0j9IX0Ckrj/Cii9zoLS03PcoSXjnrdQG/ZkuONoj/2KRddy9Yu17sY2DXYmm9AkjkbnhOT10qwAFMxP6xMHgtVU+Tr/DIXEGrH4GucXkaSUo1irEbflzbA2xxKSSbc0P1fmebWtEitcPDG8SkWYONNkajH/v6X8bChe7udlb+6ECGiNbvegeWhHW+2e7YWzoqV+dg0a3BgvF/NFsWmO8/2imBk/illPo6QQQg2RiwlvH1+/0zCU++tSDucV4B8GrWU/MPe2tA6ZAxEb0uaV0Nm6DCpSL6hm2spvZNt+xkpvD0E3Qv0oENUcw0iEuArhzeO7zCplwZ3d8ce1VnSWEIhHI+JYW+wivnra6CCcnIyUQX5t5U8dDIxIGTLCB4J+JMO7JjArN+3jyywLF/s0/x4QCRfETuyMhzyS6bY7WNxX1XSg1/soMTcp+1p5p5kvyoRyGG9tnoGSdWtYXF0b9I3cjZqruzkZpHHP88PZN6IQrs4C300CrqBPdOAqY53Cu30a493lL0EhliYdoJGwOCGzes8arbAWuqdoC4GthaPB/pj6XRfjbelPtVuw0a9aJzFCDsUlUYskDoNXXuFfxv/knCRuDFKUYsI=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTY1MjkwNzI5ODg4MywKICAicHJvZmlsZUlkIiA6ICIwNDNkZWIzOGIyNjE0MTg1YTIzYzU4ZmI2YTc5ZWZkYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJWaXRhbFNpZ256MiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iNjI5OGRlZDZhOGM5ZTZjZGZiNWRiMGU1NGViMzUyOWY4YjA4ZmUwYjA3MDliMTA0NzFmZjQ2N2VmZjg0NjYxIgogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(10.500, 71.000, -15.500, 45, 0);
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

        setDialogue(e.player(), "hello").thenAccept(key -> {
            e.player().sendMessage("<click:url:'https://support.hypixel.net/hc/en-us/articles/360019538060-How-to-Keep-Your-Account-Secure-on-Hypixel'><e>General Security Support Article <b><l>CLICK HERE");
        });
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder()
                        .key("hello").lines(
                                "Downloading suspicious mods or visiting untrusted discord servers can put your account at risk. It is upto you to keep your account secure!",
                                "Here are some helpful support articles that will help you keep your account more secure and avoid losing valuable progress or items."
                        ).build()
        ).toArray(DialogueSet[]::new);
    }
}
