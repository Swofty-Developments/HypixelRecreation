package net.swofty.type.hub.npcs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCMort extends HypixelNPC {

    public NPCMort() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Mort", "<e><l>GATE KEEPER"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "VEyyNaHlOvqAGUk+r9tWnKp09JSL6jrCdphYgxbldPga6YCvWv8w8Lluebx6gX8R+TLRpg2EWpSUPRBgwEMKBolUxzV9gFQBxQD1IaUDdbVTucNCqp8tPnBEVazM9HOcje4XTwm47yucZbjnEQFndHwyPyFBORCd5vbO6JfzopR0ZD00A0lZn07JGwJz/2WTGjqM8CtP8Yi7RHykaJaso2xfYKcIBaLLE1iMm5G4ZIQZEtfgstCQ58/W9R+FBegqGfgDccwqXP/zOTDl110BE77cRTufeAjjCXZfmSjegF6ctbcA+SxJYgXpQdHlFaWO8fQhJmuauhCMBcKMzL3nP/EMlDFvnFTYlnQBTz7dUqolLqY8fX6jbr1F9eCH8Rb8CBrVsMx4kAX/G5QGKeyzoWRQtDJxAJuHp5U8Gw9c1zDW4Yapse1Gp/0Gj4r1XfWoPjEGDv7FQgdBRMlggBKxtrctbFvDCfHHExZh6y/PQfP6U9BdIy2TRbPCD2tb9r86mvYSmFd1PXV8POElLLaIJTvqqYNewe2pwgdVt4aj2JLBGrP7my8ditkR5q7bWHFTzl+8DbhF4ect5g6I3MhRjq/61InzNwky6lObsRYVHsDAj4FYc6eT5CLfLZnePzeM/RBTCf+K3Jx6eCF7XWgO1OZGr5W4y5/daWTZpKXz/yw=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1Nzg0MDk0MTMxNjksInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzliNTY4OTViOTY1OTg5NmFkNjQ3ZjU4NTk5MjM4YWY1MzJkNDZkYjljMWIwMzg5YjhiYmViNzA5OTlkYWIzM2QiLCJtZXRhZGF0YSI6eyJtb2RlbCI6InNsaW0ifX19fQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-88.5, 55, -128.5, -90, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        e.player().notImplemented();
    }

}
