package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.stream.Stream;

public class NPCSalesman extends HypixelNPC {

    public NPCSalesman() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Salesman", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "N1ypz7pBZlbwToXhi3vBN1naQCOPXQq9l3Ps2V5FEsLlIgXQTe9AUKGYsHxmxlu6rinMX2euQn1eyv3eS759C6LrqGoMdBYLJbLClyBy8VfaDOZjaCUG6f6wmRNaGcoce0Cf7wB9QC+tgutuy1QV6AtefdpgMQPYCDXLLzbjqwUyIJ/sLzViiecZXN8g13dd8f4LipRXCPV10xtO8Kq3eXLZeyOj6r4HE+R6UVQ+QIEvG857XmnH1o0ehmwRBg/0ftbgsVPgprfu8M0LgYb6VVswpMou++jXhUDzvHKzOICVNsKMC3BLouXzsVqSXhFP081kkw/cqVaHgtMXbRTxGIfIoPg+Q6sB53fqGe6U04M3YpjtlQO+SvmRAwpU2Zcqu39KhjBs0z58uawx1PNKo6dJkRgP1RpIkkPCJTQukNPrICxvy7o+s22DA5y7T5y8Hf4G5LbQ7q1stqiflttZ8RvHImVXL4biw9EACxGcqy04CdEXTh36lm2Il/r85cLTK2D94z18xFuQ4RYP1DVkmoDW1me5qOqNYhMy9hC9/ZW6QkhMJ9gmpDDqxpAeIDdFc3w1YNkS527smpLimdjY3gbxxl2AtGU8ZLo72yFCo8s3Ox8vi8TiU4s4HpMtGaL8bS3Sfr7z+aB4pJTmQwY/Ll4dLyzK7aLPWdCoiEQbQt0=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NDUxNTQ2NzY3MjEsInByb2ZpbGVJZCI6ImRiYTJmMTE1NmNjMjQwMWJhOWU5YjRkMjdmN2M4OTdkIiwicHJvZmlsZU5hbWUiOiJjb2RlbmFtZV9CIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85NTJiYjQ4YWYyZjU3ZDZhYzljOGU4NzVjMGM5ODc3YTM5NTNmMDQ4MDgxN2QyODQxZTU4NmU3NDdjODdjZTBjIn19fQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-9.5, 71, -15.5, 0, 0);
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        if (isInDialogue(e.player())) return;
        setDialogue(e.player(), "hello");
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder()
                        .key("hello").lines(
                                "Thank you for supporting the server and allowing us maintain SkyBlock!",
                                "If you want to support us, head over to the Hypixel Store <b>store.hypixel.net<f>!"
                        ).build()
        ).toArray(DialogueSet[]::new);
    }
}
