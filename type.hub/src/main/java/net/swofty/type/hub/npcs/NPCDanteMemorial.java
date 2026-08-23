package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCDanteMemorial extends HypixelNPC {

    public NPCDanteMemorial() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{""};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "bB1TQWe6O950wmZ8VEcR7p8ZONgPXIW8EzryTeYaw6bXzRDolpzbNr2Fz4MxzX5/dvBS5V18xEFl/6qf4oCorYopWSejJ0D3q9T8BLBjpYqmfx+K2aQjXpWiSmK7/i0ecsbGc0TeJbl6Q/8YYSQ2DqcF6J/5TJYNErs/XJvcNtx/XAleYJzLVxEgZ7CMti6OkopJyYZCyobEKG8ZHvJH4zEkYvPBnU+rAjEVRA+ai1WxQ96WJESF6amw4VCAeGn7jiTPQktU14ifcwBA8be+sCOkdV5J0Dimv46bk+N/LNdnCY5H9vDgsV4ymLqq2aX5/FY6y28Z1XnHR/S7I0IvyQdZbsGLV23eGFxQUJ13hjooVW7cc2cCm39PMDuji7ycL0IXkbbEVNyYuBBkXBaf6jB6+NefIvpUBFhN9Llfncw6SUKK9cUeaph2j8m8XH2RLeY5qBriUwdL0EBOJGsUgEDP9phWAeFb2BmsCONYYbFEI2cO4+rg3PYIJuNgrsamv70X1SYyy4xvDCN3J4RuJqT+rR67leJzeBje3cj1wcUbjecquODIUAM7PZcmU60gMTExOv5BTOxfkgoWWgtcYRVy+Zn7MTx93JllIGMDqqOQey1g6uKRujZqtYgX50a6TwByQPf1uAMHx5TXDuVi3wmpmjufsw6cQRuOqGjVUMs=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYxODk2NjA2MDYyMSwKICAicHJvZmlsZUlkIiA6ICI0NWY3YTJlNjE3ODE0YjJjODAwODM5MmRmN2IzNWY0ZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJfSnVzdERvSXQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWFiZDE5YWVmZGZkYWM3ZTI3ZGVhNjIxNjZiNDFmOWMxMzQ4NDFhMjkwNmY3MDRmMjJkMGQzY2QwNGNmM2MwZiIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-80.5, 76, -152.5, 90, 0);
            }

            @Override
            public boolean shouldDisplayHolograms(HypixelPlayer player) {
                return false;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        HypixelPlayer player = event.player();
        player.sendMessage("<7>In Memoriam");
        player.sendMessage("While Dante will be remembered as a cruel tyrant, his righteous ideals live on in people's hearts.");
        player.sendMessage("dante <6>best<f>.");
    }
}
